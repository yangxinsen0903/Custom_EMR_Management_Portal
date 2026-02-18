package com.sunbox.sdpadmin.model.admin.request;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.ConfCluster;
import com.sunbox.domain.ConfClusterVm;
import com.sunbox.domain.enums.SceneType;
import org.bouncycastle.util.Strings;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AdminSaveClusterRequest {
    private String userName;
    private AmbariDbCfg ambariDbCfgs;
    private String ambariPassword;
    private String ambariUsername;
    private List<ClusterCfg> clusterCfgs;
    private String clusterName;
    private List<ConfClusterScript> confClusterScript;
    private String deleteProtected;
    private HiveMetadataDbCfg hiveMetadataDbCfgs;
    private List<InstanceGroupSkuCfg> instanceGroupSkuCfgs;

    /**
     * 场景： 默认 和 HBASE
     */
    private String scene;
    private InstanceGroupVersion instanceGroupVersion;
    private Integer isHa;
    private String keypairId;

    /**
     * 集群节点托管标识
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
     * 日志桶托管MI的TenantId
     */
    private String logMITenantId;

    /**
     * 日志桶托管MI的ClientId
     */
    private String logMIClientId;

    private String logPath;

    /**
     * 日志桶节点托管标识
     */
    private String logMI;
    private String masterSecurityGroup;
    private String slaveSecurityGroup;
    private String subNet;
    private JSONObject tagMap;

    /**
     * 可用区代码
     */
    private String zone;

    /**
     * 可用区名称
     */
    private String zoneName;

    private String vNet;

    /**
     * 是否内嵌ambaridb
     */
    private Integer isEmbedAmbariDb;

    /**
     * 数据中心
     */
    private String region;

    /**
     * 复制集群ID
     */
    private String srcClusterId;
    private String vmRole;
    private String groupName;
    private String groupId;
    private ConfGroupElasticScalingData confGroupElasticScalingData;
    private Integer enableGanglia;

    /**
     * 拆分创建
     * null or DIRECTLY 直接创建
     * SPLIT 先创建小集群然后创建扩容任务
     * @see ConfCluster.CreationMode
     */
    private String creationMode;

    /**
     * 是否加入直接销毁白名单,是:空或1,否:0
     */
    private Integer isWhiteAddr;
    /**
     * 是否通过工单来创建集群, 接口没有此参数,只做参数的传递
     */
    private Integer isWorkOrderCreate;

    public void validate() {
        if (CollectionUtil.isEmpty(instanceGroupSkuCfgs)) {
            return;
        }

        int totalVmCount = totalVmInstanceCount();
        // 如果安装Ganglia，限制最多只能选择200台主机
        if (Objects.equals(enableGanglia, 1) && totalVmCount > 200) {
            throw new RuntimeException("开启Ganglia监控后，集群允许开通最大主机数量为200台，当前主机数：" + totalVmCount);
        }

//        if (Objects.equals(isEmbedAmbariDb, 1) && totalVmCount > 200) {
//            SdpExceptionUtil.wrapRuntimeAndThrow("开启内置MySQL后，集群允许开通最大主机数量为200台，当前主机数：{}", totalVmCount);
//        }

        int ambariDataVolumeSize = getAmbariDataVolumeSize();
        if (Objects.equals(enableGanglia, 1) && ambariDataVolumeSize < 2000) {
            throw new RuntimeException("开启Ganglia监控后，Ambari数据盘大小不能小于2T，当前大小：" + ambariDataVolumeSize);
        }

        // 检查磁盘大小不能超过4096，此限制去掉
//        for (InstanceGroupSkuCfg instanceCfg : instanceGroupSkuCfgs) {
//            if (instanceCfg.getDataVolumeSize() > 4096) {
//                throw new RuntimeException("数据盘大小不能超过4T(4096G)");
//            }
//        }

        // 检查磁盘大小不能超过4096
        for (InstanceGroupSkuCfg instanceCfg : instanceGroupSkuCfgs) {
            if (instanceCfg.getDataVolumeSize() > 4096) {
                throw new RuntimeException("数据盘大小不能超过4T(4096G)");
            }
        }

        for (InstanceGroupSkuCfg instanceGroupSkuCfg : instanceGroupSkuCfgs) {
            if (instanceGroupSkuCfg.getPurchaseType() != null
                    && instanceGroupSkuCfg.getPurchaseType() == ConfClusterVm.PURCHASETYPE_SPOT) {
                if (instanceGroupSkuCfg.getPurchasePriority() == null
                        || instanceGroupSkuCfg.getPriceStrategy() == null
                        || instanceGroupSkuCfg.getMaxPrice() == null) {
                    throw new RuntimeException("竞价实例组" + instanceGroupSkuCfg.getGroupName() + "的竞价配置不正确");
                } else if (instanceGroupSkuCfg.getMaxPrice().compareTo(BigDecimal.ZERO) < 0) {
                    throw new RuntimeException("竞价实例组" + instanceGroupSkuCfg.getGroupName() + "的价格设置不正确");
                }
            }
        }

        // 检查Tag里面是否有4个必填的tag
        List<String> mustExistTags = Arrays.asList("svcid", "svc", "service", "for");
        for (String mustExistTag : mustExistTags) {
            Object tag = this.tagMap.get(mustExistTag);
            if (Objects.isNull(tag)) {
                throw new RuntimeException("标签[" + mustExistTag + "]不存在，以下4个标签必填[svcid, svc, service, for]");
            }
        }

        // 检查各个实例组的SKU必须填写
        for (InstanceGroupSkuCfg skuCfg : instanceGroupSkuCfgs) {
            List<String> skuNameList = skuCfg.getSkuNames();
            if (CollUtil.isEmpty(skuNameList)) {
                throw new RuntimeException("实例组的Vm sku不能为空, 实例组="+skuCfg.getGroupName());
            }
        }

        // 检查Ambari和Master实例组不能选L系列机型
        for (InstanceGroupSkuCfg skuCfg : instanceGroupSkuCfgs) {
            if (StrUtil.containsAnyIgnoreCase(skuCfg.getVmRole(), "ambari", "master")) {
                List<String> skuNameList = skuCfg.getSkuNames();
                for (String skuName : skuNameList) {
                    if (StrUtil.contains(skuName, "_L")) {
                        throw new RuntimeException("ambari/master实例组的Sku不能选择L系列机型, 当前选择: 实例组="
                                + skuCfg.getGroupName() + " 机型=" + skuCfg.getSkuNames());
                    }
                 }
            }
        }

        // Core和Task实例组, L系列机型与非L系列机型不能混用.
        for (InstanceGroupSkuCfg skuCfg : instanceGroupSkuCfgs) {
            if (StrUtil.containsAnyIgnoreCase(skuCfg.getVmRole(), "core", "task")) {
                if (skuCfg.isMixedLVmSku()) {
                    throw new RuntimeException("core/task实例组使用多个Sku时,不能混合使用L与非L系列机型, 当前选择: 实例组="
                            + skuCfg.getGroupName() + " 机型=" + skuCfg.getSkuNames());
                }
            }
        }

        // 检查OS的磁盘是否是Pv2，磁盘OS不能是PV2类型磁盘
        for (InstanceGroupSkuCfg skuCfg : instanceGroupSkuCfgs) {
            if (skuCfg.isOSUsePv2DataVolume()) {
                throw new RuntimeException("系统盘不能使用PremiumV2_LRS类型的磁盘");
            }
        }

        this.ambariDbCfgs.setAccount(StrUtil.trim(this.ambariDbCfgs.getAccount()));
        this.ambariDbCfgs.seturl(StrUtil.trim(this.ambariDbCfgs.geturl()));
        this.ambariDbCfgs.setDatabase(StrUtil.trim(this.ambariDbCfgs.getDatabase()));
        this.ambariDbCfgs.setPassword(StrUtil.trim(this.ambariDbCfgs.getPassword()));
        this.hiveMetadataDbCfgs.setAccount(StrUtil.trim(this.hiveMetadataDbCfgs.getAccount()));
        this.hiveMetadataDbCfgs.seturl(StrUtil.trim(this.hiveMetadataDbCfgs.geturl()));
        this.hiveMetadataDbCfgs.setDatabase(StrUtil.trim(this.hiveMetadataDbCfgs.getDatabase()));
        this.hiveMetadataDbCfgs.setPassword(StrUtil.trim(this.hiveMetadataDbCfgs.getPassword()));
    }

    /**
     * 虚拟机实例的总数
     *
     * @return
     */
    public int totalVmInstanceCount() {
        return instanceGroupSkuCfgs.stream().collect(Collectors.summingInt(cfg -> Objects.isNull(cfg.getCnt()) ? 0 : cfg.getCnt()));
    }

    public int getOsVolumeSize() {
        return instanceGroupSkuCfgs.get(0).getOsVolumeSize();
    }

    public int getAmbariDataVolumeSize() {
        for (InstanceGroupSkuCfg instanceGroupSkuCfg : instanceGroupSkuCfgs) {
            String vmRole = instanceGroupSkuCfg.getVmRole();
            if (StrUtil.equalsIgnoreCase(vmRole, "ambari")) {
                return instanceGroupSkuCfg.getDataVolumeSize();
            }
        }
        return 0;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public String getSrcClusterId() {
        return srcClusterId;
    }

    public void setSrcClusterId(String srcClusterId) {
        this.srcClusterId = srcClusterId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public AmbariDbCfg getAmbariDbCfgs() {
        return ambariDbCfgs;
    }

    public void setAmbariDbCfgs(AmbariDbCfg value) {
        this.ambariDbCfgs = value;
    }

    public String getAmbariPassword() {
        return ambariPassword;
    }

    public void setAmbariPassword(String value) {
        this.ambariPassword = value;
    }

    public String getAmbariUsername() {
        return ambariUsername;
    }

    public void setAmbariUsername(String value) {
        this.ambariUsername = value;
    }

    public List<ClusterCfg> getClusterCfgs() {
        return clusterCfgs;
    }

    public void setClusterCfgs(List<ClusterCfg> value) {
        this.clusterCfgs = value;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String value) {
        this.clusterName = value;
    }

    public List<ConfClusterScript> getConfClusterScript() {
        return confClusterScript;
    }

    public void setConfClusterScript(List<ConfClusterScript> value) {
        this.confClusterScript = value;
    }

    public String getDeleteProtected() {
        return deleteProtected;
    }

    public void setDeleteProtected(String value) {
        this.deleteProtected = value;
    }

    public HiveMetadataDbCfg getHiveMetadataDbCfgs() {
        return hiveMetadataDbCfgs;
    }

    public void setHiveMetadataDbCfgs(HiveMetadataDbCfg value) {
        this.hiveMetadataDbCfgs = value;
    }

    public List<InstanceGroupSkuCfg> getInstanceGroupSkuCfgs() {
        return instanceGroupSkuCfgs;
    }

    public void setInstanceGroupSkuCfgs(List<InstanceGroupSkuCfg> value) {
        this.instanceGroupSkuCfgs = value;
    }

    public InstanceGroupVersion getInstanceGroupVersion() {
        return instanceGroupVersion;
    }

    public void setInstanceGroupVersion(InstanceGroupVersion value) {
        this.instanceGroupVersion = value;
    }

    public Integer getIsHa() {
        return isHa;
    }

    public void setIsHa(Integer value) {
        this.isHa = value;
    }

    public String getKeypairId() {
        return keypairId;
    }

    public void setKeypairId(String value) {
        this.keypairId = value;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String value) {
        this.logPath = value;
    }

    public String getMasterSecurityGroup() {
        return masterSecurityGroup;
    }

    public void setMasterSecurityGroup(String value) {
        this.masterSecurityGroup = value;
    }

    public String getSlaveSecurityGroup() {
        return slaveSecurityGroup;
    }

    public void setSlaveSecurityGroup(String value) {
        this.slaveSecurityGroup = value;
    }

    public String getSubNet() {
        return subNet;
    }

    public void setSubNet(String value) {
        this.subNet = value;
    }

    public JSONObject getTagMap() {
        return tagMap;
    }

    public void setTagMap(JSONObject value) {
        this.tagMap = value;
    }

    public String getVNet() {
        return vNet;
    }

    public void setVNet(String value) {
        this.vNet = value;
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

    public String getvNet() {
        return vNet;
    }

    public void setvNet(String vNet) {
        this.vNet = vNet;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
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

    public String getVmRole() {
        return vmRole;
    }

    public void setVmRole(String vmRole) {
        this.vmRole = vmRole;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }

    /**
     * 检查Hbase场景下， Core节点是否3台以上
     */
    public void validateHBaseScene() {
        // 检查Hbase场景下， Core节点是否3台以上
        if (Objects.equals(Strings.toUpperCase(getScene()), SceneType.HBASE.name())) {
            // 找到Core分组
            for (InstanceGroupSkuCfg instanceGroupSkuCfg : instanceGroupSkuCfgs) {
                String upperRole = Strings.toUpperCase(instanceGroupSkuCfg.getVmRole());
                if (Objects.equals(upperRole, "CORE")) {
                    // 如果是Core，检查数量
                    if (instanceGroupSkuCfg.getCnt() < 3) {
                        throw new RuntimeException("HBase场景下，Core节点数量应该超过3台");
                    }
                }
            }
        }
    }

    public ConfGroupElasticScalingData getConfGroupElasticScalingData() {
        return confGroupElasticScalingData;
    }

    public void setConfGroupElasticScalingData(ConfGroupElasticScalingData confGroupElasticScalingData) {
        this.confGroupElasticScalingData = confGroupElasticScalingData;
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

    public ConfCluster.CreationMode getCreationMode(ConfCluster.CreationMode defaultValue) {
        if (creationMode == null) {
            return defaultValue;
        }
        return ConfCluster.CreationMode.getByValue(creationMode, defaultValue);
    }

    public Integer getIswhiteAddr() {
        return isWhiteAddr;
    }

    public void setIswhiteAddr(Integer iswhiteAddr) {
        this.isWhiteAddr = iswhiteAddr;
    }

    public Integer getWorkOrderCreate() {
        return isWorkOrderCreate;
    }

    public void setWorkOrderCreate(Integer workOrderCreate) {
        isWorkOrderCreate = workOrderCreate;
    }

    @Override
    public String toString() {
        return "AdminSaveClusterRequest{" +
                "userName='" + userName + '\'' +
                ", ambariDbCfgs=" + ambariDbCfgs +
                ", ambariPassword='" + ambariPassword + '\'' +
                ", ambariUsername='" + ambariUsername + '\'' +
                ", clusterCfgs=" + clusterCfgs +
                ", clusterName='" + clusterName + '\'' +
                ", confClusterScript=" + confClusterScript +
                ", deleteProtected='" + deleteProtected + '\'' +
                ", hiveMetadataDbCfgs=" + hiveMetadataDbCfgs +
                ", instanceGroupSkuCfgs=" + instanceGroupSkuCfgs +
                ", scene='" + scene + '\'' +
                ", instanceGroupVersion=" + instanceGroupVersion +
                ", isHa=" + isHa +
                ", keypairId='" + keypairId + '\'' +
                ", vmMI='" + vmMI + '\'' +
                ", vmMITenantId='" + vmMITenantId + '\'' +
                ", vmMIClientId='" + vmMIClientId + '\'' +
                ", logMITenantId='" + logMITenantId + '\'' +
                ", logMIClientId='" + logMIClientId + '\'' +
                ", logPath='" + logPath + '\'' +
                ", logMI='" + logMI + '\'' +
                ", masterSecurityGroup='" + masterSecurityGroup + '\'' +
                ", slaveSecurityGroup='" + slaveSecurityGroup + '\'' +
                ", subNet='" + subNet + '\'' +
                ", tagMap=" + tagMap +
                ", zone='" + zone + '\'' +
                ", zoneName='" + zoneName + '\'' +
                ", vNet='" + vNet + '\'' +
                ", isEmbedAmbariDb=" + isEmbedAmbariDb +
                ", region='" + region + '\'' +
                ", srcClusterId='" + srcClusterId + '\'' +
                ", vmRole='" + vmRole + '\'' +
                ", groupName='" + groupName + '\'' +
                ", groupId='" + groupId + '\'' +
                ", confGroupElasticScalingData=" + confGroupElasticScalingData +
                ", enableGanglia=" + enableGanglia +
                ", creationMode='" + creationMode + '\'' +
                ", isWhiteAddr='" + isWhiteAddr + '\'' +
                ", isWorkOrderCreate='" + isWorkOrderCreate + '\'' +
                '}';
    }
}