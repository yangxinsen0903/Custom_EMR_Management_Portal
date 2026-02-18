package com.sunbox.domain.ambari;

import java.util.Date;

/**
 * Ambari组件部署布局
 * @author: wangda
 * @date: 2022/12/8
 */
public class AmbariComponentLayout {
    public static final String VALID = "VALID";
    public static final String INVALID = "INVALID";
    public static final String DELETED = "DELETED";

    /** 自增主键;自增主键 */
    private Long id ;

    /** Stack版本 */
    private String stackCode;

    /** 大数据服务代码;如：HDFS，YARN，ZOOKEEPER */
    private String serviceCode ;

    /** 主机组名称;固定的几个名称：ambari, master, master1, master2, core, task */
    private String hostGroup ;

    /** 大数据组件代码;如：NAMENODE，DATANODE */
    private String componentCode ;

    /** 是否高可用;1：高可用；0：非高可用 */
    private Integer isHa ;

    /** 状态 VALID, INVALID, DELETED */
    private String state;

    /** 创建人;创建人 */
    private String createdBy ;

    /** 创建时间;创建时间 */
    private Date createdTime ;

    /** 更新人;更新人 */
    private String updatedBy ;

    /** 更新时间;更新时间 */
    private Date updatedTime ;

    public String getStackCode() {
        return stackCode;
    }

    public void setStackCode(String stackCode) {
        this.stackCode = stackCode;
    }

    /** 自增主键;自增主键 */
    public Long getId(){
        return this.id;
    }
    /** 自增主键;自增主键 */
    public void setId(Long id){
        this.id=id;
    }
    /** 大数据服务代码;如：HDFS，YARN，ZOOKEEPER */
    public String getServiceCode(){
        return this.serviceCode;
    }
    /** 大数据服务代码;如：HDFS，YARN，ZOOKEEPER */
    public void setServiceCode(String serviceCode){
        this.serviceCode=serviceCode;
    }
    /** 主机组名称;固定的几个名称：ambari, master, master1, master2, core, task */
    public String getHostGroup(){
        return this.hostGroup;
    }
    /** 主机组名称;固定的几个名称：ambari, master, master1, master2, core, task */
    public void setHostGroup(String hostGroup){
        this.hostGroup=hostGroup;
    }
    /** 大数据组件代码;如：NAMENODE，DATANODE */
    public String getComponentCode(){
        return this.componentCode;
    }
    /** 大数据组件代码;如：NAMENODE，DATANODE */
    public void setComponentCode(String componentCode){
        this.componentCode=componentCode;
    }
    /** 是否高可用;1：高可用；0：非高可用 */
    public Integer getIsHa(){
        return this.isHa;
    }
    /** 是否高可用;1：高可用；0：非高可用 */
    public void setIsHa(Integer isHa){
        this.isHa=isHa;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
