package com.sunbox.service;

import com.sunbox.domain.InfoClusterAmbariHostDelete;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.ambari.AmbariInfo;
import sunbox.sdp.ambari.client.ApiClient;

import java.util.List;

public interface INeoAmbariService {

    /**
     * 从Ambari-server中删除指定的hostNames
     *
     * @param clusterId
     * @param hostNames
     * @return
     */
    ResultMsg deleteAmbariHosts(String clusterId, List<String> hostNames);

    /**
     * 获取Ambari信息
     * @param clusterId 集群ID
     * @return
     */
    AmbariInfo getAmbariInfo(String clusterId);


    /**
     * 根据SDP的集群名称，获取ambari中的集群名称
     *
     * @param sdpClusterName SDP中的集群名称
     * @return
     */
    String getAmbariClusterName(String sdpClusterName);

    /**
     *  查询ambari上的所有Host
     *
     * @param clusterId
     * @return
     */
    List<String> queryAllHosts(String clusterId);

    /**
     *  保存Ambari删除Host失败记录，
     *  后续由守护任务负责清理
     * @param clusterId
     * @param planId
     * @param hostNames
     * @return
     */
    ResultMsg saveAmbariHostDelete(String clusterId,String planId,List<String> hostNames);

    /**
     * 从 ambarihostdelete表 获取需要清理的host
     *
     * @return
     */
    List<InfoClusterAmbariHostDelete> getNeedClearAmbariHost();

    /**
     * NodeManager 逐个 Decommission
     *
     * @param clusterId
     * @param hostNames
     * @return
     */
    ResultMsg nodeManagerDecommissionOneByOne(String clusterId,List<String> hostNames);


    /**
     *  批量删除AmbariHost
     *
     * @param deleteList
     * @return
     */
    ResultMsg deleteAmbariHosts(List<InfoClusterAmbariHostDelete> deleteList);

}
