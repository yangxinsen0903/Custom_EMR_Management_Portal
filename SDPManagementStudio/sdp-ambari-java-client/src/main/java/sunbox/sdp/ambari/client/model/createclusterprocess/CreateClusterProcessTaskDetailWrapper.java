package sunbox.sdp.ambari.client.model.createclusterprocess;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

/**
 */
public class CreateClusterProcessTaskDetailWrapper {

    /** href字段 */
    @SerializedName("href")
    private String href;

    /** Task字段 */
    @SerializedName("Tasks")
    private CreateClusterProcessTaskDetail tasks;

    @ApiModelProperty(value = "")
    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    @ApiModelProperty(value = "")
    public CreateClusterProcessTaskDetail getTasks() {
        return tasks;
    }

    public void setTasks(CreateClusterProcessTaskDetail tasks) {
        this.tasks = tasks;
    }
}
