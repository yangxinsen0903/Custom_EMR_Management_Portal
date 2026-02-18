package sunbox.sdp.ambari.client.model.createclusterprocess;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * 创建集群进度响应对象
 *
 */
public class CreateClusterProcess {
    /** 请求的URL */
    @SerializedName("href")
    private String href;

    @SerializedName("Requests")
    private CreateClusterProcessRequest request;

    @SerializedName("Stage")
    private CreateClusterProcessStageDetail stageDetail;

    @SerializedName("stages")
    private List<CreateClusterProcessStageWrapper> stages;

    @SerializedName("tasks")
    private List<CreateClusterProcessTaskWrapper> tasks;

    @ApiModelProperty(value = "")
    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    @ApiModelProperty(value = "")
    public CreateClusterProcessRequest getRequest() {
        return request;
    }

    public void setRequest(CreateClusterProcessRequest request) {
        this.request = request;
    }

    @ApiModelProperty(value = "")
    public List<CreateClusterProcessStageWrapper> getStages() {
        return stages;
    }

    public void setStages(List<CreateClusterProcessStageWrapper> stages) {
        this.stages = stages;
    }

    @ApiModelProperty(value = "")
    public List<CreateClusterProcessTaskWrapper> getTasks() {
        return tasks;
    }

    public void setTasks(List<CreateClusterProcessTaskWrapper> tasks) {
        this.tasks = tasks;
    }

    @ApiModelProperty(value = "")
    public CreateClusterProcessStageDetail getStageDetail() {
        return stageDetail;
    }

    public void setStageDetail(CreateClusterProcessStageDetail stageDetail) {
        this.stageDetail = stageDetail;
    }
}
