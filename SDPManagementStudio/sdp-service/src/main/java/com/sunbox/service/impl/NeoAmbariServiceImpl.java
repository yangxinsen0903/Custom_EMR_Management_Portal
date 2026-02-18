package com.sunbox.service.impl;

import com.sunbox.dao.mapper.ConfClusterNeoMapper;
import com.sunbox.dao.mapper.InfoClusterAmbariHostDeleteMapper;
import com.sunbox.dao.mapper.InfoClusterNeoMapper;
import com.sunbox.domain.ConfCluster;
import com.sunbox.domain.InfoCluster;
import com.sunbox.domain.InfoClusterAmbariHostDelete;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.ambari.AmbariInfo;
import com.sunbox.service.INeoAmbariService;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sunbox.sdp.ambari.client.ApiClient;
import sunbox.sdp.ambari.client.ApiException;
import sunbox.sdp.ambari.client.api.CustomActionApi;
import sunbox.sdp.ambari.client.model.customaction.DeleteHostsResponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author : [niyang]
 * @className : NeoAmbariServiceImpl
 * @description : [描述说明该类的功能]
 * @createTime : [2023/8/14 10:57 AM]
 */
@Service
public class NeoAmbariServiceImpl implements INeoAmbariService, BaseCommonInterFace {

    @Value("${ambari.default.user:admin}")
    private String username;

    @Value("${ambari.default.pwd:admin}")
    private String password;


    @Value("${ambari.host.delete.duration:600}")
    private Integer ambariHostDeleteDuraton;

    /**
     * 是否开启Ambari Api调用调试模式, 1:开启  0:不开启 <br/> 默认开启调试模式,便于查问题
     */
    @Value("${ambari.api.debug:0}")
    private String debug;

    @Autowired
    private InfoClusterNeoMapper infoClusterNeoMapper;

    @Autowired
    private ConfClusterNeoMapper confClusterNeoMapper;

    @Autowired
    private InfoClusterAmbariHostDeleteMapper ambariHostDeleteMapper;

    /**
     * 从Ambari-server中删除指定的hostName
     *
     * @param clusterId
     * @param hostNames
     * @return
     */
    @Override
    public ResultMsg deleteAmbariHosts(String clusterId, List<String> hostNames) {
        ResultMsg resultMsg = new ResultMsg();

        try {
            AmbariInfo ambariInfo = getAmbariInfo(clusterId);
            ConfCluster confCluster = confClusterNeoMapper.selectByPrimaryKey(clusterId);
            String ambariClusterName = getAmbariClusterName(confCluster.getClusterName());
            ApiClient apiClient = getAmbariApiClient(ambariInfo);

            CustomActionApi actionApi = new CustomActionApi(apiClient);
            setAmbariDebugState(actionApi.getApiClient());
            List<String> failHosts = new ArrayList<>();
            for (String host : hostNames) {
                try {
                    DeleteHostsResponse response = actionApi.deleteHost(ambariClusterName, host);
                } catch (ApiException ex) {
                    getLogger().error(ex.getMessage(), ex);
                    if (ex.getCode() == 404) {
                        getLogger().info("从集群中删除主机时,被删除的主机不存在,可以认为删除成功: clusterName={}, hostName={}", ambariClusterName, host);
                    } else {
                        failHosts.add(host.toLowerCase());
                    }
                } catch (Exception e) {
                    getLogger().error("删除单个AmbariHost异常：", e);
                    failHosts.add(host.toLowerCase());
                }
                try {
                    // 补偿删除一下
                    actionApi.deleteHost(host);
                } catch (ApiException ex) {
                    getLogger().error(ex.getMessage(), ex);
                    if (ex.getCode() == 404) {
                        getLogger().info("从Ambari删除主机时,被删除的主机不存在,可以认为删除成功: hostName={}", host);
                    }
                } catch (Exception ex) {
                    getLogger().error("补偿删除单个AmbariHost异常：", ex);
                }
            }
            if (failHosts!=null && failHosts.size()>0){
                resultMsg.setRows(failHosts);
                resultMsg.setResult(false);
            }else{
                resultMsg.setResult(true);
            }
        }catch (Exception e){
            getLogger().error("deleteAmbariHost异常,",e);
        }
        return resultMsg;
    }

