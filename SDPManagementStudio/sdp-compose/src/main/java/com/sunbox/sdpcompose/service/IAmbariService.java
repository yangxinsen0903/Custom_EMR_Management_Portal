package com.sunbox.sdpcompose.service;

import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpcompose.enums.AmbariHostState;
import com.sunbox.sdpcompose.service.ambari.*;
import com.sunbox.sdpcompose.service.ambari.blueprint.Blueprint;
import com.sunbox.sdpcompose.service.ambari.blueprint.BlueprintConfiguration;
import com.sunbox.sdpcompose.service.ambari.blueprint.CreateBlueprintCmd;
import com.sunbox.sdpcompose.service.ambari.blueprint.HostInstance;
import com.sunbox.sdpcompose.service.ambari.clustertemplate.ClusterTemplate;
import com.sunbox.sdpcompose.service.ambari.clustertemplate.CreateClusterTemplateCmd;
import com.sunbox.sdpcompose.service.ambari.enums.ConfigItemType;
import com.sunbox.sdpcompose.service.ambari.enums.HostGroupRole;
import sunbox.sdp.ambari.client.model.customaction.QueryComponentInHostsResponse;

import java.util.List;
import java.util.Map;

/**
 * Ambari相关功能服务
 * author: wangda
 * date: 2022/12/4
 */
public interface IAmbariService {

    /**
     * 创建生成一个Blueprint
     *
     * @param cmd 创建命令
     * @return 蓝图
     */
    Blueprint createBlueprint(CreateBlueprintCmd cmd);

    /**
     * 创建生成一个Blueprint
     *
     * @param cmd            创建命令
     * @param originBlueprint 覆盖的配置
     * @return 蓝图
     */
    Blueprint createBlueprint(CreateBlueprintCmd cmd, Blueprint originBlueprint);

    List<BlueprintConfiguration> generateCustomConfig(HostInstance hostInstance, HostGroupRole role);

    /**
     * 创建一个创建集群模板
     *
     * @param cmd 创建一个“创建集群模板”
     * @return 蓝图
     */
    ClusterTemplate createCluterCreateTemplate(CreateClusterTemplateCmd cmd);

    /**
     * 创建集群
     *
     * @param cmd 创建集群的请求参数
     * @return 创建集群响应对象
     */
    InProgressResult createCluster(CreateClusterCmd cmd);

    InProgressResult duplicateCluster(DuplicateClusterCmd cmd);

    /**
     * 生成一个Task实例组的多磁盘配置
     * @param hosts 主机列表，会从里面取一个主机的磁盘数量
     * @param ha 是否高可用
     * @return 多磁盘配置
     */
    List<Map<String, BlueprintConfiguration>> generateOneTaskGroupMultiDiskConfig(String stackVersion, List<HostInstance> hosts, ConfigItemType ha);

    /**
     * 查询一个任务的执行进展, 由于要查询每个任务的执行情况, 所以此接口查询时间比较长
     *
     * @param cmd 查询请求
     * @return 进展详情
     */
    QueryProgressResult queryCreateClusterProgress(QueryProgressCmd cmd);


    QueryProgressResult queryCreateClusterProgressWithAllTask(QueryProgressCmd cmd);

    /**
     * 启动集群里的服务
     *
     * @param clusterName 集群名称
     * @param services    要启动的服务列表
     */
    void startClusterService(AmbariInfo ambariInfo, String clusterName, List<String> services);

    InProgressResult startAllClusterServices(AmbariInfo ambariInfo,
                                             String clusterName, String clusterId);

    InProgressResult startAllClusterServicesHA(AmbariInfo ambariInfo, String clusterName, String clusterId);


    /**
     * 设置一个集群里的某些服务可以自动启动<br/>
     * 此接口可以重复执行
     *
     * @param ambariInfo  Ambari信息
     * @param clusterName 集群名称
     * @param services    需要设置自动启动的服务列表
     */
    void enableClusterAutoStart(AmbariInfo ambariInfo, String clusterName, List<String> services);

    /**
     * 取消一个集群里的某些服务可以自动启动<br/>
     *
     * @param ambariInfo
     * @param clusterName
     * @param services
     */
    void disableClusterAutoStart(AmbariInfo ambariInfo,String clusterName,List<String> services);


