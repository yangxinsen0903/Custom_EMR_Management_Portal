package com.sunbox.domain.ambari;

/**
 * @author : [niyang]
 * @className : AmbariInfo
 * @description : [描述说明该类的功能]
 * @createTime : [2023/8/14 11:06 AM]
 */
public class AmbariInfo {

    private String baseUri;

    private String referer;

    private String userName;

    private String password;

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "AmbariInfo{" +
                "baseUri='" + baseUri + '\'' +
                ", referer='" + referer + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
