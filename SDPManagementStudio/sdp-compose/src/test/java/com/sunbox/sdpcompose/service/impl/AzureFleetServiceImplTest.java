package com.sunbox.sdpcompose.service.impl;/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */

import cn.hutool.json.JSONUtil;
import com.sunbox.domain.metaData.VMSku;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author wangda
 * @date 2024/12/19
 */
public class AzureFleetServiceImplTest {
    private AzureFleetServiceImpl azureFleetService = new AzureFleetServiceImpl();

    private static List<VMSku> allSkus;


    @BeforeAll
    public static void setUp() {
        allSkus = JSONUtil.toList(skuStr, VMSku.class);
    }

    @Test
    public void getSameSpecVmSku_SameFamilyAndSpecs_ReturnsMatchingSkus() {
        String skuName = "Standard_E8s_v5";
        List<VMSku> result = azureFleetService.getSameSpecVmSku(skuName, allSkus, 2);

        assertEquals(2, result.size());
    }

    @Test
    public void getSameSpecVmSku_SameFamilyDifferentSpecs_ReturnsLargerCpuSkus() {
        String skuName = "Standard_E32as_v5";
        List<VMSku> result = azureFleetService.getSameSpecVmSku(skuName, allSkus, 3);

        assertEquals(1, result.size());
        assertEquals("Standard_E32s_v5", result.get(0).getName());
    }

    @Test
    public void getSameSpecVmSku_LimitedCount_ReturnsLimitedSkus() {
        String skuName = "Standard_E16s_v5";
        List<VMSku> result = azureFleetService.getSameSpecVmSku(skuName, allSkus, 2);

        assertEquals(2, result.size());
        assertEquals("Standard_E16as_v5", result.get(0).getName());
        assertEquals("Standard_E32s_v5", result.get(1).getName());
    }



