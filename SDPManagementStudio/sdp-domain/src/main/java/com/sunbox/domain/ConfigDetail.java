package com.sunbox.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * config_detail
 * @author 
 */

public class ConfigDetail  {
    private Integer id;

    private String akey;

    private String avalue;

    /**
     * 应用名称
     */
    private String application;

    /**
     * 应用模块
     */
    private String profile;

    /**
     * 应用环境
     */
    private String label;

    /**
     * 中间件类型
     */
    private String mwtype;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAkey() {
        return akey;
    }

    public void setAkey(String akey) {
        this.akey = akey;
    }

    public String getAvalue() {
        return avalue;
    }

    public void setAvalue(String avalue) {
        this.avalue = avalue;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getMwtype() {
        return mwtype;
    }

    public void setMwtype(String mwtype) {
        this.mwtype = mwtype;
    }
}