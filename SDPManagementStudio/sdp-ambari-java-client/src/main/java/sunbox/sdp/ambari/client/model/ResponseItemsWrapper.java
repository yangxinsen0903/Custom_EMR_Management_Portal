package sunbox.sdp.ambari.client.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 针对如下格式报文的封装<br/>
 * <code>
 *     {<br/>
 *   "href" : "http://xxxxx",<br/>
 *   "items" : [<br/>
 *     {<br/>
 *       xxxx<br/>
 *     }<br/>
 *   ]<br/>
 * }
 * </code>
 */
public class ResponseItemsWrapper<T> {

    /** href字段 */
    @SerializedName("href")
    private String href;

    /** items字段 */
    @SerializedName("items")
    private List<T> items;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
