package com.sunbox.sdpadmin.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.gson.JsonArray;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.azure.AzureVmtraceInfoRequest;
import com.sunbox.sdpadmin.controller.annotation.PermissionLimit;
import com.sunbox.sdpadmin.service.IComposeMetaService;
import com.sunbox.service.IAzureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 *  Azure元数据
 */
@RestController
@RequestMapping("/admin/api/azure/metas")
public class IAzureController {

    @Autowired
    private IComposeMetaService composeMetaService;
    @Autowired
    private IAzureService azureService;

    /**
     * 查询可用区域数据
     *
     * @return
     */
    @GetMapping("/supportedAvailabilityZoneList")
    @PermissionLimit(role = {"Administrator"})
    public ResultMsg supportedAvailabilityZoneList(@RequestParam String region) {
        return composeMetaService.getAzList(region);
    }

    /**
     * 查询磁盘Sku列表数据
     *
     * @return
     */
    @GetMapping("/supportedDiskSkuList")
    @PermissionLimit(role = {"Administrator"})
    public ResultMsg supportedDiskSkuList(@RequestParam String region) {
        return composeMetaService.getDiskSku(region);
    }

    /**
     * 查询磁盘keyVault列表数据
     *
     * @return
     */
    @GetMapping("/supportedKVList")
    @PermissionLimit(role = {"Administrator"})
    public ResultMsg supportedKVList(@RequestParam String region,@RequestParam String subscriptionId) {
        return composeMetaService.getKeyVaultList(region,subscriptionId);
    }

    /**
     * 查询SSH密钥对列表数据根据id
     *
     * @return
     */
    @GetMapping("/getSSHKeyPairById")
    @PermissionLimit(role = {"Administrator"})
    public ResultMsg getSSHKeyPairById(@RequestParam String kvId, @RequestParam String region,@RequestParam String subscriptionId) {
        return composeMetaService.getSSHKeyPairById(kvId,region,subscriptionId);
    }

    /**
     * 查询存储帐户列表
     *
     * @return
     */
    @GetMapping("/getStorageAccountList")
    @PermissionLimit(role = {"Administrator"})
    public ResultMsg getStorageAccountList(@RequestParam String region,@RequestParam String subscriptionId) {
        return composeMetaService.getStorageAccountList(region,subscriptionId);
    }

    /**
     * 查询日志桶元数据根据id
     *
     * @return
     */
    @GetMapping("/getLogsBlobContainerListById")
    @PermissionLimit(role = {"Administrator"})
    public ResultMsg getLogsBlobContainerListById(@RequestParam String saId, @RequestParam String region,@RequestParam String subscriptionId) {
        return composeMetaService.getLogsBlobContainerListById(saId,region,subscriptionId);
    }

    /**
     * 查询MI列表
     *
     * @return
     */
    @GetMapping("/getManagedIdentityList")
    @PermissionLimit(role = {"Administrator"})
    public ResultMsg getManagedIdentityList(@RequestParam String region,@RequestParam String subscriptionId) {
        return composeMetaService.getMIList(region,subscriptionId);
    }

    /**
     * 查询虚拟网络列表
     *
     * @return
     */
    @GetMapping("/getNetworkList")
    @PermissionLimit(role = {"Administrator"})
    public ResultMsg getNetworkList(@RequestParam String region) {
        return composeMetaService.getNetworkList(region);
    }

    /**
     * 根据id获取子网列表
     *
     * @param vnetId
     * @return
     */
    @GetMapping("/getSubnetListById")
    @PermissionLimit(role = {"Administrator"})
    public ResultMsg getSubnetListById(@RequestParam String vnetId, @RequestParam String region) {
        return composeMetaService.getSubnetListById(vnetId,region);
    }

    /**
     * 查询安全组列表
     *
     * @return
     */
    @GetMapping("/getNSGSkuList")
    @PermissionLimit(role = {"Administrator"})
    public ResultMsg getNSGSkuList(@RequestParam String region) {
        return composeMetaService.getNSGSku(region);
    }

    /**
     * 查询数据中心列表
     *
     * @return
     */
    @GetMapping("/getRegionList")
    @PermissionLimit(role = {"Administrator"})
    public ResultMsg getRegionList(@RequestParam String subscriptionId) {
        return composeMetaService.getRegionList(subscriptionId);
    }


    /**
     * 查询机型sku
     *
     * @return
     */
    @GetMapping("/getVMSkuList")
    @PermissionLimit(role = {"Administrator"})
    public ResultMsg getVMSkuList(@RequestParam String region ) {
        return composeMetaService.geVmSkus(region);
    }

    /**
     * 查询订阅列表
     *
     * @return
     */
    @GetMapping("/listSubscription")
    @PermissionLimit(role = {"Administrator"})
    public ResultMsg listSubscription() {
       return composeMetaService.listSubscription();
    }



}
