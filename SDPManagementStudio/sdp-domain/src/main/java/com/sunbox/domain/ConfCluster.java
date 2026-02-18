package com.sunbox.domain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 集群配置; 1
 */
public class ConfCluster {

    public static final Object ENABLE_GANGLIA_YES = 1;
    public static final Object ENABLE_GANGLIA_NO = 0;
    /**
     * 集群ID
     */
    private String clusterId;

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * 集群版本号
     */
    private String clusterReleaseVer;

    /**
     * 区域/数据中心
     */
    private String region;

    /**
     * 订阅Id
     */
    private String subscriptionId;

    /**
     * 场景
     */
    private String scene;

    /**
     * 可用区
     */
    private String zone;

    /**
     * 可用区名称
     */
    private String zoneName;

    /**
     * 虚拟网/VPC
     */
    private String vnet;

    /**
     * 子网
     */
    private String subnet;

    /**
     * 创建的虚拟主机MI（节点托管标识）
     */
    private String vmMI;

    /**
     * 集群节点托管MI的TenantId
     */
    private String vmMITenantId;

    /**
     * 集群节点托管MI的ClientId
     */
    private String vmMIClientId;

    /**
     * 集群组件配置模式;分类/自定义
     */
    private String configType;

    /**
     * 主安全组(master节点使用)
     */
    private String masterSecurityGroup;

    /**
     * 从安全组(core/task节点使用）
     */
    private String slaveSecurityGroup;

    /**
     * 日志的对象存储路径
     */
    private String logPath;

    /**
     * 日志桶MI（日志桶节点托管标识）
     */
    private String logMI;

    /**
     * 日志桶托管MI的TenantId
     */
    private String logMITenantId;

    /**
     * 日志桶托管MI的ClientId
     */
    private String logMIClientId;

    /**
     * 密钥对ID
     */
    private String keypairId;

    /**
     * 关闭保护
     */
    private String deleteProtected;

    /**
     * 实例组织类型;group 实例组 / queue 实例队列
     */
    private String instanceCollectionType;

    /**
     * 公网IP是否可用
     */
    private String publicipAvailable;

    /**
     * ambari数据库地址
     */
    private String ambariDburl;

    private String ambariPort;

    private String ambariDatabase;

    private int ambariDbAutocreate;

    /**
     * hive元数据数据库地址
     */
    private String hiveMetadataDburl;

    private String hiveMetadataPort;

    private String hiveMetadataDatabase;

    /**
     * ambari账号
     */
    private String ambariAcount;

    /**
     * 是否高可用
     */
    private Integer isHa;

    /**
     * 状态;0 待创建 1 创建中 2 已创建  -1释放中 -2 已释放
     */
    private Integer state;

    //public final static int WAIT_CREATE = 0;
    public final static int CREATING = 1;
    public final static int CREATED = 2;
    /** 创建审核中 */
    public final static int CREATE_AUDITING = 3;
    /** 创建审核拒绝 */
    public final static int CREATE_AUDIT_REJECT = -5;
    /** 删除审核中 */
    public final static int DELETE_AUDITING = 4;
    /** 删除审核拒绝, 以后要去掉, 仍为 已创建 */
    //public final static int DELETE_AUDIT_REJECT = -6;
    public final static int DELETING = -1;
    public final static int DELETED = -2;
    public final static int FAILED = -9;
    /** 待销毁 */
    public final static int WAIT_DELETE = -3;
    /** 销毁失败 */
    public final static int DELETE_FAILED = -4;

    /**
     * 创建人
     */
    private String createdby;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 修改人
     */
    private String modifiedby;

    /**
     * 修改时间
     */
    private Date modifiedTime;

    /**
     * 创建模式
     *
     * @see CreationMode
     */
    private String creationMode;

    /**
     * 创建模式对应的子状态
     * @see CreationSubState
     */
    private String creationSubState;

    /**
     * 集群虚拟机资源配置表
     */
    private List<ConfClusterVm> confClusterVmList;

    private String srcClusterId;

    /**
     * 是否使用内嵌数据库
     */
    private Integer isEmbedAmbariDb;
    private Integer enableGanglia;

    /**
     * 1,并行扩缩容;0,串行扩缩容
     */
    private Integer isParallelScale;

    /**
     * 是否加入直接销毁白名单,是:空或1,否:0
     */
    private Integer isWhiteAddr;

    /**
     * 获取状态的中文形式
     * @return
     */
    public String getStateStr() {
        switch (state) {
            case CREATING: return "创建中";
            case CREATED: return "已创建";
            case DELETING: return "销毁中";
            case DELETED: return "已销毁";
            case FAILED: return "创建失败";
            case WAIT_DELETE: return "待销毁";
            case DELETE_FAILED: return "销毁失败";
            case CREATE_AUDITING: return "创建审核中";
            case CREATE_AUDIT_REJECT: return "创建审核拒绝";
            case DELETE_AUDITING: return "删除审核中";
        }

        return "未知状态(" + state + ")";
    }

