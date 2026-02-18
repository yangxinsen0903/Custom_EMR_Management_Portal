package com.sunbox.sdpcompose.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.*;
import com.sunbox.domain.metaData.SSHKeyPair;
import com.sunbox.sdpcompose.consts.ComposeConstant;
import com.sunbox.sdpcompose.consts.JobNameConstant;
import com.sunbox.sdpcompose.enums.IntegrationSystem;
import com.sunbox.sdpcompose.manager.AnalysisManager;
import com.sunbox.sdpcompose.mapper.*;
import com.sunbox.sdpcompose.service.*;
import com.sunbox.service.IMetaDataItemService;
import com.sunbox.service.IVMClearLogService;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author : [niyang]
 * @className : AnsibleServiceImpl
 * @description : [调用ansible接口按照ambari agent 以及查询安装进度]
 * @createTime : [2022/11/30 5:16 PM]
 */
@Service("InstallSDP")
public class AnsibleServiceImpl implements IAnsibleService, BaseCommonInterFace {


    @Autowired
    private DistributedRedisLock redisLock;

    @Autowired
    private IPlanExecService planExecService;

    @Autowired
    private IVMService ivmService;

    @Autowired
    private IPlayBookService playBookService;

    @Autowired
    private ConfScalingTaskMapper scalingTaskMapper;

    @Autowired
    private ConfClusterAppMapper clusterAppMapper;

    @Autowired
    private InfoClusterOperationPlanMapper planMapper;

    @Autowired
    private InfoClusterVmMapper infoClusterVmMapper;

    @Autowired
    private InfoClusterOperationPlanActivityLogMapper planActivityLogMapper;

    @Autowired
    private IFullLogService logService;

    @Autowired
    private IVMClearLogService ivmClearLogService;

    @Autowired
    private AnalysisManager analysisManager;

    @Autowired
    private IMetaDataItemService metaDataItemService;

    @Value("${compose.message.clientname}")
    private String clientname;

    @Value("${sdp.hdfs.balance.bandwidth.max:209715200}")
    private String hdfsmaxwidth;

    @Value("${sdp.hdfs.balance.bandwidth.min:20971520}")
    private String hdfsminwidth;

    @Value("${sdp.install.ansible.reduce:0}")
    private Integer sdp_reduce;

    @Value("${sdp.install.ansible.retry.enable:1}")
    private Integer sdp_ansible_retry_enable;

    @Value("${sdp.install.ansible.retry.times:5}")
    private Integer sdp_ansible_retry_times;

    @Value("${sdp.install.ansible.retry.waittime:30}")
    private Long sdp_ansible_retry_waittime_second;

    @Value("${sdp.vm.ssh.port:2222}")
    private Integer sdp_vm_ssh_port;

    /**
     *  安装ambari server前，检测ssh端口重试次数
     */
    @Value("${sdp.install.ambariserver.retry.times:10}")
    private Integer sdp_ambariserver_retry_times;

    /**
     * 安装ambari server前，检测ssh端口重试间隔时间
     */
    @Value("${sdp.install.ambariserver.retry.waittime:30}")
    private Long sdp_ambariserver_retry_waittime;



    /**
     * ansible 服务消息订阅
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg playbookApiMessage(String message) {
        return null;
    }

    /**
     * 安装ambari
     * 生成playbook 调用微软云ansible接口
     * @param messageparam
     * @return
     */
    @Override
    public ResultMsg installAmbari(String messageparam) {
        getLogger().info(timeTickLog("收到安装Ambari消息"));
        ResultMsg resultMsg = new ResultMsg();
        InfoClusterOperationPlanActivityLogWithBLOBs activityLog = null;

        getLogger().info("installAmbari param:{}", messageparam);
        // 0. 解析参数
        JSONObject param = JSON.parseObject(messageparam);
        String activityLogId = param.getString("activityLogId");
        activityLog = planExecService.getInfoActivityLogByLogId(activityLogId);
        getLogger().info(timeTickLog("获取执行步骤对象完成"));

        if (activityLog.getBegtime() == null) {
            activityLog.setBegtime(new Date());
            getLogger().info(timeTickLog("设置步骤开始时间:" + DateUtil.formatDateTime(activityLog.getBegtime())));
        }

        ConfCluster cluster = planExecService.getConfClusterByActivityLogId(activityLogId);
        getLogger().info(timeTickLog("获取集群confCluster完成"));
        // 1.获取安装的ambari server的机器
        List<InfoClusterVm> ambarivm = ivmService.getRoleVms(cluster.getClusterId(), "ambari");
        getLogger().info(timeTickLog("查询Ambari VM信息完成"));

        if (ambarivm !=null && ambarivm.size()>0) {
            ResultMsg ckresult = checkAmbariServerConnection(ambarivm.get(0).getInternalip(), sdp_vm_ssh_port);
            getLogger().info(timeTickLog("Ambari IP地址探活完成:" + ambarivm.get(0).getInternalip()));
            // 测试端口失败
            if (!ckresult.getResult()){
                // 判断是否可以重试
                ResultMsg retryMsg = checkAmbariServerRetry(param);

                if (retryMsg.getResult()){
                    JSONObject retryObj = JSON.parseObject(JSON.toJSONString(retryMsg.getData()));
                    param.put(JobNameConstant.Param_Ambari_Retry_obj,retryObj);
                    planExecService.loopActivity(clientname,JSON.toJSONString(param),sdp_ambariserver_retry_waittime,activityLogId);
                    getLogger().info(timeTickLog("发送循环重试步骤消息完成(更新了步骤的状态)"));
                    return resultMsg;
                }else{
                    activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                    activityLog.setLogs("Pod到AmbariServer的端口不通。");
                    planExecService.complateActivity(activityLog);
                    getLogger().info(timeTickLog("Ambari端口不通, 步骤结束"));
                    return resultMsg;
                }
            }else{
                // 测试端口通过
                param.remove(JobNameConstant.Switch_Ambari_Retry);
                param.remove(JobNameConstant.Param_Ambari_Retry_obj);
            }
        }

        List<InfoClusterVm> allvms = ivmService.getAllVms(cluster.getClusterId());
        getLogger().info(timeTickLog("获取集群全部VM完成"));

        if (ambarivm == null || ambarivm.size() == 0) {
            activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            activityLog.setLogs("not found ambari vm");
            planExecService.complateActivity(activityLog);
            getLogger().info(timeTickLog("没找到VM, 步骤执行结束"));
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("not found ambari vm");
            getLogger().error("not found ambari vm:" + messageparam);
            return resultMsg;
        }

        String nodelist = ambarivm.get(0).getInternalip();

        StringBuilder vmInternalIps = new StringBuilder();
        allvms.stream().forEach(vm -> {
            //状态running的VM 加入到集群
            if (vm.getState() == InfoClusterVm.VM_RUNNING){
                vmInternalIps.append(vm.getInternalip() + ",");
            }
        });
        String gangliaIpList = vmInternalIps.substring(0, vmInternalIps.lastIndexOf(","));

        // 2. 执行playbook
        getLogger().info(timeTickLog("开始执行playbook"));
        getLogger().info("begin playBookService savePlaybook clusterReleaseVer:{},runtime_ambari_server:{},clusterId:{},activityLogId:{},nodeList:{},gangliaIpList:{},region:{},",
                cluster.getClusterReleaseVer(),
                BaseImageScripts.RUNTIMING_AMBARI_SERVER,
                cluster.getClusterId(),
                activityLogId,
                nodelist,
                gangliaIpList,cluster.getRegion());

        ResultMsg msg = playBookService.savePlaybook(
                cluster.getClusterReleaseVer(),
                BaseImageScripts.RUNTIMING_AMBARI_SERVER,
                cluster.getClusterId(),
                activityLogId,
                nodelist,
                gangliaIpList,cluster.getRegion());
        getLogger().info(timeTickLog("执行playbook完成"));

        if (msg.getResult()) {
            param.put(JobNameConstant.Run_PlayBook, msg.getBizid());
            planExecService.sendNextActivityMsg(activityLogId, param);
            getLogger().info(timeTickLog("发送下一步骤ServiceBus消息完成"));
            activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
        } else {
            activityLog.setLogs(msg.getErrorMsg());
            activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
        }
        planExecService.complateActivity(activityLog);
        getLogger().info(timeTickLog("更新步骤状态完成"));

        return resultMsg;
    }