    /**
     * 获取Ambari信息
     *
     * @param clusterId 集群ID
     * @return
     */
    @Override
    public AmbariInfo getAmbariInfo(String clusterId) {
        InfoCluster infoCluster = infoClusterNeoMapper.selectByPrimaryKey(clusterId);
        if (infoCluster!=null){
            AmbariInfo ambari = new AmbariInfo();
            ambari.setBaseUri(infoCluster.getAmbariHost());
            ambari.setUserName(username);
            ambari.setPassword(password);
            String baseUri = "http://" + infoCluster.getAmbariHost() + ":8080/api/v1";
            ambari.setBaseUri(baseUri);
            // 计算Referer
            int index = baseUri.indexOf("/api/v1");
            if (index > 0) {
                ambari.setReferer(baseUri.substring(0, index));
            }

            return ambari;
        }
        return null;
    }

    private void setAmbariDebugState(ApiClient client) {
        if (Objects.isNull(client)) {
            return;
        }
        client.setDebugging(Objects.equals(this.debug, "1"));
    }

    /**
     *
     * 获取Ambari Api Client
     *
     * @param ambariInfo
     * @return
     */
    private ApiClient getAmbariApiClient(AmbariInfo ambariInfo){
        ApiClient apiClient = ApiClient.newInstance();
        apiClient.setBasePath(ambariInfo.getBaseUri());
        apiClient.setUsername(ambariInfo.getUserName());
        apiClient.setPassword(ambariInfo.getPassword());
        apiClient.addDefaultHeader("Referer", ambariInfo.getReferer());

        return apiClient;
    }

    /**
     * 根据SDP的集群名称，获取ambari中的集群名称
     *
     * @param sdpClusterName SDP中的集群名称
     * @return
     */
    @Override
    public String getAmbariClusterName(String sdpClusterName) {
        return sdpClusterName.replaceAll("-", "");
    }


    /**
     * 查询ambari上的所有Host
     *
     * @param clusterId
     * @return hostName
     */
    @Override
    public List<String> queryAllHosts(String clusterId) {
        List<String> hosts = new ArrayList<>();
        try {
            AmbariInfo ambariInfo = getAmbariInfo(clusterId);
            ConfCluster confCluster = confClusterNeoMapper.selectByPrimaryKey(clusterId);
            String ambariClusterName = getAmbariClusterName(confCluster.getClusterName());
            ApiClient apiClient = getAmbariApiClient(ambariInfo);
            CustomActionApi actionApi = new CustomActionApi(apiClient);
            hosts = actionApi.queryAllHostsName(ambariClusterName);
            return hosts;
        } catch (Exception e) {
            getLogger().error("根据host获取组件异常，", e);
            throw e;
        }
    }

    /**
     * 保存Ambari删除Host失败记录，
     * 后续由守护任务负责清理
     *
     * @param clusterId
     * @param planId
     * @param hostNames
     * @return
     */
    @Override
    public ResultMsg saveAmbariHostDelete(String clusterId, String planId, List<String> hostNames) {
        ResultMsg msg = new ResultMsg();
        try {
            InfoCluster infoCluster = infoClusterNeoMapper.selectByPrimaryKey(clusterId);
            ConfCluster confCluster = confClusterNeoMapper.selectByPrimaryKey(clusterId);
            String ambari_ip = infoCluster.getAmbariHost();
            String ambariClusterName = getAmbariClusterName(confCluster.getClusterName());

            hostNames.stream().forEach(x->{
                //region 构建delete对象
                InfoClusterAmbariHostDelete infoClusterAmbariHostDelete = new InfoClusterAmbariHostDelete();
                infoClusterAmbariHostDelete.setClusterId(clusterId);
                infoClusterAmbariHostDelete.setPlanId(planId);
                infoClusterAmbariHostDelete.setAmbariServerIp(ambari_ip);
                infoClusterAmbariHostDelete.setAmbariClusterName(ambariClusterName);
                infoClusterAmbariHostDelete.setHostName(x);
                infoClusterAmbariHostDelete.setCreatedTime(new Date());
                infoClusterAmbariHostDelete.setRetryCount(0);
                infoClusterAmbariHostDelete.setStatus(0);
                //endregion
                List<InfoClusterAmbariHostDelete> hisambariHostDeletes = ambariHostDeleteMapper.selectByClusterIdAndHostName(clusterId,x);
                if (hisambariHostDeletes !=null && hisambariHostDeletes.size() >0){
                    getLogger().info("hostName:{},已存在。",x);
                }else{
                    ambariHostDeleteMapper.insertSelective(infoClusterAmbariHostDelete);
                }
            });

            msg.setResult(true);

        }catch (Exception e){
            getLogger().error("保存AmbariHostDelete，异常",e);
            msg.setResult(false);
        }
        return msg;
    }


