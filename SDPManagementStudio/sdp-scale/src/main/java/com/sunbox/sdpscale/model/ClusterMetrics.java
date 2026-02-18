package com.sunbox.sdpscale.model;

import com.alibaba.fastjson.annotation.JSONField;

public class ClusterMetrics {
    private String name;
    private String modelerType;
    private String tagQueue;
    private String tagContext;
    private String tagHostname;
    private int running_0;
    private int running_60;
    private int running_300;
    private int running_1440;
    private long AMResourceLimitMB;
    private int AMResourceLimitVCores;
    private double AbsoluteUsedCapacity;
    private int GuaranteedAbsoluteCapacity;
    private int GuaranteedCapacity;
    private long GuaranteedMB;
    private int GuaranteedVCores;
    private int MaxAbsoluteCapacity;
    private int MaxCapacity;
    private long MaxCapacityMB;
    private int MaxCapacityVCores;
    private long UsedAMResourceMB;
    private int UsedAMResourceVCores;
    private double UsedCapacity;
    private int ActiveApplications;
    private int ActiveUsers;
    /*private int AggregateContainersAllocated;
    private int AggregateContainersPreempted;
    private int AggregateContainersReleased;
    private int AggregateMemoryMBPreempted;
    private int AggregateMemoryMBSecondsPreempted;
    private int AggregateNodeLocalContainersAllocated;
    private int AggregateOffSwitchContainersAllocated;
    private int AggregateRackLocalContainersAllocated;
    private int AggregateVcoreSecondsPreempted;
    private int AggregateVcoresPreempted;*/

    private int AppAttemptFirstContainerAllocationDelayNumOps;
    private int AppAttemptFirstContainerAllocationDelayAvgTime;

    // ///////////////////////////////////
    //          CPU指标
    // //////////////////////////////////////
    /** 总虚拟CPU核数 */
    private int AllocatedVCores;
    /** 可用的虚拟CPU核数 */
    private int AvailableVCores;
    /** 待分配的虚拟CPU核数 */
    private int PendingVCores;
    /** 保留的虚拟CPU核数 */
    private int ReservedVCores;

    // ///////////////////////////////////
    //          任务数指标
    // //////////////////////////////////////
    /** 队列已完成的任务数 */
    private int AppsCompleted;
    /** 队列失败的任务数 */
    private int AppsFailed;
    /** 队列被杀掉的任务数 */
    private int AppsKilled;
    /** 队列挂起的任务数 */
    private int AppsPending;
    /** 队列运行中的任务数 */
    private int AppsRunning;
    /** 队列提交的任务数 */
    private int AppsSubmitted;

    // ///////////////////////////////////
    //          容器指标
    // //////////////////////////////////////
    /** 已分配的容器数 */
    private int AllocatedContainers;
    /** 待分配的容器数 */
    private int PendingContainers;
    /** 预留的容器数 */
    private int ReservedContainers;

    // ///////////////////////////////////
    //          内存指标
    // //////////////////////////////////////
    /** 可用内存数 */
    private long AvailableMB;
    /** 已分配的内存数 */
    private long AllocatedMB;
    /** 待分配的内存数 */
    private long PendingMB;
    /** 预留的内存数 */
    private long ReservedMB;

    /** 集群ID */
    private String clusterId;

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setModelerType(String modelerType) {
        this.modelerType = modelerType;
    }
    public String getModelerType() {
        return modelerType;
    }

    public void setTagQueue(String tagQueue) {
        this.tagQueue = tagQueue;
    }
    @JSONField(name = "tag.Queue")
    public String getTagQueue() {
        return tagQueue;
    }

    public void setTagContext(String tagContext) {
        this.tagContext = tagContext;
    }
    @JSONField(name = "tag.Context")
    public String getTagContext() {
        return tagContext;
    }

    public void setTagHostname(String tagHostname) {
        this.tagHostname = tagHostname;
    }
    @JSONField(name = "tag.Hostname")
    public String getTagHostname() {
        return tagHostname;
    }

    public void setRunning_0(int running_0) {
        this.running_0 = running_0;
    }
    public int getRunning_0() {
        return running_0;
    }

    public void setRunning_60(int running_60) {
        this.running_60 = running_60;
    }
    public int getRunning_60() {
        return running_60;
    }

    public void setRunning_300(int running_300) {
        this.running_300 = running_300;
    }
    public int getRunning_300() {
        return running_300;
    }

    public void setRunning_1440(int running_1440) {
        this.running_1440 = running_1440;
    }
    public int getRunning_1440() {
        return running_1440;
    }

    public void setAMResourceLimitMB(long AMResourceLimitMB) {
        this.AMResourceLimitMB = AMResourceLimitMB;
    }
    public long getAMResourceLimitMB() {
        return AMResourceLimitMB;
    }

    public void setAMResourceLimitVCores(int AMResourceLimitVCores) {
        this.AMResourceLimitVCores = AMResourceLimitVCores;
    }
    public int getAMResourceLimitVCores() {
        return AMResourceLimitVCores;
    }