    /**
     * 集群是否已删除: 状态为DELETED或DELETING
     * @return
     */
    public Boolean isDeleted() {
        return Objects.equals(state, DELETED) || Objects.equals(state, DELETING);
    }

    public Integer getIsParallelScale() {
        return isParallelScale;
    }

    public void setIsParallelScale(Integer isParallelScale) {
        this.isParallelScale = isParallelScale;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
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

    public String getAmbariClusterName() {
        return clusterName.replaceAll("-", "");
    }

    public int getAmbariDbAutocreate() {
        return ambariDbAutocreate;
    }

    public void setAmbariDbAutocreate(int ambariDbAutocreate) {
        this.ambariDbAutocreate = ambariDbAutocreate;
    }

    public String getSrcClusterId() {
        return srcClusterId;
    }

    public void setSrcClusterId(String srcClusterId) {
        this.srcClusterId = srcClusterId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getVnet() {
        return vnet;
    }

    public void setVnet(String vnet) {
        this.vnet = vnet;
    }

    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public String getMasterSecurityGroup() {
        return masterSecurityGroup;
    }

    public void setMasterSecurityGroup(String masterSecurityGroup) {
        this.masterSecurityGroup = masterSecurityGroup;
    }

    public String getSlaveSecurityGroup() {
        return slaveSecurityGroup;
    }

    public void setSlaveSecurityGroup(String slaveSecurityGroup) {
        this.slaveSecurityGroup = slaveSecurityGroup;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getKeypairId() {
        return keypairId;
    }

    public void setKeypairId(String keypairId) {
        this.keypairId = keypairId;
    }

    public String getDeleteProtected() {
        return deleteProtected;
    }

    public void setDeleteProtected(String deleteProtected) {
        this.deleteProtected = deleteProtected;
    }

    public String getInstanceCollectionType() {
        return instanceCollectionType;
    }

    public void setInstanceCollectionType(String instanceCollectionType) {
        this.instanceCollectionType = instanceCollectionType;
    }

    public String getPublicipAvailable() {
        return publicipAvailable;
    }

    public void setPublicipAvailable(String publicipAvailable) {
        this.publicipAvailable = publicipAvailable;
    }

    public String getAmbariDburl() {
        return ambariDburl;
    }

    public void setAmbariDburl(String ambariDburl) {
        this.ambariDburl = ambariDburl;
    }

    public String getAmbariPort() {
        return ambariPort;
    }

    public void setAmbariPort(String ambariPort) {
        this.ambariPort = ambariPort;
    }

    public String getAmbariDatabase() {
        return ambariDatabase;
    }

    public void setAmbariDatabase(String ambariDatabase) {
        this.ambariDatabase = ambariDatabase;
    }

    public String getHiveMetadataDburl() {
        return hiveMetadataDburl;
    }

    public void setHiveMetadataDburl(String hiveMetadataDburl) {
        this.hiveMetadataDburl = hiveMetadataDburl;
    }

    public String getHiveMetadataPort() {
        return hiveMetadataPort;
    }

    public void setHiveMetadataPort(String hiveMetadataPort) {
        this.hiveMetadataPort = hiveMetadataPort;
    }

    public String getHiveMetadataDatabase() {
        return hiveMetadataDatabase;
    }

    public void setHiveMetadataDatabase(String hiveMetadataDatabase) {
        this.hiveMetadataDatabase = hiveMetadataDatabase;
    }

    public String getAmbariAcount() {
        return ambariAcount;
    }

    public void setAmbariAcount(String ambariAcount) {
        this.ambariAcount = ambariAcount;
    }

    public Integer getIsHa() {
        return isHa;
    }

    public void setIsHa(Integer isHa) {
        this.isHa = isHa;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getModifiedby() {
        return modifiedby;
    }

    public void setModifiedby(String modifiedby) {
        this.modifiedby = modifiedby;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getVmMI() {
        return vmMI;
    }

    public void setVmMI(String vmMI) {
        this.vmMI = vmMI;
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

    public String getVmMIClientId() {
        return vmMIClientId;
    }

    public void setVmMIClientId(String vmMIClientId) {
        this.vmMIClientId = vmMIClientId;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public Integer getEnableGanglia() {
        return enableGanglia;
    }

    public void setEnableGanglia(Integer enableGanglia) {
        this.enableGanglia = enableGanglia;
    }

    public String getCreationMode() {
        return creationMode;
    }

    public void setCreationMode(String creationMode) {
        this.creationMode = creationMode;
    }

    public String getCreationSubState() {
        return creationSubState;
    }

    public void setCreationSubState(String creationSubState) {
        this.creationSubState = creationSubState;
    }

    public Integer getIsWhiteAddr() {
        return isWhiteAddr;
    }

    public void setIsWhiteAddr(Integer isWhiteAddr) {
        this.isWhiteAddr = isWhiteAddr;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConfCluster{");
        sb.append("clusterId='").append(clusterId).append('\'');
        sb.append(", clusterName='").append(clusterName).append('\'');
        sb.append(", clusterReleaseVer='").append(clusterReleaseVer).append('\'');
        sb.append(", region='").append(region).append('\'');
        sb.append(", scene='").append(scene).append('\'');
        sb.append(", zone='").append(zone).append('\'');
        sb.append(", zoneName='").append(zoneName).append('\'');
        sb.append(", vnet='").append(vnet).append('\'');
        sb.append(", subnet='").append(subnet).append('\'');
        sb.append(", vmMI='").append(vmMI).append('\'');
        sb.append(", vmMITenantId='").append(vmMITenantId).append('\'');
        sb.append(", vmMIClientId='").append(vmMIClientId).append('\'');
        sb.append(", configType='").append(configType).append('\'');
        sb.append(", masterSecurityGroup='").append(masterSecurityGroup).append('\'');
        sb.append(", slaveSecurityGroup='").append(slaveSecurityGroup).append('\'');
        sb.append(", logPath='").append(logPath).append('\'');
        sb.append(", logMI='").append(logMI).append('\'');
        sb.append(", logMITenantId='").append(logMITenantId).append('\'');
        sb.append(", logMIClientId='").append(logMIClientId).append('\'');
        sb.append(", keypairId='").append(keypairId).append('\'');
        sb.append(", deleteProtected='").append(deleteProtected).append('\'');
        sb.append(", instanceCollectionType='").append(instanceCollectionType).append('\'');
        sb.append(", publicipAvailable='").append(publicipAvailable).append('\'');
        sb.append(", ambariDburl='").append(ambariDburl).append('\'');
        sb.append(", ambariPort='").append(ambariPort).append('\'');
        sb.append(", ambariDatabase='").append(ambariDatabase).append('\'');
        sb.append(", ambariDbAutocreate=").append(ambariDbAutocreate);
        sb.append(", hiveMetadataDburl='").append(hiveMetadataDburl).append('\'');
        sb.append(", hiveMetadataPort='").append(hiveMetadataPort).append('\'');
        sb.append(", hiveMetadataDatabase='").append(hiveMetadataDatabase).append('\'');
        sb.append(", ambariAcount='").append(ambariAcount).append('\'');
        sb.append(", isHa=").append(isHa);
        sb.append(", state=").append(state);
        sb.append(", createdby='").append(createdby).append('\'');
        sb.append(", createdTime=").append(createdTime);
        sb.append(", modifiedby='").append(modifiedby).append('\'');
        sb.append(", modifiedTime=").append(modifiedTime);
        sb.append(", confClusterVmList=").append(confClusterVmList);
        sb.append(", srcClusterId='").append(srcClusterId).append('\'');
        sb.append(", isEmbedAmbariDb=").append(isEmbedAmbariDb);
        sb.append(", enableGanglia=").append(enableGanglia);
        sb.append(", creationMode=").append(creationMode);
        sb.append(", isWhiteAddr=").append(isWhiteAddr);
        sb.append('}');
        return sb.toString();
    }

    public enum CreationMode {
        DIRECTLY("DIRECTLY", "直接创建"),
        SPLIT("SPLIT", "分批创建"),
        ;

        private String value;
        private String text;

        CreationMode(String value, String text) {
            this.value = value;
            this.text = text;
        }


        public String getValue() {
            return value;
        }

        public String getText() {
            return text;
        }

        public static CreationMode findByValue(String valueString) {
            for (CreationMode creationMode : CreationMode.values()) {
                if (creationMode.getValue().equalsIgnoreCase(valueString)) {
                    return creationMode;
                }
            }
            return null;
        }

        public static CreationMode getByValue(String valueString, CreationMode defaultValue) {
            CreationMode creationMode = findByValue(valueString);
            if(creationMode == null){
                return defaultValue;
            }
            return creationMode;
        }
    }

    public enum CreationSubState {
        RUNNING("RUNNING", "运行中"),
        FINISHED("FINISHED", "完成"),
        ;

        private String value;
        private String text;

        CreationSubState(String value, String text) {
            this.value = value;
            this.text = text;
        }


        public String getValue() {
            return value;
        }

        public String getText() {
            return text;
        }

        public static CreationSubState findByValue(String valueString) {
            for (CreationSubState creationSubState : CreationSubState.values()) {
                if (creationSubState.getValue().equalsIgnoreCase(valueString)) {
                    return creationSubState;
                }
            }
            return null;
        }

        public static CreationSubState getByValue(String valueString, CreationSubState defaultValue) {
            CreationSubState creationSubState = findByValue(valueString);
            if(creationSubState == null){
                return defaultValue;
            }
            return creationSubState;
        }
    }
}