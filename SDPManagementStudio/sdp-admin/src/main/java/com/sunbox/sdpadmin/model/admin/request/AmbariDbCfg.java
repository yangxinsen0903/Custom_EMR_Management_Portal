package com.sunbox.sdpadmin.model.admin.request;

public class AmbariDbCfg {
    private String account;
    private String password;
    private String url;
    private String port;
    private String database;

    public String getAccount() { return account; }
    public void setAccount(String value) { this.account = value; }

    public String getPassword() { return password; }
    public void setPassword(String value) { this.password = value; }

    public String geturl() { return url; }
    public void seturl(String value) { this.url = value; }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }
}