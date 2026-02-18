package com.sunbox.properties;

public class TokenProperties {

    private String account;
    private String accountPas;
    private String secret;
    private Integer expiresHours;
    private String headAlg;
    private String headType;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccountPas() {
        return accountPas;
    }

    public void setAccountPas(String accountPas) {
        this.accountPas = accountPas;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Integer getExpiresHours() {
        return expiresHours;
    }

    public void setExpiresHours(Integer expiresHours) {
        this.expiresHours = expiresHours;
    }

    public String getHeadAlg() {
        return headAlg;
    }

    public void setHeadAlg(String headAlg) {
        this.headAlg = headAlg;
    }

    public String getHeadType() {
        return headType;
    }

    public void setHeadType(String headType) {
        this.headType = headType;
    }

    @Override
    public String toString() {
        return "TokenProperties{" +
                "account='" + account + '\'' +
                ", accountPas='" + accountPas + '\'' +
                ", secret='" + secret + '\'' +
                ", expiresHours=" + expiresHours +
                ", headAlg='" + headAlg + '\'' +
                ", headType='" + headType + '\'' +
                '}';
    }
}
