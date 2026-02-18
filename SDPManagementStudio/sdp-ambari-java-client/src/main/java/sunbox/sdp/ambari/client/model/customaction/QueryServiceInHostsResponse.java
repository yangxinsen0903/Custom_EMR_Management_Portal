package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 查询组件所在的主机信息 响应对象
 * @author: wangda
 * @date: 2023/1/14
 */
public class QueryServiceInHostsResponse {

    Logger logger = LoggerFactory.getLogger(QueryServiceInHostsResponse.class);

    @SerializedName("itemTotal")
    private Integer itemTotal;

    @SerializedName("items")
    private List<ServiceInHost> items;

    public Integer getItemTotal() {
        return itemTotal;
    }

    public void setItemTotal(Integer itemTotal) {
        this.itemTotal = itemTotal;
    }

    public List<ServiceInHost> getItems() {
        return items;
    }

    public void setItems(List<ServiceInHost> items) {
        this.items = items;
    }
}
