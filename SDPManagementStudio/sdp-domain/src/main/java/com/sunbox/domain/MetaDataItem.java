package com.sunbox.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName meta_data_item
 */
@Data
public class MetaDataItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String region;

    private String type;

    private String version;

    private String data;

    private String remark;

    private Date createTime;

    private Date lastModifiedTime;

    private String createUserId;

    private String lastModifiedId;
}