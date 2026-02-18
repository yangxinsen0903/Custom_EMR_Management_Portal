package com.sunbox.sdpcompose.service.ambari;

import com.sunbox.domain.enums.SceneType;
import com.sunbox.sdpcompose.service.ambari.blueprint.ClusterHostGroup;
import com.sunbox.sdpcompose.service.ambari.blueprint.DBConnectInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 创建Blueprint的命令，主要参数：
 * <ol>
 *     <li>Blueprint名称</li>
 *     <li>集群名称</li>
 *     <li>Stack名称</li>
 *     <li>Stck版本</li>
 *     <li>是否HA（高可用）</li>
 *     <li>需要安装布署的大数据服务，如：HDFS，YARN，HIVE...</li>
 *     <li>主机信息：各个主机组的主机信息</li>
 *     <li>数据库配置信息：Hive或其它</li>
 *     <li>用户自定义配置信息</li>
 * </ol>
 * @author: wangda
 * @date: 2022/12/5
 */
public class CreateClusterCmd {
    /** 集群ID */
    String clusterId;
    /** 集群名称 */
    String clusterName;

    /** blueprint名称 */
    String blueprintName;

    /** Stack名 */
    String stackName;

    /** Stack版本 */
    String stackVersion;

    /** 是否是高可用。 true:是   false: 不是 */
    boolean isHa = false;

    /** 场景类型, 对于HBase要做特殊处理 */
    SceneType scene = SceneType.DEFAULT;

    /** 安装的大数据服务列表 */
    List<String> services = new ArrayList<>();

    /** 集群的实例组，根据实例组生成布局 */
    List<ClusterHostGroup> hostGroups = new ArrayList<>();

//    /** Ambari主机 */
//    List<HostInstance> ambariHosts = new ArrayList<>();
//
//    /** Master主机实例 */
//    List<HostInstance> masterHosts = new ArrayList<>();
//
//    /** Core主机实例 */
//    List<HostInstance> coreHosts = new ArrayList<>();
//
//    /** Task主机组的实例 */
//    List<HostInstance> taskHosts = new ArrayList<>();

    /** 数据库相关的配置 */
    List<DBConnectInfo> dbConfigs = new ArrayList<>();

    /**
     * 用户输入的集群默认自定义配置
     */
    Map<String, Map<String, Object>> configurations;


    /** 修改ABFS在Core-Site中的配置 */
    String miTenantId;

    /** 修改ABFS在core-site中的配置 */
    String miClientId;

    /** 是否启用Ganglia, 默认不启用 */
    int enableGanglia;

    /**
     * 需要访问的Ambari服务器的信息。
     */
    AmbariInfo ambarInfo;

    public void addClusterHostGroup(ClusterHostGroup group) {
        this.hostGroups.add(group);
    }

    public List<ClusterHostGroup> getHostGroups() {
        return hostGroups;
    }

    public void setHostGroups(List<ClusterHostGroup> hostGroups) {
        this.hostGroups = hostGroups;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    /**
     * 增加一个数据库配置信息
     * @param dbConnectInfo
     */
    public void addDBConnectInfo(DBConnectInfo dbConnectInfo) {
        this.dbConfigs.add(dbConnectInfo);
    }

    public String getBlueprintName() {
        return blueprintName;
    }

    public CreateClusterCmd setBlueprintName(String blueprintName) {
        this.blueprintName = blueprintName;
        return this;
    }

    public String getStackName() {
        return stackName;
    }

    public CreateClusterCmd setStackName(String stackName) {
        this.stackName = stackName;
        return this;
    }

    public String getStackVersion() {
        return stackVersion;
    }

    public CreateClusterCmd setStackVersion(String stackVersion) {
        this.stackVersion = stackVersion;
        return this;
    }

    public List<String> getServices() {
        return services;
    }

    public CreateClusterCmd setServices(List<String> services) {
        this.services = services;
        return this;
    }

    public boolean isHa() {
        return isHa;
    }

    public CreateClusterCmd setHa(boolean ha) {
        isHa = ha;
        return this;
    }


    public List<DBConnectInfo> getDbConfigs() {
        return dbConfigs;
    }

    public CreateClusterCmd setDbConfigs(List<DBConnectInfo> dbConfigs) {
        this.dbConfigs = dbConfigs;
        return this;
    }

    public String getClusterName() {
        return clusterName;
    }

    public CreateClusterCmd setClusterName(String clusterName) {
        this.clusterName = clusterName;
        return this;
    }

    public Map<String, Map<String, Object>> getConfigurations() {
        return configurations;
    }

    public CreateClusterCmd setConfigurations(Map<String, Map<String, Object>> configurations) {
        this.configurations = configurations;
        return this;
    }

    public AmbariInfo getAmbarInfo() {
        return ambarInfo;
    }

    public CreateClusterCmd setAmbarInfo(AmbariInfo ambarInfo) {
        this.ambarInfo = ambarInfo;
        return this;
    }

    public SceneType getScene() {
        return scene;
    }

    public void setScene(SceneType scene) {
        this.scene = scene;
    }

    public String getMiTenantId() {
        return miTenantId;
    }

    public void setMiTenantId(String miTenantId) {
        this.miTenantId = miTenantId;
    }

    public String getMiClientId() {
        return miClientId;
    }

    public void setMiClientId(String miClientId) {
        this.miClientId = miClientId;
    }

    public int getEnableGanglia() {
        return enableGanglia;
    }

    public void setEnableGanglia(int enableGanglia) {
        this.enableGanglia = enableGanglia;
    }
}
