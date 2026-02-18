package com.sunbox.sdpscale.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.sunbox.dao.mapper.*;
import com.sunbox.domain.*;
import com.sunbox.domain.enums.EvictVmStateType;
import com.sunbox.domain.result.SingleResult;
import com.sunbox.sdpservice.service.ComposeService;
import com.sunbox.service.scale.strategy.ClusterScaleOutContext;
import com.sunbox.service.scale.strategy.ComposeStrategyFactory;
import com.sunbox.service.scale.strategy.ScaleTaskStrategy;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 处理Azure推过来的补全被驱逐VM消息.<p/>
 * <ol>
 *  <li>拿到VM，确定扩容了</li>
 *  <li>生成扩容任务，conf_scaling_task表， 生成conf_scaling_task id</li>
 *  <li>将VM放到conf_scaling_task_vm表中，生成 scale_vm_detail_id字段。</li>
 *  <li>将VM放到info_cluster_vm表, 只保存task_id 和 scale_vm_detail_id</li>
 *  <li>向ServiceBus发送启动流程的消息</li>
 * </ol>
 */
@Component
public class HandleEvictVmTask implements BaseCommonInterFace {

    private String lockKey = "lock:handle:evictvm";

    @Value("${vm.username:hadoop}")
    private String defaultUserName;

    @Autowired
    private AutoCreatedEvictVmMapper autoCreatedEvictVmMapper;

    @Autowired
    private ConfClusterNeoMapper confClusterNeoMapper;

    @Autowired
    private InfoClusterVmNeoMapper infoClusterVmNeoMapper;

    @Autowired
    private ConfScalingTaskNeoMapper confScalingTaskNeoMapper;

    @Autowired
    private ConfClusterVmNeoMapper confClusterVmNeoMapper;

    @Autowired
    private ComposeStrategyFactory scaleStrategyFactory;

    @Autowired
    private DistributedRedisLock redisLock;

    private Map<String, ConfCluster> clusterMap = new HashMap<>();

    /**
     * 弹性伸缩性能指标采集
     */
    @Scheduled(cron = "${cron.handleEvictVm.expr: 30 * * * * ? }")
    public void start() {
        getLogger().info("开始处理补全的被驱逐VM....");
        // 获取销
        boolean locked = redisLock.tryLock(lockKey);
        if (!locked) {
            getLogger().info("未获取到锁, 退出本次处理流程");
            return;
        }
        try {
            clusterMap.clear();

            // 获取最近一天的所有VM
            List<AutoCreatedEvictVm> evictVms = getLastestEvictVm();

            // 检查VM是否需要做补齐操作, 对于不需要补齐的, 从列表中删掉, 只留下需要处理的VM
            evictVms = checkEvictVm(evictVms);

            // 对剩下的VM分组
            Collection<List<AutoCreatedEvictVm>> groupedEvictVm = groupEvictVm(evictVms);

            // 生成补齐VM任务
            generateTask(groupedEvictVm);

            getLogger().info("处理补全的被驱逐VM完成");
        } finally {
            redisLock.tryUnlock(lockKey);
        }
    }

    /**
     * 生成一个扩容任务
     * @param groupedEvictVm
     */
    private void generateTask(Collection<List<AutoCreatedEvictVm>> groupedEvictVm) {
        if (CollectionUtil.isEmpty(groupedEvictVm)) {
            getLogger().info("待补全VM为空, 不生成扩容任务");
            return;
        }
        getLogger().info("开始生成补全被驱逐Vm的任务...");
        for (List<AutoCreatedEvictVm> evictVms : groupedEvictVm) {

            // 生成Task对象
            ConfScalingTask task = generateScalingTask(evictVms);

            // 生成上下文数据
            ScaleTaskStrategy scaleTaskStrategy = scaleStrategyFactory.createScaleTaskStrategy(task);
            ClusterScaleOutContext context = generateScaleOutContext(scaleTaskStrategy, task);

            // 保存InfoClusterVM数据
            saveInfoClusterVm(context, evictVms, task);

            // 生成扩容任务
            scaleTaskStrategy.validateScaleOutBeforeAdjustCount(context, task);
            scaleTaskStrategy.createScaleOutTask(context, task);

            // 更新EvictVm的状态.
            updateEvictVmSuccess(evictVms);
        }
    }

    private void updateEvictVmSuccess(List<AutoCreatedEvictVm> evictVms) {
        if (CollectionUtil.isEmpty(evictVms)) {
            return;
        }
        for (AutoCreatedEvictVm evictVm : evictVms) {
            autoCreatedEvictVmMapper.updateState(evictVm.getId(), EvictVmStateType.SUCCESS.name());
        }
    }

    private ClusterScaleOutContext generateScaleOutContext(ScaleTaskStrategy scaleTaskStrategy, ConfScalingTask task) {
        SingleResult<ClusterScaleOutContext> contextWrapper = scaleTaskStrategy.buildScaleOutContext(task.getDefaultUsername(), task);
        if (!contextWrapper.isSuccess()) {
            getLogger().error(contextWrapper.getMessage(), contextWrapper.getException());
        }
        ClusterScaleOutContext context = contextWrapper.getData();
        return context;
    }

