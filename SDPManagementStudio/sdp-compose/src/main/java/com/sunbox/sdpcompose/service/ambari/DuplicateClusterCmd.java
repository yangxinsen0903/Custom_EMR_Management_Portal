package com.sunbox.sdpcompose.service.ambari;

/**
 * 复制集群命令，继承自 <code>CreateClusterCmd</code>，增加一个原集群名称
 * @author: wangda
 * @date: 2022/12/19
 */
public class DuplicateClusterCmd extends CreateClusterCmd {

    private String  srcClusterId;
    /** 源集群名称 */
    private String originClusterName;
    /** 源集群中Ambari信息 */
    private AmbariInfo originAmbariInfo;

    public String getOriginClusterName() {
        return originClusterName;
    }

    public void setOriginClusterName(String originClusterName) {
        this.originClusterName = originClusterName;
    }

    public AmbariInfo getOriginAmbariInfo() {
        return originAmbariInfo;
    }

    public void setOriginAmbariInfo(AmbariInfo originAmbariInfo) {
        this.originAmbariInfo = originAmbariInfo;
    }

    public String getSrcClusterId() {
        return srcClusterId;
    }

    public void setSrcClusterId(String srcClusterId) {
        this.srcClusterId = srcClusterId;
    }
}
