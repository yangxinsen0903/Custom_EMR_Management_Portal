package sunbox.sdp.ambari.client.model.createclusterprocess;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

/**
 */
public class CreateClusterProcessTaskWrapper {

    /** href字段 */
    @SerializedName("href")
    private String href;

    /** Task字段 */
    @SerializedName("Tasks")
    private CreateClusterProcessTask tasks;

    @ApiModelProperty(value = "")
    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    @ApiModelProperty(value = "")
    public CreateClusterProcessTask getTasks() {
        return tasks;
    }

    public void setTasks(CreateClusterProcessTask tasks) {
        this.tasks = tasks;
    }
}
