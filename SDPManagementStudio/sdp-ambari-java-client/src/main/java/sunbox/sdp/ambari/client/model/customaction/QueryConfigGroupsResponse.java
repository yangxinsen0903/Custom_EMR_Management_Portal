package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 查询配置组列表的响应对象
 * @author: wangda
 * @date: 2023/2/11
 */
public class QueryConfigGroupsResponse {

    @SerializedName("href")
    private String href;

    @SerializedName("items")
    private List<ConfigGroupWrapper> items;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public List<ConfigGroupWrapper> getItems() {
        return items;
    }

    public void setItems(List<ConfigGroupWrapper> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "QueryConfigGroupsResponse{" +
                "href='" + href + '\'' +
                ", items=" + items +
                '}';
    }
}
