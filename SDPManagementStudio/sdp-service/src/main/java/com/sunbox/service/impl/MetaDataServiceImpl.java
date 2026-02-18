package com.sunbox.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.constant.MetaDataConstants;
import com.sunbox.dao.mapper.AzurePriceHistoryMapper;
import com.sunbox.dao.mapper.MetaDataItemMapper;
import com.sunbox.domain.AzurePriceHistory;
import com.sunbox.domain.BaseUserInfo;
import com.sunbox.domain.MetaDataItem;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.enums.MetaDataType;
import com.sunbox.domain.metaData.*;
import com.sunbox.service.IMetaDataItemService;
import com.sunbox.util.DateUtil;
import com.sunbox.util.EnumUtil;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: 元数据增强
 * @Date 2024/6/2
 **/
@Service
public class MetaDataServiceImpl implements IMetaDataItemService, BaseCommonInterFace {


    @Autowired
    private MetaDataItemMapper metaDataItemMapper;

    @Autowired
    private AzurePriceHistoryMapper azurePriceHistoryMapper;

    @Value("${sdp.blob.default.region:''}")
    private String blobRegion;

    /**
     * 新增元数据  字段参考(MetaDataConstants)常量
     *
     * @return
     */
    @Override
    public ResultMsg insertMetaData(JSONObject itemRequest, BaseUserInfo userInfo) {
        String type = itemRequest.getString(MetaDataConstants.TYPE);
        if (StrUtil.isEmpty(type)) {
            return ResultMsg.FAILURE("元数据类型不能为空!");
        }
        Optional<MetaDataType> enumObject = EnumUtil.getEnumObject(MetaDataType.class, metaDataType -> type.equals(metaDataType.getCode()));
        if (!enumObject.isPresent()) {
            return ResultMsg.FAILURE("元数据类型不存在!");
        }
        String region = itemRequest.getString(MetaDataConstants.REGION);
        if (StrUtil.isEmpty(region)) {
            return ResultMsg.FAILURE("数据中心不能为空!");
        }
        //判断唯一性
        ResultMsg resultMsg = this.checkUniqueness(itemRequest);
        if (!resultMsg.isResult()) {
            return resultMsg;
        }
        //判断是否传订阅信息
        if (StrUtil.isEmpty(itemRequest.getString(MetaDataConstants.SUBSCRIPTION_ID))){
            Region regionData = this.getRegion(region);
            if (regionData==null){
                return ResultMsg.FAILURE("数据中心信息不存在!");
            }
            itemRequest.put(MetaDataConstants.SUBSCRIPTION_ID,regionData.getSubscriptionId());
            itemRequest.put(MetaDataConstants.SUBSCRIPTION_NAME,regionData.getSubscriptionName());
        }
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setRegion(region);
        metaDataItem.setType(type);
        metaDataItem.setRemark(itemRequest.getString(MetaDataConstants.REMARK));
        metaDataItem.setVersion(itemRequest.getString(MetaDataConstants.VERSION));
        metaDataItem.setCreateUserId(userInfo.getUserId());
        metaDataItem.setLastModifiedId(userInfo.getUserId());
        Date date = new Date();
        metaDataItem.setCreateTime(date);
        metaDataItem.setLastModifiedTime(date);
        metaDataItem.setData(itemRequest.toJSONString());
        int inserted = metaDataItemMapper.insert(metaDataItem);
        return ResultMsg.SUCCESS(inserted);
    }

