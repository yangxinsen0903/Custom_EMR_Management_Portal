package com.sunbox.sdpscale.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.sunbox.dao.mapper.InfoClusterVmNeoMapper;
import com.sunbox.domain.ConfCluster;
import com.sunbox.domain.InfoClusterVm;
import com.sunbox.sdpscale.mapper.ConfClusterMapper;
import com.sunbox.sdpscale.model.ClusterMetrics;
import com.sunbox.sdpscale.service.MetricService;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class MetricServiceImpl implements MetricService, BaseCommonInterFace {

    @Autowired
    private ConfClusterMapper confClusterMapper;

    @Autowired
    private InfoClusterVmNeoMapper infoClusterVmMapper;

    @Qualifier("rmCache")
    @Autowired
    private Cache<String,List<String>> rmCaches;

    /**
     * 容器分配比率
     *
     * @param clusterMetrics
     * @return
     */
    @Override
    public int metricVCoreAvailablePrecentage(ClusterMetrics clusterMetrics) {
        int total
                = clusterMetrics.getAvailableVCores()
                + clusterMetrics.getAllocatedVCores()
                + clusterMetrics.getPendingVCores()
                + clusterMetrics.getReservedVCores();
        if (total == 0) {
            return 0;
        }
        BigDecimal availableVCores = new BigDecimal(clusterMetrics.getAvailableVCores() + "");
        BigDecimal decTotal = new BigDecimal(total + "");
        int value = availableVCores.divide(decTotal, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).intValue();
        getLogger().info("容器分配比例，ClusterId={},AvailableVCores={},AllocatedVCores={},PendingVCores={},ReservedVCores={},metricValue={}",
                clusterMetrics.getClusterId(),
                clusterMetrics.getAvailableVCores(),
                clusterMetrics.getAllocatedVCores(),
                clusterMetrics.getPendingVCores(),
                clusterMetrics.getReservedVCores(), value);
        return value;
    }

    /**
     * 可用内存百分比
     *
     * @param clusterMetrics
     * @return
     */
    @Override
    public int metricMemoryAvailablePrecentage(ClusterMetrics clusterMetrics) {
        long total
                = clusterMetrics.getAllocatedMB()
                + clusterMetrics.getAvailableMB()
                + clusterMetrics.getPendingMB()
                + clusterMetrics.getReservedMB();
        if (total == 0) {
            return 0;
        }
        BigDecimal availableMB = new BigDecimal(clusterMetrics.getAvailableMB() + "");
        BigDecimal decTotal = new BigDecimal(total + "");
        int value = availableMB.divide(decTotal, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).intValue();
        getLogger().info("可用内存百分比，ClusterId={},AllocatedMB={},AvailableMB={},PendingMB={},ReservedMB={},metricValue={}",
                clusterMetrics.getClusterId(),
                clusterMetrics.getAllocatedMB(),
                clusterMetrics.getAvailableMB(),
                clusterMetrics.getPendingMB(),
                clusterMetrics.getReservedMB(), value);
        return value;
    }

    /**
     * 可用vCore百分比
     *
     * @param clusterMetrics
     * @return
     */
    @Override
    public int metricContainerPendingRatio(ClusterMetrics clusterMetrics) {
        if (clusterMetrics.getAllocatedContainers() == 0) {
            return 0;
        }
        BigDecimal pendingContainers = new BigDecimal(clusterMetrics.getPendingContainers() + "");
        BigDecimal allocatedContainers = new BigDecimal(clusterMetrics.getAllocatedContainers() + "");
        int value = pendingContainers.divide(allocatedContainers, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).intValue();
        getLogger().info("可用vCore百分比，ClusterId={},PendingContainers={},AllocatedContainers={},metricValue={}",
                clusterMetrics.getClusterId(),
                clusterMetrics.getPendingContainers(),
                clusterMetrics.getAllocatedContainers(), value);
        return value;
    }

    /**
     * 应用程序挂起数
     * @param clusterMetrics
     * @return
     */
    @Override
    public int metricAppsPending(ClusterMetrics clusterMetrics) {
        getLogger().info("应用程序挂起数，ClusterId={},AppsPending={}",
                clusterMetrics.getClusterId(),
                clusterMetrics.getAppsPending());
        return clusterMetrics.getAppsPending();
    }

    /**
     * 获取ResourceMangerHostName
     * HA 获取vmRole = master
     * 非HA获取 vmRole = ambari
     *
     * @param clusterId
     * @return
     */
    @Override
    public List<String> getResourceManagerHostNames(String clusterId) {
        List<String> hosts = rmCaches.getIfPresent(clusterId);
        if (hosts != null){
            return hosts;
        }else{
            ConfCluster confCluster = confClusterMapper.selectByPrimaryKey(clusterId);
            List<String> hostlist = new CopyOnWriteArrayList<>();
            List<InfoClusterVm> clusterVms;
            if (confCluster.getIsHa().equals(1)){
                clusterVms = infoClusterVmMapper.queryByClusterIdAndVmRole(clusterId,"master");
            }else{
                clusterVms = infoClusterVmMapper.queryByClusterIdAndVmRole(clusterId,"ambari");
            }

            clusterVms.stream().forEach(x->{
                hostlist.add(x.getHostName());
            });
            if (hostlist.size()>0){
                rmCaches.put(clusterId,hostlist);
                return hostlist;
            }else{
                return null;
            }
        }
    }
}
