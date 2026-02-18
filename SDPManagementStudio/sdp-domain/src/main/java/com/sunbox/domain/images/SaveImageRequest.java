package com.sunbox.domain.images;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class SaveImageRequest {
    /**
     * sdp版本
     */
    @NotEmpty(message = "sdp版本不能为空")
    private String releaseVersion;
    /**
     * 镜像资源id
     */
    @NotEmpty(message = "镜像资源id不能为空")
    private String osImageId;
    /**
     * 操作系统版本
     */
    @NotEmpty(message = "操作系统版本不能为空")
    private String osVersion;

    private String createdby;

    private List<SaveImageScriptRequest> imageScriptList;
}