    private void saveInfoClusterVm(ClusterScaleOutContext context, List<AutoCreatedEvictVm> evictVms, ConfScalingTask task) {
        AutoCreatedEvictVm vm = evictVms.get(0);
        InfoClusterVm existInfoClusterVm = infoClusterVmNeoMapper.selectOneByClusterIdAndGroupNameAndState(
                task.getClusterId(),
                vm.getGroupName(),
                null);
        if (Objects.isNull(existInfoClusterVm)) {
            // 从hostgroup中查找现存的VM,如果没找到, 说明hostgroup里没有VM,这种情况不进行补全操作了.
            for (AutoCreatedEvictVm evictVm : evictVms) {
                autoCreatedEvictVmMapper.updateState(evictVm.getId(), EvictVmStateType.HOSTGROUP_EMPTY.name());
            }
            return;
        }
        int idx = existInfoClusterVm.getHostName().indexOf(".");
        String domainName = existInfoClusterVm.getHostName().substring(idx);
        // 保存InfoClusterVM
        for (AutoCreatedEvictVm evictVm : evictVms) {
            JSONObject eventJson = JSONObject.parseObject(evictVm.getEventContent());
            ConfClusterVm confClusterVm = context.getConfClusterVms().get(0);
            // 生成 info_cluster_vm 记录
            InfoClusterVm infoClusterVm = new InfoClusterVm();
            infoClusterVm.setVmid(evictVm.getVmid());
            // 从同组的vm中找
            infoClusterVm.setOndemondPrice(existInfoClusterVm.getOndemondPrice());
            infoClusterVm.setSpotPrice(existInfoClusterVm.getSpotPrice());
            infoClusterVm.setMaintenanceMode(InfoClusterVm.MaintenanceModeON);
            infoClusterVm.setGroupId(confClusterVm.getGroupId());
            infoClusterVm.setScaleoutTaskId(task.getTaskId());
            infoClusterVm.setState(InfoClusterVm.VM_RUNNING);
            infoClusterVm.setGroupName(evictVm.getGroupName());
            infoClusterVm.setVmRole(evictVm.getVmRole());
            infoClusterVm.setClusterId(evictVm.getClusterId());
            infoClusterVm.setVmName(evictVm.getVmName());
            infoClusterVm.setVmConfId(context.getConfClusterVms().get(0).getVmConfId());
            String hostName = eventJson.getString("hostName");
            if (!StrUtil.contains(hostName, domainName)) {
                hostName = eventJson.getString("hostName") + domainName;
            }
            infoClusterVm.setHostName(hostName.toLowerCase());
            infoClusterVm.setInternalip(eventJson.getString("privateIp"));
            infoClusterVm.setDefaultUsername(existInfoClusterVm.getDefaultUsername());

//            infoClusterVm.setSkuName(confClusterVm.getSku());
            infoClusterVm.setSkuName(eventJson.getString("vmSize"));
            infoClusterVm.setPurchaseType(Convert.toStr(confClusterVm.getPurchaseType(), ""));
            infoClusterVm.setImageid(confClusterVm.getOsImageid());
            // 调用Azure接口时才需要此字段, 所以此处设置为空
            infoClusterVm.setCreateTranscationId("");
            // 调用Azure接口时返回JobId,此处设置为空
            infoClusterVm.setCreateJobId("");
            infoClusterVm.setCreateBegtime(new Date());
            infoClusterVm.setCreateEndtime(new Date());

            infoClusterVmNeoMapper.insert(infoClusterVm);
        }
    }

    private ConfScalingTask generateScalingTask(List<AutoCreatedEvictVm> evictVms) {
        AutoCreatedEvictVm evictVm = evictVms.get(0);
        int runningCount = infoClusterVmNeoMapper.selectCountByGroupNameAndState(evictVm.getClusterId(), evictVm.getGroupName(), InfoClusterVm.VM_RUNNING);
//        ConfClusterVm confClusterVm = confClusterVmNeoMapper.selectByAny(evictVm.getClusterId(), evictVm.getVmRole(), evictVm.getGroupName());
//        AutoCreatedEvictVm vm = evictVms.get(0);

        // 生成 Scaling Task
        ConfScalingTask task = new ConfScalingTask();
        task.setTaskId(UUID.randomUUID().toString(true));
        task.setForceScaleinDataNode(ConfScalingTask.FORCE_SCALEIN_NO);
        task.setDefaultUsername(defaultUserName);
        task.setClusterId(evictVm.getClusterId());
        task.setGroupName(evictVm.getGroupName());
        task.setScalingType(ConfScalingTask.ScaleType_OUT);
        task.setVmRole(evictVm.getVmRole());
        task.setEsRuleId("");
        task.setEsRuleName("");
        task.setBeforeScalingCount(runningCount); // 扩容前数量,
        task.setAfterScalingCount(runningCount + evictVms.size());  // 扩容后数量
        task.setScalingCount(evictVms.size());       // 扩容数量,用这个字段存储实际扩缩容数量
        task.setIsGracefulScalein(null);  // 是否优雅缩容
        task.setScaleinWaitingtime(null); // 缩容等待时间
        task.setOperatiionType(ConfScalingTask.Operation_type_Complete_Evict_Vm);     // 操作类型, 用于标识是哪类的扩缩容,如:手动,竞价,删除实例组等
        task.setBegTime(new Date());   // 开始时间
        task.setEndTime(null);   // 结束时间
        task.setState(ConfScalingTask.SCALINGTASK_Create);              // 状态
        task.setEnableBeforestartScript(ConfScalingTask.EXECUTE_SCRIPT_ENABLE);// 执行集群启动前脚本
        task.setEnableAfterstartScript(ConfScalingTask.EXECUTE_SCRIPT_DISENABLE); // 执行集群启动后脚本
        task.setMaxCount(0);               // 最大数量
        task.setMinCount(0);               // 最小数量
        task.setDeleteGroup(0);            //
        task.setInQueue(1);                // 是否在队列中
        task.setScaleoutTaskId("");        // 扩容的TaskId
        task.setExpectCount(runningCount + evictVms.size());            // 期望数量, 需要从现有的实例组中动态获取, 扩容时需要动态计算,动态更新
        task.setRemark("Azure Fleet自动补足VM，数量：" + evictVms.size());
        task.setCreatedBy("system");             // 创建者
        task.setCreateTime(new Date());

//        confScalingTaskNeoMapper.insert(task);
        return task;
    }