    /**
     * 向一个集群增加新主机。<br/>
     * 在抛出异常时，说明增加主机失败。<br/>
     * <b>此接口可以重复调用</b>
     *
     * @param clusterName 集群名
     * @param hosts       主机名列表
     * @param ambariInfo  ambari信息
     */
    void addHosts(AmbariInfo ambariInfo, String clusterName, List<String> hosts);


    /**
     * 向一个集群增加新主机。<br/>
     * 在抛出异常时，说明增加主机失败。<br/>
     * <b>此接口可以重复调用</b>
     *
     * @param ambariInfo  ambari信息
     * @param clusterName 集群名称
     * @param hosts       主机列表
     * @param components  组件列表
     */
    void configHostComponent(AmbariInfo ambariInfo, String clusterName, List<String> hosts, List<String> components);

    /**
     * 集群新增实例主机安装大数据应用组件
     *
     * @param ambariInfo  ambariinfo
     * @param clusterName 集群名称（ambari）
     * @param hosts       机器名称
     * @return
     */
    InProgressResult installHostComponent(AmbariInfo ambariInfo, String clusterName, List<String> hosts);

    /**
     * 启动集群新增机器的大数据组件
     *
     * @param ambariInfo  ambari信息
     * @param clusterName 集群名称（ambari）
     * @param hosts       机器列表
     * @param components  组件列表
     * @return
     */
    InProgressResult startHostComponent(AmbariInfo ambariInfo, String clusterName, List<String> hosts,
                                        List<String> components);

    /**
     * core 节点数据平衡
     *
     * @param ambariInfo
     */
    void rebalanceDataForCore(AmbariInfo ambariInfo, String clusterName);

    /**
     * core 节点数据平衡By ClusterId
     *
     * @param ambariInfo
     * @param clusterName
     * @param clusterId
     */
    void rebalanceDataForCore(AmbariInfo ambariInfo,String clusterName,String clusterId);

    /**
     * 查询一个组件所在的主机
     *
     * @param clusterId 集群Id
     * @param component   组件名
     * @return
     */
    QueryComponentInHostsResponse getComponentInHosts(String clusterId, String component);

    /**
     * Decommission一组主机上的所有组件<br/>
     * <b>此接口可以重复调用执行，每次调用返回来的进展requestId会发生变化</b>
     *
     * @param clusterName   集群
     * @param hosts         主机列表
     * @param componentName 需要decommission的组件名称, 目前只支持 DATANODE和 NODEMANAGER
     * @return Decommission进展
     */
    InProgressResult decommissionComponent(AmbariInfo ambariInfo, String clusterName, List<String> hosts, String componentName);

    /**
     * Decommission一组主机上的所有组件<br/>
     * <b>此接口可以重复调用执行，每次调用返回来的进展requestId会发生变化</b>
     *
     * @param clusterName   集群
     * @param hostName         主机名称
     * @param componentName 需要decommission的组件名称, 目前只支持 DATANODE和 NODEMANAGER
     * @return Decommission进展
     */
    InProgressResult decommissionComponent(AmbariInfo ambariInfo, String clusterName, String hostName, String componentName);

    /**
     * 停止一组主机上所有的组件<br/>
     * <b>此接口可以重复调用执行，每次调用返回来的进展requestId会发生变化</b>
     *
     * @param clusterName 集群名称
     * @param hosts       主机列表
     * @return 停止组件的进展
     */
    InProgressResult stopHostAllComponents(AmbariInfo ambariInfo, String clusterName, List<String> hosts);

    /**
     * 删除一组主机
     *
     * @param clusterName 集群名称
     * @param hosts       主机列表
     * @return 删除主机的进展
     */
    DeleteHostResult deleteHosts(AmbariInfo ambariInfo, String clusterName, List<String> hosts);

    /**
     * 根据hostname查询实例安装的组件
     *
     * @param ambariInfo  ambariInfo
     * @param clusterName 集群名称
     * @param hostname    机器名称
     * @return
     */
    List<String> getComponentsByHost(AmbariInfo ambariInfo, String clusterName, String hostname);

    /**
     * 获取active的组件所在机器的hostname
     *
     * @param clusterId
     * @param componentname
     * @return
     */
    ResultMsg getActiveComponentHostName(String clusterId, String componentname);

