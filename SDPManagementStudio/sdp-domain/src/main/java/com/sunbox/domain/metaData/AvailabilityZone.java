package com.sunbox.domain.metaData;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *  可用区
 */
@Data
public class AvailabilityZone implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String region;

    private String regionName;

    private String type;

    private String version;

    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 可用区名称
     */
    private String availabilityZone;

    /**
     * 逻辑可用区编号
     */
    private String logicalZone;

    /**
     * 物理可用区
     */
    private String physicalZone;

    /**
     * 物理可用区编号
     */
    private String physicalZoneNo;

    /**
     * 订阅id
     */
    private String subscriptionId;
    /**
     * 订阅name
     */
    private String subscriptionName;


    public void setPhysicalZone(String physicalZone) {
        this.physicalZone = physicalZone;
        this.physicalZoneNo = StrUtil.subSufByLength(physicalZone, 1);
    }

}