package com.sunbox.sdpcompose.service.ambari.blueprint;

import cn.hutool.core.collection.CollectionUtil;
import com.sunbox.domain.enums.SceneType;
import com.sunbox.sdpcompose.service.ambari.enums.HostGroupRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
public class CreateBlueprintCmd {

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
//
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

    /** 修改ABFS在Core-Site中的配置 */
    String miTenantId;

    /** 修改ABFS在core-site中的配置 */
    String miClientId;

    /** 是否启用Ganglia, 默认不启用 */
    int enableGanglia;

    public String getFullName(){
        if (getStackVersion().contains(getStackName())){
            return getStackVersion();
        }
        return String.join("-",getStackName(),getStackVersion());
    }
    /**
     * 验证请求的创建Blueprint的参数是否正确
     */
    public void validate() {
        // HBASE场景下，Core节点数量必须大于等于3台
        if (Objects.equals(scene, SceneType.HBASE)) {
            List<ClusterHostGroup> coreHostGroups = getClusterHostGroupByRole(HostGroupRole.CORE);
            ClusterHostGroup coreHostGroup = null;
            if (CollectionUtil.isNotEmpty(coreHostGroups)) {
                coreHostGroup = coreHostGroups.get(0);
            }
            if (Objects.isNull(coreHostGroup) || coreHostGroup.getHosts().size() < 3) {
                throw new RuntimeException("HBASE场景下，Core节点数量必须大于等于3台");
            }
        }
    }

    public ClusterHostGroup getClusterHostGroupByGroupName(String groupName) {
        if (Objects.isNull(hostGroups)) {
            return null;
        }

        if (Objects.isNull(groupName)) {
            return null;
        }

        for (ClusterHostGroup hostGroup : hostGroups) {
            if (groupName.equalsIgnoreCase(hostGroup.getGroupName())) {
                return hostGroup;
            }
        }
        return null;
    }

    public List<ClusterHostGroup> getClusterHostGroupByRole(HostGroupRole role) {
        List<ClusterHostGroup> list = new ArrayList<>();
        if (Objects.isNull(hostGroups)) {
            return list;
        }

        if (Objects.isNull(role)) {
            return list;
        }

        for (ClusterHostGroup hostGroup : hostGroups) {
            if (Objects.equals(role, hostGroup.getRole())) {
                list.add(hostGroup);
            }
        }
        return list;
    }

    public List<ClusterHostGroup> getHostGroups() {
        return hostGroups;
    }

    public void setHostGroups(List<ClusterHostGroup> hostGroups) {
        this.hostGroups = hostGroups;
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

    public CreateBlueprintCmd setBlueprintName(String blueprintName) {
        this.blueprintName = blueprintName;
        return this;
    }

    public String getStackName() {
        return stackName;
    }

    public CreateBlueprintCmd setStackName(String stackName) {
        this.stackName = stackName;
        return this;
    }

    public String getStackVersion() {
        return stackVersion;
    }

    public CreateBlueprintCmd setStackVersion(String stackVersion) {
        this.stackVersion = stackVersion;
        return this;
    }

    public List<String> getServices() {
        return services;
    }

    public CreateBlueprintCmd setServices(List<String> services) {
        this.services = services;
        return this;
    }

    public boolean isHa() {
        return isHa;
    }

    public CreateBlueprintCmd setHa(boolean ha) {
        isHa = ha;
        return this;
    }

    public List<DBConnectInfo> getDbConfigs() {
        return dbConfigs;
    }

    public CreateBlueprintCmd setDbConfigs(List<DBConnectInfo> dbConfigs) {
        this.dbConfigs = dbConfigs;
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
