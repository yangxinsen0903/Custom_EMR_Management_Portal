package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

/**
 * 进展状态
 * @author: wangda
 * @date: 2023/1/9
 */
public class ProgressStatus {
    /** requestId */
    @SerializedName("id")
    private Integer id;
    /** 状态 */
    @SerializedName("status")
    private String status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
