package com.sunbox.sdpadmin.model.admin.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sunbox.domain.BaseUserRole;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class UserRoleRegionOutput {

    private String userId;

    private String userName;

    private String realName;

    private List<BaseUserRole> baseUserRole;

    private List<Map<String,Object>> regionList;

    private String deptId;

    private String deptName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdTime;

    private String createdby;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date modifiedTime;

    private String modifiedby;

    /**
     * 员工工号
     */
    private String emNumber;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId == null ? null : deptId.trim();
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName == null ? null : deptName.trim();
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby == null ? null : createdby.trim();
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getModifiedby() {
        return modifiedby;
    }

    public void setModifiedby(String modifiedby) {
        this.modifiedby = modifiedby == null ? null : modifiedby.trim();
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public List<Map<String, Object>> getRegionList() {
        return regionList;
    }

    public void setRegionList(List<Map<String, Object>> regionList) {
        this.regionList = regionList;
    }

    public List<BaseUserRole> getBaseUserRole() {
        return baseUserRole;
    }

    public void setBaseUserRole(List<BaseUserRole> baseUserRole) {
        this.baseUserRole = baseUserRole;
    }

    public String getEmNumber() {
        return emNumber;
    }

    public void setEmNumber(String emNumber) {
        this.emNumber = emNumber;
    }
}
