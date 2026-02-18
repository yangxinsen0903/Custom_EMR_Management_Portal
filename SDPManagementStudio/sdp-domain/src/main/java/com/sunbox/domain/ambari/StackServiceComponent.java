package com.sunbox.domain.ambari;
import java.io.Serializable;
import java.util.Date;

/**
 * Stack布署的大数据组件;
 * @author : wangda
 * @date : 2022-12-3
 */
public class StackServiceComponent implements Serializable,Cloneable{
    /** 自增主键;自增主键 */
    private Long id ;

    /** 服务ID */
    private Long serviceId ;

    /** 组件代码;如：NAMENODE */
    private String code ;

    /** 组件名称;如：NameNode进程 */
    private String name ;

    /** 组件默认安装目录;如：/usr/local/hadoop */
    private String defaultPath ;

    /** 映射外部系统的组件代码;如：NAMENODE */
    private String mappingCode ;

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
    /** 服务ID */
    public Long getServiceId(){
        return this.serviceId;
    }
    /** 服务ID */
    public void setServiceId(Long serviceId){
        this.serviceId=serviceId;
    }
    /** 组件代码;如：NAMENODE */
    public String getCode(){
        return this.code;
    }
    /** 组件代码;如：NAMENODE */
    public void setCode(String code){
        this.code=code;
    }
    /** 组件名称;如：NameNode进程 */
    public String getName(){
        return this.name;
    }
    /** 组件名称;如：NameNode进程 */
    public void setName(String name){
        this.name=name;
    }
    /** 组件默认安装目录;如：/usr/local/hadoop */
    public String getDefaultPath(){
        return this.defaultPath;
    }
    /** 组件默认安装目录;如：/usr/local/hadoop */
    public void setDefaultPath(String defaultPath){
        this.defaultPath=defaultPath;
    }
    /** 映射外部系统的组件代码;如：NAMENODE */
    public String getMappingCode(){
        return this.mappingCode;
    }
    /** 映射外部系统的组件代码;如：NAMENODE */
    public void setMappingCode(String mappingCode){
        this.mappingCode=mappingCode;
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