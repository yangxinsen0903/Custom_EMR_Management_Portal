package com.sunbox.sdpcompose.service.ambari.blueprint;

import com.sunbox.domain.ambari.AmbariComponentLayout;
import com.sunbox.sdpcompose.mapper.AmbariComponentLayoutMapper;
import com.sunbox.sdpcompose.service.ambari.enums.HostGroupRole;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 默认部署布局
 * @author: wangda
 * @date: 2022/12/5
 */
@Service("DefaultDeployLayoutGenerator")
public class DefaultDeployLayoutGenerator extends DeployLayoutGenerator{

    private final String AMBARI = "AMBARI";

    private final String MASTER1 = "MASTER1";

    private final String MASTER2 = "MASTER2";

    private final String CORE = "CORE";

    private final String TASK = "TASK";

    @Autowired
    private AmbariComponentLayoutMapper mapper;

    @Override
    public List<HostGroup> generate(String stackName, Map<HostGroupRole, Integer> hostGroups, List<String> services, boolean isHa) {
        List<HostGroup> layout = new ArrayList<>();
        if (isHa) {
            // Ambari
            addHostGroupToLayout(stackName,HostGroupRole.AMBARI, AMBARI, layout, services, isHa);

            // master1
            addHostGroupToLayout(stackName,HostGroupRole.MASTER, MASTER1, layout, services, isHa);

            // master2
            addHostGroupToLayout(stackName,HostGroupRole.MASTER, MASTER2, layout, services, isHa);

        } else {
            // master1
            addHostGroupToLayout(stackName,HostGroupRole.MASTER, MASTER1, layout, services, isHa);
        }

        // core
        addHostGroupToLayout(stackName,HostGroupRole.CORE, CORE, layout, services, isHa);

        // task
        addHostGroupToLayout(stackName,HostGroupRole.TASK, TASK, layout, services, isHa);

        return layout;
    }

    @Override
    public List<HostGroup> generate(String stackName, List<ClusterHostGroup> hostGroups, List<String> services, boolean isHa) {
        List<HostGroup> layout = new ArrayList<>();
        // 高可用与非高可用的分组不同，这是历史遗留问题，在开发之初没有梳理出来正确的领域模型，留下一个天坑，需要硬编码处理。
        if (isHa) {
            // Ambari
            addHostGroupToLayout(stackName,HostGroupRole.AMBARI, AMBARI, layout, services, isHa);

            // master1
            addHostGroupToLayout(stackName,HostGroupRole.MASTER, MASTER1, layout, services, isHa);

            // master2
            addHostGroupToLayout(stackName,HostGroupRole.MASTER, MASTER2, layout, services, isHa);

        } else {
            // master1
            addHostGroupToLayout(stackName,HostGroupRole.MASTER, MASTER1, layout, services, isHa);
        }

        // Core也特殊处理   2024-12-10 Core实例组的名称也按SKU生成, 所以与TASK一样的处理方式
        // addHostGroupToLayoutUseRole(stackName, HostGroupRole.CORE, CORE, layout, services, isHa);

        EnumSet<HostGroupRole> skipSet = EnumSet.of(HostGroupRole.AMBARI, HostGroupRole.MASTER);
        for (ClusterHostGroup hostGroup : hostGroups) {
            // 由于Ambari和Master已经上面处理过了，所以此处跳过
            if (skipSet.contains(hostGroup.getRole())) {
                continue;
            }
            addHostGroupToLayoutUseRole(stackName, hostGroup.getRole(), hostGroup.getGroupName(), layout, services, isHa);
        }

        return layout;
    }

    private void addHostGroupToLayoutUseRole(String stackVersion, HostGroupRole hostGroupRole, String groupName,
                                             List<HostGroup> layout, List<String> services,
                                             boolean isHa) {
        HostGroup group = buildHostGroup(stackVersion, hostGroupRole.name(), services, isHa);
        group.setName(groupName);
        //添加默认参数导出判断分组
        HashMap<String, Map<String, Object>> configMap = new HashMap<>();
        HashMap<String, Object> config = new HashMap<>();
        config.put("host.group", groupName);
        configMap.put("yarn-site",config);
        group.getConfigurations().add(configMap);
        if (Objects.nonNull(group) && CollectionUtils.isNotEmpty(group.getComponents())) {
            group.setHostGroupRole(hostGroupRole);
            layout.add(group);
        }
    }

    private void addHostGroupToLayout(String stackVersion, HostGroupRole hostGroupRole, String groupName, List<HostGroup> layout, List<String> services, boolean isHa) {
        HostGroup group = buildHostGroup(stackVersion, groupName, services, isHa);
        if (Objects.nonNull(group) && CollectionUtils.isNotEmpty(group.getComponents())) {
            group.setHostGroupRole(hostGroupRole);
            layout.add(group);
        }
    }

    protected HostGroup buildHostGroup(String stackVersion, String groupName, List<String> services, boolean isHa) {
        HostGroup group = new HostGroup(groupName);
        List<AmbariComponentLayout> layoutList = mapper.queryByHostGroupAndServiceCode(stackVersion, groupName, services, isHa? 1: 0);
        for (AmbariComponentLayout cmp : layoutList) {
            group.addComponent(cmp.getComponentCode());
        }
        return group;
    }

}
