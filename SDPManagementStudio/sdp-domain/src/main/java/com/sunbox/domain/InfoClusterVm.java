package com.sunbox.domain;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 集群实例创建信息; 1000
 */
public class InfoClusterVm {
    /**
     * 集群ID
     */
    private String clusterId;

    /**
     * 实例名称
     */
    private String vmName;

    /**
     * 实例配置ID
     */
    private String vmConfId;

    /**
     * 机器名称
     */
    private String hostName;

    /**
     * 内网IP
     */
    private String internalip;

    /**
     * 默认用户名
     */
    private String defaultUsername;

    private String vmRole;

    /**
     * 实例组名称
     */
    private String groupName;


    /**
     * sku名称
     */
    private String skuName;

    /**
     * 购买类型
     */
    private String purchaseType;

    /**
     * 镜像ID
     */
    private String imageid;

    /**
     * 虚拟机状态
     */
    private int state;

    /**
     * 创建虚拟机流水号
     */
    private String createTranscationId;

    /**
     * 创建虚拟机的任务id
     */
    private String createJobId;

    /**
     * 虚拟机创建开始时间
     */
    private Date createBegtime;

    /**
     * 虚拟机创建完成时间
     */
    private Date createEndtime;


    /**
     * 缩容任务ID
     */
    private String scaleinTaskId;

    /**
     * 扩容任务ID
     */
    private String scaleoutTaskId;

    /**
     * 扩容vm配置ID
     */
    private String scaleVmDetailId;

    private Integer cnt;

    private String groupId;

    private String ambariConfigGroup;

    /**
     *  按需购买价
     */
    private BigDecimal ondemondPrice;

    /**
     *  竞价购买单价
     */
    private BigDecimal spotPrice;

    /**
     * 加入集群时间
     */
    private Date joinClusterTime;

    /**
     * 竞价实例主机心跳检查时间
     */
    private Date healthCheckTime;
    /**
     * 维护模式
     */
    private Integer maintenanceMode;

    /**
     * 维护模式关
     */
    public static final Integer MaintenanceModeOFF=0;

    /**
     * 维护模式关
     */
    public static final Integer MaintenanceModeON=1;

    public static final int VM_STOP = 0;
    public static final int VM_RUNNING = 1;
    public static final int VM_DELETED = -1;
    public static final int VM_UNKNOWN = -99;
    public static final int VM_DELETING = -10;
    private Integer dataVolumeSize;
    private Integer dataVolumeCount;
    private Integer osVolumeSize;
    private Integer osVolumeCount;

    /**
     *  虚拟机唯一ID
     */
    private String vmid;


    public String getVmid() {
        return vmid;
    }

    public void setVmid(String vmid) {
        this.vmid = vmid;
    }

    public Date getJoinClusterTime() {
        return joinClusterTime;
    }

    public void setJoinClusterTime(Date joinClusterTime) {
        this.joinClusterTime = joinClusterTime;
    }

    public BigDecimal getOndemondPrice() {
        return ondemondPrice;
    }

    public void setOndemondPrice(BigDecimal ondemondPrice) {
        this.ondemondPrice = ondemondPrice;
    }

    public BigDecimal getSpotPrice() {
        return spotPrice;
    }

    public void setSpotPrice(BigDecimal spotPrice) {
        this.spotPrice = spotPrice;
    }


    public Integer getMaintenanceMode() {
        return maintenanceMode;
    }

