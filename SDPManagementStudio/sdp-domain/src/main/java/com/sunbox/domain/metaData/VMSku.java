package com.sunbox.domain.metaData;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 机型sku
 */
@Data
public class VMSku implements Serializable {
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
     * 机型名称
     */
    private String name;
    /**
     * 此特定 SKU 的系列
     */
    private String family;
    /**
     * CPU核数
     */
    @JsonProperty("vCoreCount")
    private String vCoreCount;

    /**
     * 内存数(GB)
     */
    private String memoryGB;
    /**
     * 最多支持磁盘数量
     */
    private Integer maxDataDisksCount;
    /**
     * CPU类型
     */
    private String cpuType;

    private String tempSSDStorageGB;
    private String tempNVMeStorageGB;
    private String tempNVMeDisksCount;
    private String tempNVMeDiskSizeGB;


    //region 获取azure_price_history
    /**
     * 竞价实例价格
     */
    private BigDecimal spotUnitPrice;
    /**
     * 按需实例价格
     */
    private BigDecimal ondemandUnitPrice;
    /**
     * 最低驱逐率
     */
    private BigDecimal evictionRateLower;
    /**
     * 最高驱逐率
     */
    private BigDecimal evictionRateUpper;

    //endregion

    /**
     * 订阅id
     */
    private String subscriptionId;
    /**
     * 订阅name
     */
    private String subscriptionName;

    /**
     * 是推荐机型
     */
    private Boolean isRecommend;

    /**
     * 是否是Intel的CPU
     * @return
     */
    public boolean isIntelCpu() {
        return StrUtil.equalsIgnoreCase(cpuType, "INTEL");
    }

    /**
     * 判断是否是同一系列
     * @param skuName
     * @return
     */
    public boolean isSameFamilyBySkuName(String skuName) {
        // VMSku格式说明见Auzre文档: https://learn.microsoft.com/zh-cn/azure/virtual-machines/sizes/overview?tabs=breakdownseries%2Cgeneralsizelist%2Ccomputesizelist%2Cmemorysizelist%2Cstoragesizelist%2Cgpusizelist%2Cfpgasizelist%2Chpcsizelist
        // https://learn.microsoft.com/zh-cn/azure/virtual-machines/vm-naming-conventions?source=recommendations
        // skuName格式: [系列] + [子系列]* + [vCPU 数] + [受约束的 vCPU 数]* + [累加功能] + [加速器类型]* + [版本]
        if (StrUtil.isBlank(skuName)) {
            return false;
        }

        VMSkuObj currentSku = new VMSkuObj();
        currentSku.parse(this.name);

        VMSkuObj targetSku = new VMSkuObj();
        targetSku.parse(skuName);

        return StrUtil.equalsIgnoreCase(currentSku.getFamily(), targetSku.getFamily());
    }
}

