package sunbox.sdp.ambari.client.model.customaction;

/**
 * @author: wangda
 * @date: 2023/1/9
 */
public enum OperationLevelEnum {
    /** 主机的组件 */
    HOST_COMPONENT,
    /** 大数据服务 */
    SERVICE,
    /** 集群 */
    CLUSTER,
    /** 主机 */
    HOST;
}
