package com.sunbox.sdpadmin.model.admin.response;

/**
 * @date 2023/5/30
 */
public class ClusterAvalibaleImageDto {

    private String imgId;

    private String osImageId;

    private String imageVersion = "";

    public String getImgId() {
        return imgId;
    }

    public void setImgId(String imgId) {
        this.imgId = imgId;
    }

    public String getOsImageId() {
        return osImageId;
    }

    public void setOsImageId(String osImageId) {
        this.osImageId = osImageId;
        int idx = osImageId.lastIndexOf("/");
        if (idx > 0) {
            this.imageVersion = osImageId.substring(idx + 1);
        } else {
            this.imageVersion = osImageId;
        }
    }

    public String getImageVersion() {
        return imageVersion;
    }
}
