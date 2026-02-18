package com.sunbox.sdpcompose.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.dao.mapper.BaseImageScriptsMapper;
import com.sunbox.domain.*;
import com.sunbox.domain.metaData.LogsBlobContainer;
import com.sunbox.domain.metaData.SSHKeyPair;
import com.sunbox.domain.metaData.keyVault;
import com.sunbox.sdpcompose.mapper.*;
import com.sunbox.sdpcompose.model.azure.request.AzureExecuteJobPlaybookRequest;
import com.sunbox.sdpcompose.model.azure.request.MockMultipartFile;
import com.sunbox.sdpcompose.model.azure.response.PlaybookYmlResponse;
import com.sunbox.sdpcompose.service.IAmbariService;
import com.sunbox.sdpcompose.service.IAzureService;
import com.sunbox.sdpcompose.service.IPlanExecService;
import com.sunbox.sdpcompose.service.IPlayBookService;
import com.sunbox.sdpcompose.util.JacksonUtils;
import com.sunbox.sdpcompose.util.QuotedString;
import com.sunbox.service.IMetaDataItemService;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.util.HttpClientUtil;
import com.sunbox.util.KeyVaultUtil;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.sunbox.domain.BaseImageScripts.RUNTIMING_COLLECT_CLUSTER_INFO;

/**
 * @author : [niyang]
 * @className : PlayBookServiceImpl
 * @description : [描述说明该类的功能]
 * @createTime : [2022/12/8 5:12 PM]
 */

@Service
public class PlayBookServiceImpl implements IPlayBookService, BaseCommonInterFace {

    @Value("${azure.request.url}")
    private String azureUrl;

    @Value("${vm.domain}")
    private String vmDomain;

    @Value("${sdp.wgetpath}")
    private String sdpwgetpath;

    @Value("${vm.username}")
    private String vmusername;

    @Value("${keyvault.uri}")
    private String keyvaultUri;

    @Value("${keyvault.spot.uri}")
    private String spotkvuri;


    @Deprecated
    @Value("${sdp.ganglia.install.enable:0}")
    private String gangliainstallenable;

    @Value("${sdp.playbook.exec.timeout:300}")
    private Integer playBookExecTimeout;

    /**
     * 上传blob重试次数
     */
    @Value("${sdp.playbook.upload.retrytimes:5}")
    private Integer sdpPlayBookUploadRetryTimes;

    /**
     * 上传blob重试时间间隔 单位秒
     */
    @Value("${sdp.playbook.upload.retryduration:30}")
    private Long sdpPlayBookUploadRetryduration;

    @Autowired
    private KeyVaultUtil keyVaultUtil;

    @Autowired
    private ConfClusterScriptMapper confClusterScriptMapper;

    @Autowired
    private InfoClusterPlaybookJobMapper infoClusterPlaybookJobMapper;

    @Autowired
    private ConfClusterMapper confClusterMapper;

    @Autowired
    private InfoClusterVmMapper infoClusterVmMapper;

    @Autowired
    private BaseClusterScriptMapper baseClusterScriptMapper;

    @Autowired
    private IAzureService iAzureService;


    @Autowired
    private IPlanExecService planExecService;


    @Autowired
    private DistributedRedisLock redisLock;

    @Autowired
    private ConfClusterVmMapper clusterVmMapper;

    @Autowired
    private BaseImageScriptsMapper baseImageScriptsMapper;

    @Autowired
    private IAmbariService ambariService;

    @Autowired
    private InfoClusterOperationPlanActivityLogMapper planActivityLogMapper;

    @Autowired
    private IMetaDataItemService metaDataItemService;

    @Autowired
    private InfoClusterMapper infoClusterMapper;

    /** Blob中 */
    private final static String BLOB_CONTAINER_SCRIPT = "sdp2-scripts";

