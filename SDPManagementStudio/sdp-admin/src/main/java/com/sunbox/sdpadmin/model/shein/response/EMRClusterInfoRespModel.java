package com.sunbox.sdpadmin.model.shein.response;

import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.ConfClusterScript;
import com.sunbox.sdpadmin.model.shein.request.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * EMRClusterInfoRespModel emr 集群详情 response
 *
 * */

public class EMRClusterInfoRespModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 集群id
     * */
    private String clusterId;

    /**
     * 自动关闭策略
     */
    private String autoCloseStrategy;
    /**
     * EMR弹性伸缩角色，default role is EMR_AutoScaling_DefaultRole.Permissions that the automatic
     * scaling feature.
     */
    private String autoScalingRole;
    /**
     * 可用区
     */
    private Integer az;
    /**
     * 初始化脚本列表
     */
    private List<ConfClusterScript> confClusterScript;

    /**
     * 集群安装的大数据组件，Hadoop/Hive/Sqoop/Tez/Ganglia/HCatalog/Pig
     */
    private Map<String, String> clusterApps;
    /**
     * 集群配置，各大数据组件的配置信息
     */
    private List<Empty> clusterCfgs;
    /**
     * 集群启动后步骤执行
     */
    private List<ClusterJarStepElement> clusterJarSteps;
    /**
     * 集群名称
     */
    private String clusterName;
    /**
     * 集群版本
     */
    private String clusterReleaseVer;

    private String createUser;

    private Integer state;

    private String masterHostName;

    /**
     * 数据中心
     */
    private String dc;
    /**
     * EMR 集群托管伸缩策略
     */
    private Emr emrManagedScalingPolicy;
    /**
     * 镜像版本
     */
    private String imageVer;
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
     * 仅允许内网IP访问，Dataproc 包含此字段
     */
    private Boolean internalIpOnly;
    /**
     * 闲时关闭
     */
    private boolean isIdle;
    /**
     * 主安全组，EMR 托管安全组（阿里云使用一个安全组的名称）
     */
    private String masterSecurityGroup;
    /**
     * Dataproc Metadata，Map类型
     */
    private Map<String, Object> metadata;
    /**
     * 项目名
     */
    private String projectName;
    /**
     * 系统盘大小（GB），EMR 10GB~100GB //Dataproc 500GB
     */
    private Integer rootVolSize;
    /**
     * EMR 日志桶
     */
    private String s3LogLocation;
    /**
     * EMR 托管安全组，EMR 托管安全组（阿里云使用一个安全组的名称）
     */
    private String serviceAccessSecurityGroup;
    /**
     * EMR 角色，default role is EMR_DefaultRole .Amazon EMR assumes in order to access AWS
     * resources on your behalf。阿里云当前不包含
     */
    private String serviceRole;
    /**
     * 从安全组，EMR 托管安全组（阿里云使用一个安全组的名称）
     */
    private String slaveSecurityGroup;
    private Integer startHa;
    /**
     * 执行步骤后关闭
     */
    private boolean stepCompleteClose;
    /**
     * 子网ID
     */
    private String subnet;

    /**
     * 标签
     */
    private JSONObject tagMap;
    /**
     * 是否打开关闭保护
     */
    private boolean terminationProtected;

    private String vmMI;

    private String vmMITenantId;

    private String vmMIClientId;

    private String logMI;

    private Integer isEmbedAmbariDb;

    private Integer enableGanglia;

    /**
     * 1,并行扩缩容;0,串行扩缩容
     */
    private Integer isParallelScale;

    /**
     * 是否加入直接销毁白名单,是:空或1,否:0
     */
    private Integer inWhiteList;

    public String getVmMI() {
        return vmMI;
    }

    public void setVmMI(String vmMI) {
        this.vmMI = vmMI;
    }

    public String getVmMITenantId() {
        return vmMITenantId;
    }

    public void setVmMITenantId(String vmMITenantId) {
        this.vmMITenantId = vmMITenantId;
    }

    public String getVmMIClientId() {
        return vmMIClientId;
    }

    public void setVmMIClientId(String vmMIClientId) {
        this.vmMIClientId = vmMIClientId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
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

    public Integer getAz() {
        return az;
    }

    public void setAz(Integer az) {
        this.az = az;
    }

    public Map<String, String> getClusterApps() {
        return clusterApps;
    }

    public void setClusterApps(Map<String, String> clusterApps) {
        this.clusterApps = clusterApps;
    }

    public List<Empty> getClusterCfgs() {
        return clusterCfgs;
    }

    public void setClusterCfgs(List<Empty> clusterCfgs) {
        this.clusterCfgs = clusterCfgs;
    }

    public List<ClusterJarStepElement> getClusterJarSteps() {
        return clusterJarSteps;
    }

    public void setClusterJarSteps(List<ClusterJarStepElement> clusterJarSteps) {
        this.clusterJarSteps = clusterJarSteps;
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

    public List<ConfClusterScript> getConfClusterScript() {
        return confClusterScript;
    }

    public void setConfClusterScript(List<ConfClusterScript> confClusterScript) {
        this.confClusterScript = confClusterScript;
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

    public String getImageVer() {
        return imageVer;
    }

    public void setImageVer(String imageVer) {
        this.imageVer = imageVer;
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

    public boolean isIdle() {
        return isIdle;
    }

    public void setIdle(boolean idle) {
        isIdle = idle;
    }

    public String getMasterSecurityGroup() {
        return masterSecurityGroup;
    }

    public void setMasterSecurityGroup(String masterSecurityGroup) {
        this.masterSecurityGroup = masterSecurityGroup;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
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

    public String getServiceAccessSecurityGroup() {
        return serviceAccessSecurityGroup;
    }

    public void setServiceAccessSecurityGroup(String serviceAccessSecurityGroup) {
        this.serviceAccessSecurityGroup = serviceAccessSecurityGroup;
    }

    public String getServiceRole() {
        return serviceRole;
    }

    public void setServiceRole(String serviceRole) {
        this.serviceRole = serviceRole;
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

    public boolean isStepCompleteClose() {
        return stepCompleteClose;
    }

    public void setStepCompleteClose(boolean stepCompleteClose) {
        this.stepCompleteClose = stepCompleteClose;
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

    public boolean isTerminationProtected() {
        return terminationProtected;
    }

    public void setTerminationProtected(boolean terminationProtected) {
        this.terminationProtected = terminationProtected;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getMasterHostName() {
        return masterHostName;
    }

    public void setMasterHostName(String masterHostName) {
        this.masterHostName = masterHostName;
    }

    public String getLogMI() {
        return logMI;
    }

    public void setLogMI(String logMI) {
        this.logMI = logMI;
    }

    public Integer getIsEmbedAmbariDb() {
        return isEmbedAmbariDb;
    }

    public void setIsEmbedAmbariDb(Integer isEmbedAmbariDb) {
        this.isEmbedAmbariDb = isEmbedAmbariDb;
    }

    public Integer getEnableGanglia() {
        return enableGanglia;
    }

    public void setEnableGanglia(Integer enableGanglia) {
        this.enableGanglia = enableGanglia;
    }

    public Integer getIsParallelScale() {
        return isParallelScale;
    }

    public void setIsParallelScale(Integer isParallelScale) {
        this.isParallelScale = isParallelScale;
    }

    public Integer getInWhiteList() {
        return inWhiteList;
    }

    public void setInWhiteList(Integer inWhiteList) {
        this.inWhiteList = inWhiteList;
    }
}