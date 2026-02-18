package sunbox.sdp.ambari.client.model;

/**
 * @author: wangda
 * @date: 2022/12/8
 */
public enum ServiceOp {
    START("STARTED"),
    STOP("INSTALLED"),
    RESTART("");

    private final String state;

    ServiceOp(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
}
