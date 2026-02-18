package com.sunbox.sdpadmin.model.admin.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;

@Data
public class ClusterScalingLogData {

    // 伸缩任务ID
    private String taskId;

    // 集群ID
    @NotEmpty
    private String clusterId;

    // 伸缩实例组角色
    private String vmRole;

    // 伸缩实例组名称
    @NotEmpty
    private String groupName;


    // 规则名称
    private String esRuleName;

    // 伸缩类型（1扩容，2缩容）
    private Integer scalingType;
    // 伸缩类型（1扩容，2缩容）可以传多个
    private List<Integer> scalingTypes;

    // 伸缩规模
    private Integer scalingCount;

    // 任务状态
    private Integer state;

    // 伸缩前规模
    //如果scaling_type=4(磁盘调整)时,此字段代表磁盘iops
    private Integer beforeScalingCount;

    // 伸缩后规模
    //如果scaling_type=4(磁盘调整)时,此字段代表磁盘吞吐量
    private Integer afterScalingCount;

    // 是否执行集群启动前脚本（1：执行，0：不执行）
    private Integer enableBeforestartScript;

    // 是否执行集群启动后脚本（1：执行，0：不执行）
    private Integer enableAfterstartScript;

    // vm默认用户名
    private String defaultUsername;

    // 开始时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date begTime;

    // 结束时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    private Integer pageIndex;

    private Integer pageSize;
    /**
     * 操作方式
     */
    private String operatiionType;
    /**
     * 是否在等待队列中 1-等待、0-没有等待
     */
    private Integer inQueue;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    private Integer logFlag;
    private Integer osVolumeSize;
    private Integer osCount;
    private Integer dataVolumeSize;
    private Integer dataCount;

    private String remark;

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getOperatiionType() {
        return operatiionType;
    }

    public void setOperatiionType(String operatiionType) {
        this.operatiionType = operatiionType;
    }

    public Integer getInQueue() {
        return inQueue;
    }

    public void setInQueue(Integer inQueue) {
        this.inQueue = inQueue;
    }

    public Integer getLogFlag() {
        return logFlag;
    }

    public void setLogFlag(Integer logFlag) {
        this.logFlag = logFlag;
    }

    public void setOsVolumeSize(Integer osVolumeSize) {
        this.osVolumeSize = osVolumeSize;
    }

    public Integer getOsVolumeSize() {
        return osVolumeSize;
    }

    public void setOsCount(Integer osCount) {
        this.osCount = osCount;
    }

    public Integer getOsCount() {
        return osCount;
    }

    public void setDataVolumeSize(Integer dataVolumeSize) {
        this.dataVolumeSize = dataVolumeSize;
    }

    public Integer getDataVolumeSize() {
        return dataVolumeSize;
    }

    public void setDataCount(Integer dataCount) {
        this.dataCount = dataCount;
    }

    public Integer getDataCount() {
        return dataCount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
