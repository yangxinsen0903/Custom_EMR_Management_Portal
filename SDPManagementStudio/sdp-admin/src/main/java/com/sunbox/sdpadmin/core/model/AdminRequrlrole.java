package com.sunbox.sdpadmin.core.model;

import java.util.Date;

public class AdminRequrlrole {
    private String id;

    private String requrl;

    private String islogin;

    private String roleids;

    private String permids;

    private Date createdate;

    private Integer status;

    private Integer sort;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getRequrl() {
        return requrl;
    }

    public void setRequrl(String requrl) {
        this.requrl = requrl == null ? null : requrl.trim();
    }

    public String getIslogin() {
        return islogin;
    }

    public void setIslogin(String islogin) {
        this.islogin = islogin == null ? null : islogin.trim();
    }

    public String getRoleids() {
        return roleids;
    }

    public void setRoleids(String roleids) {
        this.roleids = roleids == null ? null : roleids.trim();
    }

    public String getPermids() {
        return permids;
    }

    public void setPermids(String permids) {
        this.permids = permids == null ? null : permids.trim();
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

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}