    @Override
    public ResultMsg updateMetaData(JSONObject itemRequest, BaseUserInfo userInfo) {
        Long id = itemRequest.getLong("id");
        if (id == null) {
            return ResultMsg.FAILURE("主键不能为空!");
        }
        MetaDataItem metaDataItem = metaDataItemMapper.queryById(id);
        if (metaDataItem == null) {
            return ResultMsg.FAILURE("信息不存在!");
        }
        String region = itemRequest.getString(MetaDataConstants.REGION);
        if (StrUtil.isNotEmpty(region)){
            metaDataItem.setRegion(region);
        }else {
            itemRequest.put(MetaDataConstants.REGION,metaDataItem.getRegion());
        }
        //判断唯一性
        ResultMsg resultMsg = this.checkUniqueness(itemRequest);
        if (!resultMsg.isResult()) {
            return resultMsg;
        }
        //判断是否传订阅信息
        if (StrUtil.isEmpty(itemRequest.getString(MetaDataConstants.SUBSCRIPTION_ID))){
            Region regionData = this.getRegion(region);
            if (regionData==null){
                return ResultMsg.FAILURE("数据中心信息不存在!");
            }
            itemRequest.put(MetaDataConstants.SUBSCRIPTION_ID,regionData.getSubscriptionId());
            itemRequest.put(MetaDataConstants.SUBSCRIPTION_NAME,regionData.getSubscriptionName());
        }
        metaDataItem.setLastModifiedId(userInfo.getUserId());
        Date date = new Date();
        metaDataItem.setLastModifiedTime(date);
        metaDataItem.setData(itemRequest.toJSONString());
        metaDataItem.setRemark(itemRequest.getString(MetaDataConstants.REMARK));
        int updated = metaDataItemMapper.update(metaDataItem);
        return ResultMsg.SUCCESS(updated);
    }
    /**
     * 查询元数据
     */
    @Override
    public ResultMsg selectMetaDataList(JSONObject itemRequest) {
        String type = itemRequest.getString(MetaDataConstants.TYPE);
        if (StrUtil.isEmpty(type)) {
            return ResultMsg.FAILURE("元数据类型不能为空!");
        }
        Optional<MetaDataType> enumObject = EnumUtil.getEnumObject(MetaDataType.class, metaDataType -> type.equals(metaDataType.getCode()));
        if (!enumObject.isPresent()) {
            return ResultMsg.FAILURE("元数据类型不存在!");
        }
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(type);
        metaDataItem.setRegion(itemRequest.getString(MetaDataConstants.REGION));
        List<MetaDataItem> metaDataItems = metaDataItemMapper.selectMetaDataList(metaDataItem);
        //筛选
        Set<String> keys = itemRequest.keySet();
        for (String key : keys) {
            //排除掉 type和region
            if (MetaDataConstants.TYPE.equals(key) || MetaDataConstants.REGION.equals(key) ){
                continue;
            }
            String value = itemRequest.getString(key);
            if (StrUtil.isNotEmpty(value)) {
                metaDataItems = metaDataItems.stream().filter(dataItem -> {
                    String dataStr = dataItem.getData();
                    if (StrUtil.isNotEmpty(dataStr)) {
                        JSONObject dataJson = JSONObject.parseObject(dataStr);
                        String dataValue = dataJson.getString(key);
                        return dataValue.contains(value);
                    }
                    return false;
                }).collect(Collectors.toList());
            }
        }
        Map<String, String> regionMap = this.getRegionMap();
        //数据重构
        List<JSONObject> dataItemResponses = metaDataItems.stream().map(dataItem -> {
            JSONObject object = JSONObject.parseObject(dataItem.getData());
            object.put("id", dataItem.getId());
            object.put(MetaDataConstants.REMARK, dataItem.getRemark());
            object.put(MetaDataConstants.VERSION, dataItem.getVersion());
            object.put(MetaDataConstants.TYPE, dataItem.getType());
            object.put("createTime", DateUtil.dateToStr(dataItem.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            object.put("lastModifiedTime", DateUtil.dateToStr(dataItem.getLastModifiedTime(), "yyyy-MM-dd HH:mm:ss"));
            String regionName = regionMap.get(object.getString(MetaDataConstants.REGION));
            object.put(MetaDataConstants.REGION_NAME,regionName);
            return object;
        }).collect(Collectors.toList());
        return ResultMsg.SUCCESS(dataItemResponses);
    }

    @Override
    public ResultMsg deleteMetaDataById(Long id) {
        if (id == null) {
            return ResultMsg.FAILURE("主键不能为空!");
        }
        MetaDataItem metaDataItem = metaDataItemMapper.queryById(id);
        if (metaDataItem == null) {
            return ResultMsg.FAILURE("信息不存在!");
        }
        //检查关联
        ResultMsg associationMsg = this.checkAssociation(metaDataItem);
        if (!associationMsg.getResult()) {
            return associationMsg;
        }
        int deleted = metaDataItemMapper.deleteById(id);
        return ResultMsg.SUCCESS(deleted);
    }

    /**
     * 检查关联
     *
     * @param metaDataItem
     * @return
     */
    public ResultMsg checkAssociation(MetaDataItem metaDataItem) {
        String type = metaDataItem.getType();
        String region = metaDataItem.getRegion();
        //数据中心
        if (MetaDataType.REGION.getCode().equals(type)) {
            //查询除数据中心之外的元数据
            long countBy = metaDataItemMapper.countBy(type, region);
            if (countBy > 0) {
                return ResultMsg.FAILURE("当前数据中心正在使用，请先删除数据中心下的元数据再试!");
            }
        }
        return ResultMsg.SUCCESS();
    }

    /**
     * 检查唯一性
     *
     * @param itemRequest
     * @return
     */
    public ResultMsg checkUniqueness(JSONObject itemRequest) {
        String type = itemRequest.getString(MetaDataConstants.TYPE);
        String region = itemRequest.getString(MetaDataConstants.REGION);
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(type);
        metaDataItem.setRegion(region);
        Long id = itemRequest.getLong("id");
        List<MetaDataItem> dataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        long countBy = 0;
        //数据中心
        if (MetaDataType.REGION.getCode().equals(type)) {
            //数据中心不能重复
            countBy = dataList.stream().filter(s -> !s.getId().equals(id)).count();
        }
        //key vault
        if (MetaDataType.KEY_VAULT.getCode().equals(type)) {
            //一个数据中心只能配置一个key vault
            countBy = dataList.stream().filter(s -> {
                if (s.getId().equals(id)) {
                    return false;
                }
                keyVault jsonObject = JSONObject.parseObject(s.getData(),keyVault.class);
                String dataNo = jsonObject.getResourceId();
                return StrUtil.isNotEmpty(dataNo) && dataNo.equals(itemRequest.getString(MetaDataConstants.KV_RESOURCE_ID));
            }).count();
        }
        //ssh
        if (MetaDataType.SSH_KEY.getCode().equals(type)) {
            //一个数据中心只能有一个公钥和一个私钥
            countBy = dataList.stream().filter(s -> {
                if (s.getId().equals(id)) {
                    return false;
                }
                SSHKeyPair sshKeyPair = JSONObject.parseObject(s.getData(),SSHKeyPair.class);
                Integer keyType = sshKeyPair.getKeyType();
                return keyType!=null && keyType.equals(itemRequest.getInteger(MetaDataConstants.KEY_TYPE));
            }).count();
        }
        if (countBy > 0) {
            return ResultMsg.FAILURE("元数据已存在!");
        }
        return ResultMsg.SUCCESS();
    }


    /**
     * 获取机型sku信息并封装成map
     *
     * @return
     */
    public Map<String, VMSku> getVmSkuMap(String region) {
        Assert.notEmpty(region, " 获取机型sku信息并封装成map,Azure Region不能为空");
        Map<String, VMSku> map = new HashMap<>();
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.VM_SKU.getCode());
        metaDataItem.setRegion(region);
        List<MetaDataItem> metaDataItems = metaDataItemMapper.selectMetaDataList(metaDataItem);
        for (MetaDataItem dataItem : metaDataItems) {
            VMSku vmSku = JSON.parseObject(dataItem.getData(), VMSku.class);
            vmSku.setRegion(dataItem.getRegion());
            vmSku.setType(dataItem.getType());
            vmSku.setVersion(dataItem.getVersion());
            vmSku.setRemark(dataItem.getRemark());
            vmSku.setCreateTime(dataItem.getCreateTime());
            vmSku.setId(dataItem.getId());
            map.put(vmSku.getName(), vmSku);
        }
        return map;
    }

    /**
     * 获取region信息 map
     *
     * @return
     */
    public Map<String, String> getRegionMap() {
        Map<String, String> map = new HashMap<>();
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.REGION.getCode());
        List<MetaDataItem> metaDataItems = metaDataItemMapper.selectMetaDataList(metaDataItem);
        for (MetaDataItem dataItem : metaDataItems) {
            Region region = JSON.parseObject(dataItem.getData(), Region.class);
            map.put(region.getRegion(), region.getRegionName());
        }
        return map;
    }

