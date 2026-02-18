package com.sunbox.domain.ambari;

import java.io.Serializable;
import java.util.Date;

/**
 * author: wangda
 * date: 2022/12/3
 */
public class AmbariConfigItem implements Serializable,Cloneable{
    /** 自增主键;自增主键 */
    private Long id ;

    /** stack的代码 */
    private String stackCode;

    /** 大数据服务ID */
    private String serviceCode;

    /** 大数据组件ID */
    private String componentCode;

    /** 配置项代码 */
    private String configTypeCode ;

    /** 配置项名 */
    private String key ;

    /** 配置项值 */
    private String value ;

    /** 是否是内容字段 */
    private Integer isContentProp ;

    /** 是否动态计算的配置字段 */
    private Integer isDynamic ;

    /** 动态配置类型 */
    private String dynamicType ;

    /** 配置参数类型  */
    private String itemType;

    /** 状态 */
    private String state;

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
    /** 大数据服务ID */
    public String getServiceCode(){
        return this.serviceCode;
    }
    /** 大数据服务ID */
    public void setServiceCode(String serviceCode){
        this.serviceCode = serviceCode;
    }
    /** 大数据组件ID */
    public String getComponentCode(){
        return this.componentCode;
    }
    /** 大数据组件ID */
    public void setComponentCode(String componentCode){
        this.componentCode = componentCode;
    }

    /** 配置项代码 */
    public String getConfigTypeCode(){
        return this.configTypeCode;
    }
    /** 配置项代码 */
    public void setConfigTypeCode(String configTypeCode){
        this.configTypeCode=configTypeCode;
    }
    /** 配置项名 */
    public String getKey(){
        return this.key;
    }
    /** 配置项名 */
    public void setKey(String key){
        this.key=key;
    }
    /** 配置项值 */
    public String getValue(){
        return this.value;
    }
    /** 配置项值 */
    public void setValue(String value){
        this.value=value;
    }
    /** 是否是内容字段 */
    public Integer getIsContentProp(){
        return this.isContentProp;
    }
    /** 是否是内容字段 */
    public void setIsContentProp(Integer isContentProp){
        this.isContentProp=isContentProp;
    }
    /** 是否动态计算的配置字段 */
    public Integer getIsDynamic(){
        return this.isDynamic;
    }
    /** 是否动态计算的配置字段 */
    public void setIsDynamic(Integer isDynamic){
        this.isDynamic=isDynamic;
    }
    /** 动态配置类型 */
    public String getDynamicType(){
        return this.dynamicType;
    }
    /** 动态配置类型 */
    public void setDynamicType(String dynamicType){
        this.dynamicType=dynamicType;
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

    public String getStackCode() {
        return stackCode;
    }

    public void setStackCode(String stackCode) {
        this.stackCode = stackCode;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}