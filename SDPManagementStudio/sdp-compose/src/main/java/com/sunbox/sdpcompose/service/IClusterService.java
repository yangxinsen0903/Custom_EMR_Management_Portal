package com.sunbox.sdpcompose.service;

import com.sunbox.domain.ConfCluster;
import com.sunbox.domain.InfoClusterInfoCollectLog;
import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpcompose.service.ambari.AmbariInfo;
import com.sunbox.sdpcompose.service.ambari.DuplicateClusterCmd;

import java.util.Map;


/**
 * @author : [niyang]
 * @className : ClusterService
 * @description : [描述说明该类的功能]
 * @createTime : [2022/11/30 4:48 PM]
 */
public interface IClusterService {

    /**
     * 安装SDP集群
     *
     * @param message
     * @return
     */
    ResultMsg createSDPCluster(String message);

    ResultMsg querySDPClusterInstallProcess(String message);

    /**
     * 设置集群自动启动
     *
     * @param confCluster
     * @return
     */
    ResultMsg enableClusterAutoStart(ConfCluster confCluster);

    /**
     * 启动SDP集群
     *
     * @param message
     * @return
     */
    ResultMsg startSDPClusterApps(String message);

    //region 批量设置集群组件自动启动
    /**
     * 批量设置集群组件自动启动
     * @return
     */
    void batchEnableClusterAutoStart();

    //endregion

    ResultMsg sleep(String message);

    /**
     * 获取集群对应的Ambari信息
     *
     * @param clusterId
     * @return
     */
    AmbariInfo getAmbariInfo(String clusterId);

    /**
     * 检查集群是否可用
     *
     * @param clusterId 集群id
     * @return result=true 可用； result=false 不可用
     */
    ResultMsg checkClusterAvailable(String clusterId);

    /**
     * @param clusterId
     * @return
     */
    ResultMsg getClusterBlueprint(String clusterId);

    /**
     * 重置弹性伸缩规则启用状态
     *
     * @param clusterId
     * @param is_valid
     */
    void resetScalingRule(String clusterId, int is_valid);

    // region 扩容

    /**
     * ambariserver中新增host
     *
     * @param message
     * @return
     */
    ResultMsg ambariAddHosts(String message);


    /**
     * 将主机添加到ambari配置组中
     *
     * @param message
     * @return
     */
    ResultMsg ambariAddHostsToConfigGroup(String message);


    /**
     * 配置新增的实例上需要的大数据组件
     *
     * @param meesage
     * @return
     */
    ResultMsg configAddSDP(String meesage);

    /**
     * 新增服务器安装SDP大数据组件
     *
     * @param message
     * @return
     */
    ResultMsg installAddSDP(String message);

    /**
     * 启动新增实例的组件
     *
     * @param message
     * @return
     */
    ResultMsg startAddHostComponents(String message);

    /**
     * core 节点数据平衡指令发送
     * 非core节点，不执行。
     *
     * @param message
     * @return
     */
    ResultMsg dataBalanceForCore(String message);


    //endregion


    //region 缩容

    //region 各类组件Decommission
    /**
     * core dataNode节点decommionsion
     *
     * @param message
     * @return
     */
    ResultMsg dataNodeDecommionsion(String message);

    /**
     * task nodeManger 节点的Decommionsion
     *
     * @param message
     * @return
     */
    ResultMsg nodeManagerDecommionsion(String message);

    /**
     * hbase RegionServerDecommionsion 节点的Decommionsion
     *
     * @param message
     * @return
     */
    ResultMsg hbaseRegionServerDecommionsion(String message);

    //endregion

    /**
     * 查询DecommionsionStatus
     *
     * @param message
     * @return
     */
    ResultMsg queryDecommionsionStatus(String message);

    //region 查询decommission结果
    /**
     * 查询datanodeDecommission
     * @param message
     * @return
     */
    ResultMsg queryDataNodeDecommission(String message);

    /**
     * 查询nodemanager Decommission
     * @param message
     * @return
     */
    ResultMsg queryNodeManagerDecommission(String message);

    /**
     * 查询HBase RegionServerDecommission 进度
     *
     * @param message
     * @return
     */
    ResultMsg queryHbaseRegionServerDecommission(String message);

    //ednregion

    /**
     * 关闭指定机器的组件
     *
     * @param message
     * @return
     */
    ResultMsg closeComponentByHost(String message);

    /**
     * 查询关闭组件的进度
     *
     * @param message
     * @return
     */
    ResultMsg queryCloseComponentStatus(String message);
    /**
     *关闭ambari-agent
     */
    ResultMsg shutdownAmbariAgent(String message);

    /**
     * ambari中删除hosts
     *
     * @param message
     * @return
     */
    ResultMsg deleteAmbariHosts(String message);

    /**
     * 清理ambari中的残留del失败的hosts-缩容
     *
     * @param message
     * @return
     */
    ResultMsg clearAmbariHosts(String message);


    /**
     * 清理ambari中的残留del失败的hosts-创建集群或扩容流程完成后清理VM
     *
     * @param message
     * @return
     */
    ResultMsg clearAmbariHostsForClearVM(String message);

    /**
     * core 节点检查数据块
     *
     * @param message
     * @return
     */
    ResultMsg checkDataForCore(String message);

    /**
     * 创建集群，扩缩容任务失败
     *
     * @param message
     * @return
     */
    ResultMsg collectLogs(String message);


    /**
     * 优雅缩容等待
     *
     * @param message
     * @return
     */
    ResultMsg gracefullWating(String message);
    //endregion


    //region集群操作

    /**
     * 重启大数据服务
     */
    ResultMsg restartClusterService(Map<String, Object> param);

    /**
     * 重启大数据服务
     *
     * @param message
     * @return
     */
    ResultMsg restartSDPService(String message);

    /**
     * 查询大数据服务重启进度
     *
     * @param message
     * @return
     */
    ResultMsg QuerySDPServiceRestartProcess(String message);

    ResultMsg createConfigGroup(String clusterId, String groupName, String groupId, String vmRole);

    /**
     * 删除配置组
     *
     * @param message
     * @return
     */
    ResultMsg deleteAmbariHostGroup(String message);

    /**
     * 缩容清理Ganglia数据
     *
     * @param message
     * @return
     */
    ResultMsg clearGangliaData(String message);

    /**
     * 清理VM时清理Ganglia数据
     *
     * @param message
     * @return
     */
    ResultMsg clearGangliaDataForClearVM(String message);

    // endregion

    /**
     * 更新集群配置
     */
    ResultMsg updateClusterConfig(String jsonStr);

    ResultMsg updateLocalClusterConfig(String clusterId);

    /**
     * 检查执行中状态且超时的任务
     */
    void checkTimeoutActivity();

    String getSdpClusterNameforAmbari(String clusterId);

    ResultMsg collectClusterInfo(String clusterId);
    ResultMsg collectClusterInfoList(InfoClusterInfoCollectLog infoClusterInfoCollectLog);
}