    @Override
    public List<String> listRegion() {
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.REGION.getCode());
        List<MetaDataItem> metaDataItems = metaDataItemMapper.selectMetaDataList(metaDataItem);
        return metaDataItems.stream().map(MetaDataItem::getRegion).collect(Collectors.toList());
    }

    @Override
    public Region getRegion(String region) {
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.REGION.getCode());
        metaDataItem.setRegion(region);
        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        //获取第一个
        return metaDataList.stream()
                .findFirst()
                .map(dataItem -> JSON.parseObject(dataItem.getData(), Region.class))
                .orElse(null);
    }

    /**
     * 获取VM规格列表
     *
     * @return
     */
    @Override
    public List<VMSku> getVMSKURecommend(String region) {
        List<VMSku> vmSkuList = this.getVMSKU(region);
        return this.setIsRecommend(vmSkuList);
    }

    @Override
    public List<VMSku> getVMSKU(String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        //从元数据表获取地区信息
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.VM_SKU.getCode());
        metaDataItem.setRegion(region);
        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        //获取今天Azure价格记录
        Map<String, AzurePriceHistory> azurePriceHistoryMap = this.getAzurePriceHistoryMap(region);

        List<VMSku> vmSkuList = metaDataList.stream().map(dataItem -> {
            VMSku vmSku = JSON.parseObject(dataItem.getData(), VMSku.class);
            vmSku.setRegion(dataItem.getRegion());
            vmSku.setType(dataItem.getType());
            vmSku.setVersion(dataItem.getVersion());
            vmSku.setRemark(dataItem.getRemark());
            vmSku.setCreateTime(dataItem.getCreateTime());
            vmSku.setId(dataItem.getId());
            //获取价格,驱逐率
            String key = dataItem.getRegion() + ":" + vmSku.getName();
            if (azurePriceHistoryMap.containsKey(key)) {
                AzurePriceHistory azurePriceHistory = azurePriceHistoryMap.get(key);
                vmSku.setOndemandUnitPrice(azurePriceHistory.getOndemandUnitPrice());
                vmSku.setSpotUnitPrice(azurePriceHistory.getSpotUnitPrice());
                vmSku.setEvictionRateLower(azurePriceHistory.getEvictionRateLower());
                vmSku.setEvictionRateUpper(azurePriceHistory.getEvictionRateUpper());
            }
            return vmSku;
        }).collect(Collectors.toList());
        return vmSkuList;
    }

    /**
     * 获取sku列表,根据skuNameList就行筛选
     * @param region
     * @param skuNameList
     * @return
     */
    @Override
    public List<VMSku> getVMSKU(String region, List<String> skuNameList) {
        List<VMSku> vmsku = this.getVMSKU(region);
        return vmsku.stream()
                .filter(vmSku ->
                        CollUtil.isNotEmpty(skuNameList) && skuNameList.contains(vmSku.getName())
                ).collect(Collectors.toList());
    }

    /**
     * 设置推荐
     * @param vmSkuList
     * @return
     */
    private List<VMSku> setIsRecommend(List<VMSku> vmSkuList) {
        //获取最小驱逐率,驱逐率不能为空或0
        Optional<VMSku> evictionRateUpperMin = vmSkuList.stream()
                .filter(vmSku1 -> vmSku1.getEvictionRateUpper() != null && BigDecimal.ZERO.compareTo(vmSku1.getEvictionRateUpper()) < 0)
                .min(Comparator.comparing(VMSku::getEvictionRateUpper));
        if (evictionRateUpperMin.isPresent()) {
            BigDecimal evictionRateUpper = evictionRateUpperMin.get().getEvictionRateUpper();
            //设置推荐字段
            vmSkuList = vmSkuList.stream()
                    .peek(sku -> {
                        if (sku.getEvictionRateUpper() != null) {
                            sku.setIsRecommend(evictionRateUpper.compareTo(sku.getEvictionRateUpper()) == 0);
                        } else {
                            sku.setIsRecommend(false);
                        }
                    }).collect(Collectors.toList());
        }
        return vmSkuList;
    }
    /**
     * 获取今天Azure价格记录,封装成map
     * @return
     */
    public Map<String, AzurePriceHistory> getAzurePriceHistoryMap(String region){
        Date endDate = new Date();
        Date startDate = cn.hutool.core.date.DateUtil.beginOfDay(endDate);
        List<AzurePriceHistory> azurePriceHistories = azurePriceHistoryMapper.selectDayLastByRegionAndDateRange(region,null,startDate, endDate);
        return azurePriceHistories.stream().collect(
                Collectors.toMap(azurePriceHistory -> azurePriceHistory.getRegion()+ ":" + azurePriceHistory.getVmSkuName(), Function.identity(), (v1, v2) -> v1)
        );
    }

    /**
     * 获取可用区
     *
     * @return
     */
    @Override
    public AvailabilityZone getAZ(String region, String zoneNumber) {
        Assert.notEmpty(region, "Azure Region不能为空");
        Assert.notEmpty(zoneNumber, "Zone Number不能为空");
        //从元数据表获取可用区列表
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.AVAILABILITY_ZONE.getCode());
        metaDataItem.setRegion(region);
        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        //获取第一个
        return metaDataList.stream()
                .filter(dataItem -> {
                    AvailabilityZone zone = JSON.parseObject(dataItem.getData(), AvailabilityZone.class);
                    return zoneNumber.equals(zone.getLogicalZone());
                })
                .findFirst()
                .map(dataItem -> JSON.parseObject(dataItem.getData(), AvailabilityZone.class))
                .orElse(null);
    }

    @Override
    public AvailabilityZone getAZByPhysicalZone(String region,String zoneNumber) {
        Assert.notEmpty(region, "Azure Region不能为空");
        Assert.notEmpty(zoneNumber, "物理Zone Number不能为空");

        //从元数据表获取可用区列表
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.AVAILABILITY_ZONE.getCode());
        metaDataItem.setRegion(region);
        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);

        return metaDataList.stream().filter(metaData -> {
                    AvailabilityZone zone = JSON.parseObject(metaData.getData(), AvailabilityZone.class);
                    return StrUtil.equals(zoneNumber, zone.getPhysicalZoneNo());
                }).findFirst()
                .map(item -> JSON.parseObject(item.getData(), AvailabilityZone.class))
                .orElse(null);
    }

    /**
     * 子网
     * @param region
     * @param subnetId
     * @return
     */
    @Override
    public Subnet getSubnet(String region, String subnetId) {
        Assert.notEmpty(region, "Azure Region不能为空");
        Assert.notEmpty(subnetId, "子网id不能为空");
        //从元数据表获取可用区列表
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.SUBNET.getCode());
        metaDataItem.setRegion(region);
        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        //获取第一个
        return metaDataList.stream()
                .filter(dataItem -> {
                    Subnet subnet = JSON.parseObject(dataItem.getData(), Subnet.class);
                    return subnetId.equals(subnet.getSubnetId());
                })
                .findFirst()
                .map(dataItem -> JSON.parseObject(dataItem.getData(), Subnet.class))
                .orElse(null);
    }

    /**
     * 获取安全组sku
     * @param region
     * @param resourceId
     * @return
     */
    @Override
    public NSGSku getNSGSku(String region, String resourceId) {
        Assert.notEmpty(region, "Azure Region不能为空");
        Assert.notEmpty(resourceId, "安全组resourceId不能为空");
        //从元数据表获取可用区列表
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.NSG_SKU.getCode());
        metaDataItem.setRegion(region);
        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        //获取第一个
        return metaDataList.stream()
                .filter(dataItem -> {
                    NSGSku nsgSku = JSON.parseObject(dataItem.getData(), NSGSku.class);
                    return resourceId.equals(nsgSku.getResourceId());
                })
                .findFirst()
                .map(dataItem -> JSON.parseObject(dataItem.getData(), NSGSku.class))
                .orElse(null);
    }

    /**
     * 获取密钥对
     * @param region
     * @param secretResourceId
     * @return
     */
    @Override
    public SSHKeyPair getSSHKeyPair(String region, String secretResourceId) {
        Assert.notEmpty(region, "Azure Region不能为空");
        Assert.notEmpty(secretResourceId, "密钥对secretResourceId不能为空");
        //从元数据表获取可用区列表
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.SSH_KEY.getCode());
        metaDataItem.setRegion(region);
        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        //获取第一个
        return metaDataList.stream()
                .filter(dataItem -> {
                    SSHKeyPair nsgSku = JSON.parseObject(dataItem.getData(), SSHKeyPair.class);
                    return secretResourceId.equals(nsgSku.getSecretResourceId());
                }).findFirst()
                .map(dataItem -> JSON.parseObject(dataItem.getData(), SSHKeyPair.class))
                .orElse(null);
    }

    /**
     * 获取托管标识
     * @param region
     * @param resourceId
     * @return
     */
    @Override
    public ManagedIdentity getMI(String region, String resourceId) {
        Assert.notEmpty(region, "Azure Region不能为空");
        Assert.notEmpty(resourceId, "mi resourceId不能为空");
        //从元数据表获取可用区列表
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.MANAGED_IDENTITY.getCode());
        metaDataItem.setRegion(region);
        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        //获取第一个
        return metaDataList.stream()
                .filter(dataItem -> {
                    ManagedIdentity managedIdentity = JSON.parseObject(dataItem.getData(), ManagedIdentity.class);
                    return resourceId.equals(managedIdentity.getResourceId());
                }).findFirst()
                .map(dataItem -> JSON.parseObject(dataItem.getData(), ManagedIdentity.class))
                .orElse(null);
    }

    /**
     * 获取日志桶
     * @param region
     * @param blobContainerUrl
     * @return
     */
    @Override
    public LogsBlobContainer getLogsBlobContainer(String region, String blobContainerUrl) {
        Assert.notEmpty(region, "Azure Region不能为空");
        Assert.notEmpty(blobContainerUrl, "LogsBlobContainer blobContainerUrl不能为空");
        //从元数据表获取可用区列表
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.LOGS_BLOB_CONTAINER.getCode());
        metaDataItem.setRegion(region);
        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        //获取第一个
        return metaDataList.stream()
                .filter(dataItem -> {
                    LogsBlobContainer blobContainer = JSON.parseObject(dataItem.getData(), LogsBlobContainer.class);
                    return blobContainerUrl.equals(blobContainer.getBlobContainerUrl());
                }).findFirst()
                .map(dataItem -> JSON.parseObject(dataItem.getData(), LogsBlobContainer.class))
                .orElse(null);
    }
    /**
     * 获取日志桶 根据blobContainerUrl
     * @param region
     * @param blobContainerUrl
     * @return
     */
    @Override
    public LogsBlobContainer getLogsBlobContainerByUrl(String region, String blobContainerUrl) {
        Assert.notEmpty(region, "Azure Region不能为空");
        Assert.notEmpty(blobContainerUrl, "LogsBlobContainer blobContainerUrl不能为空");
        //从元数据表获取可用区列表
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.LOGS_BLOB_CONTAINER.getCode());
        metaDataItem.setRegion(region);
        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        //获取第一个
        return metaDataList.stream()
                .filter(dataItem -> {
                    LogsBlobContainer blobContainer = JSON.parseObject(dataItem.getData(), LogsBlobContainer.class);
                    return blobContainerUrl.equals(blobContainer.getBlobContainerUrl());
                }).findFirst()
                .map(dataItem -> JSON.parseObject(dataItem.getData(), LogsBlobContainer.class))
                .orElse(null);
    }

    /**
     * 获取磁盘sku
     */
    @Override
    public DiskSku getDiskSku(String region, String skuName) {
        Assert.notEmpty(region, "Azure Region不能为空");
        Assert.notEmpty(skuName, "DiskSku skuName不能为空");
        //从元数据表获取可用区列表
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.DISK_SKU.getCode());
        metaDataItem.setRegion(region);
        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        //获取第一个
        return metaDataList.stream()
                .filter(dataItem -> {
                    DiskSku diskSku = JSON.parseObject(dataItem.getData(), DiskSku.class);
                    return skuName.equals(diskSku.getName());
                }).findFirst()
                .map(dataItem -> JSON.parseObject(dataItem.getData(), DiskSku.class))
                .orElse(null);
    }
    /**
     * 获取vmsku
     */
    @Override
    public VMSku getVMSKU(String region, String name) {
        Assert.notEmpty(region, "Azure Region不能为空");
        Assert.notEmpty(name, "VMSKU name不能为空");
        //从元数据表获取可用区列表
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.VM_SKU.getCode());
        metaDataItem.setRegion(region);
        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        //获取第一个
        return metaDataList.stream()
                .filter(dataItem -> {
                    VMSku vmSku = JSON.parseObject(dataItem.getData(), VMSku.class);
                    return name.equals(vmSku.getName());
                }).findFirst()
                .map(dataItem -> JSON.parseObject(dataItem.getData(), VMSku.class))
                .orElse(null);
    }

    /**
     * 获取子网列表
     *
     * @return
     */
    @Override
    public List<Subnet> getsubnetlist(String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        //从元数据表获取子网信息
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.SUBNET.getCode());
        metaDataItem.setRegion(region);
        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        return metaDataList.stream().map(dataItem -> {
            Subnet subnet = JSON.parseObject(dataItem.getData(), Subnet.class);
            subnet.setRegion(dataItem.getRegion());
            subnet.setType(dataItem.getType());
            subnet.setVersion(dataItem.getVersion());
            subnet.setRemark(dataItem.getRemark());
            subnet.setCreateTime(dataItem.getCreateTime());
            subnet.setId(dataItem.getId());
            return subnet;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Subnet> getsubnetlist() {
        //从元数据表获取子网信息
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.SUBNET.getCode());
        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        return metaDataList.stream().map(dataItem -> {
            Subnet subnet = JSON.parseObject(dataItem.getData(), Subnet.class);
            subnet.setRegion(dataItem.getRegion());
            subnet.setType(dataItem.getType());
            subnet.setVersion(dataItem.getVersion());
            subnet.setRemark(dataItem.getRemark());
            subnet.setCreateTime(dataItem.getCreateTime());
            subnet.setId(dataItem.getId());
            return subnet;
        }).collect(Collectors.toList());
    }

    /**
     * 获取日志BLob列表
     *
     * @return
     */
    @Override
    public List<LogsBlobContainer> getLogsBlobContainerList(String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        //从元数据表获取日志BLob列表
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.LOGS_BLOB_CONTAINER.getCode());
        metaDataItem.setRegion(region);
        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        return metaDataList.stream().map(dataItem -> {
            LogsBlobContainer blobContainer = JSON.parseObject(dataItem.getData(), LogsBlobContainer.class);
            blobContainer.setRegion(dataItem.getRegion());
            blobContainer.setType(dataItem.getType());
            blobContainer.setVersion(dataItem.getVersion());
            blobContainer.setRemark(dataItem.getRemark());
            blobContainer.setCreateTime(dataItem.getCreateTime());
            blobContainer.setId(dataItem.getId());
            return blobContainer;
        }).collect(Collectors.toList());
    }

    /**
     * 获取MI列表
     *
     * @return
     */
    @Override
    public List<ManagedIdentity>  getMIList(String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        //从元数据表操作系统磁盘类型列表
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.MANAGED_IDENTITY.getCode());
        metaDataItem.setRegion(region);
        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        return metaDataList.stream().map(dataItem -> {
            ManagedIdentity managedIdentity = JSON.parseObject(dataItem.getData(), ManagedIdentity.class);
            managedIdentity.setRegion(dataItem.getRegion());
            managedIdentity.setType(dataItem.getType());
            managedIdentity.setVersion(dataItem.getVersion());
            managedIdentity.setRemark(dataItem.getRemark());
            managedIdentity.setCreateTime(dataItem.getCreateTime());
            managedIdentity.setId(dataItem.getId());
            return managedIdentity;
        }).collect(Collectors.toList());
    }

    /**
     * 获取操作系统磁盘类型列表
     *
     * @return
     */
    @Override
    public List<DiskSku> getosdisktypelist(String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        //从元数据表操作系统磁盘类型列表
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.DISK_SKU.getCode());
        metaDataItem.setRegion(region);

        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        return metaDataList.stream().map(dataItem -> {
            DiskSku diskSku = JSON.parseObject(dataItem.getData(), DiskSku.class);
            diskSku.setRegion(dataItem.getRegion());
            diskSku.setType(dataItem.getType());
            diskSku.setVersion(dataItem.getVersion());
            diskSku.setRemark(dataItem.getRemark());
            diskSku.setCreateTime(dataItem.getCreateTime());
            diskSku.setId(dataItem.getId());
            return diskSku;
        }).collect(Collectors.toList());
    }

    /**
     * 获取主安全组列表
     *
     * @return
     */
    @Override
    public List<NSGSku> getPrimaryNSGList(String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        //从元数据表获取主安全组列表
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.NSG_SKU.getCode());
        metaDataItem.setRegion(region);
        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        return metaDataList.stream().map(dataItem -> {
            NSGSku nsgSku = JSON.parseObject(dataItem.getData(), NSGSku.class);
            nsgSku.setRegion(dataItem.getRegion());
            nsgSku.setType(dataItem.getType());
            nsgSku.setVersion(dataItem.getVersion());
            nsgSku.setRemark(dataItem.getRemark());
            nsgSku.setCreateTime(dataItem.getCreateTime());
            nsgSku.setId(dataItem.getId());
            return nsgSku;
        }).collect(Collectors.toList());
    }

    /**
     * 获取密钥列表
     *
     * @return
     */
    @Override
    public List<SSHKeyPair> getkeypairlist(String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        //从元数据表获取密钥列表
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.SSH_KEY.getCode());
        metaDataItem.setRegion(region);
        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        return metaDataList.stream().map(dataItem -> {
            SSHKeyPair sshKeyPair = JSON.parseObject(dataItem.getData(), SSHKeyPair.class);
            sshKeyPair.setRegion(dataItem.getRegion());
            sshKeyPair.setType(dataItem.getType());
            sshKeyPair.setVersion(dataItem.getVersion());
            sshKeyPair.setRemark(dataItem.getRemark());
            sshKeyPair.setCreateTime(dataItem.getCreateTime());
            sshKeyPair.setId(dataItem.getId());
            return sshKeyPair;
        }).collect(Collectors.toList());
    }

    /**
     * 获取可用区列表
     *
     * @return
     */
    @Override
    public List<AvailabilityZone> getazlist(String region) {
        Assert.notEmpty(region, "Azure Region不能为空");
        //从元数据表获取可用区列表
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.AVAILABILITY_ZONE.getCode());
        metaDataItem.setRegion(region);
        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        return metaDataList.stream().map(dataItem -> {
            AvailabilityZone zone = JSON.parseObject(dataItem.getData(), AvailabilityZone.class);
            zone.setRegion(dataItem.getRegion());
            zone.setType(dataItem.getType());
            zone.setVersion(dataItem.getVersion());
            zone.setRemark(dataItem.getRemark());
            zone.setCreateTime(dataItem.getCreateTime());
            zone.setId(dataItem.getId());
            return zone;
        }).collect(Collectors.toList());
    }

    /**
     * 获取数据中心
     * @return
     */
    @Override
    public List<Region> getRegionList() {
        //从元数据表获取地区信息
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.REGION.getCode());
        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        return metaDataList.stream().map(dataItem -> {
            Region region = JSON.parseObject(dataItem.getData(), Region.class);
            region.setRegion(dataItem.getRegion());
            region.setType(dataItem.getType());
            region.setVersion(dataItem.getVersion());
            region.setRemark(dataItem.getRemark());
            region.setCreateTime(dataItem.getCreateTime());
            region.setId(dataItem.getId());
            return region;
        }).collect(Collectors.toList());
    }

    /**
     * @Description: 根据region 获取订阅id
     * @Date 2024/6/13
     **/
    public String getSubscriptionId(String region) {
        Assert.notEmpty(region,"Azure Region不能为空!");
        // 获取数据中心
        MetaDataItem item = new MetaDataItem();
        item.setType(MetaDataType.REGION.getCode());
        item.setRegion(region);
        List<String> regionObjests = metaDataItemMapper.selectMetaData(item);
        Assert.notEmpty(regionObjests,"根据region获取订阅id失败!");
        //取第一个
        Region object = JSON.parseObject(regionObjests.get(0),Region.class);
        return object.getSubscriptionId();
    }

    /**
     * @Description: 根据region获取keyVault
     * @Date 2024/6/13
     **/
    @Override
    public keyVault getkeyVault(String region) {
        Assert.notEmpty(region,"Azure Region不能为空!");
        // 获取数据中心
        MetaDataItem item = new MetaDataItem();
        item.setType(MetaDataType.KEY_VAULT.getCode());
        item.setRegion(region);
        List<String> regionObjests = metaDataItemMapper.selectMetaData(item);
        Assert.notEmpty(regionObjests,"根据region获取keyVault失败! region={}", region);
        //取第一个
        return JSON.parseObject(regionObjests.get(0),keyVault.class);
    }
    /**
     * 获取日志桶
     * @return
     */
    @Override
    public LogsBlobContainer getLogsBlobContainer(String container) {
        getLogger().info("获取脚本日志桶默认region:{},container:{}", blobRegion, container);
        //从元数据表获取可用区列表
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.LOGS_BLOB_CONTAINER.getCode());
        metaDataItem.setRegion(blobRegion);
        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        LogsBlobContainer logsBlobContainer = metaDataList.stream().filter(dataItem -> {
                    JSONObject jsonObject = JSON.parseObject(dataItem.getData());
                    return container.equals(jsonObject.getString(MetaDataConstants.LOG_BLOB_CONTAINER_NAME));
                }).findFirst()
                .map(dataItem -> {
                    LogsBlobContainer blobContainer = JSON.parseObject(dataItem.getData(), LogsBlobContainer.class);
                    blobContainer.setRegion(dataItem.getRegion());
                    blobContainer.setType(dataItem.getType());
                    blobContainer.setVersion(dataItem.getVersion());
                    blobContainer.setRemark(dataItem.getRemark());
                    blobContainer.setCreateTime(dataItem.getCreateTime());
                    blobContainer.setId(dataItem.getId());
                    return blobContainer;
                }).orElse(null);
        Assert.notNull(logsBlobContainer, "获取日志桶失败!未配置日志桶");
        return logsBlobContainer;
    }

    /**
     * 获取vmsku
     */
    @Override
    public List<VMSku> listVmSkuDistinct(String region, Integer coreCount, BigDecimal memoryGB) {
        Assert.notEmpty(region, "Azure Region不能为空");
        Assert.notNull(coreCount, "cpu 核心数不能为空");
        Assert.notNull(memoryGB, "运行内存数不能为空");
        //从元数据表获取可用区列表
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.VM_SKU.getCode());
        metaDataItem.setRegion(region);
        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        Map<String, VMSku> vmSkuMap= metaDataList.stream()
                .filter(dataItem -> {
                    VMSku vmSku = JSON.parseObject(dataItem.getData(), VMSku.class);
                    String memory = vmSku.getMemoryGB();
                    String vCoreCount = vmSku.getVCoreCount();
                    return coreCount.compareTo(Integer.valueOf(vCoreCount)) == 0
                            && memoryGB.compareTo(new BigDecimal(memory)) == 0;
                }).map(dataItem -> JSON.parseObject(dataItem.getData(), VMSku.class))
                .collect(Collectors.toMap(VMSku::getName, vmsku -> vmsku, (v1, v2) -> v1));
        return new ArrayList<>(vmSkuMap.values());
    }

    /**
     * 根据skuName列表获取vm sku并去重复,如果有重复的,只取第一个
     * @param region
     * @param skuNameList
     * @return
     */
    @Override
    public List<VMSku> listVmSkuDistinct(String region, List<String> skuNameList) {
        Assert.notEmpty(region, "Azure Region不能为空");
        Assert.notNull(skuNameList, "skuNameList不能为空");
        //从元数据表获取可用区列表
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.VM_SKU.getCode());
        metaDataItem.setRegion(region);
        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        Map<String, VMSku> vmSkuMap = metaDataList.stream()
                .filter(dataItem -> {
                    VMSku vmSku = JSON.parseObject(dataItem.getData(), VMSku.class);
                    String skuName = vmSku.getName();
                    return skuNameList.contains(skuName);
                }).map(dataItem -> JSON.parseObject(dataItem.getData(), VMSku.class))
                .collect(Collectors.toMap(VMSku::getName, vmsku -> vmsku, (v1, v2) -> v1));
        return new ArrayList<>(vmSkuMap.values());
    }

    @Override
    public SSHKeyPair getSSHKeyPair(String region, Integer keyType) {
        Assert.notEmpty(region, "Azure Region不能为空");
        Assert.notNull(keyType, "密钥类型不能为空");
        //从元数据表获取可用区列表
        MetaDataItem metaDataItem = new MetaDataItem();
        metaDataItem.setType(MetaDataType.SSH_KEY.getCode());
        metaDataItem.setRegion(region);
        List<MetaDataItem> metaDataList = metaDataItemMapper.selectMetaDataList(metaDataItem);
        //获取第一个
        return metaDataList.stream()
                .filter(dataItem -> {
                    SSHKeyPair nsgSku = JSON.parseObject(dataItem.getData(), SSHKeyPair.class);
                    return keyType.equals(nsgSku.getKeyType());
                }).findFirst()
                .map(dataItem -> JSON.parseObject(dataItem.getData(), SSHKeyPair.class))
                .orElse(null);
    }
}
