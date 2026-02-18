package com.sunbox.service;

import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.BaseUserInfo;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.metaData.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface IMetaDataItemService {

    ResultMsg insertMetaData(JSONObject itemRequest, BaseUserInfo userInfo);

    ResultMsg updateMetaData(JSONObject itemRequest,BaseUserInfo userInfo);

    ResultMsg selectMetaDataList(JSONObject itemRequest);

    ResultMsg deleteMetaDataById(Long id);



    Map<String, VMSku> getVmSkuMap(String region);

    /**
     * 获取region信息 ,region和regionName
     * @return
     */
    Map<String, String> getRegionMap();

    List<String> listRegion();

    Region getRegion(String region);

    /**
     * 获取可用区
     * @param region
     * @param zoneNumber
     * @return
     */
     AvailabilityZone getAZ(String region,String zoneNumber);

    /**
     * 根据物理Zone的序号，获取物理Zone信息
     * @param region
     * @param zoneNumber 物理Zone编号
     * @return
     */
    AvailabilityZone getAZByPhysicalZone(String region,String zoneNumber);

    /**
     * 获取子网
     * @param region
     * @param subnetId
     * @return
     */
    Subnet getSubnet(String region,String subnetId);
    /**
     * 获取安全组
     * @param region
     * @param resourceId
     * @return
     */
    NSGSku getNSGSku(String region,String resourceId);

    /**
     * 获取密钥对
     * @param region
     * @param secretResourceId
     * @return
     */
    SSHKeyPair getSSHKeyPair(String region, String secretResourceId);

    /**
     * 获取托管标识
     * @param region
     * @param resourceId
     * @return
     */
    ManagedIdentity getMI(String region,String resourceId);

    /**
     * 获取日志桶
     * @param region
     * @param blobContainerUrl
     * @return
     */
    LogsBlobContainer getLogsBlobContainer(String region,String blobContainerUrl);
    /**
     * 获取日志桶
     * @param region
     * @param blobContainerUrl
     * @return
     */
    LogsBlobContainer getLogsBlobContainerByUrl(String region,String blobContainerUrl);

    /**
     * 获取磁盘
     * @param region
     * @param skuName
     * @return
     */
    DiskSku getDiskSku(String region,String skuName);

    /**
     * 获取机型
     * @param region
     * @param name
     * @return
     */
    VMSku getVMSKU(String region,String name );


    //region 下拉选择列表
    /**
     * 获取VM规格列表 带推荐字段
     * @return
     */
    List<VMSku> getVMSKURecommend(String region);
    /**
     * 获取VM规格列表
     * @return
     */
    List<VMSku> getVMSKU(String region);
    /**
     * 获取VM规格列表,根据skuNameList查询
     * @return
     */
    List<VMSku> getVMSKU(String region,List<String> skuNameList);

    /**
     * 获取子网列表
     * @return
     */
    List<Subnet> getsubnetlist(String region);

    /**
     * 获取SupportedSubnetList子网列表,所有region
     * @return
     */
    List<Subnet> getsubnetlist();

    /**
     * 获取日志BLob列表
     * @return
     */
    List<LogsBlobContainer> getLogsBlobContainerList(String region);

    /**
     * 获取MI列表
     * @return
     */
    List<ManagedIdentity>  getMIList(String region);

    /**
     * 获取操作系统磁盘类型列表
     * @return
     */
    List<DiskSku> getosdisktypelist(String region);

    /**
     * 获取主安全组列表
     * @return
     */
    List<NSGSku> getPrimaryNSGList(String region);

    /**
     * 获取密钥列表
     * @return
     */
    List<SSHKeyPair> getkeypairlist(String region);

    /**
     * 获取可用区列表
     * @return
     */
    List<AvailabilityZone> getazlist(String region);

    /**
     * 获取数据中心
     * @return
     */
    List<Region> getRegionList();
    //endregion

    /**
     * 获取订阅id
     * @return
     */
    String getSubscriptionId(String region) ;
    /**
     * 获取keyVault
     * @return
     */
    keyVault getkeyVault(String region) ;

    /**
     * 获取日志桶
     * @return
     */
    LogsBlobContainer getLogsBlobContainer(String container);

    /**
     * 获取符合规格的vm sku并去重复,如果有重复的,只取第一个
     * @param region
     * @param coreCount
     * @param memoryGB
     * @return
     */
    List<VMSku> listVmSkuDistinct(String region, Integer coreCount, BigDecimal memoryGB);

    /**
     * 根据skuName列表获取vm sku并去重复,如果有重复的,只取第一个
     * @param region
     * @param skuNameList
     * @return
     */
    List<VMSku> listVmSkuDistinct(String region, List<String> skuNameList);

    /**
     * 获取密钥对 根据region,keyType(1公钥 2私钥)
     * @param region
     * @param keyType
     * @return
     */
    SSHKeyPair getSSHKeyPair(String region, Integer keyType);
}
