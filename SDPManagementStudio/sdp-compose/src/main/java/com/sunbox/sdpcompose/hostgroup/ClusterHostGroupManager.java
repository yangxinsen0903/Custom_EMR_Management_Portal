package com.sunbox.sdpcompose.hostgroup;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.alibaba.fastjson.JSON;
import com.sunbox.sdpcompose.mapper.ConfClusterHostGroupMapper;
import com.sunbox.dao.mapper.ConfHostGroupVmSkuMapper;
import com.sunbox.dao.mapper.InfoAmbariConfigGroupMapper;
import com.sunbox.domain.ConfClusterHostGroup;
import com.sunbox.domain.ConfHostGroupVmSku;
import com.sunbox.domain.InfoAmbariConfigGroup;
import com.sunbox.domain.InfoClusterVm;
import com.sunbox.sdpcompose.mapper.InfoClusterVmMapper;
import com.sunbox.sdpcompose.service.IClusterService;
import com.sunbox.sdpcompose.service.ambari.AmbariInfo;
import com.sunbox.sdpcompose.util.SpringContextUtil;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import sunbox.sdp.ambari.client.ApiClient;
import sunbox.sdp.ambari.client.api.CustomActionApi;
import sunbox.sdp.ambari.client.model.customaction.*;
import sunbox.sdp.ambari.client.model.customaction.enums.ConfigGroupField;

import java.util.*;


public class ClusterHostGroupManager implements BaseCommonInterFace {
    String clusterId;
    String clusterName;
    String sdpClusterName;

    @Autowired
    IClusterService clusterService;

    @Autowired
    ConfClusterHostGroupMapper confClusterHostGroupMapper;

    @Autowired
    InfoAmbariConfigGroupMapper infoAmbariConfigGroupMapper;

    @Autowired
    InfoClusterVmMapper infoClusterVmMapper;

    @Autowired
    ConfHostGroupVmSkuMapper confHostGroupVmSkuMapper;

    public ConfHostGroupVmSkuMapper getConfHostGroupVmSkuMapper() {
        if (confHostGroupVmSkuMapper == null) {
            confHostGroupVmSkuMapper = SpringContextUtil.getBean(ConfHostGroupVmSkuMapper.class);
        }
        return confHostGroupVmSkuMapper;
    }

    public ClusterHostGroupManager(String clusterId, String sdpClusterName) {
        this.clusterId = clusterId;
        this.clusterName = sdpClusterName.replace("-", "");
        this.sdpClusterName = sdpClusterName;
    }

    private ConfClusterHostGroupMapper getConfClusterHostGroupMapper(){
        if (confClusterHostGroupMapper == null){
            confClusterHostGroupMapper = SpringContextUtil.getBean(ConfClusterHostGroupMapper.class);
        }
        return confClusterHostGroupMapper;
    }
    private InfoAmbariConfigGroupMapper getInfoAmbariConfigGroupMapper() {
        if (infoAmbariConfigGroupMapper == null) {
            infoAmbariConfigGroupMapper = SpringContextUtil.getBean(InfoAmbariConfigGroupMapper.class);
        }
        return infoAmbariConfigGroupMapper;
    }
    private IClusterService getClusterService() {
        if (clusterService == null) {
            clusterService = SpringContextUtil.getBean(IClusterService.class);
        }
        return clusterService;
    }

    private InfoClusterVmMapper getInfoClusterVmMapper() {
        if (infoClusterVmMapper == null) {
            infoClusterVmMapper = SpringContextUtil.getBean(InfoClusterVmMapper.class);
        }
        return infoClusterVmMapper;
    }
    private AmbariInfo ambariInfo;
    private AmbariInfo getAmbariInfo() {
        if (ambariInfo == null) {
            ambariInfo = getClusterService().getAmbariInfo(clusterId);
        }
        return ambariInfo;
    }
    private ApiClient apiClient;
    public ApiClient getAmbariApiClient() {
        if (apiClient == null) {
            apiClient = getAmbariInfo().getAmbariApiClient();
            apiClient.setDebugging(true);
        }
        return apiClient;
    }
    private CustomActionApi customActionApi;
    public CustomActionApi getCustomActionApi() {
        if (customActionApi == null) {
            customActionApi = new CustomActionApi(getAmbariApiClient());
        }
        return customActionApi;
    }

    public String ambariConfigGroupName(String sdpGroupName, String vmSku, String serviceName) {
        return String.join("_","V2",sdpGroupName,vmSku,serviceName);
    }

