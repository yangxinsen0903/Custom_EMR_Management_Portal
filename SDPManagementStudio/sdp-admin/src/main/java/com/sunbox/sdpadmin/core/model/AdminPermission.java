package com.sunbox.sdpadmin.core.model;

import java.util.Date;
import java.util.List;

public class AdminPermission {

    private String id;

    private String pid;

    private String url;

    private String alias;

    private String name;

    private Date createdate;

    private Integer status;

    private Date starttime;

    private Date endtime;

    private Integer menu;

    private Integer reqesttype;

     private Integer sortIndex;

     private String parentName;
    /**
     * 图标
     */
    private String icon;

    public Integer getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(Integer sortIndex) {
        this.sortIndex = sortIndex;
    }

    private List<AdminPermission> childPermission;

    public List<AdminPermission> getChildPermission() {
        return childPermission;
    }

    public void setChildPermission(List<AdminPermission> childPermission) {
        this.childPermission = childPermission;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreatedate() {
        return createdate;
    }

    public void setCreatedate(Date createdate) {
        this.createdate = createdate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }



    public Integer getMenu() {
        return menu;
    }

    public void setMenu(Integer menu) {
        this.menu = menu;
    }

    public Date getStarttime() {
        return starttime;
    }

    public void setStarttime(Date starttime) {
        this.starttime = starttime;
    }

    public Date getEndtime() {
        return endtime;
    }

    public void setEndtime(Date endtime) {
        this.endtime = endtime;
    }

    public Integer getReqesttype() {
        return reqesttype;
    }

    public void setReqesttype(Integer reqesttype) {
        this.reqesttype = reqesttype;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
