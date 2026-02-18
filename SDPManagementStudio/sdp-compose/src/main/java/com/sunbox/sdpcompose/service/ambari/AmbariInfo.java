package com.sunbox.sdpcompose.service.ambari;

import com.fasterxml.jackson.annotation.JsonIgnore;
import sunbox.sdp.ambari.client.ApiClient;

/**
 * Ambari信息, 一台Ambari需要一个Ambari信息对象
 * @author: wangda
 * @date: 2022/12/10
 */
public class AmbariInfo {

    private String baseUri;

    private String referer;

    private String userName;

    private String password;

    /**
     * 实例化一个AmbariInfo
     * @param baseUri Ambari的API地址, 格式为:http://20.118.166.170:8080/api/v1 。注意后面不带斜杠。
     * @param userName 用户名
     * @param password 密码
     * @return AmbariInfo 实例
     */
    public static AmbariInfo of(String baseUri, String userName, String password) {
        AmbariInfo ambari = new AmbariInfo();
        ambari.baseUri = baseUri;
        ambari.userName = userName;
        ambari.password = password;

        // 计算Referer
        int index = baseUri.indexOf("/api/v1");
        if (index > 0) {
            ambari.referer = baseUri.substring(0, index);
        }

        return ambari;
    }

    /**
     * 生成一个通过Http请求访问这个Ambari的Http ApiClient。
     * @return
     */
    @JsonIgnore
    public ApiClient getAmbariApiClient() {
        ApiClient apiClient = ApiClient.newInstance();
        apiClient.setBasePath(baseUri);
        apiClient.setUsername(userName);
        apiClient.setPassword(password);
        apiClient.addDefaultHeader("Referer", referer);

        return apiClient;
    }
}
