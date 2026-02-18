package com.sunbox.sdpadmin.model.shein.request;

import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.ConfClusterScript;

import java.util.List;

public class SheinRequestModel {

    /**
     * 自动关闭策略
     */
    private String autoCloseStrategy;

    /**
     * EMR弹性伸缩角色
     */
    private String autoScalingRole;

    /**
     * 可用区
     */
    private String az;

    private String vNet;

    private String logMI;

    private String vmMITenantId;

    private String vmMIClientId;

    private String vmMI;

    /**
     * 日志桶托管MI的TenantId
     */
    private String logMITenantId;

    /**
     * 日志桶托管MI的ClientId
     */
    private String logMIClientId;

    /**
     * 用户自定义脚本
     */
    private List<ConfClusterScript> confClusterScript;

    private DbCfgs ambariDbCfgs;

    private DbCfgs hiveMetadataDbCfgs;

    /**
     * 集群安装的大数据组件，Hadoop/Hive/Sqoop/Tez/Ganglia/HCatalog/Pig
     */
    private List<String> clusterApps;

    /**
     * 集群配置，各大数据组件的配置信息
     */
    private List<Empty> clusterCfgs;

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * 集群版本
     */
    private String clusterReleaseVer;

    private String createUser;

    /**
     * 数据中心
     */
    private String dc;

    /**
     * EMR 集群托管伸缩策略
     */
    private Emr emrManagedScalingPolicy;

    private String group;

    private Integer instanceCollectionType;

    /**
     * EMR 集群实例队列
     */
    private List<InstanceFleetNewConfigElement> instanceFleetNewConfigs;

    /**
     * EMR 集群实例组
     */
    private List<InstanceGroupNewConfigElement> instanceGroupNewConfigs;

    /**
     * EMR 密钥对标识
     */
    private String instanceKeyPair;

    /**
     * 仅允许内网IP访问
     */
    private Boolean internalIpOnly;

    /**
     * 主安全组，
     */
    private String masterSecurityGroup;

    /**
     * 系统盘大小（GB），EMR 10GB~100GB
     */
    private Integer rootVolSize;

    /**
     * 系统盘存储介质类型
     */
    private String rootVolType;

    /**
     * EMR 日志桶
     */
    private String s3LogLocation;

    /**
     * 从安全组
     */
    private String slaveSecurityGroup;

    private Integer startHa;

    /**
     * 子网ID
     */
    private String subnet;

    private String scene;

    /**
     * 标签
     */
    private JSONObject tagMap;

    /**
     * 是否打开关闭保护
     */
    private String deleteProtected;

    /**
     * 是否内嵌数据库
     */
    private Integer isEmbedAmbariDb;

    /**
     * 是否启用Ganglia
     */
    private Integer enableGanglia;

    /**
     * null or 1 直接创建（DIRECTLY）
     * 2 先创建小集群然后创建扩容任务（SPLIT）
     */
    private Integer creationMode;

    /**
     * 是否加入直接销毁白名单,是空或1,否0
     */
    private Integer inWhiteList;

    public String getvNet() {
        return vNet;
    }

    public void setvNet(String vNet) {
        this.vNet = vNet;
    }

    public Integer getCreationMode() {
        return creationMode;
    }

    public void setCreationMode(Integer creationMode) {
        this.creationMode = creationMode;
    }

    public Integer getIsEmbedAmbariDb() {
        return isEmbedAmbariDb;
    }

    public void setIsEmbedAmbariDb(Integer isEmbedAmbariDb) {
        this.isEmbedAmbariDb = isEmbedAmbariDb;
    }

    public String getLogMITenantId() {
        return logMITenantId;
    }

    public void setLogMITenantId(String logMITenantId) {
        this.logMITenantId = logMITenantId;
    }

    public String getLogMIClientId() {
        return logMIClientId;
    }

    public void setLogMIClientId(String logMIClientId) {
        this.logMIClientId = logMIClientId;
    }

    public DbCfgs getAmbariDbCfgs() {
        return ambariDbCfgs;
    }

    public void setAmbariDbCfgs(DbCfgs ambariDbCfgs) {
        this.ambariDbCfgs = ambariDbCfgs;
    }

    public DbCfgs getHiveMetadataDbCfgs() {
        return hiveMetadataDbCfgs;
    }

