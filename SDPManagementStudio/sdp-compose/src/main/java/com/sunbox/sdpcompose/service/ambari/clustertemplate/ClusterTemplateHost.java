package com.sunbox.sdpcompose.service.ambari.clustertemplate;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 集群创建模板中主机组里的一个主机信息<br>
 *
 * @author: wangda
 * @date: 2022/12/7
 */
public class ClusterTemplateHost {
    /** 主机组名称, FQDN：(Fully Qualified Domain Name)全限定域名*/
    @JsonProperty("fqdn")
    String fqdn;

    public ClusterTemplateHost() {

    }

    public ClusterTemplateHost(String fqdn) {
        this.fqdn = fqdn;
    }

    public String getFqdn() {
        return fqdn;
    }

    public void setFqdn(String fqdn) {
        this.fqdn = fqdn;
    }
}
