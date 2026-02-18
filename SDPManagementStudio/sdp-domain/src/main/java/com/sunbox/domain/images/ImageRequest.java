package com.sunbox.domain.images;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class ImageRequest {

    @NotEmpty(message = "镜像资源id不能为空")
    private String imgId;

    private String releaseVersion;

    private Integer pageIndex;

    private Integer pageSize;


    private Integer pageStart;
    private Integer pageLimit;

    public void page(){
        int pageIndex= (this.pageIndex == null ? 1 : this.pageIndex);
        int pageSize= (this.pageSize == null ? 20 : this.pageSize);
        this.pageStart = (pageIndex-1) * pageSize;
        this.pageLimit = pageSize;
    }


}
