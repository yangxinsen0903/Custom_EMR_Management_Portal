package com.sunbox.domain;

import java.util.Date;

/**
    * 字典表
    */
public class BaseDictionary {
    /**
    * 字典ID
    */
    private Integer dictId;

    /**
    * 父节点ID
    */
    private String pdictId;

    /**
    * 字典名称
    */
    private String dictName;

    /**
    * 字典值
    */
    private String dictValue;

    /**
    * 字典别名;别名为英文，且唯一。
    */
    private String aliasName;

    /**
    * 是否删除
    */
    private String isDelete;

    /**
    * 排序
    */
    private String sortno;

    /**
    * 创建人
    */
    private String createdby;

    /**
    * 创建时间
    */
    private Date createdTime;

    /**
    * 修改人
    */
    private String modifiedby;

    /**
    * 修改时间
    */
    private Date modifiedTime;

    public Integer getDictId() {
        return dictId;
    }

    public void setDictId(Integer dictId) {
        this.dictId = dictId;
    }

    public String getPdictId() {
        return pdictId;
    }

    public void setPdictId(String pdictId) {
        this.pdictId = pdictId;
    }

    public String getDictName() {
        return dictName;
    }

    public void setDictName(String dictName) {
        this.dictName = dictName;
    }

    public String getDictValue() {
        return dictValue;
    }

    public void setDictValue(String dictValue) {
        this.dictValue = dictValue;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(String isDelete) {
        this.isDelete = isDelete;
    }

    public String getSortno() {
        return sortno;
    }

    public void setSortno(String sortno) {
        this.sortno = sortno;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getModifiedby() {
        return modifiedby;
    }

    public void setModifiedby(String modifiedby) {
        this.modifiedby = modifiedby;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
}