    public void setAbsoluteUsedCapacity(double AbsoluteUsedCapacity) {
        this.AbsoluteUsedCapacity = AbsoluteUsedCapacity;
    }
    public double getAbsoluteUsedCapacity() {
        return AbsoluteUsedCapacity;
    }

    public void setGuaranteedAbsoluteCapacity(int GuaranteedAbsoluteCapacity) {
        this.GuaranteedAbsoluteCapacity = GuaranteedAbsoluteCapacity;
    }
    public int getGuaranteedAbsoluteCapacity() {
        return GuaranteedAbsoluteCapacity;
    }

    public void setGuaranteedCapacity(int GuaranteedCapacity) {
        this.GuaranteedCapacity = GuaranteedCapacity;
    }
    public int getGuaranteedCapacity() {
        return GuaranteedCapacity;
    }

    public void setGuaranteedMB(long GuaranteedMB) {
        this.GuaranteedMB = GuaranteedMB;
    }
    public long getGuaranteedMB() {
        return GuaranteedMB;
    }

    public void setGuaranteedVCores(int GuaranteedVCores) {
        this.GuaranteedVCores = GuaranteedVCores;
    }
    public int getGuaranteedVCores() {
        return GuaranteedVCores;
    }

    public void setMaxAbsoluteCapacity(int MaxAbsoluteCapacity) {
        this.MaxAbsoluteCapacity = MaxAbsoluteCapacity;
    }
    public int getMaxAbsoluteCapacity() {
        return MaxAbsoluteCapacity;
    }

    public void setMaxCapacity(int MaxCapacity) {
        this.MaxCapacity = MaxCapacity;
    }
    public int getMaxCapacity() {
        return MaxCapacity;
    }

    public void setMaxCapacityMB(long MaxCapacityMB) {
        this.MaxCapacityMB = MaxCapacityMB;
    }
    public long getMaxCapacityMB() {
        return MaxCapacityMB;
    }

    public void setMaxCapacityVCores(int MaxCapacityVCores) {
        this.MaxCapacityVCores = MaxCapacityVCores;
    }
    public int getMaxCapacityVCores() {
        return MaxCapacityVCores;
    }

    public void setUsedAMResourceMB(long UsedAMResourceMB) {
        this.UsedAMResourceMB = UsedAMResourceMB;
    }
    public long getUsedAMResourceMB() {
        return UsedAMResourceMB;
    }

    public void setUsedAMResourceVCores(int UsedAMResourceVCores) {
        this.UsedAMResourceVCores = UsedAMResourceVCores;
    }
    public int getUsedAMResourceVCores() {
        return UsedAMResourceVCores;
    }

    public void setUsedCapacity(double UsedCapacity) {
        this.UsedCapacity = UsedCapacity;
    }
    public double getUsedCapacity() {
        return UsedCapacity;
    }

    public void setActiveApplications(int ActiveApplications) {
        this.ActiveApplications = ActiveApplications;
    }
    public int getActiveApplications() {
        return ActiveApplications;
    }

    public void setActiveUsers(int ActiveUsers) {
        this.ActiveUsers = ActiveUsers;
    }
    public int getActiveUsers() {
        return ActiveUsers;
    }

    /*public void setAggregateContainersAllocated(int AggregateContainersAllocated) {
        this.AggregateContainersAllocated = AggregateContainersAllocated;
    }
    public int getAggregateContainersAllocated() {
        return AggregateContainersAllocated;
    }

    public void setAggregateContainersPreempted(int AggregateContainersPreempted) {
        this.AggregateContainersPreempted = AggregateContainersPreempted;
    }
    public int getAggregateContainersPreempted() {
        return AggregateContainersPreempted;
    }

    public void setAggregateContainersReleased(int AggregateContainersReleased) {
        this.AggregateContainersReleased = AggregateContainersReleased;
    }
    public int getAggregateContainersReleased() {
        return AggregateContainersReleased;
    }

    public void setAggregateMemoryMBPreempted(int AggregateMemoryMBPreempted) {
        this.AggregateMemoryMBPreempted = AggregateMemoryMBPreempted;
    }
    public int getAggregateMemoryMBPreempted() {
        return AggregateMemoryMBPreempted;
    }

    public void setAggregateMemoryMBSecondsPreempted(int AggregateMemoryMBSecondsPreempted) {
        this.AggregateMemoryMBSecondsPreempted = AggregateMemoryMBSecondsPreempted;
    }
    public int getAggregateMemoryMBSecondsPreempted() {
        return AggregateMemoryMBSecondsPreempted;
    }

    public void setAggregateNodeLocalContainersAllocated(int AggregateNodeLocalContainersAllocated) {
        this.AggregateNodeLocalContainersAllocated = AggregateNodeLocalContainersAllocated;
    }
    public int getAggregateNodeLocalContainersAllocated() {
        return AggregateNodeLocalContainersAllocated;
    }

    public void setAggregateOffSwitchContainersAllocated(int AggregateOffSwitchContainersAllocated) {
        this.AggregateOffSwitchContainersAllocated = AggregateOffSwitchContainersAllocated;
    }
    public int getAggregateOffSwitchContainersAllocated() {
        return AggregateOffSwitchContainersAllocated;
    }

    public void setAggregateRackLocalContainersAllocated(int AggregateRackLocalContainersAllocated) {
        this.AggregateRackLocalContainersAllocated = AggregateRackLocalContainersAllocated;
    }
    public int getAggregateRackLocalContainersAllocated() {
        return AggregateRackLocalContainersAllocated;
    }

    public void setAggregateVcoreSecondsPreempted(int AggregateVcoreSecondsPreempted) {
        this.AggregateVcoreSecondsPreempted = AggregateVcoreSecondsPreempted;
    }
    public int getAggregateVcoreSecondsPreempted() {
        return AggregateVcoreSecondsPreempted;
    }

    public void setAggregateVcoresPreempted(int AggregateVcoresPreempted) {
        this.AggregateVcoresPreempted = AggregateVcoresPreempted;
    }
    public int getAggregateVcoresPreempted() {
        return AggregateVcoresPreempted;
    }
*/
    public void setAllocatedContainers(int AllocatedContainers) {
        this.AllocatedContainers = AllocatedContainers;
    }
    public int getAllocatedContainers() {
        return AllocatedContainers;
    }

