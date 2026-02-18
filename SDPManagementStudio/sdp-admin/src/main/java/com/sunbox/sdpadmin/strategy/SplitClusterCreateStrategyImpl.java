package com.sunbox.sdpadmin.strategy;

import com.sunbox.domain.ConfCluster;
import com.sunbox.domain.ConfClusterSplitTask;
import com.sunbox.domain.ConfClusterVm;
import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpadmin.manager.ClusterCreationManager;
import com.sunbox.sdpadmin.mapper.ConfClusterSplitTaskMapper;
import com.sunbox.sdpadmin.model.admin.request.AdminSaveClusterRequest;
import com.sunbox.sdpadmin.model.admin.request.InstanceGroupSkuCfg;
import com.sunbox.sdpservice.service.ComposeService;
import com.sunbox.util.DateUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SplitClusterCreateStrategyImpl implements ClusterCreationStrategy {
    private static final int MIN_SIZE_CORE = 10;
    private static final int MIN_SIZE_TASK = 10;
    private static final int SEGMENT_SIZE_CORE = 100;
    private static final int SEGMENT_SIZE_TASK = 100;

    @Autowired
    ClusterCreationManager clusterCreationManager;

    @Autowired
    ComposeService composeService;

    @Autowired
    ConfClusterSplitTaskMapper confClusterSplitTaskMapper;
    @Autowired
    DirectlyClusterCreateStrategyImpl directlyClusterCreateStrategy;

    public ResultMsg createCluster(String clusterId, AdminSaveClusterRequest adminSaveClusterRequest) {
        getLogger().info("begin createCluster,clusterId:{},request:{}", clusterId, adminSaveClusterRequest);

        Date now = new Date();
        boolean needSplit = false;
        for (InstanceGroupSkuCfg splitInstanceGroupSkuCfg : adminSaveClusterRequest.getInstanceGroupSkuCfgs()) {
            if (splitInstanceGroupSkuCfg.getVmRole().equalsIgnoreCase("CORE")) {
                if (splitInstanceGroupSkuCfg.getCnt() > MIN_SIZE_CORE) {
                    needSplit = true;
                }
            } else if (splitInstanceGroupSkuCfg.getVmRole().equalsIgnoreCase("TASK")) {
                if (splitInstanceGroupSkuCfg.getCnt() > MIN_SIZE_TASK) {
                    needSplit = true;
                }
            }
        }

        if (!needSplit) {
            return directlyClusterCreateStrategy.createCluster(clusterId, adminSaveClusterRequest);
        }

        List<InstanceGroupSkuCfg> splitInstanceGroupSkuCfgs = splitHostGroupAndUpdateRequest(adminSaveClusterRequest);

        getLogger().info("createCluster after split,clusterId:{},request:{}", clusterId, adminSaveClusterRequest);

        /** 开始保存数据 **/
        clusterCreationManager.addConfCluster(clusterId, adminSaveClusterRequest);
        clusterCreationManager.addConfClusterTag(clusterId, adminSaveClusterRequest);
        clusterCreationManager.addConfClusterApp(clusterId, adminSaveClusterRequest);

        clusterCreationManager.addConfClusterAppsConfig(clusterId, adminSaveClusterRequest.getClusterCfgs(), adminSaveClusterRequest.getInstanceGroupVersion().getClusterReleaseVer());

        clusterCreationManager.addConfClusterHostGroupAppsConfig(clusterId, adminSaveClusterRequest);
        clusterCreationManager.addConfClusterVmAndDataVolume(clusterId, adminSaveClusterRequest);
        clusterCreationManager.addConfClusterScript(clusterId, adminSaveClusterRequest.getConfClusterScript());
        clusterCreationManager.addElasticScaleRules(clusterId, adminSaveClusterRequest, adminSaveClusterRequest.getInstanceGroupSkuCfgs());
        clusterCreationManager.addInfoCluster(clusterId);
        ResultMsg createClusterResult=new ResultMsg();
        //如果是创建审核中时,先不执行集群.
        if (adminSaveClusterRequest.getWorkOrderCreate() == null || 0==adminSaveClusterRequest.getWorkOrderCreate()) {
            /** 结束保存数据 **/
            createClusterResult = createCluster(clusterId,
                    adminSaveClusterRequest.getInstanceGroupVersion().getClusterReleaseVer(),
                    adminSaveClusterRequest.getCreationMode(ConfCluster.CreationMode.SPLIT));
            if (!createClusterResult.isResult()) {
                getLogger().error("create cluster error,clusterId:{},errorMsg:{}", clusterId, createClusterResult.getErrorMsg());
                return createClusterResult;
            }
        }
        for (int index = 0; index < splitInstanceGroupSkuCfgs.size(); index++) {
            InstanceGroupSkuCfg splitInstanceGroupSkuCfg = splitInstanceGroupSkuCfgs.get(index);
            ConfClusterSplitTask confClusterSplitTask = new ConfClusterSplitTask();
            confClusterSplitTask.setId(UUID.randomUUID().toString());
            confClusterSplitTask.setClusterId(clusterId);
            confClusterSplitTask.setCreateTime(DateUtils.addSeconds(now, (index + 1)));
            confClusterSplitTask.setState(ConfClusterSplitTask.State.WAITING.getValue());
            confClusterSplitTask.setExpectCount(null);
            confClusterSplitTask.setGroupName(splitInstanceGroupSkuCfg.getGroupName());
            confClusterSplitTask.setScalingOutCount(splitInstanceGroupSkuCfg.getCnt());
            confClusterSplitTask.setSubject(ConfClusterSplitTask.Subject.SCALE_OUT_CREATE_CLUSTER.getValue());
            confClusterSplitTask.setSortIndex(index);
            confClusterSplitTask.setVmRole(splitInstanceGroupSkuCfg.getVmRole());
            getLogger().info("insert ConfClusterSplitTask:{}", confClusterSplitTask);
            confClusterSplitTaskMapper.insertSelective(confClusterSplitTask);
        }
        //todo confClusterSplitTaskMapper 有何作用??
        createClusterResult.setResult(true);
        return createClusterResult;
    }

    private ResultMsg createCluster(String clusterId, String releaseVer, ConfCluster.CreationMode creationMode) {
        getLogger().info("createCluster clusterId:{},releaseVer:{},creationMode:{}",
                clusterId,
                releaseVer,
                creationMode);

        ResultMsg createPlanResult = composeService.createPlan(clusterId, "create", releaseVer);
        if (null == createPlanResult || !createPlanResult.getResult()) {
            return createPlanResult;
        }
        Map<String, String> datamap = new HashMap<>();
        datamap.put("clusterId", clusterId);
        createPlanResult.setData(datamap);
        return createPlanResult;
    }

    public ResultMsg createAndStartPlan(String clusterId, String releaseVer, ConfCluster.CreationMode creationMode){
        ResultMsg cluster = createCluster(clusterId, releaseVer, creationMode);
        return cluster;
    }


    private List<InstanceGroupSkuCfg> splitHostGroupAndUpdateRequest(AdminSaveClusterRequest adminSaveClusterRequest) {
        List<InstanceGroupSkuCfg> splitInstanceGroupSkuCfgs = new ArrayList<>();

        for (InstanceGroupSkuCfg instanceGroupSkuCfg : adminSaveClusterRequest.getInstanceGroupSkuCfgs()) {
            if (instanceGroupSkuCfg.getVmRole().equalsIgnoreCase("CORE")) {
                List<InstanceGroupSkuCfg> coreInstanceGroupSkuCfgs = splitForCoreHostGroup(instanceGroupSkuCfg);
                if (!coreInstanceGroupSkuCfgs.isEmpty()) {
                    splitInstanceGroupSkuCfgs.addAll(coreInstanceGroupSkuCfgs);
                }
            } else if (instanceGroupSkuCfg.getVmRole().equalsIgnoreCase("TASK")) {
                List<InstanceGroupSkuCfg> taskInstanceGroupSkuCfgs = splitForTaskHostGroup(instanceGroupSkuCfg);
                if (!taskInstanceGroupSkuCfgs.isEmpty()) {
                    splitInstanceGroupSkuCfgs.addAll(taskInstanceGroupSkuCfgs);
                }
            }
        }
        return splitInstanceGroupSkuCfgs;
    }

    private List<InstanceGroupSkuCfg> splitForCoreHostGroup(InstanceGroupSkuCfg instanceGroupSkuCfg) {
        if (instanceGroupSkuCfg.getCnt() < MIN_SIZE_CORE) {
            return new ArrayList<>();
        }

        List<InstanceGroupSkuCfg> instanceGroupSkuCfgs = new ArrayList<>();
        int leftCount = instanceGroupSkuCfg.getCnt() - MIN_SIZE_CORE;
        instanceGroupSkuCfg.setCnt(MIN_SIZE_CORE);
        while (leftCount > 0) {
            int splitCount = leftCount - SEGMENT_SIZE_CORE;

            InstanceGroupSkuCfg splitGroupSkuCfg = new InstanceGroupSkuCfg();
            try {
                BeanUtils.copyProperties(splitGroupSkuCfg, instanceGroupSkuCfg);
            } catch (Exception ignored) {
            }
            if (splitCount > 0) {
                splitGroupSkuCfg.setCnt(SEGMENT_SIZE_CORE);
            } else {
                splitGroupSkuCfg.setCnt(leftCount);
            }
            instanceGroupSkuCfgs.add(splitGroupSkuCfg);
            leftCount = splitCount;
        }
        return instanceGroupSkuCfgs;
    }

    private List<InstanceGroupSkuCfg> splitForTaskHostGroup(InstanceGroupSkuCfg instanceGroupSkuCfg) {
        if (instanceGroupSkuCfg.getCnt() < MIN_SIZE_TASK) {
            return new ArrayList<>();
        }

        List<InstanceGroupSkuCfg> instanceGroupSkuCfgs = new ArrayList<>();
        int leftCount = instanceGroupSkuCfg.getCnt() - MIN_SIZE_TASK;
        instanceGroupSkuCfg.setCnt(MIN_SIZE_TASK);
        while (leftCount > 0) {
            int splitCount = leftCount - SEGMENT_SIZE_TASK;

            InstanceGroupSkuCfg splitGroupSkuCfg = new InstanceGroupSkuCfg();
            try {
                BeanUtils.copyProperties(splitGroupSkuCfg, instanceGroupSkuCfg);
            } catch (Exception ignored) {
            }
            if (splitCount > 0) {
                splitGroupSkuCfg.setCnt(SEGMENT_SIZE_TASK);
            } else {
                splitGroupSkuCfg.setCnt(leftCount);
            }
            instanceGroupSkuCfgs.add(splitGroupSkuCfg);
            leftCount = splitCount;
        }
        return instanceGroupSkuCfgs;
    }

    private static class InstanceGroupSkuCfgWrapper {
        private InstanceGroupSkuCfg instanceGroupSkuCfg;
        private Integer scalingCount;

        public InstanceGroupSkuCfgWrapper(InstanceGroupSkuCfg instanceGroupSkuCfg, Integer scalingCount) {
            this.instanceGroupSkuCfg = instanceGroupSkuCfg;
            this.scalingCount = scalingCount;
        }

        public InstanceGroupSkuCfg getInstanceGroupSkuCfg() {
            return instanceGroupSkuCfg;
        }

        public Integer getScalingCount() {
            return scalingCount;
        }
    }
}