package sunbox.sdp.ambari.client.model.customaction;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: wangda
 * @date: 2023/3/22
 */
public class ConfigGroupTest {

    @Test
    public void completeOneDesiredConfig_新增() {
        ConfigGroup configGroup = new ConfigGroup();
        configGroup.setServiceName("yarn");
        Map<String, Object> properties = new HashMap<>();
        properties.put("yarn.nodemanager.resource.cpu-vcores", "16");
        properties.put("yarn.nodemanager.resource.memory-mb", "64");
        configGroup.completeOneDesiredConfig("yarn-site", properties);
    }

    @Test
    public void completeOneDesiredConfig_存在不覆盖() {
        ConfigGroup configGroup = new ConfigGroup();
        Map<String, Object> aConfig = new HashMap<>();
        Map<String, Object> props = new HashMap<>();
        props.put("yarn.nodemanager.resource.cpu-vcores", "99");
        aConfig.put("type", "yarn-site");
        aConfig.put("properties", props);
        configGroup.addOneDesiredConfig(aConfig);

        Map<String, Object> properties = new HashMap<>();
        properties.put("yarn.nodemanager.resource.cpu-vcores", "16");
        properties.put("yarn.nodemanager.resource.memory-mb", "64");
        configGroup.completeOneDesiredConfig("yarn-site", properties);

        Map<String, Object> expectedProps = (Map<String, Object>)configGroup.getDesiredConfigs().get(0).get("properties");
        Assert.assertEquals(expectedProps.get("yarn.nodemanager.resource.cpu-vcores"), "99");
        Assert.assertEquals(expectedProps.get("yarn.nodemanager.resource.memory-mb"), "64");
    }

    @Test
    public void handleSpark3Config() {
        ConfigGroup configGroup = new ConfigGroup();
        Map<String, Object> aConfig = new HashMap<>();
        Map<String, Object> propertes = new HashMap<>();
        propertes.put("akey", "aValue");
        aConfig.put("type", "spark-defaults");
        aConfig.put("properties", propertes);
        configGroup.addOneDesiredConfig(aConfig);

        configGroup.handleSpark3Config();
        String type = (String)configGroup.getDesiredConfigs().get(0).get("type");
        Assert.assertEquals(type, "spark3-defaults");
    }
}