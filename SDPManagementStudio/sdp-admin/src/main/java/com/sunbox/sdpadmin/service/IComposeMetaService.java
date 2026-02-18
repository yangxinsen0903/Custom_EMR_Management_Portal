package com.sunbox.sdpadmin.service;

import com.sunbox.domain.ResultMsg;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface IComposeMetaService {

    ResultMsg getMIList(String region,String subscriptionId);

    ResultMsg geVmSkus(String region);

    ResultMsg getDiskSku(String region);

    ResultMsg getSSHKeyPair(String region);

    ResultMsg getNSGSku(String region);

    ResultMsg getSubnet(String region);

    ResultMsg getAzList(String region);

    ResultMsg getBolbPath();

    ResultMsg getInstancePriceList(List<String> skuNames, String region);


    /**
     *  获取keyVault
     * @return
     */
    ResultMsg getKeyVaultList(String region,String subscriptionId);

    /**
     *  SSHKeyPair 查询 根据kvId
     * @return
     */
    ResultMsg getSSHKeyPairById(String kvId, String region,String subscriptionId);

    /**
     * 查询存储帐户列表
     *
     * @return
     */
    ResultMsg getStorageAccountList(String region,String subscriptionId);

    /**
     * 查询日志桶元数据根据id
     *
     * @return
     */
    ResultMsg getLogsBlobContainerListById(String saId, String region, String subscriptionId);
    /**
     * 查询虚拟网络
     *
     * @return
     */
    ResultMsg getNetworkList(String region);

    /**
     * 根据id获取子网列表
     * @param vnetId
     * @return
     */
    ResultMsg getSubnetListById(String vnetId,String region);

    /**
     * 查询数据中心
     * @return
     */
    ResultMsg getRegionList(String subscriptionId);
    /**
     * 查询订阅列表
     * @return
     */
    ResultMsg listSubscription();

}
