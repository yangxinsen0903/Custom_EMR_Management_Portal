package com.sunbox.sdpadmin.model.shein.response;

import lombok.Data;

/**
 * @Description TODO
 * @Author shishicheng
 * @Date 2023/4/3 19:19
 */
@Data
public class ScalingConstraint {

    // 实例组最大实例数
    private Integer maxCnt;

    // 实例组最小实例数
    private Integer minCnt;

    private Integer isGracefulScalein;
    private Integer scaleinWaitingTime;
    private Integer enableBeforestartScript;
    private Integer enableAfterstartScript;
    private Integer isFullCustody;
}
