/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.domain.metaData;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.util.List;

/**
 * 根据skuName解析出VMSku对象
 * @author wangda
 * @date 2024/12/19
 */
@Data
public class VMSkuObj {
    private String family = "";
    private Integer vCpu;
    private String features = "";
    // 加速器类型
    private String acceleratorType = "";
    // 版本
    private String version = "";

    // VmSku 对象
    private VMSku vmSku;

    /**
     * 判断是否是AMD的CPU
     *
     * @return
     */
    public boolean isAMDCpu() {
        return StrUtil.containsIgnoreCase(features, "a");
    }

    /**
     * 是否是ARM CPU
     *
     * @return
     */
    public boolean isARMCpu() {
        return StrUtil.containsIgnoreCase(features, "p");
    }

    public boolean isIntelCPU() {
        return !StrUtil.containsIgnoreCase(features, "a") && !StrUtil.containsIgnoreCase(features, "p");
    }

    /**
     * 是否是L系列机型
     *
     * @return
     */
    public boolean isLSerial() {
        return StrUtil.containsIgnoreCase(family, "l");
    }

    /**
     * 是否支持高级存储
     *
     * @return
     */
    public boolean isSupportAdvanceStorage() {
        return StrUtil.containsIgnoreCase(features, "s");
    }

    /**
     * 是否支持加速器(GPU)
     *
     * @return
     */
    public boolean isSupportAccelerator() {
        return StrUtil.isNotBlank(acceleratorType);
    }

    /**
     * 是否是相同的Family
     * @param family
     * @return
     */
    public boolean isSameFamily(String family) {
        return StrUtil.equalsIgnoreCase(family, this.family);
    }

    /**
     * 解析skuName,得到Sku的相关信息
     *
     * @param skuName
     */
    public void parse(String skuName) {
        // 1. 按_拆分
        List<String> split = StrUtil.split(skuName, "_");
        if (split.size() < 2) {
            throw new RuntimeException("SkuName格式不正确:" + skuName);
        }

        String sizeInfo = "";
        if (split.size() == 2) {
            sizeInfo = split.get(1);
        } else if (split.size() == 3) {
            version = split.get(2);
            sizeInfo = split.get(1);
        } else if (split.size() == 4) {
            version = split.get(3);
            sizeInfo = split.get(1);
            acceleratorType = split.get(2);
        }

        // 2. 找到vCore数
        int numStartIndex = 0;
        int numEndIndex = 0;
        for (int i = 0; i < sizeInfo.length(); i++) {
            char c = sizeInfo.charAt(i);
            if (CharUtil.isNumber(c)) {
                numStartIndex = i;
                break;
            }
        }

        for (int i = sizeInfo.length() - 1; i > 0; i--) {
            char c = sizeInfo.charAt(i);
            if (CharUtil.isNumber(c)) {
                numEndIndex = i;
                break;
            }
        }

        // CPU vCore
        String vCoreStr = sizeInfo.substring(numStartIndex, numEndIndex + 1);
        vCpu = parseVCpu(vCoreStr);
        // Family
        family = sizeInfo.substring(0, numStartIndex);
        // feature
        features = sizeInfo.substring(numEndIndex + 1);
    }

    /**
     * 解析CPU的VCore数量<br/>
     * 一共有两种类型:  n-n , 如: 32-2
     * n: 如: 32
     *
     * @param vCpuStr
     * @return
     */
    private Integer parseVCpu(String vCpuStr) {
        if (StrUtil.isBlank(vCpuStr)) {
            return null;
        }
        String[] splitCore = vCpuStr.split("-");
        if (splitCore.length == 1) {
            return Convert.toInt(vCpuStr);
        } else if (splitCore.length > 1) {
            return Convert.toInt(splitCore[0]);
        } else {
            return null;
        }
    }
}