    public void setAllocatedMB(long AllocatedMB) {
        this.AllocatedMB = AllocatedMB;
    }
    public long getAllocatedMB() {
        return AllocatedMB;
    }

    public void setAllocatedVCores(int AllocatedVCores) {
        this.AllocatedVCores = AllocatedVCores;
    }
    public int getAllocatedVCores() {
        return AllocatedVCores;
    }

    public void setAppAttemptFirstContainerAllocationDelayNumOps(int AppAttemptFirstContainerAllocationDelayNumOps) {
        this.AppAttemptFirstContainerAllocationDelayNumOps = AppAttemptFirstContainerAllocationDelayNumOps;
    }
    public int getAppAttemptFirstContainerAllocationDelayNumOps() {
        return AppAttemptFirstContainerAllocationDelayNumOps;
    }

    public void setAppAttemptFirstContainerAllocationDelayAvgTime(int AppAttemptFirstContainerAllocationDelayAvgTime) {
        this.AppAttemptFirstContainerAllocationDelayAvgTime = AppAttemptFirstContainerAllocationDelayAvgTime;
    }
    public int getAppAttemptFirstContainerAllocationDelayAvgTime() {
        return AppAttemptFirstContainerAllocationDelayAvgTime;
    }

    public void setAppsCompleted(int AppsCompleted) {
        this.AppsCompleted = AppsCompleted;
    }
    public int getAppsCompleted() {
        return AppsCompleted;
    }

    public void setAppsFailed(int AppsFailed) {
        this.AppsFailed = AppsFailed;
    }
    public int getAppsFailed() {
        return AppsFailed;
    }

    public void setAppsKilled(int AppsKilled) {
        this.AppsKilled = AppsKilled;
    }
    public int getAppsKilled() {
        return AppsKilled;
    }

    public void setAppsPending(int AppsPending) {
        this.AppsPending = AppsPending;
    }
    public int getAppsPending() {
        return AppsPending;
    }

    public void setAppsRunning(int AppsRunning) {
        this.AppsRunning = AppsRunning;
    }
    public int getAppsRunning() {
        return AppsRunning;
    }

    public void setAppsSubmitted(int AppsSubmitted) {
        this.AppsSubmitted = AppsSubmitted;
    }
    public int getAppsSubmitted() {
        return AppsSubmitted;
    }

    public void setAvailableMB(long AvailableMB) {
        this.AvailableMB = AvailableMB;
    }
    public long getAvailableMB() {
        return AvailableMB;
    }

    public void setAvailableVCores(int AvailableVCores) {
        this.AvailableVCores = AvailableVCores;
    }
    public int getAvailableVCores() {
        return AvailableVCores;
    }

    public void setPendingContainers(int PendingContainers) {
        this.PendingContainers = PendingContainers;
    }
    public int getPendingContainers() {
        return PendingContainers;
    }

    public void setPendingMB(long PendingMB) {
        this.PendingMB = PendingMB;
    }
    public long getPendingMB() {
        return PendingMB;
    }

    public void setPendingVCores(int PendingVCores) {
        this.PendingVCores = PendingVCores;
    }
    public int getPendingVCores() {
        return PendingVCores;
    }

    public void setReservedContainers(int ReservedContainers) {
        this.ReservedContainers = ReservedContainers;
    }
    public int getReservedContainers() {
        return ReservedContainers;
    }

    public void setReservedMB(long ReservedMB) {
        this.ReservedMB = ReservedMB;
    }
    public long getReservedMB() {
        return ReservedMB;
    }

    public void setReservedVCores(int ReservedVCores) {
        this.ReservedVCores = ReservedVCores;
    }
    public int getReservedVCores() {
        return ReservedVCores;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterId() {
        return clusterId;
    }
}