    /**
     * 将Vm按 cluster, group, purcharseType 分组,分组后的VM就可以直接生成补全VM的任务了.
     * @param evictVms 待分组的VM
     * @return 分组后的VM列表. 每个分组项都是一个VM列表.
     */
    private Collection<List<AutoCreatedEvictVm>> groupEvictVm(List<AutoCreatedEvictVm> evictVms) {
        if (CollectionUtil.isEmpty(evictVms)) {
            return Lists.newArrayList();
        }
        Map<String, List<AutoCreatedEvictVm>> grouped = evictVms.stream().collect(Collectors.groupingBy(v -> {
            return v.getClusterId() + v.getGroupName() + v.getPurchaseType();
        }));
        return grouped.values();
    }

    /**
     * 检查补齐的VM, 只保留真正需要补齐的VM
     * @param evictVms
     * @return
     */
    private List<AutoCreatedEvictVm> checkEvictVm(List<AutoCreatedEvictVm> evictVms) {
        List<AutoCreatedEvictVm> result = new ArrayList<>();
        // 检查内容:
        for (AutoCreatedEvictVm evictVm : evictVms) {
            // 1. 集群必须是运行中
            ConfCluster confCluster = confClusterNeoMapper.selectByPrimaryKey(evictVm.getClusterId());
            if (!Objects.equals(confCluster.getState(), ConfCluster.CREATED)) {
                evictVm.setState(EvictVmStateType.CLUSTER_NOT_RUNNING.name());
                autoCreatedEvictVmMapper.updateState(evictVm.getId(), EvictVmStateType.CLUSTER_NOT_RUNNING.name());
                continue;
            }

            // 2. 实例组必须没有正在扩容和缩容
            List<ConfScalingTask> confScalingTasks = confScalingTaskNeoMapper.selectRunningOrInqueueTasksByClusterAndGroupName(evictVm.getClusterId(), evictVm.getGroupName());
            if (CollectionUtil.isNotEmpty(confScalingTasks)) {
                getLogger().info("补齐VM时发现实例有正在执行的扩缩容任务, 跳过不处理: clusterId={}, clusterName={}, groupName={}, vmName={}",
                        confCluster.getClusterId(), confCluster.getClusterName(), evictVm.getGroupName(), evictVm.getVmName());
                continue;
            }

            // 3. info_cluster_vm表中必须没有此VM
            InfoClusterVm infoClusterVm = infoClusterVmNeoMapper.selectByPrimaryKey(confCluster.getClusterId(), evictVm.getVmName());
            if (Objects.nonNull(infoClusterVm)) {
                getLogger().info("补齐VM时发现实例已加入集群, 跳过不处理: clusterId={}, clusterName={}, groupName={}, vmName={}",
                    confCluster.getClusterId(), confCluster.getClusterName(), evictVm.getGroupName(), evictVm.getVmName());

                evictVm.setState(EvictVmStateType.VM_IN_CLUSTER.name());
                autoCreatedEvictVmMapper.updateState(evictVm.getId(), EvictVmStateType.VM_IN_CLUSTER.name());
                continue;
            }
            result.add(evictVm);
        }
        getLogger().info("检查过后,需要补全的被驱逐VM数量为:{}", result.size());
        return result;
    }

    private List<AutoCreatedEvictVm> getLastestEvictVm() {
        Date now = new Date();
        Date yestoday = DateUtil.offsetDay(now, -1);
        List<AutoCreatedEvictVm> list = autoCreatedEvictVmMapper.selectByTimeAndState(yestoday, now,
                Arrays.asList(EvictVmStateType.INIT.name()));
        getLogger().info("查询获取补全驱逐VM数量: {}", list.size());
        return list;
    }
}