    /**
     * 从 ambarihostdelete表 获取需要清理的host
     *
     * @return
     */
    @Override
    public List<InfoClusterAmbariHostDelete> getNeedClearAmbariHost() {
        return ambariHostDeleteMapper.selectNeedClearHostsByCreateTime(ambariHostDeleteDuraton);
    }


    /**
     * NodeManager 逐个 Decommission
     *
     * @param clusterId
     * @param hostNames
     * @return
     */
    @Override
    public ResultMsg nodeManagerDecommissionOneByOne(String clusterId, List<String> hostNames) {
        return null;
    }

    /**
     * 批量删除AmbariHost
     *
     * @param deleteList
     * @return
     */
    @Override
    public ResultMsg deleteAmbariHosts(List<InfoClusterAmbariHostDelete> deleteList) {
        ResultMsg msg = new ResultMsg();
        try{
            if (deleteList == null || deleteList.size()==0){
                msg.setResult(false);
                return msg;
            }

            ConfCluster confCluster = confClusterNeoMapper.selectByPrimaryKey(
                    deleteList.get(0).getClusterId()
            );
            if (confCluster.getState().equals(ConfCluster.DELETING)
                    || confCluster.getState().equals(ConfCluster.DELETED))
            {
                //region 集群销毁
                deleteList.stream().forEach(item->{
                    item.setStatus(InfoClusterAmbariHostDelete.STATUS_Complete);
                    item.setModifiedTime(new Date());
                    ambariHostDeleteMapper.updateByPrimaryKeySelective(item);
                });
                msg.setResult(true);
                return msg;
                //endregion
            }else{
                List<String> hostNames = queryAllHosts(confCluster.getClusterId());

                List<InfoClusterAmbariHostDelete> needDeleteHost = new CopyOnWriteArrayList<>();

                //region 检查ambari server 返回hostnames与要删除的是否相同
                deleteList.stream().forEach(x->{
                    if (!hostNames.contains(x.getHostName())){
                        x.setStatus(InfoClusterAmbariHostDelete.STATUS_Complete);
                        x.setModifiedTime(new Date());
                        ambariHostDeleteMapper.updateByPrimaryKeySelective(x);
                    }else{
                        needDeleteHost.add(x);
                    }
                });
                //endregion

                if (needDeleteHost!=null && needDeleteHost.size()>0){
                    List<String> needDeleteHostNames = needDeleteHost.stream()
                            .map(InfoClusterAmbariHostDelete::getHostName)
                            .collect(Collectors.toList());
                    getLogger().info("需要删除的HostNames：{}",needDeleteHostNames);

                    ResultMsg deleteMsg = deleteAmbariHosts(confCluster.getClusterId(),needDeleteHostNames);
                    getLogger().info("删除AmbariHost结果:{}",deleteMsg);
                    if (deleteMsg.getResult()){
                        // 全部删除成功
                        needDeleteHost.stream().forEach(x->{
                            x.setStatus(InfoClusterAmbariHostDelete.STATUS_Complete);
                            x.setModifiedTime(new Date());
                            ambariHostDeleteMapper.updateByPrimaryKeySelective(x);
                        });
                    }else{
                        // 有失败的情况
                        if (deleteMsg.getRows()!=null && deleteMsg.getRows().size()>0) {
                            List<String> failedHosts = deleteMsg.getRows();
                            needDeleteHost.stream().forEach(x->{
                                if (!failedHosts.contains(x.getHostName().toLowerCase())){
                                    x.setStatus(InfoClusterAmbariHostDelete.STATUS_Complete);
                                    x.setModifiedTime(new Date());
                                    ambariHostDeleteMapper.updateByPrimaryKeySelective(x);
                                }
                            });
                        }
                    }
                }
                msg.setResult(true);
            }

        }catch (Exception e){
            getLogger().error("删除异常，",e);
            msg.setResult(false);
            msg.setErrorMsg(ExceptionUtils.getStackTrace(e));
        }
        return msg;
    }
}
