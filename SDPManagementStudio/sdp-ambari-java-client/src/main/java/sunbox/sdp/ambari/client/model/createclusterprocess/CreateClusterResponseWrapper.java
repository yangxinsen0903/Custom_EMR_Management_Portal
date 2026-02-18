package sunbox.sdp.ambari.client.model.createclusterprocess;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * @author: wangda
 * @date: 2022/12/7
 */
public class CreateClusterResponseWrapper {

    @SerializedName("href")
    /** requestId */
    private String  href;

    @SerializedName("Requests")
    private CreateClusterResponse response;

    public Long getRequestId() {
        if (Objects.nonNull(response)) {
            return new Long(response.getId());
        } else {
            return null;
        }
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public CreateClusterResponse getResponse() {
        return response;
    }

    public void setResponse(CreateClusterResponse response) {
        this.response = response;
    }
}
