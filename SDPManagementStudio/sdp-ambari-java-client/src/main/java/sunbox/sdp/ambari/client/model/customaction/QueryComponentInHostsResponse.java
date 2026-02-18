package sunbox.sdp.ambari.client.model.customaction;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.collections.CollectionUtils;
import org.bouncycastle.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 查询组件所在的主机信息 响应对象
 * @author: wangda
 * @date: 2023/1/14
 */
public class QueryComponentInHostsResponse  {

    Logger logger = LoggerFactory.getLogger(QueryComponentInHostsResponse.class);

    @SerializedName("items")
    private List<ComponentInHost> items;

    public int getHostCount() {
        return items.size();
    }

    /**
     * 找到第一台主机信息
     * @return 如果没有,返回null
     */
    public HostRole getFirstHost() {
        if (CollectionUtils.isEmpty(items)) {
            return null;
        }

        List<HostComponent> hostComponents = items.get(0).getHostComponents();
        if (CollectionUtils.isEmpty(hostComponents)) {
            return null;
        }

        return hostComponents.get(0).getHostRole();
    }

    public List<HostRole> getHosts(String componentName, boolean isActive) {
        componentName = Strings.toUpperCase(componentName);
        List<HostRole> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(items)) {
            return result;
        }
        for (ComponentInHost componentInHost : items) {
            if (CollectionUtils.isEmpty(componentInHost.getHostComponents())) {
                continue;
            }
            for (HostComponent hostComponent : componentInHost.getHostComponents()) {
                String hostComponentName = Strings.toUpperCase(hostComponent.getHostRole().getComponentName());
                if (Objects.equals(componentName, hostComponentName)) {
                    // 组件名相同， 检查是否是active
                    if (!isActive) {
                        result.add(hostComponent.getHostRole());
                        continue;
                    }

                    // 需要检查active时，开始检查
                    ComponentMetrics metrics = hostComponent.getMetrics();
                    if (Objects.isNull(metrics) || Objects.isNull(metrics.getDfs())) {
                        // 如果没有metrics，说明不是active, 放弃此主机
                        continue;
                    }

                    if(metrics.getDfs().getFsNameSystem().getHaState().equalsIgnoreCase("active")) {
                        result.add(hostComponent.getHostRole());
                    }
                }
            }
        }

        return result;
    }

    /**
     *  获取getActive的NameNodeComponentInHost对象
     * @return
     */
    public ComponentInHost getActiveNameNodeComponentInHost(){
        AtomicReference<ComponentInHost> componentInHost=new AtomicReference<>();
        items.stream().forEach(item->{
            item.getHostComponents().stream().forEach(hc->{
                ComponentMetrics metrics=hc.getMetrics();
                if (metrics!=null) {
                    try {
                        if (metrics.getDfs().getFsNameSystem().getHaState().equalsIgnoreCase("active")) {
                            componentInHost.set(item);
                            return;
                        }
                    } catch (Exception e) {
                        logger.error("获取NameNode异常,", e);
                        throw e;
                    }
                }
            });
        });
        if (componentInHost.get()==null){
            return null;
        }else {
            return componentInHost.get();
        }
    }

    /**
     * 获取Active状态的NameNode的HostName
     * @return
     */
    public String getActiveNameNodeHostName(){
        ComponentInHost sd=getActiveNameNodeComponentInHost();
        if (sd!=null) {
            return sd.getHostComponents().get(0).getHostRole().getHostName();
        }else{
            return null;
        }
    }

    /**
     * 找到组件状态是Started的主机
     * @return
     */
    public List<String> getHostsByComponentStarted(String componentName) {
        List<String> hosts = new ArrayList<>();
        if (CollectionUtil.isEmpty(items)) {
            return hosts;
        }

        for (ComponentInHost item : items) {
            ServiceComponentInfo serviceInfo = item.getServiceComponentInfo();
            if (Objects.isNull(serviceInfo)) {
                continue;
            }
            if (!StrUtil.equalsIgnoreCase(componentName, serviceInfo.getComponentName())) {
                continue;
            }
            List<HostComponent> hostComponents = item.getHostComponents();
            for (HostComponent hostComponent : hostComponents) {
                HostRole hostRole = hostComponent.getHostRole();
                // 检查状态为STARTED的主机，关闭的状态是：INSTALLED
                if (StrUtil.equalsIgnoreCase("STARTED", hostRole.getState())) {
                    hosts.add(hostRole.getHostName());
                }
            }
        }

        return hosts;
    }

    /**
     * 找到组件状态是Started的主机，并且这些主机在参定的主机列表中
     * @param inHosts 给定的主机列表
     * @return
     */
    public List<String> getHostsByComponentStarted(String componentName, List<String> inHosts) {
        List<String> hosts = getHostsByComponentStarted(componentName);
        Set inHostsSet = new HashSet<>(inHosts);

        List<String> finalHosts = new ArrayList<>();
        for (String host : hosts) {
            if (inHostsSet.contains(host)) {
                finalHosts.add(host);
            }
        }
        return finalHosts;
    }


    public List<ComponentInHost> getItems() {
        return items;
    }

    public void setItems(List<ComponentInHost> items) {
        this.items = items;
    }
}
