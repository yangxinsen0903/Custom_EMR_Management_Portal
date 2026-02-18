package com.sunbox.sdpadmin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.enums.CpuType;
import com.sunbox.sdpadmin.service.IComposeMetaService;
import com.sunbox.service.IAzureService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : [niyang]
 * @className : ComposeMetaServceImpl
 * @description : 调用Azure接口
 * @createTime : [2023/7/20 2:30 PM]
 */
@Service
public class ComposeMetaServiceImpl implements IComposeMetaService {

    @Autowired
    private IAzureService azureService;

    @Override
    public ResultMsg getMIList(String region,String subscriptionId) {
        return azureService.getMIList(region,subscriptionId);
    }

    @Override
    public ResultMsg geVmSkus(String region) {
        ResultMsg resultMsg = azureService.getVmSkus(region);
        if (resultMsg.getResult() && resultMsg.getData() != null
                && StringUtils.isEmpty(resultMsg.getErrorMsg())) {
            JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(resultMsg.getData()));
            List<JSONObject> vmSkus = new ArrayList<>();
            if (null != jsonArray && !jsonArray.isEmpty()) {
                for (Object obj : jsonArray) {
                    JSONObject jsonObject = JSONObject.parseObject(obj.toString());
                    JSONObject params = new JSONObject();
                    params.put("name", jsonObject.containsKey("name") ? jsonObject.getString("name") : "");
                    params.put("family", jsonObject.containsKey("family") ? jsonObject.getString("family") : "");
                    params.put("vCoreCount", jsonObject.containsKey("vCPUs") ? jsonObject.getString("vCPUs") : "0");
                    params.put("memoryGB", jsonObject.containsKey("MemoryGB") ? jsonObject.getString("MemoryGB") : "0");
                    params.put("maxDataDisksCount", jsonObject.containsKey("MaxDataDiskCount") ? jsonObject.getString("MaxDataDiskCount") : "0");
                    // 检查是否包含a字段, 如果包含a, 是AMD,如果不包含a, 是Intel
                    String name = jsonObject.getString("name");
                    if (StrUtil.isNotEmpty(name)) {
                        String[] strings = name.split("_");
                        boolean b = strings.length > 1;
                        if (b) {
                            if (strings[1].contains("a")) {
                                params.put("cpuType", CpuType.AMD64.getCode());
                            } else {
                                params.put("cpuType", CpuType.INTEL.getCode());
                            }
                        }
                    }
                    params.put("tempSSDStorageGB", jsonObject.containsKey("tempNVMeDiskSizeGB") ? jsonObject.getString("tempNVMeDiskSizeGB") : "0");
                    params.put("tempNVMeStorageGB", jsonObject.containsKey("tempNVMeDisksCount") ? jsonObject.getString("tempNVMeDisksCount") : "0");
                    params.put("tempNVMeDisksCount", jsonObject.containsKey("tempNVMeStorageGB") ? jsonObject.getString("tempNVMeStorageGB") : "0");
                    params.put("tempNVMeDiskSizeGB", jsonObject.containsKey("tempNVMeDiskSizeGB") ? jsonObject.getString("tempNVMeDiskSizeGB") : "0");
                    vmSkus.add(params);
                }
            }
            resultMsg.setData(vmSkus);
        }
        return resultMsg;
    }

    @Override
    public ResultMsg getDiskSku(String region) {
        return azureService.getDiskSku(region);
    }

    @Override
    public ResultMsg getSSHKeyPair(String region) {
        return azureService.getSSHKeyPair(region);
    }

    @Override
    public ResultMsg getNSGSku(String region) {
        return azureService.getNSGSku(region);
    }

    @Override
    public ResultMsg getSubnet(String region) {
        return azureService.getSubnet(region);

    }

    @Override
    public ResultMsg getAzList(String region) {
        return azureService.getAzList(region);
    }

    @Override
    public ResultMsg getBolbPath() {
        return azureService.getBolbPath();
    }

    @Override
    public ResultMsg getInstancePriceList(List<String> skuNames, String region) {
        if (CollUtil.isEmpty(skuNames)) {
            return ResultMsg.FAILURE("skuName 最少需要一个");
        }
        if (StrUtil.isEmpty(region)) {
            return ResultMsg.FAILURE("Azure Region不能为空");
        }
        List<JSONObject> prices = azureService.getInstancePriceList(skuNames, region);
        return ResultMsg.SUCCESS(prices);
    }

    @Override
    public ResultMsg getKeyVaultList(String region,String subscriptionId) {
        return azureService.getKeyVaultList(region,subscriptionId);
    }

    @Override
    public ResultMsg getSSHKeyPairById(String kvId, String region,String subscriptionId) {
        return azureService.getSSHKeyPairById(kvId, region,subscriptionId);
    }

    @Override
    public ResultMsg getStorageAccountList(String region, String subscriptionId) {
        return azureService.getStorageAccountList(region, subscriptionId);
    }

    @Override
    public ResultMsg getLogsBlobContainerListById(String saId, String region, String subscriptionId) {
        return azureService.getLogsBlobContainerListById(saId, region,subscriptionId);
    }

    @Override
    public ResultMsg getNetworkList(String region) {
        return azureService.getNetworkList(region);
    }

    @Override
    public ResultMsg getSubnetListById(String vnetId, String region) {
        return azureService.getSubnetListById(vnetId, region);
    }

    @Override
    public ResultMsg getRegionList(String subscriptionId) {
        return azureService.getRegionList(subscriptionId);
    }

    @Override
    public ResultMsg listSubscription() {
        return azureService.listSubscription();
    }
}
