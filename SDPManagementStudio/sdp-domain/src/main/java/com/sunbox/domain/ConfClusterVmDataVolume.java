package com.sunbox.domain;

import cn.hutool.core.util.StrUtil;
import com.sunbox.domain.enums.DataVolumeType;
import lombok.Data;

/**
    * 集群实例数据盘配置; 1000
    */
@Data
public class ConfClusterVmDataVolume {
    /**
    * 磁盘配置ID
    */
    private String volumeConfId;

    /**
    * 实例配置ID
    */
    private String vmConfId;

    /**
    * 云数据盘类型
    */
    private String dataVolumeType;

    /**
    * 数据盘大小（GB）
    */
    private Integer dataVolumeSize;

    /**
    * 购买磁盘数量
    */
    private Integer count;

    /**
     * 本地数据盘类型
     */
    private String localVolumeType;

    /**
     * Pv2磁盘的IOPS
     */
    private Integer iops;
    /**
     * 吞吐量,单位(M)
     */
    private Integer throughput;


    /**
     * 数据盘是否使用了PV2磁盘
     * @return
     */
    public boolean isPv2DataVolume() {
        // PV2磁盘的Sku是：PremiumV2_LRS
        return StrUtil.equalsIgnoreCase(DataVolumeType.PremiumV2_LRS.name(), dataVolumeType);
    }

    /**
     * 磁盘iops
     * @return
     */
    public int getBaseIOPS() {
        if (isPv2DataVolume()){
            return 3000;
        }
        return 0;
    }

    /**
     * 磁盘throughput(MB)
     * @return
     */
    public int getBaseThroughput() {
        if (isPv2DataVolume()){
            return 125;
        }
        return 0;
    }
}