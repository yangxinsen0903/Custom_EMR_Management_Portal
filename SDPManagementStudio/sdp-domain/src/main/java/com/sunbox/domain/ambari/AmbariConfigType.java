package com.sunbox.domain.ambari;

import java.io.Serializable;
import java.util.Date;

/**
 * Ambari配置类型
 * author: wangda
 * date: 2022/12/3
 */
public class AmbariConfigType implements Serializable,Cloneable{
    /** 自增主键;自增主键 */
    private Long id ;

    /** 大数据服务ID */
    private Long serviceId ;

    /** 大数据组件ID */
    private Long componentId ;

    /** 配置代码;如：core-site */
    private String code ;

    /** 配置名称;如：HDFS设置环境变量脚本 */
    private String name ;

    /** 文件类型;SHELL, PROPERTIES, YAML, XML */
    private String fileType ;

    /** 是否是配置文件内容;1:是  0：不是 */
    private Integer isFileContent ;

    /** 映射代码;Shein对应的代码为 core-site */
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
    /** 大数据服务ID */
    public Long getServiceId(){
        return this.serviceId;
    }
    /** 大数据服务ID */
    public void setServiceId(Long serviceId){
        this.serviceId=serviceId;
    }
    /** 大数据组件ID */
    public Long getComponentId(){
        return this.componentId;
    }
    /** 大数据组件ID */
    public void setComponentId(Long componentId){
        this.componentId=componentId;
    }
    /** 配置代码;如：core-site */
    public String getCode(){
        return this.code;
    }
    /** 配置代码;如：core-site */
    public void setCode(String code){
        this.code=code;
    }
    /** 配置名称;如：HDFS设置环境变量脚本 */
    public String getName(){
        return this.name;
    }
    /** 配置名称;如：HDFS设置环境变量脚本 */
    public void setName(String name){
        this.name=name;
    }
    /** 文件类型;SHELL, PROPERTIES, YAML, XML */
    public String getFileType(){
        return this.fileType;
    }
    /** 文件类型;SHELL, PROPERTIES, YAML, XML */
    public void setFileType(String fileType){
        this.fileType=fileType;
    }
    /** 是否是配置文件内容;1:是  0：不是 */
    public Integer getIsFileContent(){
        return this.isFileContent;
    }
    /** 是否是配置文件内容;1:是  0：不是 */
    public void setIsFileContent(Integer isFileContent){
        this.isFileContent=isFileContent;
    }
    /** 映射代码;Shein对应的代码为 core-site */
    public String getMappingCode(){
        return this.mappingCode;
    }
    /** 映射代码;Shein对应的代码为 core-site */
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