    public void syncAmbariConfigGroup(){
        QueryConfigGroupsResponse queryConfigGroupsResponse = getCustomActionApi().queryConfigGroups(clusterName, ConfigGroupField.GROUP_NAME, "*");
        if (queryConfigGroupsResponse == null || queryConfigGroupsResponse.getItems() == null || queryConfigGroupsResponse.getItems().isEmpty()) {
            return;
        }
        for (ConfigGroupWrapper item : queryConfigGroupsResponse.getItems()) {
             String oldGroupName = item.getConfigGroup().getGroupName();
            if (oldGroupName.startsWith("blue-print")) {
                InfoAmbariConfigGroup infoAmbariConfigGroup = getInfoAmbariConfigGroupMapper().selectByClusterIdAndAmbariId(clusterId, item.getConfigGroup().getId());
                if (infoAmbariConfigGroup == null) {
                    getLogger().info("InfoAmbariConfigGroup not exist clusterId:{} ambariId:{}",clusterId,item.getConfigGroup().getId());
                    continue;
                }
                String sdpGroupName = infoAmbariConfigGroup.getSdpGroupName();
                String skuName = null;
                if (oldGroupName.endsWith("CORE")){
                    List<ConfHostGroupVmSku> confHostGroupVmSkus = getConfHostGroupVmSkuMapper().selectByClusterId(clusterId);
                    if (CollUtil.isEmpty(confHostGroupVmSkus)){
                        getLogger().error("ConfHostGroupVmSku not exist clusterId:{}",clusterId);
                        return;
                    }
                    for (ConfHostGroupVmSku confHostGroupVmSku : confHostGroupVmSkus) {
                        if (Objects.equals(confHostGroupVmSku.getGroupName(),"core")) {
                            skuName = confHostGroupVmSku.getSku();
                            break;
                        }
                    }
                } else if (oldGroupName.contains("CORE_")) {
                    skuName = oldGroupName.substring(oldGroupName.indexOf("CORE_")+5);
                } else if (oldGroupName.contains(sdpGroupName+"_")) {
                    skuName = oldGroupName.substring(oldGroupName.indexOf(sdpGroupName+"_")+(sdpGroupName+"_").length());
                }else {
                    return;
                }

                String ambariGroupName = ambariConfigGroupName(infoAmbariConfigGroup.getSdpGroupName(),skuName,infoAmbariConfigGroup.getAmbariServiceName());
                item.getConfigGroup().setServiceName(infoAmbariConfigGroup.getAmbariServiceName());
                item.getConfigGroup().setGroupName(ambariGroupName);
                item.setHref(null);
                for (HostRole host : item.getConfigGroup().getHosts()) {
                    host.setHref(null);
                }
                getCustomActionApi().updateConfigGroup(clusterName, item.getConfigGroup().getId(), item);
                infoAmbariConfigGroup.setAmbariGroupName(ambariGroupName);
                infoAmbariConfigGroup.setVmSkuId(skuName);
                getLogger().info("update infoAmbariConfigGroup :{}",JSON.toJSONString(infoAmbariConfigGroup));
                getInfoAmbariConfigGroupMapper().updateByGroupIdSelective(infoAmbariConfigGroup);
            }
        }
    }
    /**
     * 更新资源组配置到ambari config group
     * @param sdpGroupName
     * @param vmSku
     * @param serviceName
     * @param desiredConfigs
     */
    public void saveHostGroup(String sdpGroupName, String vmSku, String serviceName,List<Map<String, Object>> desiredConfigs) {
        getLogger().info("更新资源组配置到ambari config group sdpGroupName: {} vmSku: {} serviceName: {}", sdpGroupName, vmSku, serviceName);
        syncAmbariConfigGroup();
        String ambariConfigGroupName = ambariConfigGroupName(sdpGroupName, vmSku, serviceName);
        //删除blueprint默认配置组

        List<InfoClusterVm> infoClusterVms = getInfoClusterVmMapper().selectByClusterIdAndGroupNameAndSkuNameAndState(clusterId, sdpGroupName,vmSku, InfoClusterVm.VM_RUNNING);
        ArrayList<HostRole> hostRoles = new ArrayList<>();
        for (InfoClusterVm infoClusterVm : infoClusterVms) {
            HostRole hostRole = new HostRole();
            hostRole.setHostName(infoClusterVm.getHostName());
            hostRoles.add(hostRole);
        }
        InfoAmbariConfigGroup infoAmbariConfigGroup = getInfoAmbariConfigGroupMapper().selectByClusterIdAndAmbariGroupNameAndVmSkuIdAndServiceName(clusterId, ambariConfigGroupName,vmSku,serviceName);
        getLogger().info("infoAmbariConfigGroup: {}", JSON.toJSONString(infoAmbariConfigGroup));
        if (infoAmbariConfigGroup == null){
            addHostGroup(sdpGroupName,vmSku,serviceName, hostRoles,desiredConfigs);
        }else {
            desiredConfigs = new ArrayList<>();
            QueryConfigGroupDetailsResponse  configGroupDetailsResponse = getCustomActionApi().queryConfigGroupDetails(clusterName, ConfigGroupField.GROUP_NAME, ambariConfigGroupName);

            if (configGroupDetailsResponse != null) {
                HashMap<String, Map<String, Object>> configMap = new HashMap<>();
                for (Map<String, Object> desiredConfig : desiredConfigs) {
                    configMap.put(desiredConfig.get("type").toString(), desiredConfig);
                }
                for (ConfigGroupDetailWrapper item : configGroupDetailsResponse.getItems()) {
                    Map<String, Object> stringObjectMap = configMap.get(item.getType());

                    if (stringObjectMap == null) {
                        stringObjectMap = new HashMap<>();
                        stringObjectMap.put("type",item.getType());
                        stringObjectMap.put("properties",item.getProperties());
                        desiredConfigs.add(stringObjectMap);
                    }else {
                        Map propteties =  (Map)stringObjectMap.get("properties");
                        for (Map.Entry<String, Object> entry : item.getProperties().entrySet()) {
                            if (!propteties.containsKey(entry.getKey())) {
                                propteties.put(entry.getKey(), entry.getValue());
                            }
                        }
                    }

                }
            }
            updateHostGroup(infoAmbariConfigGroup.getAmbariId(),sdpGroupName,serviceName,vmSku,hostRoles,desiredConfigs);
        }
    }