    /**
     * 执行Playbook,然后保存
     * @param clusterId
     * @param runTiming
     * @param activityLogId
     * @param ipList
     * @return
     */
    @Override
    public ResultMsg executePlaybookJobYmlAndSave(String clusterId,
                                                  String runTiming,
                                                  String activityLogId,
                                                  String taskId,
                                                  String ipList,String region) {
        //transactionId
        String transactionId = UUID.randomUUID().toString();

        String lock_key="user_script"+activityLogId;
        ResultMsg msg = new ResultMsg();

        List<ConfClusterScript> confClusterScriptList =
                confClusterScriptMapper.selectConfClusterScript(clusterId, runTiming);

        if(CollectionUtils.isEmpty(confClusterScriptList)) {
            getLogger().error("executePlaybookJob return empty collection clusterId {}, runTiming {} ", clusterId, runTiming);
            msg.setResult(true);
            msg.setMsg("executePlaybookJob return empty collection");
            return msg;
        }

        // 分布式锁防止并发
        boolean lock=redisLock.tryLock(lock_key,TimeUnit.SECONDS,0,300);

        //拿锁失败
        if (!lock){
            getLogger().error("执行用户定义脚本获取锁失败，activityLogId"+activityLogId);
            return new ResultMsg();
        }

        //region 获取需要执行的机器，task不为空时为扩容场景，反之为集群创建的场景
        List<InfoClusterVm> infoClusterVmList = new ArrayList<>();

        if (StringUtils.isEmpty(taskId)){
            infoClusterVmList = infoClusterVmMapper.selectByClusterId(clusterId);
        }else{
            infoClusterVmList = infoClusterVmMapper.selectByClusterIdAndScaleOutTaskId(clusterId,taskId);
        }

        getLogger().info("vm_list:"+infoClusterVmList.toString());
        //endregion

        //region 构建playbook文件
        // ScriptFileUri
        String scriptFileUri = confClusterScriptList.stream().map(confClusterScript -> {
            return confClusterScript.getScriptPath();
        }).collect(Collectors.joining(","));


        confClusterScriptList.forEach(confClusterScript -> {

            //region 获取filename
            try {
                String[] fils = confClusterScript.getScriptPath().split("\\/");
                String filename = fils[fils.length - 1];
                getLogger().info("用户自定义脚本filename：" + filename);
                confClusterScript.setFilename(filename);
            }catch (Exception e){
                getLogger().error("获取用户自定义脚本filename异常，",e);
            }
            //endregion

            if(StringUtils.isNotBlank(confClusterScript.getScriptParam())) {
                String toAdd = " " + confClusterScript.getScriptParam();
                String a = confClusterScript.getFilename() + toAdd;
                confClusterScript.setScriptPath(a);
            }else{
                String a = confClusterScript.getFilename();
                confClusterScript.setScriptPath(a);
            }
        });

        //confClusterScriptList 生成 playbook yml
        String remoteUser = vmusername;
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
        String clusterName = confCluster.getClusterName();
        String fileName = clusterName + "_" + runTiming + ".yaml";
        //生成 playbook yml
        getLogger().info("生成 playbook yaml的数据 {}  <br/> {}",JSONObject.toJSONString(confClusterScriptList),remoteUser);
        String playbookYmlStr = getPlaybookYmlUri(confClusterScriptList, remoteUser);
        getLogger().info("execute Playbook Job, get playbook yml response {}", playbookYmlStr);

        //将playbook yml文档上传blob
        String playbookUri = this.uploadYml2MsBlob(fileName, playbookYmlStr,region);
        getLogger().info("将playbook yml文档上传blob file name {} str {} uri {}", fileName, playbookYmlStr, playbookUri);
        if(StringUtils.isEmpty(playbookUri)){
            String msgstr=String.format("ClusterId: %s,上传Playbook到Blob失败，未获取到Playbook Uri:%s",clusterId,fileName);
            getLogger().info(msgstr);
            msg.setResult(false);
            msg.setErrorMsg(msgstr);
            return msg;
        }

        //endregion
        List<String> ips = infoClusterVmList.stream()
                .filter(vm -> {return vm.getState() == InfoClusterVm.VM_RUNNING;})
                .map(InfoClusterVm::getInternalip).collect(Collectors.toList());

        if (ips == null || ips.size()==0){
            getLogger().error("无可用的VM执行脚本，可查询vmreject表，查询降级详情。");
            msg.setResult(false);
            msg.setErrorMsg("无可用的VM执行脚本，可查询vmreject表，查询降级详情");
            return msg;
        }

        String nodeListStr = "";
        if (StringUtils.isNotEmpty(ipList)){
            getLogger().info("由于指定了重试IPList, 所以本次执行IP列表为:" + ipList);
            nodeListStr = ipList;
        } else {
            nodeListStr = CollectionUtil.join(ips, ",");
            getLogger().info("本次执行IP列表为:" + nodeListStr);
        }

        /**执行playbook 调用微软接口 begin*/
        AzureExecuteJobPlaybookRequest azureExecuteJobPlaybookRequest = new AzureExecuteJobPlaybookRequest();
        azureExecuteJobPlaybookRequest.setApiVersion("v1");
        azureExecuteJobPlaybookRequest.setTransactionId(transactionId);
        azureExecuteJobPlaybookRequest.setNodeList(nodeListStr);
        azureExecuteJobPlaybookRequest.setPlaybookUri(playbookUri);
        getLogger().info("执行计划参数 {}", scriptFileUri);
        azureExecuteJobPlaybookRequest.setScriptFileUris(Arrays.asList(scriptFileUri.split(",")));

        azureExecuteJobPlaybookRequest.setExtraVars("");
        azureExecuteJobPlaybookRequest.setTimeout(0); //非必填
        azureExecuteJobPlaybookRequest.setPlaybookType(AzureExecuteJobPlaybookRequest.playbookType_custom);
        azureExecuteJobPlaybookRequest.setRegion(region);
        //获取ssh密钥对
        SSHKeyPair sshKeyPair = metaDataItemService.getSSHKeyPair(region, SSHKeyPair.privateKeyType);
        if (sshKeyPair == null){
            getLogger().error("获取ssh密钥对失败!");
            msg.setResult(false);
            msg.setErrorMsg("获取ssh密钥对失败，查询ssh密钥管理详情");
            return msg;
        }
        azureExecuteJobPlaybookRequest.setSubscriptionId(sshKeyPair.getSubscriptionId());
        azureExecuteJobPlaybookRequest.setSshKeyVaultName(sshKeyPair.getKeyVaultResourceName());
        azureExecuteJobPlaybookRequest.setSshPrivateSecretName(sshKeyPair.getSecretResourceId());
        getLogger().info("执行Playbook, 请求参数 {}", JSONObject.toJSONString(azureExecuteJobPlaybookRequest));
        ResultMsg exePlaybookJobResult = iAzureService.executeJobPlaybook(azureExecuteJobPlaybookRequest);
        getLogger().info("执行Playbook, response {}", JSONObject.toJSONString(exePlaybookJobResult));
        /**执行playbook 调用微软接口 end*/

        //保存 info_cluster_playbook_job
        Date now = new Date();
        InfoClusterPlaybookJobWithBLOBs infoClusterPlaybookJob = new InfoClusterPlaybookJobWithBLOBs();
        infoClusterPlaybookJob.setTransactionId(transactionId);
        infoClusterPlaybookJob.setClusterId(clusterId);
        infoClusterPlaybookJob.setClusterName(clusterName);
        infoClusterPlaybookJob.setActivityLogId(activityLogId);
        infoClusterPlaybookJob.setJobId(transactionId);
        infoClusterPlaybookJob.setNodeList(nodeListStr);
        infoClusterPlaybookJob.setPlaybookUri(playbookUri);
        infoClusterPlaybookJob.setScriptFileUri(scriptFileUri);

        //0=初始化 1=执行中 2=ok 3=失败
        infoClusterPlaybookJob.setJobStatus(InfoClusterPlaybookJob.JOB_INIT);
        infoClusterPlaybookJob.setBegTime(now);
        saveInfoClusterPlaybookJob(infoClusterPlaybookJob);
        getLogger().info("执行Playbook,然后保存完成，保存数据 {}", JSONObject.toJSONString(infoClusterPlaybookJob));

        //将playbook yml 和URI封装返回
        PlaybookYmlResponse response = new PlaybookYmlResponse();
        response.setUri(playbookUri);
        response.setYml(playbookYmlStr);

        msg.setResult(true);
        msg.setData(response);
        msg.setBizid(transactionId);
        try {
            if (lock){
                redisLock.unlock(lock_key);
            }
        }catch (Exception e){
            getLogger().error("释放锁异常，activityLogId: "+activityLogId,e);
        }
        return msg;
    }

    @Override
    public ResultMsg saveUserScriptPlayBookJob(ConfClusterScript confClusterScript,
                                               String transactionId,
                                               String nodeList,String region) {
        ResultMsg msg = new ResultMsg();

        //region 保存confclusterScript
        int n=confClusterScriptMapper.insertSelective(confClusterScript);
        if (n==0){
            msg.setResult(false);
            msg.setErrorMsg("保存ConfClusterScript数据出错。");
            return msg;
        }
        //endregion

        // ScriptFileUri
        String scriptFileUri = confClusterScript.getScriptPath();

        //region 1.处理脚本数据
        //region 获取filename
        try {
            String[] fils = confClusterScript.getScriptPath().split("\\/");
            String filename = fils[fils.length - 1];
            getLogger().info("用户自定义脚本filename：" + filename);
            confClusterScript.setFilename(filename);
        }catch (Exception e){
            getLogger().error("获取用户自定义脚本filename异常，",e);

        }
        //endregion

        String source = confClusterScript.getScriptPath();
        if(StringUtils.isNotBlank(confClusterScript.getScriptParam())) {
            String toAdd = " " + confClusterScript.getScriptParam();
            String a = confClusterScript.getFilename() + toAdd;
            confClusterScript.setScriptPath(a);
        }else{
            String a = confClusterScript.getFilename();
            confClusterScript.setScriptPath(a);
        }


        //endregion 1.处理脚本数据
        String clusterId=confClusterScript.getClusterId();
        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);

