package com.sunbox.sdpcompose.service;

import com.sunbox.domain.ConfCluster;
import com.sunbox.domain.ConfClusterScript;
import com.sunbox.domain.ResultMsg;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IPlayBookService {


    /**
     * 集群安装过程中执行用户脚本，支持全新安装和扩容操作
     * 包含初始化脚本 集群启动前脚本 集群启动后脚本
     * @param clusterId 集群ID
     * @param runTiming 运行时机
     * @param activityLogId actionid
     * @param scalingTaskId 扩容任务ID
     * @param ipList 指定IP列表执行（优先级高）
     * @return
     */
    ResultMsg executePlaybookJobYmlAndSave(String clusterId,
                                           String runTiming,
                                           String activityLogId,
                                           String scalingTaskId,
                                           String ipList,String region);

    /**
     * 执行用户自定义脚本
     */
    ResultMsg executeUserCustomScript(String activityLogId,String region);

    /**
     * hdfs数据检查
     * @param clusterId
     * @return
     */
    ResultMsg hdfsFSck(String clusterId);


    /**
     * upload multipart file to get playbook uri
     * @param multipartFile
     * @param fileName
     * @return playbook uri
     * */
    String getPlaybookUri(MultipartFile multipartFile, String fileName,String region);


    ResultMsg saveUserScriptPlayBookJob(ConfClusterScript confClusterScript,
                                        String transactionId,
                                        String nodeList,String region);

    /**
     * 上传文本内容到blob
     * @param fileName 文件名
     * @param ymlStr 文件内容
     * @return
     */
    String uploadYml2MsBlob(String fileName, String ymlStr,String region);

    /**
     * 保存playbook数据（执行安装amberi server 和agent 用
     * @param releaseVersion
     * @param runTiming
     * @param clusterId
     * @param activityLogId
     * @param nodeListStr
     * @param ganglialist
     * */
    ResultMsg savePlaybook(String releaseVersion,
                           String runTiming,
                           String clusterId,
                           String activityLogId,
                           String nodeListStr,
                           String ganglialist,String region);

    /**
     * 查询playbook job
     * @return processing=执行中 success=执行成功 fail=执行失败
     * */
    ResultMsg queryPlaybookJob(String transactionId,String subscriptionId,String keyVaultResourceName,String secretResourceId);

    /**
     * 更新playbook job status
     * @param status 0=初始化 1=执行中 2=ok 3=失败
     * */
    ResultMsg updatePlaybookJobStatus(String transactionId, Integer status);

    /**
     * 校验用户自定义脚本URI是否包含 sdp.wgetpath domain
     * @param customScriptUri 自定义脚本，例如：<br/> https://sasdpscriptstmp.blob.core.windows.net/sunbox3/shell/customshell2.sh
     * */
    ResultMsg checkCustomScriptUri(String customScriptUri);

    /**
     *  playbooke task complete
     * @param transactionId
     * @param status
     * @return
     */
    ResultMsg completePlayBookJob(String transactionId,Integer status);


    /**
     * 清理集群ambari host历史数据
     * @param clusterId
     * @param startDate
     * @return
     */
    ResultMsg cleanAmbariHistory(String clusterId,String startDate);

    /**
     * 关闭ambari agent
     * @param clusterId
     * @param hosts
     * @return
     */
    ResultMsg shutdownAmbariAgent(String clusterId,List<String> hosts);

    /**
     * 收集集群信息
     * @param clusterId
     * @param hosts
     * @return
     */
    ResultMsg collectClusterInfo(String clusterId,String filePath,List<String> hosts);
}
