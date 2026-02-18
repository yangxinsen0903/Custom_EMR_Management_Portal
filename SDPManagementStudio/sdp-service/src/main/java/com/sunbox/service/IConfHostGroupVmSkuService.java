package com.sunbox.service;

import com.sunbox.domain.ConfClusterVm;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.metaData.VMSku;
import com.sunbox.domain.vmSku.ConfHostGroupVmSkuRequest;

import java.util.List;

/**
 * 实例组SKU表(ConfHostGroupVmSku)表服务接口
 *
 * @author makejava
 * @since 2024-08-05 10:44:46
 */
public interface IConfHostGroupVmSkuService {

    void addConfHostGroupVmSku(String region, ConfClusterVm confClusterVm, List<String> skuNameList);

    void addConfHostGroupVmSku(ConfClusterVm confClusterVm, List<VMSku> vmSkuList);

    ResultMsg listConfHostGroupVmSku(ConfHostGroupVmSkuRequest request);


}
