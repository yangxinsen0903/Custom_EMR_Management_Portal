package sunbox.sdp.ambari.client.model.createclusterprocess;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 */
public class CreateClusterProcessStageWrapper {

    /** href字段 */
    @SerializedName("href")
    private String href;

    /** Stage字段 */
    @SerializedName("Stage")
    private CreateClusterProcessStage stage;

    @SerializedName("tasks")
    private List<CreateClusterProcessTaskWrapper> tasks;

    public List<CreateClusterProcessTaskWrapper> getTasks() {
        return tasks;
    }

    public void setTasks(List<CreateClusterProcessTaskWrapper> tasks) {
        this.tasks = tasks;
    }

    @ApiModelProperty(value = "")
    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    @ApiModelProperty(value = "")
    public CreateClusterProcessStage getStage() {
        return stage;
    }

    public void setStage(CreateClusterProcessStage stage) {
        this.stage = stage;
    }
}