    /**
     * 判断安装ambari前检测ambari端口失败是否可以重试
     * 判断重试次数有无超过阈值
     *
     * @param param
     * @return
     */
    private ResultMsg checkAmbariServerRetry(JSONObject param){
        ResultMsg msg = new ResultMsg();
        if (!param.containsKey(JobNameConstant.Param_Ansible_Retry_Obj)){
            //未重试过，构建重试信息结构体
            Map<String,Object> ansibleRestryObj = new HashMap<>();
            ansibleRestryObj.put("count",1);
            msg.setResult(true);
            msg.setData(ansibleRestryObj);
            return msg;
        }else{
            //已有重试记录
            JSONObject restyJsonObj = param.getJSONObject(JobNameConstant.Param_Ansible_Retry_Obj);
            Integer retryCnt = restyJsonObj.getInteger("count");

            if (retryCnt >= sdp_ambariserver_retry_times){
                msg.setResult(false);
                msg.setErrorMsg("重试次数达到最大重试次数："  + sdp_ambariserver_retry_times);
                getLogger().warn(msg.getErrorMsg());
                return msg;
            }
            retryCnt++;
            restyJsonObj.put("count",retryCnt);

            msg.setResult(true);
            msg.setData(restyJsonObj);
            return msg;
        }
    }