        //region 生成执行计划
        ResultMsg pmsg=planExecService.createPlanAndRun(clusterId,confCluster.getClusterReleaseVer(),
                "runuserscript",
                null,
                null);
        InfoClusterOperationPlanActivityLogWithBLOBs activityLog=
                planExecService.getFirstActivity(pmsg.getBizid());
        //endregion

        //region confClusterScriptList 生成 playbook yml

        String remoteUser = vmusername;
        String clusterName = confCluster.getClusterName();
        String fileName = clusterName + "_" +transactionId+ ".yaml";

        //region 生成 playbook yml
        getLogger().info("生成 playbook yaml的数据 {}  <br/> {}",JSONObject.toJSONString(confClusterScript),remoteUser  );
        List<ConfClusterScript> confClusterScriptList=new ArrayList<>();
        confClusterScriptList.add(confClusterScript);
        String playbookYmlStr = getPlaybookYmlUri(confClusterScriptList, remoteUser);
        getLogger().info("execute Playbook Job, get playbook yml response {}", playbookYmlStr);
        //endregion

        //region 将playbook yml文档上传blob
        String playbookUri = this.uploadYml2MsBlob(fileName, playbookYmlStr,region);
        getLogger().info("将playbook yml文档上传blob file name {} str {} uri {}", fileName, playbookYmlStr, playbookUri);
        //endregion

        //region 保存 info_cluster_playbook_job
        Date now = new Date();
        InfoClusterPlaybookJobWithBLOBs infoClusterPlaybookJob = new InfoClusterPlaybookJobWithBLOBs();
        infoClusterPlaybookJob.setTransactionId(transactionId);
        infoClusterPlaybookJob.setClusterId(clusterId);
        infoClusterPlaybookJob.setClusterName(clusterName);
        infoClusterPlaybookJob.setActivityLogId(activityLog.getActivityLogId());
        infoClusterPlaybookJob.setJobId(transactionId);
        infoClusterPlaybookJob.setNodeList(nodeList);
        infoClusterPlaybookJob.setPlaybookUri(playbookUri);
        infoClusterPlaybookJob.setScriptFileUri(scriptFileUri);
        infoClusterPlaybookJob.setJobStatus(InfoClusterPlaybookJob.JOB_INIT); //0=初始化 1=执行中 2=ok 3=失败
        infoClusterPlaybookJob.setBegTime(now);
        infoClusterPlaybookJob.setConfScriptId(confClusterScript.getConfScriptId());
        infoClusterPlaybookJob.setJobType("user");

        saveInfoClusterPlaybookJob(infoClusterPlaybookJob);
        getLogger().info("执行Playbook,然后保存完成，保存数据 {}", JSONObject.toJSONString(infoClusterPlaybookJob));
        //endregion 保存 info_cluster_playbook_job

        //endregion

        // 将playbook yml 和URI封装返回
        PlaybookYmlResponse response = new PlaybookYmlResponse();
        response.setUri(playbookUri);
        response.setYml(playbookYmlStr);

        msg.setResult(true);
        msg.setData(response);
        msg.setBizid(transactionId);

        //region 执行计划启动
        planExecService.startPlan(pmsg.getBizid());
        //endregion

