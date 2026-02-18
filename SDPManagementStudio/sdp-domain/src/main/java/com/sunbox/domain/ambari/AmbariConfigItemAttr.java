package com.sunbox.domain.ambari;

import java.util.Date;

/**
 * Ambari配置项属性，默认在Stack中的配置
 * @author: wangda
 * @date: 2022/12/9
 */
public class AmbariConfigItemAttr {
    /** 自增主键;自增主键 */
    private Long id ;

    /** Stack的Code;如：SDP-1.0.0 */
    private String stackCode ;

    /** 大数据服务Code;如：HADOOP */
    private String serviceCode ;

    /** 大数据组件Code;如：NAMENODE */
    private String componentCode ;

    /** 配置项代码;如:core-site */
    private String configTypeCode ;

    /** 标识;生成配置的对象Tag名，如：final */
    private String tagName ;

    /** 配置项名;配置项名称 */
    private String key ;

    /** 配置项值;配置项值 */
    private String value ;

    /** 状态;VALID，INVALID，DELETED */
    private String state ;

    /** 创建人;创建人 */
    private String createdBy ;

    /** 创建时间;创建时间 */
    private Date createdTime ;
    /** 更新人;更新人 */
    private String updatedBy ;

    /** 更新时间;更新时间 */
    private Date updatedTime ;

    /** 自增主键;自增主键 */
    public Long getId(){
        return this.id;
    }
    /** 自增主键;自增主键 */
    public void setId(Long id){
        this.id=id;
    }
    /** Stack的Code;如：SDP-1.0.0 */
    public String getStackCode(){
        return this.stackCode;
    }
    /** Stack的Code;如：SDP-1.0.0 */
    public void setStackCode(String stackCode){
        this.stackCode=stackCode;
    }
    /** 大数据服务Code;如：HADOOP */
    public String getServiceCode(){
        return this.serviceCode;
    }
    /** 大数据服务Code;如：HADOOP */
    public void setServiceCode(String serviceCode){
        this.serviceCode=serviceCode;
    }
    /** 大数据组件Code;如：NAMENODE */
    public String getComponentCode(){
        return this.componentCode;
    }
    /** 大数据组件Code;如：NAMENODE */
    public void setComponentCode(String componentCode){
        this.componentCode=componentCode;
    }
    /** 配置项代码;如:core-site */
    public String getConfigTypeCode(){
        return this.configTypeCode;
    }
    /** 配置项代码;如:core-site */
    public void setConfigTypeCode(String configTypeCode){
        this.configTypeCode=configTypeCode;
    }
    /** 标识;生成配置的对象Tag名，如：final */
    public String getTagName(){
        return this.tagName;
    }
    /** 标识;生成配置的对象Tag名，如：final */
    public void setTagName(String tagName){
        this.tagName=tagName;
    }
    /** 配置项名;配置项名称 */
    public String getKey(){
        return this.key;
    }
    /** 配置项名;配置项名称 */
    public void setKey(String key){
        this.key=key;
    }
    /** 配置项值;配置项值 */
    public String getValue(){
        return this.value;
    }
    /** 配置项值;配置项值 */
    public void setValue(String value){
        this.value=value;
    }
    /** 状态;VALID，INVALID，DELETED */
    public String getState(){
        return this.state;
    }
    /** 状态;VALID，INVALID，DELETED */
    public void setState(String state){
        this.state=state;
    }
    /** 创建人;创建人 */
    public String getCreatedBy(){
        return this.createdBy;
    }
    /** 创建人;创建人 */
    public void setCreatedBy(String createdBy){
        this.createdBy=createdBy;
    }
    /** 创建时间;创建时间 */
    public Date getCreatedTime(){
        return this.createdTime;
    }
    /** 创建时间;创建时间 */
    public void setCreatedTime(Date createdTime){
        this.createdTime=createdTime;
    }
    /** 更新人;更新人 */
    public String getUpdatedBy(){
        return this.updatedBy;
    }
    /** 更新人;更新人 */
    public void setUpdatedBy(String updatedBy){
        this.updatedBy=updatedBy;
    }
    /** 更新时间;更新时间 */
    public Date getUpdatedTime(){
        return this.updatedTime;
    }
    /** 更新时间;更新时间 */
    public void setUpdatedTime(Date updatedTime){
        this.updatedTime=updatedTime;
    }
}
