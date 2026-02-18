package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * 在进行中的任务的响应
 * @author: wangda
 * @date: 2023/1/9
 */
public class InProgressResponse {
    @SerializedName("href")
    /** requestId */
    private String  href;

    @SerializedName("Requests")
    private ProgressStatus response;

    private boolean taskCompleted = false;

    public Long getRequestId() {
        if (Objects.nonNull(response)) {
            return new Long(response.getId());
        }
        return null;
    }

    /**
     * 任务是否已经完成，如果返回true,不需要再查询，可以认为任务已完成<br/>
     * 请求Ambari完成一个任务时,如果这个任务已经完成,不需要再做查询了, Ambari会返回 200 -> 空报文.
     * @return
     */
    public boolean isTaskCompleted() {
        return this.taskCompleted;
    }

    public void setTaskCompleted(boolean taskCompleted) {
        this.taskCompleted = taskCompleted;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public ProgressStatus getResponse() {
        return response;
    }

    public void setResponse(ProgressStatus response) {
        this.response = response;
    }
}
