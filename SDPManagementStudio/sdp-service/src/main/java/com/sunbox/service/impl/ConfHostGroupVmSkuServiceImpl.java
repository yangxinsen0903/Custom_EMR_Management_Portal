package com.sunbox.service.impl;

import com.sunbox.dao.mapper.ConfHostGroupVmSkuMapper;
import com.sunbox.domain.ConfClusterVm;
import com.sunbox.domain.ConfHostGroupVmSku;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.metaData.VMSku;
import com.sunbox.domain.vmSku.ConfHostGroupVmSkuRequest;
import com.sunbox.service.IConfHostGroupVmSkuService;
import com.sunbox.service.IMetaDataItemService;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ConfHostGroupVmSkuServiceImpl implements IConfHostGroupVmSkuService , BaseCommonInterFace {
    @Autowired
    private ConfHostGroupVmSkuMapper confHostGroupVmSkuMapper;
    @Autowired
    private IMetaDataItemService metaDataItemService;

    /**
     * 初始化 ConfHostGroupVmSku
     * @param region
     * @param confClusterVm
     */
    public void addConfHostGroupVmSku(String region,ConfClusterVm confClusterVm, List<String> skuNameList){
        getLogger().info("新增实例构建 ConfHostGroupVmSku 数据:{},skuNameList:{}",confClusterVm.toString(),skuNameList);
        List<VMSku> vmSkuList =metaDataItemService.listVmSkuDistinct(region, skuNameList);
        getLogger().info("新增实例构建 ConfHostGroupVmSku vmSkuList:{}",vmSkuList);
        this.addConfHostGroupVmSku(confClusterVm,vmSkuList);
        //实例队列 暂时不支持
    }

    /**
     * 根据 skus 插入 ConfHostGroupVmSku
     * @param confClusterVm
     * @param vmSkuList
     */
    public void addConfHostGroupVmSku(ConfClusterVm confClusterVm,List<VMSku> vmSkuList){
        for (VMSku sku : vmSkuList) {
            ConfHostGroupVmSku confHostGroupVmSku = new ConfHostGroupVmSku();
            confHostGroupVmSku.setVmSkuId(UUID.randomUUID().toString());
            confHostGroupVmSku.setClusterId(confClusterVm.getClusterId());
            confHostGroupVmSku.setGroupId(confClusterVm.getGroupId());
            confHostGroupVmSku.setGroupName(confClusterVm.getGroupName());
            confHostGroupVmSku.setVmConfId(confClusterVm.getVmConfId());
            confHostGroupVmSku.setSku(sku.getName());
            confHostGroupVmSku.setVmRole(confClusterVm.getVmRole());
            confHostGroupVmSku.setCpuType(sku.getCpuType());
            confHostGroupVmSku.setVcpus(sku.getVCoreCount());
            confHostGroupVmSku.setMemory(sku.getMemoryGB());
            confHostGroupVmSku.setPurchaseType(confClusterVm.getPurchaseType());
            confHostGroupVmSku.setCreatedTime(new Date());
            confHostGroupVmSku.setCreatedby("sysadmin");
            confHostGroupVmSkuMapper.insert(confHostGroupVmSku);
        }
    }
    @Override
    public ResultMsg listConfHostGroupVmSku(ConfHostGroupVmSkuRequest request) {
        request.page();
        ConfHostGroupVmSku confHostGroupVmSku = new ConfHostGroupVmSku();
        confHostGroupVmSku.setVmConfId(request.getVmConfId());
        List<ConfHostGroupVmSku> confHostGroupVmSkus = confHostGroupVmSkuMapper.queryAllByLimit(confHostGroupVmSku, request.getPageStart(), request.getPageLimit());
        long count = confHostGroupVmSkuMapper.count(confHostGroupVmSku);
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setTotal(count);
        resultMsg.setData(confHostGroupVmSkus);
        resultMsg.setResult(true);
        return resultMsg;
    }
}