    public void setHiveMetadataDbCfgs(DbCfgs hiveMetadataDbCfgs) {
        this.hiveMetadataDbCfgs = hiveMetadataDbCfgs;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getLogMI() {
        return logMI;
    }

    public void setLogMI(String logMI) {
        this.logMI = logMI;
    }

    public String getVmMITenantId() {
        return vmMITenantId;
    }

    public void setVmMITenantId(String vmMITenantId) {
        this.vmMITenantId = vmMITenantId;
    }

    public String getRootVolType() {
        return rootVolType;
    }

    public void setRootVolType(String rootVolType) {
        this.rootVolType = rootVolType;
    }

    public String getVmMIClientId() {
        return vmMIClientId;
    }

    public void setVmMIClientId(String vmMIClientId) {
        this.vmMIClientId = vmMIClientId;
    }

    public String getVmMI() {
        return vmMI;
    }

    public void setVmMI(String vmMI) {
        this.vmMI = vmMI;
    }

    public String getAutoCloseStrategy() {
        return autoCloseStrategy;
    }

    public void setAutoCloseStrategy(String autoCloseStrategy) {
        this.autoCloseStrategy = autoCloseStrategy;
    }

    public String getAutoScalingRole() {
        return autoScalingRole;
    }

    public void setAutoScalingRole(String autoScalingRole) {
        this.autoScalingRole = autoScalingRole;
    }

    public String getAz() {
        return az;
    }

    public void setAz(String az) {
        this.az = az;
    }

    public List<ConfClusterScript> getConfClusterScript() {
        return confClusterScript;
    }

    public void setConfClusterScript(List<ConfClusterScript> confClusterScript) {
        this.confClusterScript = confClusterScript;
    }

    public String getDeleteProtected() {
        return deleteProtected;
    }

    public void setDeleteProtected(String deleteProtected) {
        this.deleteProtected = deleteProtected;
    }

    public List<String> getClusterApps() {
        return clusterApps;
    }

    public void setClusterApps(List<String> clusterApps) {
        this.clusterApps = clusterApps;
    }

    public List<Empty> getClusterCfgs() {
        return clusterCfgs;
    }

    public void setClusterCfgs(List<Empty> clusterCfgs) {
        this.clusterCfgs = clusterCfgs;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getClusterReleaseVer() {
        return clusterReleaseVer;
    }

    public void setClusterReleaseVer(String clusterReleaseVer) {
        this.clusterReleaseVer = clusterReleaseVer;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getDc() {
        return dc;
    }

    public void setDc(String dc) {
        this.dc = dc;
    }

    public Emr getEmrManagedScalingPolicy() {
        return emrManagedScalingPolicy;
    }

    public void setEmrManagedScalingPolicy(Emr emrManagedScalingPolicy) {
        this.emrManagedScalingPolicy = emrManagedScalingPolicy;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Integer getInstanceCollectionType() {
        return instanceCollectionType;
    }

    public void setInstanceCollectionType(Integer instanceCollectionType) {
        this.instanceCollectionType = instanceCollectionType;
    }

    public List<InstanceFleetNewConfigElement> getInstanceFleetNewConfigs() {
        return instanceFleetNewConfigs;
    }

    public void setInstanceFleetNewConfigs(List<InstanceFleetNewConfigElement> instanceFleetNewConfigs) {
        this.instanceFleetNewConfigs = instanceFleetNewConfigs;
    }

    public List<InstanceGroupNewConfigElement> getInstanceGroupNewConfigs() {
        return instanceGroupNewConfigs;
    }

    public void setInstanceGroupNewConfigs(List<InstanceGroupNewConfigElement> instanceGroupNewConfigs) {
        this.instanceGroupNewConfigs = instanceGroupNewConfigs;
    }

    public String getInstanceKeyPair() {
        return instanceKeyPair;
    }

    public void setInstanceKeyPair(String instanceKeyPair) {
        this.instanceKeyPair = instanceKeyPair;
    }

    public Boolean getInternalIpOnly() {
        return internalIpOnly;
    }

    public void setInternalIpOnly(Boolean internalIpOnly) {
        this.internalIpOnly = internalIpOnly;
    }

    public String getMasterSecurityGroup() {
        return masterSecurityGroup;
    }

    public void setMasterSecurityGroup(String masterSecurityGroup) {
        this.masterSecurityGroup = masterSecurityGroup;
    }

    public Integer getRootVolSize() {
        return rootVolSize;
    }

    public void setRootVolSize(Integer rootVolSize) {
        this.rootVolSize = rootVolSize;
    }

    public String getS3LogLocation() {
        return s3LogLocation;
    }

    public void setS3LogLocation(String s3LogLocation) {
        this.s3LogLocation = s3LogLocation;
    }

    public String getSlaveSecurityGroup() {
        return slaveSecurityGroup;
    }

    public void setSlaveSecurityGroup(String slaveSecurityGroup) {
        this.slaveSecurityGroup = slaveSecurityGroup;
    }

    public Integer getStartHa() {
        return startHa;
    }

    public void setStartHa(Integer startHa) {
        this.startHa = startHa;
    }

    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }

    public JSONObject getTagMap() {
        return tagMap;
    }

    public void setTagMap(JSONObject tagMap) {
        this.tagMap = tagMap;
    }

    public Integer getEnableGanglia() {
        return enableGanglia;
    }

    public void setEnableGanglia(Integer enableGanglia) {
        this.enableGanglia = enableGanglia;
    }

    public Integer getInWhiteList() {
        return inWhiteList;
    }

    public void setInWhiteList(Integer inWhiteList) {
        this.inWhiteList = inWhiteList;
    }
}