        return msg;

    }

    /**
     * 执行用户自定义脚本
     * activityLog
     */
    @Override
    public ResultMsg executeUserCustomScript(String activityLogId,String region) {

        ResultMsg msg = new ResultMsg();

        List<InfoClusterPlaybookJobWithBLOBs> playbookjobs
                =infoClusterPlaybookJobMapper.selectByActivityLogId(activityLogId);
        InfoClusterPlaybookJobWithBLOBs playbookjob=playbookjobs.get(0);

        /**执行playbook 调用微软接口 begin*/
        AzureExecuteJobPlaybookRequest azureExecuteJobPlaybookRequest = new AzureExecuteJobPlaybookRequest();
        azureExecuteJobPlaybookRequest.setApiVersion("v1");
        azureExecuteJobPlaybookRequest.setTransactionId(playbookjob.getTransactionId());
        azureExecuteJobPlaybookRequest.setNodeList(playbookjob.getNodeList());
        azureExecuteJobPlaybookRequest.setPlaybookUri(playbookjob.getPlaybookUri());
        getLogger().info("执行计划参数 {}", playbookjob.getScriptFileUri());
        azureExecuteJobPlaybookRequest.setScriptFileUris(Arrays.asList(playbookjob.getScriptFileUri().split(",")));

        azureExecuteJobPlaybookRequest.setExtraVars("");
        azureExecuteJobPlaybookRequest.setTimeout(0); //非必填
        azureExecuteJobPlaybookRequest.setPlaybookType(AzureExecuteJobPlaybookRequest.playbookType_custom);
        azureExecuteJobPlaybookRequest.setRegion(region);
        //获取ssh密钥对
        SSHKeyPair sshKeyPair = metaDataItemService.getSSHKeyPair(region, SSHKeyPair.privateKeyType);
        if (sshKeyPair == null){
            getLogger().error("获取ssh密钥对失败!");
            msg.setResult(false);
            msg.setErrorMsg("获取ssh密钥对失败，查询ssh密钥管理详情");
            return msg;
        }
        azureExecuteJobPlaybookRequest.setSubscriptionId(sshKeyPair.getSubscriptionId());
        azureExecuteJobPlaybookRequest.setSshKeyVaultName(sshKeyPair.getKeyVaultResourceName());
        azureExecuteJobPlaybookRequest.setSshPrivateSecretName(sshKeyPair.getSecretResourceId());
        getLogger().info("执行Playbook, 请求参数 {}", JSONObject.toJSONString(azureExecuteJobPlaybookRequest));
        ResultMsg exePlaybookJobResult = iAzureService.executeJobPlaybook(azureExecuteJobPlaybookRequest);
        getLogger().info("执行Playbook, response {}", JSONObject.toJSONString(exePlaybookJobResult));

        /**执行playbook 调用微软接口 end*/
        msg.setResult(true);
        msg.setData(azureExecuteJobPlaybookRequest);
        msg.setBizid(playbookjob.getTransactionId());
        return msg;


    }

    // String clusterId, String clusterName, String activityLogId, String jobId, String nodeList,
    public void saveInfoClusterPlaybookJob(InfoClusterPlaybookJobWithBLOBs infoClusterPlaybookJobWithBLOBs) {
        infoClusterPlaybookJobMapper.insertSelective(infoClusterPlaybookJobWithBLOBs);
    }

    /**
     * hdfs数据检查
     *
     * @param clusterId
     * @return
     */
    @Override
    public ResultMsg hdfsFSck(String clusterId) {
        ResultMsg msg=new ResultMsg();

        ConfCluster cluster = confClusterMapper.selectByPrimaryKey(clusterId);

        if (Objects.isNull(cluster)) {
            msg.setResult(false);
            msg.setErrorMsg("没找到集群：clusterId = " + clusterId);
            return msg;
        }

        // 状态为2时是运行中集群，其它状态均为不正常状态
        if (!Objects.equals(cluster.getState(), ConfCluster.CREATED)) {
            msg.setResult(false);
            msg.setErrorMsg("集群状态不能创建脚本执行任务，当前集群状态： " + cluster.getState());
            return msg;
        }
        //HBase不执行脚本
        if ("hbase".equalsIgnoreCase(cluster.getScene())){
            msg.setResult(false);
            msg.setErrorMsg("HBase不执行脚本");
            return msg;
        }

        List<ConfClusterVm> vms=clusterVmMapper.getVmConfsByRole("core",clusterId);

        if (vms==null || vms.size()==0){
            msg.setResult(false);
            msg.setErrorMsg("未找到imgId");
            return msg;
        }

        String imgId=vms.get(0).getImgId();
        if (StringUtils.isEmpty(imgId)){
            msg.setResult(false);
            msg.setErrorMsg("未发现imgId");
            return msg;
        }

        List<BaseImageScripts> baseImageScripts=
                baseImageScriptsMapper.getAllByImgIdAndRunTiming(imgId,"run_hdfs_fsck");
        if (baseImageScripts==null|| baseImageScripts.size()==0){
            msg.setResult(false);
            msg.setErrorMsg("未发现imgid相关的runtiming，"+clusterId+"：run_hdfs_fsck");
            return msg;
        }

        String scriptfileUri=baseImageScripts.get(0).getScriptFileUri().replace("{wgetpath}",sdpwgetpath);

        getLogger().info("run_hdfs_fsck<scriptfileurl:"+scriptfileUri);
        ConfClusterScript confClusterScript=new ConfClusterScript();
        confClusterScript.setClusterId(clusterId);
        confClusterScript.setScriptPath(scriptfileUri);
        confClusterScript.setScriptName("run_hdfs_fsck");
        confClusterScript.setScriptParam("");
        confClusterScript.setRunTiming("run_hdfs_fsck");
        confClusterScript.setConfScriptId(UUID.randomUUID().toString());

        List<InfoClusterVm> infoClusterVms=
                infoClusterVmMapper.selectByClusterIdAndRoleAndState(clusterId,"core",InfoClusterVm.VM_RUNNING);

        if (infoClusterVms==null || infoClusterVms.size()==0){
            msg.setResult(false);
            msg.setErrorMsg("找不到可用core节点");
            return msg;
        }

        String nodelist=infoClusterVms.get(0).getInternalip();

         return saveUserScriptPlayBookJob(confClusterScript,UUID.randomUUID().toString(),
                nodelist,cluster.getRegion());
    }


    /**
     * 客户自定义脚本执行
     * @param list
     * @param remoteUser
     * @return
     */
    public String getPlaybookYmlUri(List<ConfClusterScript> list, String remoteUser) {
        /**tasks 数组 开始*/
        List<Map<String, Object>> mapList = new ArrayList<>();
        AtomicInteger i= new AtomicInteger(1);

        String clusterName="";
        if (list!=null && list.size()>0){
            ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(list.get(0).getClusterId());
            clusterName=confCluster.getClusterName().toLowerCase();
        }

        final String finalClusterName = clusterName;
        list.forEach(script -> {
            String cs="custom_script_"+i.get();
            String logpath="/var/log/"+script.getFilename()+"_"+System.currentTimeMillis()+".log";
            //region cp map
            Map<String, Object> cpmap = new LinkedHashMap<>();
            Map<String,Object> copy=new LinkedHashMap<>();
            cpmap.put("name", cs);
            String cppath="/home/sunbox/"+script.getFilename();
            copy.put("src",new QuotedString(cppath));
            copy.put("dest",new QuotedString("/tmp"));
            cpmap.put("copy", copy);
            //endregion

            //region runmap

            Map<String,Object> runmap=new LinkedHashMap<>();
            String runname=cs+"_run";
            String shellStr=String.format("if [[ $(echo $(hostname)|rev|cut -d'-' -f3-|rev|grep -w -i %s|wc -l) -eq 0 ]]; then exit 1; fi && ps -ef | grep %s |grep -v grep |awk '{print $2;}' | xargs kill -9 ||true && chmod a+x /tmp/%s && /tmp/%s >>%s",
                    finalClusterName,script.getFilename(),script.getFilename(),script.getScriptPath(),logpath);
            runmap.put("name", runname);
            runmap.put("shell",new QuotedString(shellStr));
            runmap.put("async",playBookExecTimeout);
            runmap.put("poll",5);
            runmap.put("register",runname);
            //endregion

            i.getAndIncrement();

            mapList.add(cpmap);
            mapList.add(runmap);
        });
        /**tasks 数组 结束*/

        Map<String, Object> parentMap = new LinkedHashMap<>();
        parentMap.put("hosts", "webservers");
        parentMap.put("remote_user", remoteUser);
        parentMap.put("gather_facts", "No");
        parentMap.put("become", "yes");
        parentMap.put("become_method", "sudo");
        parentMap.put("become_user", "root");
        parentMap.put("tasks", mapList);

        List<Map<String, Object>> outList = new ArrayList<>();
        outList.add(parentMap);

        String ymlStr = JacksonUtils.javaBean2Yaml(outList, ArrayList.class);

        //替换
        ymlStr = ymlStr.replace("hosts: webservers", "hosts: [webservers]")
                .replace("gather_facts: 'No'", "gather_facts: No")
                .replace("become: 'yes'", "become: yes");

        return ymlStr;
    }

    /**
     * upload yml file 2 ms blob
     * */
    public String uploadYml2MsBlob(String fileName, String ymlStr,String region) {
        String name = fileName;
        String originalFilename = fileName;
        String contentType = "multipart/form-data";
        byte[] content = ymlStr.getBytes(Charset.defaultCharset());
        MockMultipartFile mockMultipartFile = generateMultipartFile(name, originalFilename, contentType, content);

        //调用微软上传blob接口，获取 playbook uri
        String playbookUri = getPlaybookUri(mockMultipartFile, name,region);

        PlaybookYmlResponse response = new PlaybookYmlResponse();
        response.setYml(ymlStr);
        response.setUri(playbookUri);//需要调用微软接口返回的URI，设置进来

        getLogger().info("generatePlaybookYml resp {}", JSONObject.toJSON(response));

        return playbookUri;
    }

    public MockMultipartFile generateMultipartFile(String name, @Nullable String originalFilename, @Nullable String contentType, @Nullable byte[] content) {
        MockMultipartFile mockMultipartFile = new MockMultipartFile(name, originalFilename, contentType, content);
        return  mockMultipartFile;

    }

    /**
     * 上传playbook到blob with retry
     *
     * @param multipartFile
     * @param fileName
     * @return
     */
    public String getPlaybookUri(MultipartFile multipartFile, String fileName,String region) {
        LogsBlobContainer blobContainer = metaDataItemService.getLogsBlobContainer(BLOB_CONTAINER_SCRIPT);
        String storageAccount = blobContainer.getStorageAccountName();
        String container = blobContainer.getName();
        String subscriptionId = blobContainer.getSubscriptionId();
        Assert.notEmpty(storageAccount,"上传脚本到blob,storageAccount不能为空,请检查元数据配置");
        Assert.notEmpty(container,"上传脚本到blob,container不能为空,请检查元数据配置");
        Assert.notEmpty(subscriptionId,"上传脚本到blob,subscriptionId不能为空,请检查元数据配置");

        String reqUri = azureUrl + "/api/v1/blobs/"+storageAccount+"/"+container+"/" + fileName;
        HashMap<String, String> headerMap = new HashMap<>(1);
        headerMap.put("subscriptionId",subscriptionId);
        Integer i = 0;
        while(true) {
            try {
                //返回结果,只返回success字符串
                String httpRespStr = HttpClientUtil.doPostWithMultipartFile(reqUri, multipartFile,headerMap);
                getLogger().info("get Playbook Uri req url {}, response {}", reqUri, httpRespStr);
                if (StringUtils.isNotEmpty(httpRespStr)){
                    JSONObject respJson = JSONObject.parseObject(httpRespStr);
                    if("Success".equals(respJson.getString("result"))) {
                        String blobUrl = respJson.getString("blobUrl");
                        if (StringUtils.isNotEmpty(blobUrl)){
                            return blobUrl;
                        }
                    }
                }
            }catch (Exception e){
                getLogger().error("上传playbook到blob异常",e);
            }
            i++;
            if (i > sdpPlayBookUploadRetryTimes){
                return "";
            }
            ThreadUtil.sleep(1000 * sdpPlayBookUploadRetryduration);
        }
    }

    /**
     * 保存集群安装过程中playbook数据
     * 主要包含安装ambariserver 和agent操作
     * @param releaseVersion 发布版本
     * @param runTiming 运行时机
     * @param clusterId 集群ID
     * @param activityLogId actionID
     * @param nodeListStr ambari 安装列表
     * @param ganglialist ganglia 安装列表
     * */
    public ResultMsg savePlaybook(String releaseVersion,
                                  String runTiming,
                                  String clusterId,
                                  String activityLogId,
                                  String nodeListStr,
                                  String ganglialist,String region) {
        Date now = new Date();

        //region 处理nodelist
        // 此处替换\u0000掉， 避免奇怪的创建失败
        if (Objects.nonNull(nodeListStr)) {
            nodeListStr = nodeListStr.replace("\u0000", "");
        }
        //endregion

        //region 获取集群vm信息
        List<ConfClusterVm> confClusterVms = clusterVmMapper.getVmConfs(clusterId);
        if (confClusterVms == null || confClusterVms.size() == 0) {
            ResultMsg msg = new ResultMsg();
            msg.setErrorMsg("集群缺少VM配置信息。");
            msg.setResult(false);
            return msg;
        }

        if (StringUtils.isEmpty(confClusterVms.get(0).getImgId())) {
            ResultMsg msg = new ResultMsg();
            msg.setErrorMsg("集群缺少img_id配置信息。");
            msg.setResult(false);
            return msg;
        }
        //endregion

        //region 获取img_id
        String imgId = confClusterVms.get(0).getImgId();
        //endregion

        //region 获取集群镜像对应的脚本
        List<BaseImageScripts> imageScripts = baseImageScriptsMapper.getAllByImgIdAndRunTiming(imgId, runTiming);
        if (imageScripts == null || imageScripts.size() > 1) {
            ResultMsg msg = new ResultMsg();
            msg.setErrorMsg("镜像脚本配置有误。");
            msg.setResult(false);
            return msg;
        }

        BaseImageScripts baseClusterScript = imageScripts.get(0);
        //endregion

        ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
        String clusterName = confCluster.getClusterName();


        String lock_key = "ambari_" + activityLogId;
        boolean lock = redisLock.tryLock(lock_key, TimeUnit.SECONDS, 0, 300);

        if (!lock) {
            getLogger().info("执行ansible脚本未拿到锁，activityLogId：" + activityLogId);
            ResultMsg resultMsg = new ResultMsg();
            resultMsg.setResult(false);
            resultMsg.setErrorMsg("执行ansible脚本未拿到锁(" + activityLogId + ")");
            return resultMsg;
        }

        //region 构建playbookjob
        InfoClusterPlaybookJobWithBLOBs infoClusterPlaybookJob = new InfoClusterPlaybookJobWithBLOBs();
        String transactionId = UUID.randomUUID().toString();
        infoClusterPlaybookJob.setTransactionId(transactionId);
        infoClusterPlaybookJob.setClusterId(clusterId);
        infoClusterPlaybookJob.setClusterName(clusterName);
        infoClusterPlaybookJob.setActivityLogId(activityLogId);
        infoClusterPlaybookJob.setJobId(transactionId);
        infoClusterPlaybookJob.setNodeList(nodeListStr);
        infoClusterPlaybookJob.setPlaybookUri(baseClusterScript.getPlaybookUri());
        infoClusterPlaybookJob.setScriptFileUri(baseClusterScript.getScriptFileUri());
        infoClusterPlaybookJob.setExtraVars(baseClusterScript.getExtraVars());
        infoClusterPlaybookJob.setSortNo(baseClusterScript.getSortNo());
        infoClusterPlaybookJob.setJobStatus(InfoClusterPlaybookJob.JOB_INIT); //0=初始化 1=执行中 2=ok 3=失败
        infoClusterPlaybookJob.setBegTime(now);
        saveInfoClusterPlaybookJob(infoClusterPlaybookJob);
        getLogger().info("保存安装ambari的playbook数据完成， 保存数据 {} ", JSONObject.toJSONString(infoClusterPlaybookJob));
        //endregion

        //region 构建访问ansible server的参数
        char escape = ' ';
        String patternPrefix = "{";
        String patternSuffix = "}";

        AzureExecuteJobPlaybookRequest azureExecuteJobPlaybookRequest = new AzureExecuteJobPlaybookRequest();
        azureExecuteJobPlaybookRequest.setApiVersion("v1");
        azureExecuteJobPlaybookRequest.setTransactionId(transactionId);
        azureExecuteJobPlaybookRequest.setNodeList(nodeListStr);
        //进行动态匹配
        Map<String, Object> mapPlaybookUri = new HashMap<>();
        mapPlaybookUri.put("wgetpath", sdpwgetpath);
        StrSubstitutor strSubstitutor1 = new StrSubstitutor(mapPlaybookUri, patternPrefix, patternSuffix, escape);
        String playbookUri = strSubstitutor1.replace(baseClusterScript.getPlaybookUri());
        azureExecuteJobPlaybookRequest.setPlaybookUri(playbookUri);

        Map<String, Object> scriptFileMap = new HashMap<>();
        scriptFileMap.put("wgetpath", sdpwgetpath);
        StrSubstitutor strSubstitutor2 = new StrSubstitutor(scriptFileMap, patternPrefix, patternSuffix, escape);
        String scriptFileUri = strSubstitutor2.replace(baseClusterScript.getScriptFileUri());
        List<String> scriptFileUriList = Arrays.asList(scriptFileUri.split(","));
        azureExecuteJobPlaybookRequest.setScriptFileUris(scriptFileUriList);

        List<InfoClusterVm> ambarihosts = infoClusterVmMapper.selectByClusterIdAndRole(clusterId, "ambari");

        if (ambarihosts == null || ambarihosts.size() == 0) {
            getLogger().info("保存playbook数据 未查询到ambarihosts数据");
            ResultMsg msg = new ResultMsg();
            msg.setResult(false);
            msg.setErrorMsg("保存playbook数据 未查询到ambarihosts数据");
            return msg;
        }

        //拼接 extra vars
        String extraVars = baseClusterScript.getExtraVars();
        Map<String, Object> map = new HashMap<>();

        map.put("clustername", confCluster.getClusterName());

        //region 设置安装ambariserver 和agent的公共参数
        if (runTiming.equalsIgnoreCase(BaseImageScripts.RUNTIMING_AMBARI_SERVER)
                || runTiming.equalsIgnoreCase(BaseImageScripts.RUNTIMING_AMBARI_AGENT)) {
            map.put("ambarihost", ambarihosts.get(0).getHostName());
            map.put("domain", vmDomain);
            map.put("wgetpath", sdpwgetpath);
            map.put("clusterid", confCluster.getClusterId());
            map.put("zone", confCluster.getZone());
            map.put("ganglialist", ganglialist);
            map.put("installganglia", confCluster.getEnableGanglia());
            map.put("logblob", confCluster.getLogPath());
            map.put("username", vmusername);
            map.put("miclientid", confCluster.getLogMIClientId());
            map.put("kvuri", keyvaultUri);
            map.put("spotkvuri",spotkvuri);
            map.put("opsmiclientid",confCluster.getLogMIClientId());

            azureExecuteJobPlaybookRequest.setPlaybookType(AzureExecuteJobPlaybookRequest.playbookType_sdp);

        }
        //endregion

        //region 设置 agent 个性参数
        if (runTiming.equalsIgnoreCase(BaseImageScripts.RUNTIMING_AMBARI_AGENT)) {
            map.put("ganglialist", "123");
        }

        //endregion

        //region 设置ambari server 数据库

        if (runTiming.equalsIgnoreCase(BaseImageScripts.RUNTIMING_AMBARI_SERVER)) {
            if (Objects.equals(confCluster.getIsEmbedAmbariDb(), 1)) {
                //使用内嵌数据库
                map.put("ambaridb", "1");
                map.put("ambaridbport", "1");
                map.put("ambaridbname", "1");
                map.put("dbuser", "1");
                map.put("dbpassword", "1");
                map.put("isembedambaridb", 1);
            } else {
                map.put("ambaridb", confCluster.getAmbariDburl());
                map.put("ambaridbport", confCluster.getAmbariPort());
                map.put("ambaridbname", confCluster.getAmbariDatabase());
                keyVault keyVault = metaDataItemService.getkeyVault(region);
                String dbUser = keyVaultUtil.getSecretVal("ambari-db-user-" + clusterName,keyVault.getEndpoint());
                map.put("dbuser", dbUser);
                String dbpassword = keyVaultUtil.getSecretVal("ambari-db-pwd-" + clusterName,keyVault.getEndpoint());
                map.put("dbpassword", dbpassword);
                map.put("isembedambaridb", 0);
            }
        }

        //endregion

        //region 设置安装tezui的参数
        if (runTiming.equalsIgnoreCase(BaseImageScripts.RUNTIMING_TEZUI)) {
            //region 获取timeline server
            ResultMsg tlmsg = ambariService.getActiveComponentHostName(
                    confCluster.getClusterId(),
                    "APP_TIMELINE_SERVER");
            if (tlmsg.getResult()) {
                map.put("tlhost", tlmsg.getData().toString());
            } else {
                tlmsg.setErrorMsg("getActiveComponentHostName失败," + tlmsg.getErrorMsg());
                return tlmsg;
            }
            //endregion

            //region 获取RESOURCEMANAGER
            ResultMsg rmmsg = ambariService.getActiveComponentHostName(
                    confCluster.getClusterId(),
                    "RESOURCEMANAGER");
            if (rmmsg.getResult()) {
                map.put("rmhost", rmmsg.getData().toString());
            } else {
                rmmsg.setErrorMsg("getActiveComponentHostName失败," + rmmsg.getErrorMsg());
                return rmmsg;
            }
            //endregion

            map.put("username", vmusername);

            azureExecuteJobPlaybookRequest.setPlaybookType(AzureExecuteJobPlaybookRequest.playbookType_custom);
        }

        //endregion

        //region 设置 收集日志的参数
        if (runTiming.equalsIgnoreCase(BaseImageScripts.RUNTIMING_COLLECTLOG)) {
            InfoClusterOperationPlanActivityLog activityLog = planActivityLogMapper.selectByPrimaryKey(activityLogId);
            map.put("logblob", confCluster.getLogPath());
            map.put("clusterid", confCluster.getClusterId());
            map.put("miclientid", confCluster.getLogMIClientId());
            map.put("username", vmusername);
            map.put("logid", activityLog.getPlanId());
            azureExecuteJobPlaybookRequest.setPlaybookType(AzureExecuteJobPlaybookRequest.playbookType_custom);
        }
        //endregion

        //region 磁盘扩容脚本
        if (runTiming.equalsIgnoreCase(BaseImageScripts.RUNTIMING_DISKSCALEOUT)) {
            map.put("username", vmusername);
            azureExecuteJobPlaybookRequest.setPlaybookType(AzureExecuteJobPlaybookRequest.playbookType_custom);
        }
        //endregion

        //region 调整带宽
        if (runTiming.equalsIgnoreCase(BaseImageScripts.RUNTIMING_BANDWITDTH_ADJUEST)) {
            String bwkey = "bw_" + clusterId;
            String bwwidth = redisLock.getValue(bwkey);
            map.put("username", vmusername);
            map.put("bandwidth", bwwidth);
            azureExecuteJobPlaybookRequest.setPlaybookType(AzureExecuteJobPlaybookRequest.playbookType_custom);
        }
        //endregion

        //region 清理ganglia数据
        if (runTiming.equalsIgnoreCase(BaseImageScripts.RUNTIMING_CLEAER_GANGLIA_DATA)) {
            map.put("username", vmusername);
            map.put("agentname", ganglialist);
            azureExecuteJobPlaybookRequest.setPlaybookType(AzureExecuteJobPlaybookRequest.playbookType_custom);
        }
        //endregion

        StrSubstitutor strSubstitutor = new StrSubstitutor(map, patternPrefix, patternSuffix, escape);
        String toPatternPath = strSubstitutor.replace(extraVars);
        getLogger().info("extraVars 进行匹配之后 生成数据  {}", toPatternPath);

        azureExecuteJobPlaybookRequest.setExtraVars(toPatternPath);
        azureExecuteJobPlaybookRequest.setTimeout(0); //非必填

        azureExecuteJobPlaybookRequest.setRegion(region);
        //获取ssh密钥对
        SSHKeyPair sshKeyPair = metaDataItemService.getSSHKeyPair(region, SSHKeyPair.privateKeyType);
        if (sshKeyPair == null){
            ResultMsg msg = new ResultMsg();
            getLogger().error("获取ssh密钥对失败!");
            msg.setResult(false);
            msg.setErrorMsg("获取ssh密钥对失败，查询ssh密钥管理详情");
            return msg;
        }
        azureExecuteJobPlaybookRequest.setSubscriptionId(sshKeyPair.getSubscriptionId());
        azureExecuteJobPlaybookRequest.setSshKeyVaultName(sshKeyPair.getKeyVaultResourceName());
        azureExecuteJobPlaybookRequest.setSshPrivateSecretName(sshKeyPair.getSecretResourceId());
        getLogger().info("执行Playbook, 请求参数 {}", JSONObject.toJSONString(azureExecuteJobPlaybookRequest));
        ResultMsg exePlaybookJobResult = iAzureService.executeJobPlaybook(azureExecuteJobPlaybookRequest);
        getLogger().info("执行Playbook, response {}", JSONObject.toJSONString(exePlaybookJobResult));

        //endregion

        try {
            if (lock) {
                redisLock.unlock(lock_key);
            }
        } catch (Exception e) {
            getLogger().error("释放锁异常，" + lock_key, e);
        }

        ResultMsg msg = new ResultMsg();
        msg.setResult(true);
        msg.setBizid(transactionId);
        return msg;
    }

    @Override
    public ResultMsg queryPlaybookJob(String transactionId,String subscriptionId,String keyVaultResourceName,String secretResourceId) {
        //去微软查询执行状态
        getLogger().info("查询ansible执行状态 {}", transactionId);
        return iAzureService.queryPlaybookExecuteResult(transactionId,subscriptionId,keyVaultResourceName,secretResourceId);
    }

    @Override
    public ResultMsg updatePlaybookJobStatus(String transactionId, Integer status) {
        InfoClusterPlaybookJobWithBLOBs job = new InfoClusterPlaybookJobWithBLOBs();
        job.setTransactionId(transactionId);
        job.setJobStatus(status);
        int updateNo = infoClusterPlaybookJobMapper.updateByPrimaryKeySelective(job);
        getLogger().info("updatePlaybookJobStatus 执行参数 {} {} ，结果 {}",
                transactionId, status, updateNo == 1);
        ResultMsg msg = new ResultMsg();
        msg.setResult(updateNo == 1);
        return msg;
    }

    public ResultMsg checkCustomScriptUri(String customScriptUri) {
        String domainName = "";
        if(!sdpwgetpath.contains("https://")) {
            domainName = "https://" + sdpwgetpath;
        } else {
            domainName = sdpwgetpath;
        }
        boolean data = customScriptUri.contains(domainName);
        ResultMsg msg = new ResultMsg();
        msg.setResult(true);
        msg.setData(data);
        msg.setErrorMsg(data ? "" : "请检查脚本位置数据");
        return msg;
    }

    @Override
    public ResultMsg completePlayBookJob(String transactionId, Integer status) {
        ResultMsg msg=new ResultMsg();
        try {
            InfoClusterPlaybookJobWithBLOBs playjob =
                    infoClusterPlaybookJobMapper.selectByPrimaryKey(transactionId);
            playjob.setEndTime(new Date());
            playjob.setJobStatus(status);
            infoClusterPlaybookJobMapper.updateByPrimaryKeySelective(playjob);
            msg.setResult(true);
        }catch (Exception e){
            msg.setResult(false);
            msg.setErrorMsg(e.getMessage());
            getLogger().error("completejob,",e);
        }
        return msg;
    }

    /**
     * 清理集群ambari host历史数据
     * @param clusterId
     * @param startDate
     * @return
     */
    public ResultMsg cleanAmbariHistory(String clusterId,String startDate) {
        ResultMsg msg=new ResultMsg();

        ConfCluster cluster = confClusterMapper.selectByPrimaryKey(clusterId);
        if (Objects.isNull(cluster)) {
            msg.setResult(false);
            msg.setErrorMsg("没找到集群：clusterId = " + clusterId);
            return msg;
        }

        // 状态为2时是运行中集群，其它状态均为不正常状态
        if (!Objects.equals(cluster.getState(), ConfCluster.CREATED)) {
            msg.setResult(false);
            msg.setErrorMsg("集群状态不能创建脚本执行任务，当前集群状态： " + cluster.getState());
            return msg;
        }
        //HBase不执行脚本
        if ("hbase".equalsIgnoreCase(cluster.getScene())){
            msg.setResult(false);
            msg.setErrorMsg("HBase不执行脚本");
            return msg;
        }

        List<ConfClusterVm> vms=clusterVmMapper.getVmConfsByRole("ambari",clusterId);

        if (CollUtil.isEmpty(vms)){
            msg.setResult(false);
            msg.setErrorMsg("未找到imgId");
            return msg;
        }

        String imgId=vms.get(0).getImgId();
        if (StringUtils.isEmpty(imgId)){
            msg.setResult(false);
            msg.setErrorMsg("未发现imgId");
            return msg;
        }

        List<BaseImageScripts> baseImageScripts=
                baseImageScriptsMapper.getAllByImgIdAndRunTiming(imgId,BaseImageScripts.RUNTIMING_CLEAN_AMBARI_HISTORY);
        if (CollUtil.isEmpty(baseImageScripts)){
            msg.setResult(false);
            msg.setErrorMsg("未发现imgid相关的runtiming，"+clusterId+"："+BaseImageScripts.RUNTIMING_CLEAN_AMBARI_HISTORY);
            return msg;
        }

        String scriptfileUri=baseImageScripts.get(0).getScriptFileUri().replace("{wgetpath}",sdpwgetpath);

        getLogger().info("clean_ambari_history<scriptfileurl>:"+scriptfileUri);
        ConfClusterScript confClusterScript=new ConfClusterScript();
        confClusterScript.setClusterId(clusterId);
        confClusterScript.setScriptPath(scriptfileUri);
        confClusterScript.setScriptName(BaseImageScripts.RUNTIMING_CLEAN_AMBARI_HISTORY);
        confClusterScript.setScriptParam(cluster.getClusterName().replaceAll("-", "")+" "+startDate);
        confClusterScript.setRunTiming(BaseImageScripts.RUNTIMING_CLEAN_AMBARI_HISTORY);
        confClusterScript.setConfScriptId(UUID.randomUUID().toString());

        List<InfoClusterVm> infoClusterVms=
                infoClusterVmMapper.selectByClusterIdAndRoleAndState(clusterId,"ambari",InfoClusterVm.VM_RUNNING);

        if (CollUtil.isEmpty(infoClusterVms)){
            msg.setResult(false);
            msg.setErrorMsg("找不到可用ambari节点");
            return msg;
        }

        String nodelist=infoClusterVms.get(0).getInternalip();

        return saveUserScriptPlayBookJob(confClusterScript,UUID.randomUUID().toString(),
                nodelist,cluster.getRegion());
    }

    @Override
    public ResultMsg shutdownAmbariAgent(String clusterId, List<String> hosts) {
        ResultMsg msg=new ResultMsg();

        ConfCluster cluster = confClusterMapper.selectByPrimaryKey(clusterId);
        if (Objects.isNull(cluster)) {
            msg.setResult(false);
            msg.setErrorMsg("没找到集群：clusterId = " + clusterId);
            return msg;
        }

        // 状态为2时是运行中集群，其它状态均为不正常状态
        if (!Objects.equals(cluster.getState(), ConfCluster.CREATED)) {
            msg.setResult(false);
            msg.setErrorMsg("集群状态不能创建脚本执行任务，当前集群状态： " + cluster.getState());
            return msg;
        }
        List<ConfClusterVm> vms=clusterVmMapper.getVmConfsByRole("ambari",clusterId);

        if (CollUtil.isEmpty(vms)){
            msg.setResult(false);
            msg.setErrorMsg("未找到imgId");
            return msg;
        }

        String imgId=vms.get(0).getImgId();
        if (StringUtils.isEmpty(imgId)){
            msg.setResult(false);
            msg.setErrorMsg("未发现imgId");
            return msg;
        }
        List<BaseImageScripts> baseImageScripts=
                baseImageScriptsMapper.getAllByImgIdAndRunTiming(imgId,BaseImageScripts.RUNTIMING_SHUTDOWN_AMBAERI_AGENT);
        String scriptfileUri=baseImageScripts.get(0).getScriptFileUri().replace("{wgetpath}",sdpwgetpath);
        ConfClusterScript confClusterScript=new ConfClusterScript();
        confClusterScript.setClusterId(clusterId);
        confClusterScript.setScriptPath(scriptfileUri);
        confClusterScript.setScriptName("run_shutdown_ambari_agent");
        confClusterScript.setScriptParam("");
        confClusterScript.setRunTiming("run_shutdown_ambari_agent");
        confClusterScript.setConfScriptId(UUID.randomUUID().toString());
        String hostsStr = StrUtil.join(",", hosts); // Arrays.toString(hosts.toArray());
        return saveUserScriptPlayBookJob(confClusterScript,UUID.randomUUID().toString(),hostsStr
                ,cluster.getRegion());

    }
    public ResultMsg collectClusterInfo(String clusterId,String filePath,List<String> hosts){
        ResultMsg msg=new ResultMsg();

        ConfCluster cluster = confClusterMapper.selectByPrimaryKey(clusterId);
        if (Objects.isNull(cluster)) {
            msg.setResult(false);
            msg.setErrorMsg("没找到集群：clusterId = " + clusterId);
            return msg;
        }

        // 状态为2时是运行中集群，其它状态均为不正常状态
        if (!Objects.equals(cluster.getState(), ConfCluster.CREATED)) {
            msg.setResult(false);
            msg.setErrorMsg("集群状态不能创建脚本执行任务，当前集群状态： " + cluster.getState());
            return msg;
        }
        List<ConfClusterVm> vms=clusterVmMapper.getVmConfsByRole("ambari",clusterId);

        if (CollUtil.isEmpty(vms)){
            msg.setResult(false);
            msg.setErrorMsg("未找到imgId");
            return msg;
        }

        String imgId=vms.get(0).getImgId();
        if (StringUtils.isEmpty(imgId)){
            msg.setResult(false);
            msg.setErrorMsg("未发现imgId");
            return msg;
        }
        List<BaseImageScripts> baseImageScripts=
                baseImageScriptsMapper.getAllByImgIdAndRunTiming(imgId, RUNTIMING_COLLECT_CLUSTER_INFO);
        String scriptfileUri=baseImageScripts.get(0).getScriptFileUri().replace("{wgetpath}",sdpwgetpath);
        ConfClusterScript confClusterScript=new ConfClusterScript();
        confClusterScript.setClusterId(clusterId);
        confClusterScript.setScriptPath(scriptfileUri);
        confClusterScript.setScriptName(RUNTIMING_COLLECT_CLUSTER_INFO);
        confClusterScript.setScriptParam(String.join(" ",cluster.getLogMITenantId(),cluster.getLogMIClientId(),filePath));
        confClusterScript.setRunTiming(RUNTIMING_COLLECT_CLUSTER_INFO);
        confClusterScript.setConfScriptId(UUID.randomUUID().toString());
        String hostsStr = StrUtil.join(",", hosts); // Arrays.toString(hosts.toArray());
        String uuid = UUID.randomUUID().toString();
        return saveUserScriptPlayBookJob(confClusterScript,uuid,hostsStr
                ,cluster.getRegion());

    }
}
