package com.sunbox.sdpcompose.model.azure.response;

/**
 * @Description: PlaybookYml
 * @Title: PlaybookYml
 * @Package: com.sunbox.sdpcompose.model.azure.response
 * @Author: wangshihao
 * @Copyright: 版权
 * @CreateTime: 2022/12/8 22:17
 */
public class PlaybookYmlResponse {

    /**
     * yml文档
     * */
    private String yml;

    /**
     * 返回的URI
     * */
    private String uri;

    public String getYml() {
        return yml;
    }

    public void setYml(String yml) {
        this.yml = yml;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
