package com.sunbox.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户上传的需要执行的脚本库
 * @author: wangda
 * @date: 2022/12/23
 */
public class BaseScript implements Serializable,Cloneable{
    /** 脚本ID（UUID） */
    private String scriptId ;

    /** 脚本名 */
    private String scriptName ;

    /** 脚本在blob上的存储路径 */
    private String blobPath ;

    /** 脚本上传时间 */
    private Date uploadTime ;

    /** 脚本备注 */
    private String remark ;

    /** 脚本状态;VALID，INVALID，DELETED */
    private String state ;

    /** 脚本ID（UUID） */
    public String getScriptId(){
        return this.scriptId;
    }
    /** 脚本ID（UUID） */
    public void setScriptId(String scriptId){
        this.scriptId=scriptId;
    }
    /** 脚本名 */
    public String getScriptName(){
        return this.scriptName;
    }
    /** 脚本名 */
    public void setScriptName(String scriptName){
        this.scriptName=scriptName;
    }
    /** 脚本在blob上的存储路径 */
    public String getBlobPath(){
        return this.blobPath;
    }
    /** 脚本在blob上的存储路径 */
    public void setBlobPath(String blobPath){
        this.blobPath=blobPath;
    }
    /** 脚本上传时间 */
    public Date getUploadTime(){
        return this.uploadTime;
    }
    /** 脚本上传时间 */
    public void setUploadTime(Date uploadTime){
        this.uploadTime=uploadTime;
    }
    /** 脚本备注 */
    public String getRemark(){
        return this.remark;
    }
    /** 脚本备注 */
    public void setRemark(String remark){
        this.remark=remark;
    }
    /** 脚本状态;VALID，INVALID，DELETED */
    public String getState(){
        return this.state;
    }
    /** 脚本状态;VALID，INVALID，DELETED */
    public void setState(String state){
        this.state=state;
    }
}