    /**
     * 检测 Pod 到AmbariServer的ssh 端口是否畅通
     *
     * @param host
     * @param port
     * @return
     */
    private ResultMsg checkAmbariServerConnection(String host,int port){
        ResultMsg resultMsg = new ResultMsg();

        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), 1500);
            resultMsg.setResult(true);
            return resultMsg;
        } catch (IOException e) {
            resultMsg.setResult(false);
            resultMsg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            getLogger().error("检查端口异常，",e);
            return resultMsg;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                getLogger().error("关闭socket连接异常，",e);
            }
        }
    }


    /**
     * 安装sdp Agent
     *  支持集群安装和扩容安装
     * @param messageparam
     * @return
     */
    @Override
    public ResultMsg installAgent(String messageparam) {
        getLogger().info(timeTickLog("收到安装Agent消息"));
        ResultMsg resultMsg=new ResultMsg();
        InfoClusterOperationPlanActivityLogWithBLOBs activityLog=null;

        // 0. 解析参数
        JSONObject param = JSON.parseObject(messageparam);
        String activityLogId = param.getString("activityLogId");
        String taskId=param.getString("taskId");
        activityLog=planExecService.getInfoActivityLogByLogId(activityLogId);
        getLogger().info(timeTickLog("获取activatyLog对象"));

        if (activityLog.getBegtime()==null){
            activityLog.setBegtime(new Date());
            getLogger().info(timeTickLog("生成步骤开始时间:" + DateUtil.formatDateTime(activityLog.getBegtime())));
        }

        ConfCluster cluster = planExecService.getConfClusterByActivityLogId(activityLogId);
        getLogger().info(timeTickLog("获取集群信息confCluster"));
        // region 1.获取安装的Agent的机器
        List<InfoClusterVm> agentvms=new ArrayList<>();

        if (!org.apache.commons.lang3.StringUtils.isNotEmpty(taskId)){
            //集群创建时安装
            agentvms=ivmService.getAllVms(cluster.getClusterId());
            getLogger().info(timeTickLog("获取集群中全部的VM列表"));
        }else{
            // 扩容安装
            agentvms=ivmService.getScaleOutVms(cluster.getClusterId(),taskId);
            getLogger().info("需要安装agent的扩容机器："+agentvms.toString());
            getLogger().info(timeTickLog("获取本次扩容的VM列表"));
        }

        //endregion

        if (agentvms==null || agentvms.size()==0){
            activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            activityLog.setLogs("not found vms: 没从数据库中获取到需要安装agent的机器。");
            planExecService.complateActivity(activityLog);
            getLogger().info(timeTickLog("没找到VM列表,步骤执行完成"));
        }

        StringBuilder sb=new StringBuilder();

        agentvms.stream().forEach(x-> {
                    if (x.getState() == InfoClusterVm.VM_RUNNING) {
                        sb.append(x.getInternalip() + ",");
                    }
                }
        );
        if (sb.length() == 0){
            activityLog.setLogs("无可用VM。所有需安装Agent的主机状态都不是运行中(数据库中状态)");
            activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            planExecService.complateActivity(activityLog);
            getLogger().info(timeTickLog("所有VM都不是运行中,步骤执行完成"));
            return resultMsg;
        }
        String nodelist=sb.substring(0,sb.lastIndexOf(","));

        String restryIpList = getRetryIpList(param);

        if (StringUtils.isNotEmpty(restryIpList)){
            nodelist = restryIpList;
        }

        getLogger().info(timeTickLog("开始执行playbook"));
        // 2. 执行playbook
        ResultMsg msg= playBookService.savePlaybook(
                cluster.getClusterReleaseVer(),
                BaseImageScripts.RUNTIMING_AMBARI_AGENT,cluster.getClusterId(),activityLogId,nodelist,nodelist,cluster.getRegion());
        getLogger().info(timeTickLog("执行playbook完成"));
        if (msg.getResult()){
            param.put(JobNameConstant.Run_PlayBook,msg.getBizid());
            // 降级功能开
            param.put(JobNameConstant.Switch_Ansible_Reduce,1);
            // 重试功能开
            param.put(JobNameConstant.Switch_Ansible_Retry,1);
            planExecService.sendNextActivityMsg(activityLogId,param);
            getLogger().info(timeTickLog("发送ServiceBus消息完成"));
            activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
        }else{
            activityLog.setLogs(msg.getErrorMsg());
            activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
        }
        planExecService.complateActivity(activityLog);
        getLogger().info(timeTickLog("更新步骤执行状态完成"));

        return resultMsg;

    }


    /**
     * 初始化脚本执行
     *
     * @param messageparam
     * @return
     */
    @Override
    public ResultMsg initScript(String messageparam) {

        ResultMsg resultMsg=new ResultMsg();
        InfoClusterOperationPlanActivityLogWithBLOBs activityLog=null;

        // 0. 解析参数
        JSONObject param = JSON.parseObject(messageparam);
        String activityLogId = param.getString("activityLogId");
        String taskId=param.getString("taskId");
        activityLog=planExecService.getInfoActivityLogByLogId(activityLogId);

        if (activityLog.getBegtime()==null){
            activityLog.setBegtime(new Date());
        }

        ConfCluster cluster = planExecService.getConfClusterByActivityLogId(activityLogId);

        //执行playbook
        ResultMsg msg=null;

        String retryIpList = getRetryIpList(param);

        if (StringUtils.isEmpty(taskId)){
            msg=playBookService.executePlaybookJobYmlAndSave(cluster.getClusterId(),
                    "aftervminit",activityLogId,null,retryIpList,cluster.getRegion());
        }else{
            msg=playBookService.executePlaybookJobYmlAndSave(cluster.getClusterId(),
                    "aftervminit",activityLogId,taskId,retryIpList,cluster.getRegion());
        }

        if (msg.getResult()){
            param.put(JobNameConstant.Run_PlayBook,msg.getBizid());
            // 降级功能开
            param.put(JobNameConstant.Switch_Ansible_Reduce,1);
            // 重试功能开
            param.put(JobNameConstant.Switch_Ansible_Retry,1);

            planExecService.sendNextActivityMsg(activityLogId,param);
            activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
        }else{
            activityLog.setLogs(msg.getErrorMsg());
            activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
        }
        planExecService.complateActivity(activityLog);
        return resultMsg;

    }

    /**
     * 执行集群启动前脚本
     *
     * @param messageparam
     * @return
     */
    @Override
    public ResultMsg beforeClusterStartScript(String messageparam) {
        ResultMsg msg=new ResultMsg();
        InfoClusterOperationPlanActivityLogWithBLOBs activityLog=null;

        // 0. 解析参数
        JSONObject param = JSON.parseObject(messageparam);
        String activityLogId = param.getString("activityLogId");
        String taskId=param.getString("taskId");
        activityLog=planExecService.getInfoActivityLogByLogId(activityLogId);

        if (activityLog.getBegtime()==null){
            activityLog.setBegtime(new Date());
        }

        ConfCluster cluster = planExecService.getConfClusterByActivityLogId(activityLogId);

        //region 执行playbook

        String retryIpList = getRetryIpList(param);

        if (StringUtils.isEmpty(taskId)){
            // 全新安装
            msg=playBookService.executePlaybookJobYmlAndSave(cluster.getClusterId(),
                    "beforestart",activityLogId,null, retryIpList,cluster.getRegion());
        }else {
            // 扩容
            ConfScalingTask task=scalingTaskMapper.selectByPrimaryKey(taskId);
            if (task.getEnableBeforestartScript() !=null && task.getEnableBeforestartScript().equals(1)) {
                msg = playBookService.executePlaybookJobYmlAndSave(cluster.getClusterId(),
                        "beforestart", activityLogId, taskId, retryIpList,cluster.getRegion());
            } else {
                getLogger().info("不执行集群启动前脚本，跳过");
                msg.setResult(true);
                msg.setBizid("");
            }

        }
        //endregion

        if (msg.getResult()){
            param.put(JobNameConstant.Run_PlayBook,msg.getBizid());
            // 降级功能开
            param.put(JobNameConstant.Switch_Ansible_Reduce,1);
            // 重试功能开
            param.put(JobNameConstant.Switch_Ansible_Retry,1);

            planExecService.sendNextActivityMsg(activityLogId,param);
            activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
        }else{
            activityLog.setLogs(msg.getErrorMsg());
            activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
        }
        planExecService.complateActivity(activityLog);
        return msg;
    }

    /**
     * 执行集群启动后脚本
     *
     * @param messageparam
     * @return
     */
    @Override
    public ResultMsg afterClusterCompletedScript(String messageparam) {

        ResultMsg msg=new ResultMsg();
        InfoClusterOperationPlanActivityLogWithBLOBs activityLog=null;

        // 0. 解析参数
        JSONObject param = JSON.parseObject(messageparam);
        String activityLogId = param.getString("activityLogId");
        String taskId=param.getString("taskId");
        activityLog=planExecService.getInfoActivityLogByLogId(activityLogId);

        if (activityLog.getBegtime()==null){
            activityLog.setBegtime(new Date());
        }

        ConfCluster cluster = planExecService.getConfClusterByActivityLogId(activityLogId);

        // 支持重试, 如果是重试, 则只在需要重试的VM上执行脚本
        String retryIpList = getRetryIpList(param);

        //region 执行playbook
        if (StringUtils.isEmpty(taskId)){
            //全新创建集群
            msg=playBookService.executePlaybookJobYmlAndSave(cluster.getClusterId(),
                    "afterstart",activityLogId,null, retryIpList,cluster.getRegion());
        }else{
            //扩容, 扩容时有开关(是否执行启动后脚本), 只有开关打开后才执行脚本
            ConfScalingTask task=scalingTaskMapper.selectByPrimaryKey(taskId);
            if (task.getEnableAfterstartScript().equals(1)) {
                msg = playBookService.executePlaybookJobYmlAndSave(cluster.getClusterId(),
                        "afterstart", activityLogId, taskId, retryIpList,cluster.getRegion());
            } else {
                msg.setResult(true);
                msg.setBizid("");
                getLogger().info("不执行集群启动后脚本，跳过。");
            }
        }
        //endregion

        if (msg.getResult()){
            //JSONObject newmsg=new JSONObject();
            param.put(JobNameConstant.Run_PlayBook,msg.getBizid());
            // 降级功能开
            param.put(JobNameConstant.Switch_Ansible_Reduce,1);
            // 重试功能开
            param.put(JobNameConstant.Switch_Ansible_Retry,1);
            planExecService.sendNextActivityMsg(activityLogId,param);
            activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
        }else{
            activityLog.setLogs(msg.getErrorMsg());
            activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
        }
        planExecService.complateActivity(activityLog);
        return msg;

    }

    /**
     * 执行用户自定义脚本
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg runUserCusterScript(String message) {
        ResultMsg resultMsg=new ResultMsg();
        InfoClusterOperationPlanActivityLogWithBLOBs activityLog=null;

        // 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String activityLogId = param.getString("activityLogId");
        activityLog=planExecService.getInfoActivityLogByLogId(activityLogId);

        if (activityLog.getBegtime()==null){
            activityLog.setBegtime(new Date());
        }

        ConfCluster cluster = planExecService.getConfClusterByActivityLogId(activityLogId);

        // 执行playbook
        ResultMsg msg= playBookService.executeUserCustomScript(activityLogId, cluster.getRegion());

        if (msg.getResult()){
            JSONObject newmsg=new JSONObject();
            newmsg.put(JobNameConstant.Run_PlayBook,msg.getBizid());
            planExecService.sendNextActivityMsg(activityLogId,newmsg);
            activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
        }else{
            activityLog.setLogs(msg.getErrorMsg());
            activityLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
        }
        planExecService.complateActivity(activityLog);
        return resultMsg;
    }

    /**
     * 查询脚本执行结果
     *
     * @param messageparam
     * @return
     */
    @Override
    public ResultMsg queryPlayJobStatus(String messageparam) {
        ResultMsg resultMsg = new ResultMsg();
        // 0. 解析参数
        JSONObject param = JSON.parseObject(messageparam);
        String rpjobId = param.getString(JobNameConstant.Run_PlayBook);
        String activityLogId = param.getString("activityLogId");

        InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                = planExecService.getInfoActivityLogByLogId(activityLogId);

        // region 没有可以执行的脚本，步骤直接跳过
        if(StringUtils.isEmpty(rpjobId)){
            planExecService.sendNextActivityMsg(activityLogId,param);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            planExecService.complateActivity(currentLog);
            return  resultMsg;
        }
        //endregion
        ConfCluster cluster = planExecService.getConfClusterByActivityLogId(activityLogId);
        //获取ssh密钥对
        SSHKeyPair sshKeyPair = metaDataItemService.getSSHKeyPair(cluster.getRegion(), SSHKeyPair.privateKeyType);
        if (sshKeyPair == null){
            getLogger().error("获取ssh密钥对失败!");
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("获取ssh密钥对失败，查询ssh密钥管理详情");
            return resultMsg;
        }

        //region 调用ansible查询任务执行结果
        ResultMsg msg = playBookService.queryPlaybookJob(rpjobId,sshKeyPair.getSubscriptionId(),sshKeyPair.getKeyVaultResourceName(),sshKeyPair.getSecretResourceId());
        //endregion

        if (!msg.getRetcode().equalsIgnoreCase("processing")) {
            //region 处理非执行中逻辑处理

            //region ansible脚本执行成功逻辑处理
            if (msg.getRetcode().equalsIgnoreCase("success")){
                getLogger().info("任务处理结果: {}  jobId={}, activityLogId={}", msg.getRetcode(), rpjobId, activityLogId);
                param.remove(JobNameConstant.Switch_Ansible_Reduce);
                param.remove(JobNameConstant.Switch_Ansible_Retry);
                param.remove(JobNameConstant.Param_Ansible_Retry_Obj);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                playBookService.completePlayBookJob(rpjobId,InfoClusterPlaybookJob.JOB_OK);
                planExecService.sendNextActivityMsg(activityLogId,param);
            }
            //endregion

            //region ansible脚本执行失败逻辑处理，1.尝试重试. 2.降级处理
            if (msg.getRetcode().equalsIgnoreCase("fail")){
                getLogger().info("任务处理失败, 结果: {}  jobId={}, activityLogId={}", msg.getRetcode(), rpjobId, activityLogId);
                //region 获取失败vm的Ip
                JSONArray jsonArray=(JSONArray)msg.getData();
                List<String> vmips = failedVMIps(jsonArray);
                //endregion

                //region 重试逻辑

                // 1.判断是否可以重试
                ResultMsg ckretryMsg = checkAnsibleJobRetry(param,vmips);

                if (ckretryMsg.getResult()){
                    getLogger().info("任务失败需要重试, 任务处理结果:  jobId={}, activityLogId={}", rpjobId, activityLogId);
                    // 2. 发送重试ansible消息
                    JSONObject retryParam = JSON.parseObject(JSON.toJSONString(ckretryMsg.getData()));
                    ResultMsg sendmsg = sendAnsibleRetryMessage(retryParam,currentLog);

                    if (!sendmsg.getResult()){
                        getLogger().error("发送重试消息失败，忽略，流程继续。");
                    }else {
                        return msg;
                    }
                }
                //endregion

                if (sdp_reduce.equals(1) && param.containsKey(JobNameConstant.Switch_Ansible_Reduce) &&
                        param.getInteger(JobNameConstant.Switch_Ansible_Reduce).equals(1)){

                    // region 降级服务开启 且当前步骤允许降级
                    getLogger().info("执行脚本过程中异常的VM，进行降级处理。planId:"+currentLog.getPlanId());
                    ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);

                    ResultMsg reducemsg = ansibleReduce(confCluster.getClusterId(),vmips,currentLog);
                    if (reducemsg.getResult()){
                        param.remove(JobNameConstant.Switch_Ansible_Reduce);
                        param.remove(JobNameConstant.Switch_Ansible_Retry);
                        param.remove(JobNameConstant.Param_Ansible_Retry_Obj);
                        currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                        playBookService.completePlayBookJob(rpjobId,InfoClusterPlaybookJob.JOB_OK);
                        planExecService.sendNextActivityMsg(activityLogId,param);

                    }else{
                        playBookService.completePlayBookJob(rpjobId, InfoClusterPlaybookJob.JOB_FAILED);
                        currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);

                        String collectedLog = analysisManager.collectLog(IntegrationSystem.ANSIBLE, msg.getData());
                        if(StringUtils.isEmpty(collectedLog)) {
                            currentLog.setLogs(msg.getData().toString());
                        } else {
                            currentLog.setLogs(collectedLog);
                        }
                    }
                    //endregion

                }else {
                    //region 不具备降级条件
                    playBookService.completePlayBookJob(rpjobId, InfoClusterPlaybookJob.JOB_FAILED);
                    currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);

                    String collectedLog = analysisManager.collectLog(IntegrationSystem.ANSIBLE, msg.getData());
                    if(StringUtils.isEmpty(collectedLog)) {
                        currentLog.setLogs(msg.getData().toString());
                    } else {
                        currentLog.setLogs(collectedLog);
                    }
                    //endregion
                }

            }
            //endregion

            planExecService.complateActivity(currentLog);

            //endregion
        }else{
            //region 执行中逻辑处理，继续轮询
           resultMsg=planExecService.loopActivity(clientname,messageparam,10l,activityLogId);
           //endregion
        }
        return resultMsg;
    }

    /**
     * 判断是否可以重试,若允许重试则返回发送的消息体
     *
     * @param param
     * @return
     */
    private ResultMsg checkAnsibleJobRetry(JSONObject param,List<String> vmIps){
        ResultMsg msg = new ResultMsg();

        if (sdp_ansible_retry_enable.equals(0)){
            msg.setResult(false);
            msg.setErrorMsg("重试功能未开启，请在配置中心SDP-Compose模块开启，配置项：sdp.install.ansible.retry.enable ，（0 关闭 1 开启）");
            getLogger().warn(msg.getErrorMsg());
            return msg;
        }

        if (!param.containsKey(JobNameConstant.Switch_Ansible_Retry)
                || param.getInteger(JobNameConstant.Switch_Ansible_Retry).equals(0)){
            msg.setResult(false);
            msg.setErrorMsg("当前步骤暂不支持重试。");
            getLogger().warn(msg.getErrorMsg());
            return msg;
        }

        StringBuilder stb = new StringBuilder();
        vmIps.stream().forEach(x->{
            stb.append("," + x);
        });

        String ipList = stb.toString().substring(1);

        if (!param.containsKey(JobNameConstant.Param_Ansible_Retry_Obj)){
            //未重试过，构建重试信息结构体
            Map<String,Object> ansibleResryObj = new HashMap<>();
            ansibleResryObj.put("count",1);
            ansibleResryObj.put("ipList",ipList);
            msg.setResult(true);
            msg.setData(ansibleResryObj);
            return msg;
        }else{
            //已有重试记录
            JSONObject restyJsonObj = param.getJSONObject(JobNameConstant.Param_Ansible_Retry_Obj);
            Integer retryCnt = restyJsonObj.getInteger("count");

            if (retryCnt >= sdp_ansible_retry_times){
                msg.setResult(false);
                msg.setErrorMsg("重试次数达到最大重试次数："  + sdp_ansible_retry_times);
                getLogger().warn(msg.getErrorMsg());
                return msg;
            }
            retryCnt++;
            restyJsonObj.put("count",retryCnt);
            restyJsonObj.put("ipList",ipList);

            msg.setResult(true);
            msg.setData(restyJsonObj);
            return msg;
        }
    }


    /**
     * 发送ansible重试下消息
     *
     * @param retryParam 重试参数
     * @param currentLog 当前步骤对象
     * @return
     */
    private ResultMsg sendAnsibleRetryMessage(JSONObject retryParam, InfoClusterOperationPlanActivityLogWithBLOBs currentLog){

        ResultMsg msg = new ResultMsg();
        try {
            // 获取上一步
            InfoClusterOperationPlanActivityLogWithBLOBs preActivityLog =
                    planExecService.getPreviousActivity(currentLog.getPlanId(), currentLog.getActivityLogId());
            //更新当前步骤为待执行
            currentLog.setState(0);
            planActivityLogMapper.updateByPrimaryKeySelective(currentLog);

            JSONObject parmJsobj = JSON.parseObject(preActivityLog.getParaminfo());
            parmJsobj.put(JobNameConstant.Param_Ansible_Retry_Obj, retryParam);
            preActivityLog.setParaminfo(JSON.toJSONString(parmJsobj));

            return planExecService.sendPrevActivityMsg(preActivityLog, sdp_ansible_retry_waittime_second);
        }catch (Exception e){
            getLogger().error("发送Ansible，retry消息异常，",e);
            msg.setResult(false);
            msg.setErrorMsg("发送Ansible，retry消息异常，"+ExceptionUtils.getStackTrace(e));
            return msg;
        }
    }

    private ResultMsg ansibleReduce(String clusterId,List<String> vmips,InfoClusterOperationPlanActivityLogWithBLOBs activityLog){
            getLogger().info("需要降级的vmIPs:"+vmips);
            return ivmService.ansibleJobRejectNode(clusterId,vmips,activityLog);
    }

    /**
     *  失败VM ip收集
     * @param jsonArray
     * @return
     */
    private List<String> failedVMIps(JSONArray jsonArray){
        List<String> ips = new CopyOnWriteArrayList<>();
        try {
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.containsKey("jobStatus") && jsonObject.getInteger("jobStatus").equals(3)) {
                    // VM的处理结果: "{\"10.221.0.25\":\"Failed\",  \"10.221.0.26\":\"Failed\"}"
                    String executeResult = jsonObject.getString("jobresult");
                    JSONObject vmExecuteResult = JSON.parseObject(executeResult);
                    List<String> vms = vmExecuteResult.entrySet().stream().map(entry -> {
                        return entry.getKey() + ":" + entry.getValue();
                    }).collect(Collectors.toList());

                    // 所有的主机: "[10.221.0.25, 10.221.0.26]"
                    String hosts= jsonObject.getString("hosts");
                    hosts = StrUtil.removePrefix(hosts, "[");
                    hosts = StrUtil.removeSuffix(hosts, "]");

                    List<String> hostList = StrUtil.splitTrim(hosts, ",");

                    for (String vm: vms) {
                        if (StringUtils.isNotEmpty(vm) && vm.split(":")[1].trim().equalsIgnoreCase("Success")) {
                            hostList.remove(vm.split(":")[0].trim());
                        }
                    }
                    getLogger().info("Job执行失败, 提取出失败的IP为: {}", hostList);
                    ips.addAll(hostList);
                }
                // Azure新接口报文格式变动
//                if (jsonObject.containsKey("jobStatus") && jsonObject.getInteger("jobStatus").equals(3)) {
//                    String executeResult = jsonObject.getString("executeResult");
//                    String[] vms = executeResult.split(",");
//                    String hosts= jsonObject.getString("hosts");
//                    List<String> hostList = new ArrayList<>(Arrays.asList(hosts.split(",")));
//                    for (int j = 0; j < vms.length; j++) {
//                        String vm = vms[j];
//                        if (StringUtils.isNotEmpty(vm) && vm.split(":")[1].trim().equalsIgnoreCase("Success")) {
//                            hostList.remove(vm.split(":")[0].trim());
//                        }
//                    }
//                    getLogger().info("失败job，未返回结果的IP，"+hostList);
//                    ips.addAll(hostList);
//                }
            }
        }catch (Exception e){
            getLogger().error("get failedVMIps 异常:",e);
        }
        getLogger().error("failedVMIps:"+ips);
        return ips;
    }


    /**
     * Ambari节点安装tezUI
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg installTezUI(String message) {
        ResultMsg resultMsg = new ResultMsg();

        //region 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);
        //endregion

        //region 获取aciton数据
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                = planExecService.getInfoActivityLogByLogId(activityLogId);
        if (currentLog.getBegtime()==null){
            currentLog.setBegtime(new Date());
        }
        //endregion

        try {
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);
            //region 判断是否安装tez
            List<ConfClusterApp> apps=clusterAppMapper.getClusterAppsByClusterId(confCluster.getClusterId());
            Optional<ConfClusterApp> app=apps.stream().filter(x->{
                return x.getAppName().equalsIgnoreCase("tez");
            }).findFirst();

            if (!app.isPresent()){
                getLogger().info("clusterId:"+confCluster.getClusterId()+",未安装TEZ，跳过。");
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs("未安装TEZ，跳过");
                param.remove(JobNameConstant.Run_PlayBook);
                planExecService.complateActivity(currentLog);
                planExecService.sendNextActivityMsg(activityLogId,param);
                resultMsg.setResult(true);
                return resultMsg;
            }
            //endreigon

            //region 获取ambari信息
            List<InfoClusterVm> ambarivm=ivmService.getRoleVms(confCluster.getClusterId(),"ambari");
            //endregion
            ResultMsg msg=playBookService.savePlaybook(confCluster.getClusterReleaseVer(),
                    BaseImageScripts.RUNTIMING_TEZUI,
                    confCluster.getClusterId(),
                    activityLogId,
                    ambarivm.get(0).getInternalip(),
                    null,confCluster.getRegion());
            if (msg.getResult()){
                param.put(JobNameConstant.Run_PlayBook,msg.getBizid());
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                planExecService.sendNextActivityMsg(activityLogId,param);
            }else{
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs(msg.getErrorMsg());
            }
        }catch (Exception e){
            getLogger().error("--Exception---",e);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs(ExceptionUtils.getStackTrace(e));
        }
        planExecService.complateActivity(currentLog);
        return resultMsg;
    }


    /**
     * 调大 HDFS 数据平衡可用带宽 1G
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg scaleUpHdfsBalanceBandWidth(String message) {
        ResultMsg resultMsg = new ResultMsg();

        //region 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);
        //endregion

        //region 获取aciton数据
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                = planExecService.getInfoActivityLogByLogId(activityLogId);

        InfoClusterOperationPlan plan= planMapper.selectByPrimaryKey(currentLog.getPlanId());

        if (currentLog.getBegtime()==null){
            currentLog.setBegtime(new Date());
        }
        //endregion

        try {
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);
            ConfScalingTask confScalingTask=scalingTaskMapper.selectByPrimaryKey(plan.getScalingTaskId());

            List<InfoClusterVm> vms=
                    infoClusterVmMapper.selectByClusterIdAndScaleInTaskId(
                            confCluster.getClusterId(),
                            confScalingTask.getTaskId());

            // 保存vms到删除vm监控表
            ivmClearLogService.insertClearVms(confCluster.getClusterId(),plan.getPlanId(),vms);

            if (!confScalingTask.getVmRole().equalsIgnoreCase("core")){
                //非core节点 不包含datanode
                param.put(JobNameConstant.Run_PlayBook,"");
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs("非core节点，跳过");
                planExecService.complateActivity(currentLog);
                planExecService.sendNextActivityMsg(activityLogId,param);
                resultMsg.setResult(true);
                return resultMsg;
            }

            StringBuilder sb=new StringBuilder();

            vms.stream().forEach(x->{
                        sb.append(x.getInternalip()+",");
                    }
            );
            String nodelist=sb.substring(0,sb.lastIndexOf(","));

            //region 缓存带宽大小数据
            String bwkey="bw_"+confCluster.getClusterId();
            redisLock.save(bwkey,hdfsmaxwidth,60*30*1000);
            //endregion 缓存带宽大小数据
            ResultMsg msg=playBookService.savePlaybook(confCluster.getClusterReleaseVer(),
                    BaseImageScripts.RUNTIMING_BANDWITDTH_ADJUEST,
                    confCluster.getClusterId(),
                    activityLogId,
                    nodelist,
                    null,confCluster.getRegion());
            if (msg.getResult()){
                param.put(JobNameConstant.Run_PlayBook,msg.getBizid());
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                planExecService.sendNextActivityMsg(activityLogId,param);
            }else{
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs("带宽调整失败："+msg.getErrorMsg());
                param.put(JobNameConstant.Run_PlayBook,"000000");
                planExecService.sendNextActivityMsg(activityLogId,param);
            }
        }catch (Exception e){
            getLogger().error("--Exception---",e);
            param.put(JobNameConstant.Run_PlayBook,"000000");
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            currentLog.setLogs("带宽调整异常："+ExceptionUtils.getStackTrace(e));
        }
        planExecService.complateActivity(currentLog);
        return resultMsg;
    }

    /**
     * 还原 HDFS 数据平衡可用带宽 20M
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg scaleDownHdfsBalanceBandWidth(String message) {
        ResultMsg resultMsg = new ResultMsg();

        //region 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);
        //endregion

        //region 获取aciton数据
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                = planExecService.getInfoActivityLogByLogId(activityLogId);

        InfoClusterOperationPlan plan= planMapper.selectByPrimaryKey(currentLog.getPlanId());

        if (currentLog.getBegtime()==null){
            currentLog.setBegtime(new Date());
        }
        //endregion

        try {
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);
            ConfScalingTask confScalingTask=scalingTaskMapper.selectByPrimaryKey(plan.getScalingTaskId());

            if (!confScalingTask.getVmRole().equalsIgnoreCase("core")){
                //非core节点 不包含datanode
                param.put(JobNameConstant.Run_PlayBook,"");
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs("非core节点，跳过");
                planExecService.complateActivity(currentLog);
                planExecService.sendNextActivityMsg(activityLogId,param);
                resultMsg.setResult(true);
                return resultMsg;
            }

            List<InfoClusterVm> vms=
                    infoClusterVmMapper.selectByClusterIdAndScaleInTaskId(
                            confCluster.getClusterId(),
                            confScalingTask.getTaskId());

            StringBuilder sb=new StringBuilder();

            vms.stream().forEach(x->{
                        sb.append(x.getInternalip()+",");
                    }
            );
            String nodelist=sb.substring(0,sb.lastIndexOf(","));

            //region 缓存带宽大小数据
            String bwkey="bw_"+confCluster.getClusterId();
            redisLock.save(bwkey,hdfsminwidth,60*30*1000);
            //endregion 缓存带宽大小数据
            ResultMsg msg=playBookService.savePlaybook(confCluster.getClusterReleaseVer(),
                    BaseImageScripts.RUNTIMING_BANDWITDTH_ADJUEST,
                    confCluster.getClusterId(),
                    activityLogId,
                    nodelist,
                    null,confCluster.getRegion());
            if (msg.getResult()){
                param.put(JobNameConstant.Run_PlayBook,msg.getBizid());
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                planExecService.sendNextActivityMsg(activityLogId,param);
            }else{
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                currentLog.setLogs("带宽调整失败："+msg.getErrorMsg());
                param.put(JobNameConstant.Run_PlayBook,"000000");
                planExecService.sendNextActivityMsg(activityLogId,param);
            }
        }catch (Exception e){
            getLogger().error("--Exception---",e);
            param.put(JobNameConstant.Run_PlayBook,"000000");
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            currentLog.setLogs("带宽调整异常："+ExceptionUtils.getStackTrace(e));
        }
        planExecService.complateActivity(currentLog);
        return resultMsg;
    }

    /**
     * 执行core节点磁盘扩容任务脚本
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg scaleOutDisk(String message) {
        ResultMsg resultMsg = new ResultMsg();

        //region 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String activityLogId = param.getString(ComposeConstant.Activity_Log_ID);
        //endregion

        //region 获取aciton数据
        InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                = planExecService.getInfoActivityLogByLogId(activityLogId);

        InfoClusterOperationPlan plan= planMapper.selectByPrimaryKey(currentLog.getPlanId());
        if (currentLog.getBegtime()==null){
            currentLog.setBegtime(new Date());
        }
        //endregion

        if (StringUtils.isEmpty(plan.getScalingTaskId())){
            getLogger().error("plan ScalingTaskId is null");
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs("plan ScalingTaskId is null");
            planExecService.complateActivity(currentLog);
            return resultMsg;
        }

        try {
            ConfCluster confCluster = planExecService.getConfClusterByActivityLogId(activityLogId);
            ConfScalingTask confScalingTask=scalingTaskMapper.selectByPrimaryKey(plan.getScalingTaskId());
            //region 获取vm信息
            List<InfoClusterVm> vms=ivmService.getGroupVmsByState(
                    confScalingTask.getClusterId(),
                    confScalingTask.getGroupName().toLowerCase(),
                    InfoClusterVm.VM_RUNNING);

            //endregion

            //region 拼接nodelist
            StringBuilder sbips=new StringBuilder();
            vms.stream().forEach(x->{
                sbips.append(x.getInternalip()+",");
            });
            sbips.deleteCharAt(sbips.lastIndexOf(","));
            //endregion

            ResultMsg msg=playBookService.savePlaybook(confCluster.getClusterReleaseVer(),
                    BaseImageScripts.RUNTIMING_DISKSCALEOUT,
                    confCluster.getClusterId(),
                    activityLogId,
                    sbips.toString(),
                    null,confCluster.getRegion());
            if (msg.getResult()){
                param.put(JobNameConstant.Run_PlayBook,msg.getBizid());
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                planExecService.sendNextActivityMsg(activityLogId,param);
            }else{
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs(msg.getErrorMsg());
            }
        }catch (Exception e){
            getLogger().error("--Exception---",e);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
            currentLog.setLogs(ExceptionUtils.getStackTrace(e));
        }
        planExecService.complateActivity(currentLog);
        return resultMsg;
    }

    /**
     * 查询core节点磁盘扩容任务执行结果
     *
     * @param message
     * @return
     */
    @Override
    public ResultMsg queryScaleOutDiskProcess(String message) {
        ResultMsg resultMsg = new ResultMsg();
        // 0. 解析参数
        JSONObject param = JSON.parseObject(message);
        String rpjobId = param.getString(JobNameConstant.Run_PlayBook);
        String activityLogId = param.getString("activityLogId");


        InfoClusterOperationPlanActivityLogWithBLOBs currentLog
                = planExecService.getInfoActivityLogByLogId(activityLogId);
        // 没有可以执行的脚本，步骤直接跳过
        if(StringUtils.isEmpty(rpjobId)){
            planExecService.sendNextActivityMsg(activityLogId,param);
            currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
            planExecService.complateActivity(currentLog);
            return  resultMsg;
        }

        ConfCluster cluster = planExecService.getConfClusterByActivityLogId(activityLogId);
        //获取ssh密钥对
        SSHKeyPair sshKeyPair = metaDataItemService.getSSHKeyPair(cluster.getRegion(), SSHKeyPair.privateKeyType);
        if (sshKeyPair == null){
            getLogger().error("获取ssh密钥对失败!");
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("获取ssh密钥对失败，查询ssh密钥管理详情");
            return resultMsg;
        }
        ResultMsg msg = playBookService.queryPlaybookJob(rpjobId, sshKeyPair.getSubscriptionId(),sshKeyPair.getKeyVaultResourceName(),sshKeyPair.getSecretResourceId());

        if (!msg.getRetcode().equalsIgnoreCase("processing")) {
            if (msg.getRetcode().equalsIgnoreCase("success")){
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_COMPLETED);
                playBookService.completePlayBookJob(rpjobId,InfoClusterPlaybookJob.JOB_OK);
                planExecService.sendNextActivityMsg(activityLogId,param);
            }
            if (msg.getRetcode().equalsIgnoreCase("fail")){
                playBookService.completePlayBookJob(rpjobId,InfoClusterPlaybookJob.JOB_FAILED);
                currentLog.setState(InfoClusterOperationPlanActivityLog.ACTION_FAILED);
                currentLog.setLogs(msg.getData().toString());
            }
            planExecService.complateActivity(currentLog);
        }else{
            resultMsg=planExecService.loopActivity(clientname,message,10l,activityLogId);
        }
        return resultMsg;
    }

    /**
     * 时间打点日志内容
     * @param log
     * @return
     */
    private String timeTickLog(String log) {
        return "time_tick:" + DateUtil.now() + ":" + log;
    }

    /**
     * 获取重试IpList
     *
     * @param param 消息
     * @return
     */
    @Override
    public String getRetryIpList(JSONObject param) {
        if(param == null
                || !param.containsKey(JobNameConstant.Param_Ansible_Retry_Obj)){
            return null;
        }
        JSONObject retryObj = param.getJSONObject(JobNameConstant.Param_Ansible_Retry_Obj);
        String iplist = retryObj.getString("ipList");
        return iplist;
    }
}
