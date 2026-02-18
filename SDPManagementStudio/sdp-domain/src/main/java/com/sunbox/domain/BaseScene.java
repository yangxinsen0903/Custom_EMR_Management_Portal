package com.sunbox.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 场景基础信息
 * @author: wangda
 * @date: 2022/12/21
 */
public class BaseScene implements Serializable,Cloneable{
    /** 场景ID */
    private String sceneId ;

    /** 集群版本号 */
    private String clusterReleaseVer ;

    /** 场景名称 */
    private String sceneName ;

    /** 场景描述 */
    private String sceneDesc ;

    /** 创建时间 */
    private Date createdTime ;

    /** 创建人 */
    private String createdby ;


    /** 场景ID */
    public String getSceneId(){
        return this.sceneId;
    }
    /** 场景ID */
    public void setSceneId(String sceneId){
        this.sceneId=sceneId;
    }
    /** 集群版本号 */
    public String getClusterReleaseVer(){
        return this.clusterReleaseVer;
    }
    /** 集群版本号 */
    public void setClusterReleaseVer(String clusterReleaseVer){
        this.clusterReleaseVer=clusterReleaseVer;
    }
    /** 场景名称 */
    public String getSceneName(){
        return this.sceneName;
    }
    /** 场景名称 */
    public void setSceneName(String sceneName){
        this.sceneName=sceneName;
    }
    /** 场景描述 */
    public String getSceneDesc(){
        return this.sceneDesc;
    }
    /** 场景描述 */
    public void setSceneDesc(String sceneDesc){
        this.sceneDesc=sceneDesc;
    }
    /** 创建时间 */
    public Date getCreatedTime(){
        return this.createdTime;
    }
    /** 创建时间 */
    public void setCreatedTime(Date createdTime){
        this.createdTime=createdTime;
    }
    /** 创建人 */
    public String getCreatedby(){
        return this.createdby;
    }
    /** 创建人 */
    public void setCreatedby(String createdby){
        this.createdby=createdby;
    }
}