    private static String skuStr = "[\n" +
            "    {\"tempSSDStorageGB\":\"0\",\"cpuType\":\"INTEL\",\"regionName\":\"美东2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"16\",\"name\":\"Standard_D16s_v5\",\"maxDataDisksCount\":\"32\",\"id\":\"\",\"memoryGB\":\"64\",\"region\":\"eastus2\",\"family\":\"standardDSv5Family\",\"tempNVMeDisksCount\":\"0\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"INTEL\",\"regionName\":\"美东2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"4\",\"name\":\"Standard_E4d_v5\",\"maxDataDisksCount\":\"8\",\"id\":\"\",\"memoryGB\":\"32\",\"region\":\"eastus2\",\"family\":\"standardEDv5Family\",\"tempNVMeDisksCount\":\"0\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"INTEL\",\"regionName\":\"美东2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"8\",\"name\":\"Standard_E8d_v5\",\"maxDataDisksCount\":\"16\",\"id\":\"\",\"memoryGB\":\"64\",\"region\":\"eastus2\",\"family\":\"standardEDv5Family\",\"tempNVMeDisksCount\":\"0\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"AMD64\",\"regionName\":\"美东2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"4\",\"name\":\"Standard_D4as_v5\",\"maxDataDisksCount\":\"8\",\"id\":\"\",\"memoryGB\":\"16\",\"region\":\"eastus2\",\"family\":\"standardDASv5Family\",\"tempNVMeDisksCount\":\"0\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"INTEL\",\"regionName\":\"美东2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"32\",\"subscriptionName\":\"SHEIN migration testing\",\"name\":\"Standard_E32s_v5\",\"maxDataDisksCount\":\"32\",\"id\":\"\",\"memoryGB\":\"256\",\"region\":\"eastus2\",\"family\":\"standardESv5Family\",\"tempNVMeDisksCount\":\"0\",\"subscriptionId\":\"bba32ad2-4ac4-4bc3-8c34-ad8b2475d857\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"INTEL\",\"regionName\":\"美东2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"16\",\"subscriptionName\":\"SHEIN migration testing\",\"name\":\"Standard_E16s_v5\",\"maxDataDisksCount\":\"32\",\"id\":\"\",\"memoryGB\":\"128\",\"region\":\"eastus2\",\"family\":\"standardESv5Family\",\"tempNVMeDisksCount\":\"0\",\"subscriptionId\":\"bba32ad2-4ac4-4bc3-8c34-ad8b2475d857\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"INTEL\",\"regionName\":\"美东2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"4\",\"subscriptionName\":\"SHEIN migration testing\",\"name\":\"Standard_E4s_v5\",\"maxDataDisksCount\":\"8\",\"id\":\"\",\"memoryGB\":\"32\",\"region\":\"eastus2\",\"family\":\"standardESv5Family\",\"tempNVMeDisksCount\":\"0\",\"subscriptionId\":\"bba32ad2-4ac4-4bc3-8c34-ad8b2475d857\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"INTEL\",\"regionName\":\"美东2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"8\",\"subscriptionName\":\"SHEIN migration testing\",\"name\":\"Standard_E8s_v5\",\"maxDataDisksCount\":\"16\",\"id\":\"\",\"memoryGB\":\"64\",\"region\":\"eastus2\",\"family\":\"standardESv5Family\",\"tempNVMeDisksCount\":\"0\",\"subscriptionId\":\"bba32ad2-4ac4-4bc3-8c34-ad8b2475d857\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"INTEL\",\"regionName\":\"美东2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"2\",\"subscriptionName\":\"SHEIN migration testing\",\"name\":\"Standard_E2s_v5\",\"maxDataDisksCount\":\"4\",\"id\":\"\",\"memoryGB\":\"16\",\"region\":\"eastus2\",\"family\":\"standardESv5Family\",\"tempNVMeDisksCount\":\"0\",\"subscriptionId\":\"bba32ad2-4ac4-4bc3-8c34-ad8b2475d857\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"INTEL\",\"regionName\":\"美东2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"16\",\"subscriptionName\":\"SHEIN migration testing\",\"name\":\"Standard_D16ds_v5\",\"maxDataDisksCount\":\"32\",\"id\":\"\",\"memoryGB\":\"64\",\"region\":\"eastus2\",\"family\":\"standardDDSv5Family\",\"tempNVMeDisksCount\":\"0\",\"subscriptionId\":\"bba32ad2-4ac4-4bc3-8c34-ad8b2475d857\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"INTEL\",\"regionName\":\"美东2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"16\",\"subscriptionName\":\"SHEIN migration testing\",\"name\":\"Standard_D16s_v5\",\"maxDataDisksCount\":\"32\",\"id\":\"\",\"memoryGB\":\"64\",\"region\":\"eastus2\",\"family\":\"standardDSv5Family\",\"tempNVMeDisksCount\":\"0\",\"subscriptionId\":\"bba32ad2-4ac4-4bc3-8c34-ad8b2475d857\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"INTEL\",\"regionName\":\"美东2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"4\",\"subscriptionName\":\"SHEIN migration testing\",\"name\":\"Standard_D4s_v5\",\"maxDataDisksCount\":\"8\",\"id\":\"\",\"memoryGB\":\"16\",\"region\":\"eastus2\",\"family\":\"standardDSv5Family\",\"tempNVMeDisksCount\":\"0\",\"subscriptionId\":\"bba32ad2-4ac4-4bc3-8c34-ad8b2475d857\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"AMD64\",\"regionName\":\"美东2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"8\",\"subscriptionName\":\"SHEIN migration testing\",\"name\":\"Standard_D8as_v4\",\"maxDataDisksCount\":\"16\",\"id\":\"\",\"memoryGB\":\"32\",\"region\":\"eastus2\",\"family\":\"standardDASv4Family\",\"tempNVMeDisksCount\":\"0\",\"subscriptionId\":\"bba32ad2-4ac4-4bc3-8c34-ad8b2475d857\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"AMD64\",\"regionName\":\"美东2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"16\",\"subscriptionName\":\"SHEIN migration testing\",\"name\":\"Standard_D16as_v4\",\"maxDataDisksCount\":\"32\",\"id\":\"\",\"memoryGB\":\"64\",\"region\":\"eastus2\",\"family\":\"standardDASv4Family\",\"tempNVMeDisksCount\":\"0\",\"subscriptionId\":\"bba32ad2-4ac4-4bc3-8c34-ad8b2475d857\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"AMD64\",\"regionName\":\"美东2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"4\",\"subscriptionName\":\"SHEIN migration testing\",\"name\":\"Standard_D4as_v4\",\"maxDataDisksCount\":\"8\",\"id\":\"\",\"memoryGB\":\"16\",\"region\":\"eastus2\",\"family\":\"standardDASv4Family\",\"tempNVMeDisksCount\":\"0\",\"subscriptionId\":\"bba32ad2-4ac4-4bc3-8c34-ad8b2475d857\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"AMD64\",\"regionName\":\"美东2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"4\",\"subscriptionName\":\"SHEIN migration testing\",\"name\":\"Standard_E4ads_v5\",\"maxDataDisksCount\":\"8\",\"id\":\"\",\"memoryGB\":\"32\",\"region\":\"eastus2\",\"family\":\"standardEADSv5Family\",\"tempNVMeDisksCount\":\"0\",\"subscriptionId\":\"bba32ad2-4ac4-4bc3-8c34-ad8b2475d857\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"INTEL\",\"regionName\":\"美东2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"4\",\"subscriptionName\":\"SHEIN migration testing\",\"name\":\"Standard_E4ds_v5\",\"maxDataDisksCount\":\"8\",\"id\":\"\",\"memoryGB\":\"32\",\"region\":\"eastus2\",\"family\":\"standardEDSv5Family\",\"tempNVMeDisksCount\":\"0\",\"subscriptionId\":\"bba32ad2-4ac4-4bc3-8c34-ad8b2475d857\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"INTEL\",\"regionName\":\"美西二\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"4\",\"name\":\"Standard_D4_v5\",\"maxDataDisksCount\":\"8\",\"id\":\"\",\"memoryGB\":\"16\",\"region\":\"westus2\",\"family\":\"standardDv5Family\",\"tempNVMeDisksCount\":\"0\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"INTEL\",\"regionName\":\"美西二\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"4\",\"name\":\"Standard_E4-2s_v5\",\"maxDataDisksCount\":\"8\",\"id\":\"\",\"memoryGB\":\"32\",\"region\":\"westus2\",\"family\":\"standardESv5Family\",\"tempNVMeDisksCount\":\"0\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"INTEL\",\"regionName\":\"美西二\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"4\",\"name\":\"Standard_E4s_v5\",\"maxDataDisksCount\":\"8\",\"id\":\"\",\"memoryGB\":\"32\",\"region\":\"westus2\",\"family\":\"standardESv5Family\",\"tempNVMeDisksCount\":\"0\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"INTEL\",\"regionName\":\"美西二\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"4\",\"name\":\"Standard_E4d_v5\",\"maxDataDisksCount\":\"8\",\"id\":\"\",\"memoryGB\":\"32\",\"region\":\"westus2\",\"family\":\"standardEDv5Family\",\"tempNVMeDisksCount\":\"0\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"INTEL\",\"regionName\":\"美西2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"8\",\"name\":\"Standard_E8ds_v5\",\"maxDataDisksCount\":\"16\",\"id\":\"\",\"memoryGB\":\"64\",\"region\":\"westus2\",\"family\":\"standardEDSv5Family\",\"tempNVMeDisksCount\":\"0\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"AMD64\",\"regionName\":\"美西2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"4\",\"name\":\"Standard_D4a_v4\",\"maxDataDisksCount\":\"8\",\"id\":\"\",\"memoryGB\":\"16\",\"region\":\"westus2\",\"family\":\"standardDAv4Family\",\"tempNVMeDisksCount\":\"0\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"AMD64\",\"regionName\":\"美西2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"16\",\"name\":\"Standard_D16a_v4\",\"maxDataDisksCount\":\"32\",\"id\":\"\",\"memoryGB\":\"64\",\"region\":\"westus2\",\"family\":\"standardDAv4Family\",\"tempNVMeDisksCount\":\"0\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"AMD64\",\"regionName\":\"美西2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"32\",\"name\":\"Standard_D32a_v4\",\"maxDataDisksCount\":\"32\",\"id\":\"\",\"memoryGB\":\"128\",\"region\":\"westus2\",\"family\":\"standardDAv4Family\",\"tempNVMeDisksCount\":\"0\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"AMD64\",\"regionName\":\"美西2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"16\",\"name\":\"Standard_D16as_v5\",\"maxDataDisksCount\":\"32\",\"id\":\"\",\"memoryGB\":\"64\",\"region\":\"westus2\",\"family\":\"standardDASv5Family\",\"tempNVMeDisksCount\":\"0\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"AMD64\",\"regionName\":\"美西2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"4\",\"name\":\"Standard_D4as_v5\",\"maxDataDisksCount\":\"8\",\"id\":\"\",\"memoryGB\":\"16\",\"region\":\"westus2\",\"family\":\"standardDASv5Family\",\"tempNVMeDisksCount\":\"0\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"AMD64\",\"regionName\":\"美西2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"8\",\"name\":\"Standard_D8as_v5\",\"maxDataDisksCount\":\"16\",\"id\":\"\",\"memoryGB\":\"32\",\"region\":\"westus2\",\"family\":\"standardDASv5Family\",\"tempNVMeDisksCount\":\"0\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"AMD64\",\"regionName\":\"美西2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"8\",\"name\":\"Standard_E8-4as_v5\",\"maxDataDisksCount\":\"16\",\"id\":\"\",\"memoryGB\":\"64\",\"region\":\"westus2\",\"family\":\"standardEASv5Family\",\"tempNVMeDisksCount\":\"0\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"AMD64\",\"regionName\":\"美西2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"4\",\"name\":\"Standard_E4a_v4\",\"maxDataDisksCount\":\"8\",\"id\":\"\",\"memoryGB\":\"32\",\"region\":\"westus2\",\"family\":\"standardEAv4Family\",\"tempNVMeDisksCount\":\"0\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"AMD64\",\"regionName\":\"美西2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"8\",\"name\":\"Standard_E8a_v4\",\"maxDataDisksCount\":\"16\",\"id\":\"\",\"memoryGB\":\"64\",\"region\":\"westus2\",\"family\":\"standardEAv4Family\",\"tempNVMeDisksCount\":\"0\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"AMD64\",\"regionName\":\"美西2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"16\",\"name\":\"Standard_E16a_v4\",\"maxDataDisksCount\":\"32\",\"id\":\"\",\"memoryGB\":\"128\",\"region\":\"westus2\",\"family\":\"standardEAv4Family\",\"tempNVMeDisksCount\":\"0\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"AMD64\",\"regionName\":\"美西2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"16\",\"name\":\"Standard_E16as_v5\",\"maxDataDisksCount\":\"32\",\"id\":\"\",\"memoryGB\":\"128\",\"region\":\"westus2\",\"family\":\"standardEASv5Family\",\"tempNVMeDisksCount\":\"0\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"INTEL\",\"regionName\":\"美西2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"1\",\"subscriptionName\":\"SHEIN migration testing\",\"name\":\"Basic_A0\",\"maxDataDisksCount\":\"1\",\"id\":\"\",\"memoryGB\":\"0.75\",\"region\":\"westus2\",\"family\":\"basicAFamily\",\"tempNVMeDisksCount\":\"0\",\"subscriptionId\":\"bba32ad2-4ac4-4bc3-8c34-ad8b2475d857\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            ",{\"tempSSDStorageGB\":\"0\",\"cpuType\":\"AMD64\",\"regionName\":\"美西2\",\"type\":\"SupportedVMSkuList\",\"tempNVMeStorageGB\":\"0\",\"vCoreCount\":\"4\",\"subscriptionName\":\"SHEIN migration testing\",\"name\":\"Standard_D4as_v4\",\"maxDataDisksCount\":\"8\",\"id\":\"\",\"memoryGB\":\"16\",\"region\":\"westus2\",\"family\":\"standardDASv4Family\",\"tempNVMeDisksCount\":\"0\",\"subscriptionId\":\"bba32ad2-4ac4-4bc3-8c34-ad8b2475d857\",\"tempNVMeDiskSizeGB\":\"0\"}\n" +
            "]";
}