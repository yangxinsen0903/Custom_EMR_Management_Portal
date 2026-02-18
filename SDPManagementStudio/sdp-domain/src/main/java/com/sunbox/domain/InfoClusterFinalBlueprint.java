package com.sunbox.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 销毁集群配置表(InfoClusterFinalBlueprint)实体类
 *
 * @since 2024-07-02 18:31:01
 */
@Data
public class InfoClusterFinalBlueprint implements Serializable {
    private static final long serialVersionUID = -28374681680399667L;
    /**
     * 集群ID
     */
    private String clusterId;
    /**
     * 集群名称
     */
    private String clusterName;
    /**
     * ambari地址
     */
    private String ambariHost;
    /**
     * Blueprint内容
     */
    private String blueprintContent;
    /**
     * 创建时间
     */
    private Date createTime;

}

