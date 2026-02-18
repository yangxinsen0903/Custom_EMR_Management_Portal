package com.sunbox.sdpadmin.strategy;

import com.sunbox.domain.ConfCluster;
import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpadmin.manager.ClusterCreationManager;
import com.sunbox.sdpadmin.model.admin.request.AdminSaveClusterRequest;
import com.sunbox.sdpadmin.model.admin.request.InstanceGroupSkuCfg;
import com.sunbox.sdpservice.service.ComposeService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class DirectlyClusterCreateStrategyImpl implements ClusterCreationStrategy {
    @Autowired
    ClusterCreationManager clusterCreationManager;

    @Autowired
    ComposeService composeService;

    public ResultMsg createCluster(String clusterId, AdminSaveClusterRequest adminSaveClusterRequest) {
        getLogger().info("begin createCluster,clusterId:{},request:{}", clusterId,
                adminSaveClusterRequest);

        /** 开始保存数据 **/
        //conf_cluster
        clusterCreationManager.addConfCluster(clusterId, adminSaveClusterRequest);
        clusterCreationManager.addConfClusterTag(clusterId, adminSaveClusterRequest);
        clusterCreationManager.addConfClusterApp(clusterId, adminSaveClusterRequest);

        clusterCreationManager.addConfClusterAppsConfig(clusterId, adminSaveClusterRequest.getClusterCfgs(), adminSaveClusterRequest.getInstanceGroupVersion().getClusterReleaseVer());

        clusterCreationManager.addConfClusterHostGroupAppsConfig(clusterId, adminSaveClusterRequest);
        clusterCreationManager.addConfClusterVmAndDataVolume(clusterId, adminSaveClusterRequest);
        clusterCreationManager.addConfClusterScript(clusterId, adminSaveClusterRequest.getConfClusterScript());
        clusterCreationManager.addElasticScaleRules(clusterId, adminSaveClusterRequest, adminSaveClusterRequest.getInstanceGroupSkuCfgs());
        clusterCreationManager.addInfoCluster(clusterId);
        //如果是创建审核中时,先不执行集群.
        if (adminSaveClusterRequest.getWorkOrderCreate() != null && 1==adminSaveClusterRequest.getWorkOrderCreate()) {
            getLogger().info("WorkOrderCreate,clusterId:{},request:{}", clusterId, adminSaveClusterRequest);
            return ResultMsg.SUCCESS("工单创建集群成功,请等待审核");
        }
        /** 结束保存数据 **/
        return createCluster(clusterId,
                adminSaveClusterRequest.getInstanceGroupVersion().getClusterReleaseVer(),
                adminSaveClusterRequest.getCreationMode(ConfCluster.CreationMode.DIRECTLY));
    }

    private ResultMsg createCluster(String clusterId, String releaseVer, ConfCluster.CreationMode creationMode) {
        getLogger().info("createCluster clusterId:{},releaseVer:{},creationMode:{}",
                clusterId,
                releaseVer,
                creationMode);

        ResultMsg createPlanResult = composeService.createPlan(clusterId, "create", releaseVer);
        if (null == createPlanResult || !createPlanResult.getResult()) {
            return createPlanResult;
        }
        Map<String, String> datamap = new HashMap<>();
        datamap.put("clusterId", clusterId);
        createPlanResult.setData(datamap);
        return createPlanResult;
    }

    public ResultMsg createAndStartPlan(String clusterId, String releaseVer, ConfCluster.CreationMode creationMode){
        ResultMsg cluster = createCluster(clusterId, releaseVer, creationMode);
        return cluster;
    }
}
