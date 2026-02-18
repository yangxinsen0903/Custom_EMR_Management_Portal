package com.sunbox.sdpadmin.model.admin.response;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunbox.sdpadmin.model.admin.request.*;

import java.io.Serializable;
import java.util.List;


/**
 * 集群查询 response <br/>
 * 主要用于集群复制的数据
 * @Author: wangshihao
 * @Copyright: 版权
 * @CreateTime: 2022/12/16 11:07
 * */

public class ClusterQueryResponse implements Serializable {

    private static final long serialVersionUID = 1L;
    private AmbariDbCfg ambariDbCfgs;
    private String ambariPassword;
    private String ambariUsername;
    private List<ClusterCfg> clusterCfgs;
    private String clusterName;
    private List<ConfClusterScript> confClusterScript;
    private String deleteProtected;
    private HiveMetadataDbCfg hiveMetadataDbCfgs;
    private List<InstanceGroupSkuCfg> instanceGroupSkuCfgs;
    private InstanceGroupVersion instanceGroupVersion;
    private Integer isHa;
    private String keypairId;
    private String logPath;
    private String masterSecurityGroup;
    private String slaveSecurityGroup;
    private String subNet;
    private JSONObject tagMap;

    @JsonProperty("vNet")
    private String vNet;
    /** 可用区 */
    private String zone;
    /** 可用区名称 */
    private String zoneName;
    private String scene;
    private String vmMI;
    private String logMI;

    private Integer state;
    private Integer enableGanglia;

    /**
     * 是否内嵌ambaridb
     */
    private Integer isEmbedAmbariDb;

    private String region;

    private String regionName;
    private Integer isParallelScale;

    /**
     * 是否加入直接销毁白名单,是:空或1,否:0
     */
    private Integer isWhiteAddr;

    public Integer getIsParallelScale() {
        return isParallelScale;
    }

    public void setIsParallelScale(Integer isParallelScale) {
        this.isParallelScale = isParallelScale;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Integer getIsEmbedAmbariDb() {
        return isEmbedAmbariDb;
    }

    public void setIsEmbedAmbariDb(Integer isEmbedAmbariDb) {
        this.isEmbedAmbariDb = isEmbedAmbariDb;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
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

    public AmbariDbCfg getAmbariDbCfgs() { return ambariDbCfgs; }
    public void setAmbariDbCfgs(AmbariDbCfg value) { this.ambariDbCfgs = value; }

    public String getAmbariPassword() { return ambariPassword; }
    public void setAmbariPassword(String value) { this.ambariPassword = value; }

    public String getAmbariUsername() { return ambariUsername; }
    public void setAmbariUsername(String value) { this.ambariUsername = value; }

    public List<ClusterCfg> getClusterCfgs() { return clusterCfgs; }
    public void setClusterCfgs(List<ClusterCfg> value) { this.clusterCfgs = value; }

    public String getClusterName() { return clusterName; }
    public void setClusterName(String value) { this.clusterName = value; }

    public List<ConfClusterScript> getConfClusterScript() { return confClusterScript; }
    public void setConfClusterScript(List<ConfClusterScript> value) { this.confClusterScript = value; }

    public String getDeleteProtected() { return deleteProtected; }
    public void setDeleteProtected(String value) { this.deleteProtected = value; }

    public HiveMetadataDbCfg getHiveMetadataDbCfgs() { return hiveMetadataDbCfgs; }
    public void setHiveMetadataDbCfgs(HiveMetadataDbCfg value) { this.hiveMetadataDbCfgs = value; }

    public List<InstanceGroupSkuCfg> getInstanceGroupSkuCfgs() { return instanceGroupSkuCfgs; }
    public void setInstanceGroupSkuCfgs(List<InstanceGroupSkuCfg> value) { this.instanceGroupSkuCfgs = value; }

    public InstanceGroupVersion getInstanceGroupVersion() { return instanceGroupVersion; }
    public void setInstanceGroupVersion(InstanceGroupVersion value) { this.instanceGroupVersion = value; }

    public Integer getIsHa() { return isHa; }
    public void setIsHa(Integer value) { this.isHa = value; }

    public String getKeypairId() { return keypairId; }
    public void setKeypairId(String value) { this.keypairId = value; }

    public String getLogPath() { return logPath; }
    public void setLogPath(String value) { this.logPath = value; }

    public String getMasterSecurityGroup() { return masterSecurityGroup; }
    public void setMasterSecurityGroup(String value) { this.masterSecurityGroup = value; }

    public String getSlaveSecurityGroup() { return slaveSecurityGroup; }
    public void setSlaveSecurityGroup(String value) { this.slaveSecurityGroup = value; }

    public String getSubNet() { return subNet; }
    public void setSubNet(String value) { this.subNet = value; }

    public JSONObject getTagMap() { return tagMap; }
    public void setTagMap(JSONObject value) { this.tagMap = value; }

    public String getVNet() { return vNet; }
    public void setVNet(String value) { this.vNet = value; }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
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

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public void setEnableGanglia(Integer enableGanglia) {
        this.enableGanglia = enableGanglia;
    }

    public Integer getEnableGanglia() {
        return enableGanglia;
    }

    public Integer getIsWhiteAddr() {
        return isWhiteAddr;
    }

    public void setIsWhiteAddr(Integer isWhiteAddr) {
        this.isWhiteAddr = isWhiteAddr;
    }
}