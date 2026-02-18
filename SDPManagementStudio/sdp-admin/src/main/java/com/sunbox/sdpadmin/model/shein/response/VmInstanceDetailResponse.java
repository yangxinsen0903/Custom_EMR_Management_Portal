package com.sunbox.sdpadmin.model.shein.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class VmInstanceDetailResponse {
    private String region;

    private String clusterId;

    private String clusterName;

    private String vmName;

    private String hostName;

    private String ip;

    private String vmRole;

    private String groupName;

    private String skuName;

    private Integer purchaseType;

    private String diskType;

    private Integer diskSize;

    private Integer state;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
