package sunbox.sdp.ambari.client.model.customaction;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 查询主机中组件的响应结果
 * @author: wangda
 * @date: 2023/1/12
 */
public class QueryHostsComponentResponse {

    @SerializedName("items")
    private List<QueryHostsComponentItem> items;

    public List<QueryHostsComponentItem> getItems() {
        return items;
    }

    public void setItems(List<QueryHostsComponentItem> items) {
        this.items = items;
    }

    /**
     * 查询一个主机上面的所有组件
     * @param hostName 主机名
     * @return
     */
    public List<String> getComponentNames(String hostName) {
        List<String> components = new ArrayList<>();
        if (Objects.isNull(items)) {
            return components;
        }

        for (QueryHostsComponentItem item : items) {
            if (Objects.isNull(item.getHost()) || !Objects.equals(item.getHost().getHostName(), hostName)) {
                continue;
            }
            List<HostComponent> componentList = item.getHostComponents();

            if (Objects.nonNull(componentList)) {
                for (HostComponent hostComponent : componentList) {
                    components.add(hostComponent.getHostRole().getComponentName());
                }
            }
        }

        return components;
    }


    /**
     * 获取主机状态是HEALTHY并且状态是INSTALLED的组件，
     * @param hostState 主机状态
     * @param components 要判断的主组列表
     * @return 匹配的组件列表
     * @return
     */
    public List<HostRole> getStopedComponentsByHostState(String hostState, List<String> components) {
        List<HostRole> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(components)) {
            return result;
        }
        if (CollectionUtil.isEmpty(items)) {
            return result;
        }

        for (QueryHostsComponentItem item : items) {
            Hosts host = item.getHost();
            if (Objects.isNull(host) || !StrUtil.equalsIgnoreCase(host.getHostState(), hostState)) {
                continue;
            }
            List<HostComponent> hostComponents = item.getHostComponents();
            if (CollectionUtil.isEmpty(hostComponents)) {
                continue;
            }
            for (HostComponent hostComponent : hostComponents) {
                HostRole hostRole = hostComponent.getHostRole();
                if (Objects.isNull(hostRole)) {
                    continue;
                }

                String componentName = hostRole.getComponentName();
                if (!CollectionUtil.contains(components, componentName)) {
                    // 不是要查询的组件，跳过
                    continue;
                }

                if (!StrUtil.equalsIgnoreCase(hostRole.getState(), "INSTALLED")) {
                    continue;
                }
                result.add(hostRole);
            }
        }
        return result;
    }
}
