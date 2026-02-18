package com.sunbox.domain;

import java.util.Date;

/**
    * 密钥配置
    */
public class ConfKeypair {
    /**
    * 密钥对ID
    */
    private String keypairId;

    /**
    * 密钥对名称
    */
    private String keypairName;

    /**
    * 密钥对加密类型;RSA/ED25519
    */
    private String keypairType;

    /**
    * 公钥
    */
    private String publicKey;

    /**
    * 私钥
    */
    private String privateKey;

    /**
    * 创建人
    */
    private String createdby;

    /**
    * 创建时间
    */
    private Date createdTime;

    public String getKeypairId() {
        return keypairId;
    }

    public void setKeypairId(String keypairId) {
        this.keypairId = keypairId;
    }

    public String getKeypairName() {
        return keypairName;
    }

    public void setKeypairName(String keypairName) {
        this.keypairName = keypairName;
    }

    public String getKeypairType() {
        return keypairType;
    }

    public void setKeypairType(String keypairType) {
        this.keypairType = keypairType;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
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
}