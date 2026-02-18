package sunbox.sdp.ambari.client.model.createclusterprocess;

/**
 * @author: wangda
 * @date: 2022/12/7
 */
public class CreateClusterResponse {

    /** requestId */
    private Integer id;
    /** 状态 */
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