    public void setMaintenanceMode(Integer maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getAmbariConfigGroup() {
        return ambariConfigGroup;
    }

    public void setAmbariConfigGroup(String ambariConfigGroup) {
        this.ambariConfigGroup = ambariConfigGroup;
    }

    public String getScaleVmDetailId() {
        return scaleVmDetailId;
    }

    public void setScaleVmDetailId(String scaleVmDetailId) {
        this.scaleVmDetailId = scaleVmDetailId;
    }

    public String getScaleinTaskId() {
        return scaleinTaskId;
    }

    public void setScaleinTaskId(String scaleinTaskId) {
        this.scaleinTaskId = scaleinTaskId;
    }

    public String getScaleoutTaskId() {
        return scaleoutTaskId;
    }

    public void setScaleoutTaskId(String scaleoutTaskId) {
        this.scaleoutTaskId = scaleoutTaskId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }


    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getVmRole() {
        return vmRole;
    }

    public void setVmRole(String vmRole) {
        this.vmRole = vmRole;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public String getVmConfId() {
        return vmConfId;
    }

    public void setVmConfId(String vmConfId) {
        this.vmConfId = vmConfId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getInternalip() {
        return internalip;
    }

    public void setInternalip(String internalip) {
        this.internalip = internalip;
    }

    public String getDefaultUsername() {
        return defaultUsername;
    }

    public void setDefaultUsername(String defaultUsername) {
        this.defaultUsername = defaultUsername;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public String getPurchaseType() {
        return purchaseType;
    }

    public void setPurchaseType(String purchaseType) {
        this.purchaseType = purchaseType;
    }

    public String getImageid() {
        return imageid;
    }

    public void setImageid(String imageid) {
        this.imageid = imageid;
    }

    public String getCreateTranscationId() {
        return createTranscationId;
    }

    public void setCreateTranscationId(String createTranscationId) {
        this.createTranscationId = createTranscationId;
    }

    public String getCreateJobId() {
        return createJobId;
    }

    public void setCreateJobId(String createJobId) {
        this.createJobId = createJobId;
    }

    public Date getCreateBegtime() {
        return createBegtime;
    }

    public void setCreateBegtime(Date createBegtime) {
        this.createBegtime = createBegtime;
    }

    public Date getCreateEndtime() {
        return createEndtime;
    }

    public void setCreateEndtime(Date createEndtime) {
        this.createEndtime = createEndtime;
    }

    public Integer getCnt() {
        return cnt;
    }

    public void setCnt(Integer cnt) {
        this.cnt = cnt;
    }


    public void setDataVolumeSize(Integer dataVolumeSize) {
        this.dataVolumeSize = dataVolumeSize;
    }

    public Integer getDataVolumeSize() {
        return dataVolumeSize;
    }

    public void setDataVolumeCount(Integer dataVolumeCount) {
        this.dataVolumeCount = dataVolumeCount;
    }

    public Integer getDataVolumeCount() {
        return dataVolumeCount;
    }

    public void setOsVolumeSize(Integer osVolumeSize) {
        this.osVolumeSize = osVolumeSize;
    }

    public Integer getOsVolumeSize() {
        return osVolumeSize;
    }

    public void setOsVolumeCount(Integer osVolumeCount) {
        this.osVolumeCount = osVolumeCount;
    }

    public Integer getOsVolumeCount() {
        return osVolumeCount;
    }

    public Date getHealthCheckTime() {
        return healthCheckTime;
    }

    public void setHealthCheckTime(Date healthCheckTime) {
        this.healthCheckTime = healthCheckTime;
    }

    @Override
    public String toString() {
        return "InfoClusterVm{" +
                "clusterId='" + clusterId + '\'' +
                ", vmName='" + vmName + '\'' +
                ", vmConfId='" + vmConfId + '\'' +
                ", hostName='" + hostName + '\'' +
                ", internalip='" + internalip + '\'' +
                ", defaultUsername='" + defaultUsername + '\'' +
                ", vmRole='" + vmRole + '\'' +
                ", groupName='" + groupName + '\'' +
                ", skuName='" + skuName + '\'' +
                ", purchaseType='" + purchaseType + '\'' +
                ", imageid='" + imageid + '\'' +
                ", state=" + state +
                ", createTranscationId='" + createTranscationId + '\'' +
                ", createJobId='" + createJobId + '\'' +
                ", createBegtime=" + createBegtime +
                ", createEndtime=" + createEndtime +
                ", scaleinTaskId='" + scaleinTaskId + '\'' +
                ", scaleoutTaskId='" + scaleoutTaskId + '\'' +
                ", scaleVmDetailId='" + scaleVmDetailId + '\'' +
                ", cnt=" + cnt +
                ", groupId='" + groupId + '\'' +
                ", ambariConfigGroup='" + ambariConfigGroup + '\'' +
                ", ondemondPrice=" + ondemondPrice +
                ", spotPrice=" + spotPrice +
                ", joinClusterTime=" + joinClusterTime +
                ", healthCheckTime=" + healthCheckTime +
                ", maintenanceMode=" + maintenanceMode +
                ", dataVolumeSize=" + dataVolumeSize +
                ", dataVolumeCount=" + dataVolumeCount +
                ", osVolumeSize=" + osVolumeSize +
                ", osVolumeCount=" + osVolumeCount +
                '}';
    }
}