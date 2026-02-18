package com.sunbox.domain;

import java.io.Serializable;

/**
 * 场景应用组件基础信息
 * @author: wangda
 * @date: 2022/12/21
 */
public class BaseSceneApps implements Serializable,Cloneable{
    /** 场景ID */
    private String sceneId ;

    /** 组件名称 */
    private String appName ;

    /** 组件版本号 */
    private String appVersion ;

    /** 是否必选;1：必选  0：不必选 */
    private Integer required ;

    /** 排序序号 */
    private Integer sortNo ;


    /** 场景ID */
    public String getSceneId(){
        return this.sceneId;
    }
    /** 场景ID */
    public void setSceneId(String sceneId){
        this.sceneId=sceneId;
    }
    /** 组件名称 */
    public String getAppName(){
        return this.appName;
    }
    /** 组件名称 */
    public void setAppName(String appName){
        this.appName=appName;
    }
    /** 组件版本号 */
    public String getAppVersion(){
        return this.appVersion;
    }
    /** 组件版本号 */
    public void setAppVersion(String appVersion){
        this.appVersion=appVersion;
    }
    /** 是否必选;1：必选  0：不必选 */
    public Integer getRequired(){
        return this.required;
    }
    /** 是否必选;1：必选  0：不必选 */
    public void setRequired(Integer required){
        this.required=required;
    }
    /** 排序序号 */
    public Integer getSortNo(){
        return this.sortNo;
    }
    /** 排序序号 */
    public void setSortNo(Integer sortNo){
        this.sortNo=sortNo;
    }

    @Override
    public String toString() {
        return "BaseSceneApps{" +
                "sceneId='" + sceneId + '\'' +
                ", appName='" + appName + '\'' +
                ", appVersion='" + appVersion + '\'' +
                ", required=" + required +
                ", sortNo=" + sortNo +
                '}';
    }
}