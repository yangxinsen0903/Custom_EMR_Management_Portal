package com.sunbox.sdpcompose.model.azure.response;

/**
 * @Description: resource response
 * @Title: ResourceSkuResponse
 * @Package: com.sunbox.sdpcompose.model.azure.response
 * @Author: wangshihao
 * @Copyright: 版权
 * @CreateTime: 2022/12/6 19:09
 */
public class ResourceSkuResponse extends BaseSkuResponse{

    private static final long serialVersionUID = 1L;

    /**资源id*/
    private String resourceId;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
}
