package com.sunbox.sdpadmin.model.admin.response;

import lombok.Data;

import java.util.Date;

/**
 * @Description 三表联查查询结果封装类
 * @Author shishicheng
 * @Date 2023/3/23 16:57
 */
@Data
public class SelectJoinCCAndActivityLogResult {

    private String planId;

    private String clusterId;

    private String clusterName;

    private String operationType;

    private String scalingTaskId;

    private Date begTime;

    private Date endTime;

    private Integer state;

    private Date createdTime;
}
