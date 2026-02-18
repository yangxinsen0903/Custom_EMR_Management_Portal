package sunbox.sdp.ambari.client.model.customaction;

import cn.hutool.core.collection.CollectionUtil;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 在进行中的任务的响应
 *
 * @author: wangda
 * @date: 2023/1/9
 */
public class CreateConfigGroupResponse {
    @SerializedName("resources")
    private List<Resource> resources;

    /**
     * 取第一个Id
     * @return
     */
    public Integer getSingleId() {
        if (CollectionUtil.isEmpty(resources)) {
            return null;
        }

        return resources.get(0).getConfigGroup().getId();
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public static class Resource {
        @SerializedName("href")
        private String href;
        @SerializedName("ConfigGroup")
        private ConfigGroup configGroup;

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public ConfigGroup getConfigGroup() {
            return configGroup;
        }

        public void setConfigGroup(ConfigGroup configGroup) {
            this.configGroup = configGroup;
        }
    }

    public static class ConfigGroup {
        @SerializedName("id")
        private Integer id;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }
    }
}
