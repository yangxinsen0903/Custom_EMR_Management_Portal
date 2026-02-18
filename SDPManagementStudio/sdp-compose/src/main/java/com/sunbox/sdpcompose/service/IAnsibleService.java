package com.sunbox.sdpcompose.service;

import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.InfoClusterVm;
import com.sunbox.domain.ResultMsg;
import org.apache.ibatis.annotations.Results;

import java.util.List;

/**
 * @author : [niyang]
 * @className : AnsibleService
 * @description : [描述说明该类的功能]
 * @createTime : [2022/11/30 5:05 PM]
 */
public interface IAnsibleService {

    /**
     * ansible 服务消息订阅
     * @param message
     * @return
     */
    ResultMsg playbookApiMessage(String message);

    /**
     * 安装ambari
     * @param messageparam
     * @return
     */
    ResultMsg installAmbari(String messageparam);


    /**
     * 安装sdp Agent
     * @param messageparam
     * @return
     */
    ResultMsg installAgent(String messageparam);

    /**
     * 初始化脚本执行
     * @param message
     * @return
     */
    ResultMsg initScript(String message);

    /**
     * 执行集群启动前脚本
     * @param message
     * @return
     */
    ResultMsg beforeClusterStartScript(String message);

    /**
     * 执行集群启动后脚本
     * @param message
     * @return
     */
    ResultMsg afterClusterCompletedScript(String message);

    /**
     *  执行用户自定义脚本
     * @param message
     * @return
     */
    ResultMsg runUserCusterScript(String message);

    /**
     * 查询脚本执行结果
     * @param message
     * @return
     */
    ResultMsg queryPlayJobStatus(String message);


    /**
     * Ambari节点安装tezUI
     * @param message
     * @return
     */
    ResultMsg installTezUI(String message);

    //region 调整 HDFS 数据平衡可用带宽
    /**
     * 调大 HDFS 数据平衡可用带宽 1G
     *
     * @param message
     * @return
     */
    ResultMsg scaleUpHdfsBalanceBandWidth(String message);

    /**
     * 还原 HDFS 数据平衡可用带宽 20M
     *
     * @param message
     * @return
     */
    ResultMsg scaleDownHdfsBalanceBandWidth(String message);

    //endregion

    /**
     * 执行core节点磁盘扩容任务脚本
     * @param message
     * @return
     */
    ResultMsg scaleOutDisk(String message);


    /**
     * 查询core节点磁盘扩容任务执行结果
     * @param message
     * @return
     */
    ResultMsg queryScaleOutDiskProcess(String message);

    /**
     * 获取重试IpList
     *
     * @param param 消息
     * @return
     */
    String getRetryIpList(JSONObject param);
}