    /**
     * 重启一个服务
     *
     * @param ambariInfo  Ambari连接信息
     * @param clusterName 集群名
     * @param serviceName 服务名
     * @return
     */
    InProgressResult restartService(AmbariInfo ambariInfo, String clusterName, String serviceName);

    /**
     * 重启个实例组中的某个服务
     * @param ambariInfo
     * @param clusterId 集群Id
     * @param serviceName 服务名
     * @param groupName 实例组名
     * @return
     */
    InProgressResult restartHostsComponents(AmbariInfo ambariInfo, String clusterId, String serviceName, String groupName);

    /**
     * 获取有Container运行的VM节点
     * @param clusterId
     * @return
     */
    ResultMsg getNodesWithContainerRunning(String clusterId);

    /**
     * 更新集群的配置. 在Ambari中，修改默认配置和修改配置组分两个接口实现。在此接口中将两种类型的配置接口集成在一起。
     * groupConfigs是配置组的配置更新。clusterDefaultConfigs是集群默认配置的更新，参数传了哪个，就更新哪个配置。
     * @param ambariInfo Ambari地址信息
     * @param clusterId 集群名
     * @param groupConfigs 如果不传,则更新集群默认配置
     * @param clusterDefaultConfigs 需要更新的集群默认配置
     */
    void updateClusterConfig(AmbariInfo ambariInfo, String clusterId, List<InstanceGroupConfiguration> groupConfigs,
                                    List<BlueprintConfiguration> clusterDefaultConfigs);

    /**
     * 返回集群所有机器
     * @param ambariInfo
     * @param clusterName
     * @return
     */
    List<String> queryAllHosts(AmbariInfo ambariInfo,String clusterName);

    /**
     * 查询某个状态的主机，
     * @param ambariInfo Ambari地址信息
     * @param clusterName 集群名称
     * @param state 主机状态， 为空时返回全部主机
     * @return 主机列表
     */
    List<String> queryHosts(AmbariInfo ambariInfo, String clusterName, AmbariHostState state);

    /**
     * 从Ambari验证一组机器名是否都存在<br/>
     * 如果调用Ambari出错，返回传入的所有被验证的主机列表
     * @param ambariInfo Ambari地址信息
     * @param clusterName 集群名
     * @param hosts 被验证的主机名列表
     * @return 验证通过的主机名列表
     */
    List<String> verifyHostsFromAmbari(AmbariInfo ambariInfo,String clusterName, List<String> hosts);

    /**
     *  查询 DataNode 节点decommionsion进度
     * @param clusterid
     * @param hostNames
     * @return
     */
    ResultMsg queryDataNodeDecommionsionProcess(String clusterid,List<String> hostNames);

    /**
     * 查询nodeManager 节点decommission 进度
     * @param clsusterid
     * @param hostNames
     * @return
     */
    ResultMsg queryNodeManagerDecommionsionProcess(String clsusterid,List<String> hostNames);


    /**
     * 调用jmx查询数据检查
     * @param clusterId
     * @return
     */
    ResultMsg hdfsCheck(String clusterId);

    /**
     * 获取集群ActiveResourceManager的HostName
     *
     * @param clusterId 集群ID
     * @return
     */
    ResultMsg getActiveResourceManager(String clusterId);


    /**
     * 获取集群Active NameNode的HostName
     *
     * @param clusterId 集群ID
     * @return
     */
    ResultMsg getActiveNameNode(String clusterId);

    /**
     * 从Yarn中获取集群的运行中的主机
     * @param clusterId 集群ID
     * @return
     */
    List<String> getRunningHostsFromYarn(String clusterId);

    /**
     *  查询NameNode JMX接口查询指定VM且DataNode状态为InService的VM列表
     *
     * @param clusterId
     * @param hostNames
     * @return
     */
    List<String> getHostsWithInServiceDataNode(String clusterId,List<String> hostNames);

    /**
     * 查询ResourceManager WebService Restful Api
     * 获取Running状态的HostName
     *
     * @param clusterId
     * @param hostNames
     * @return
     */
    List<String> getHostsWithRunningNodeManager(String clusterId,List<String> hostNames);


    Blueprint getBlueprint(AmbariInfo ambariInfo,String clusterName);
}
