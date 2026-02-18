package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QueryConfigGroupDetailsResponse {
    @SerializedName("href")
    private String href;

    @SerializedName("items")
    private List<ConfigGroupDetailWrapper> items;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public List<ConfigGroupDetailWrapper> getItems() {
        return items;
    }

    public void setItems(List<ConfigGroupDetailWrapper> items) {
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