    /**
     * 添加主机组
     * @param sdpGroupName ambari config group name
     * @param serviceName   ambari service name
     * @param hosts config group hosts
     * @param desiredConfigs    config group configs
     * @return CreateConfigGroupResponse
     */
    public CreateConfigGroupResponse addHostGroup(String sdpGroupName,String vmSku, String serviceName, List<HostRole> hosts, List<Map<String, Object>> desiredConfigs) {
        if (sdpGroupName == null ){
            throw new RuntimeException("groupName is null");
        }
        if (serviceName == null){
            throw new RuntimeException("serviceName is null");
        }
        String ambariConfigGroupName = ambariConfigGroupName(sdpGroupName, vmSku, serviceName);

        ConfigGroup configGroup = new ConfigGroup();
        configGroup.setGroupName(ambariConfigGroupName);
        configGroup.setClusterName(clusterName);
        configGroup.setServiceName(serviceName);
        configGroup.setTag(serviceName);
        configGroup.setDesiredConfigs(desiredConfigs);
        configGroup.setHosts(hosts);
        //ambari创建配置组
        CreateConfigGroupResponse response = getCustomActionApi().createConfigGroup(clusterName, Collections.singletonList(configGroup));
        //查询资源组
        ConfClusterHostGroup confClusterHostGroup = getConfClusterHostGroupMapper().selectOneByGroupNameAndClusterId(clusterId,sdpGroupName);
        InfoAmbariConfigGroup infoAmbariConfigGroup = new InfoAmbariConfigGroup();
        infoAmbariConfigGroup.setConfId(UUID.randomUUID().toString());
        infoAmbariConfigGroup.setClusterId(clusterId);
        infoAmbariConfigGroup.setGroupId(confClusterHostGroup.getGroupId());
        infoAmbariConfigGroup.setAmbariId(response.getSingleId().longValue());
        infoAmbariConfigGroup.setAmbariServiceName(serviceName);
        infoAmbariConfigGroup.setAmbariGroupName(ambariConfigGroupName);
        infoAmbariConfigGroup.setAmbariTag(serviceName);
        infoAmbariConfigGroup.setAmbariClusterName(clusterName);
        infoAmbariConfigGroup.setAmbariDescription("");
        infoAmbariConfigGroup.setState(InfoAmbariConfigGroup.STATE_RUNNING);
        infoAmbariConfigGroup.setCreatedTime(new Date());
        infoAmbariConfigGroup.setSdpGroupName(sdpGroupName);
        infoAmbariConfigGroup.setVmSkuId(vmSku);
        getInfoAmbariConfigGroupMapper().insertSelective(infoAmbariConfigGroup);
        return response;
    }
    public UpdateConfigGroupResponse updateHostGroup(Long ambariId,String sdpGroupName, String serviceName,String vmSku, List<HostRole> hosts, List<Map<String, Object>> desiredConfigs){
        ConfigGroup configGroup = new ConfigGroup();
        configGroup.setId(ambariId);
        configGroup.setGroupName(ambariConfigGroupName(sdpGroupName, vmSku, serviceName));
        configGroup.setClusterName(clusterName);
        configGroup.setServiceName(serviceName);
        configGroup.setTag(serviceName);
        configGroup.setDesiredConfigs(desiredConfigs);
        configGroup.setHosts(hosts);
        UpdateConfigGroupResponse updateConfigGroupResponse = getCustomActionApi().updateConfigGroup(clusterName, configGroup);
        return updateConfigGroupResponse;
    }
    public void removeHostGroup(Long ambariGroupId){
        getCustomActionApi().deleteConfigGroup(clusterName, ambariGroupId);
        getInfoAmbariConfigGroupMapper().deleteByClusterIdAndAmbariId(ambariGroupId, clusterId);
    }

}
