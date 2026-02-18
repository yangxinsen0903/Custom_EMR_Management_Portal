package com.sunbox.domain.images;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ImageResponse {
    private String imgId;
    private String releaseVersion;
    private String osImageId;
    private String osImageType;
    private String osVersion;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdTime;
    private String imageVersion;

    public String getImageVersion() {
        imageVersion = osImageId.substring(osImageId.lastIndexOf("/") + 1);
        return imageVersion;
    }
}
