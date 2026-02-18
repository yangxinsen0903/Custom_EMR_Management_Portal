package com.sunbox.sdpadmin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.sunbox.domain.InfoCluster;
import com.sunbox.sdpadmin.mapper.ConfClusterMapper;
import com.sunbox.sdpadmin.mapper.InfoClusterMapper;
import com.sunbox.sdpadmin.mapper.InfoClusterVmMapper;
import com.sunbox.sdpadmin.service.AutoStartClusterComponentService;
import com.sunbox.sdpadmin.util.RedisConst;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.util.L;
import io.jsonwebtoken.lang.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sunbox.sdp.ambari.client.ApiClient;
import sunbox.sdp.ambari.client.Configuration;
import sunbox.sdp.ambari.client.api.CustomActionApi;
import sunbox.sdp.ambari.client.model.customaction.HostRole;
import sunbox.sdp.ambari.client.model.customaction.QueryHostsComponentResponse;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @date 2023/6/20
 */
@Service
public class AutoStartClusterComponentServiceImpl implements AutoStartClusterComponentService {
    private Logger logger = LoggerFactory.getLogger(AutoStartClusterComponentServiceImpl.class);

    @Value("${ambari.default.user:admin}")
    private String username;

    @Value("${ambari.default.pwd:admin}")
    private String password;

    /**
     * 是否开启Ambari Api调用调试模式, 1:开启  0:不开启 <br/> 默认开启调试模式,便于查问题
     */
    @Value("${ambari.api.debug:1}")
    private String debug = "1";

    @Autowired
    private ConfClusterMapper clusterMapper;

    @Autowired
    private InfoClusterMapper infoClusterMapper;

    @Autowired
    private InfoClusterVmMapper vmMapper;

    @Autowired
    private DistributedRedisLock redisLock;

    @Override
    public void autoStartClusterComponents() {
        logger.info("开始检查并启动集群中异常停止的组件....");
        // Redis锁控制一次只能有一个实例执行
        String lockKey = RedisConst.keyLockCheckStoppedComponent();
        boolean lockSuccess = redisLock.tryLock(lockKey, TimeUnit.SECONDS, 0, 20*RedisConst.EXPIRES_ONE_MINITE);
        if (!lockSuccess) {
            logger.info("已有[检查并启动集群中异常停止的组件]任务在执行，本次不执行");
            return;
        }

        try {
            // 获取所有运行中的集群
            Map<String, Object> params = new HashMap<>();
            params.put("emrStatus", Arrays.asList("2"));
            List<Map> clusters = clusterMapper.selectByObject(params);

            // 获取集群所有运行中且不是维护模式的主机
            Integer progress = 0;
            Integer total = clusters.size();
            for (Map cluster : clusters) {
                String clusterId = (String) cluster.get("cluster_id");
                String clusterName = (String) cluster.get("cluster_name");

                progress ++;

                L l = L.b().p("clusterId", clusterId).p("clusterName", clusterName);
                logger.info(calculateCheckPercent(progress, total) + " 开始检查集群未启动的组件, " + l.s());
                try {
                    // 从Ambari中获取集群所有运行中且不是维护模式的主机上的所有组件
                    List<Map> vms = vmMapper.selectAllRunningVms(clusterId, Arrays.asList("core", "task"));
                    List<String> hosts = vms.stream().map(vm -> (String) vm.get("host_name")).collect(Collectors.toList());

                    // 关闭的组件自动启动
                    CustomActionApi ambariApiClient = getAmbariApiClient(clusterId);

                    QueryHostsComponentResponse response = ambariApiClient.queryHostsComponents(generateAmbariClusterName(clusterName), hosts);
                    List<HostRole> components = response.getStopedComponentsByHostState("HEALTHY",
                            Arrays.asList("HBASE_REGIONSERVER", "NODEMANAGER", "DATANODE"));

                    for (HostRole component : components) {
                        L compInfo = L.b().p("clusterId", clusterId)
                                .p("clusterName", clusterName)
                                .p("hostName", component.getHostName())
                                .p("componentName", component.getComponentName());
                        try {
                            logger.info("集群组件未启动，开始启动组件, " + compInfo.s());
                            ambariApiClient.startHostComponent(generateAmbariClusterName(clusterName), Arrays.asList(component.getHostName()), Arrays.asList(component.getComponentName()));
                        } catch (Exception e) {
                            logger.error("重启集群组件失败, " + compInfo.s(), e);
                        }
                    }
                } catch (Exception ex) {
                    logger.error("重启集群组件失败，" + l.s(), ex);
                }
            }
        } finally {
            redisLock.unlock(lockKey);
        }
    }

    public CustomActionApi getAmbariApiClient(String clusterId) {
        InfoCluster infoCluster = infoClusterMapper.selectByPrimaryKey(clusterId);
        Assert.notNull(infoCluster, "没找到集群信息，clusterId=" + clusterId);
        String baseUri = "http://" + infoCluster.getAmbariHost() + ":8080/api/v1";
//        baseUri = "http://20.172.10.47:8765/api/v1";
        ApiClient apiClient = ApiClient.newInstance();
        apiClient.setBasePath(baseUri);
        apiClient.setUsername(username);
        apiClient.setPassword(password);
        // 计算Referer
        int index = baseUri.indexOf("/api/v1");
        if (index > 0) {
            apiClient.addDefaultHeader("Referer", baseUri.substring(0, index));
        }
        Configuration.setDefaultApiClient(apiClient);
        Configuration.getDefaultApiClient().setBasePath(baseUri);
        Configuration.getDefaultApiClient().setDebugging(StrUtil.equalsIgnoreCase(debug, "1"));
        return new CustomActionApi();
    }

    public String generateAmbariClusterName(String clusterName) {
        // 将clusterName中的_替换为空字符串
        return clusterName.replaceAll("-", "").replaceAll("_", "");
    }

    public String calculateCheckPercent(Integer progress, Integer total) {
        if (progress == null || total == null || total == 0) {
            return "0%";
        }
        return new DecimalFormat("0.00").format(progress * 100.0 / total) + "%";
    }

}
