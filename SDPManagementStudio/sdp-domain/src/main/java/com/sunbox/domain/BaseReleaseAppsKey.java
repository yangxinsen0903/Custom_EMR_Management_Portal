package com.sunbox.domain;

public class BaseReleaseAppsKey {
    private String releaseVersion;

    private String appName;

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion == null ? null : releaseVersion.trim();
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName == null ? null : appName.trim();
    }
}