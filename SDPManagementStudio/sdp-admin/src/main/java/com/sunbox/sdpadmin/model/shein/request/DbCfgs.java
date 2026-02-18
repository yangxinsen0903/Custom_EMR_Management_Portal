package com.sunbox.sdpadmin.model.shein.request;

/**
 * @author : [niyang]
 * @className : DbCfgs
 * @description : [描述说明该类的功能]
 * @createTime : [2022/12/28 7:46 PM]
 */
public class DbCfgs {

    private String url;

    private String account;

    private String password;

    private Integer port;

    private String database;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    @Override
    public String toString() {
        return "DbCfgs{" +
                "url='" + url + '\'' +
                ", account='" + account + '\'' +
                ", password='" + password + '\'' +
                ", port='" + port + '\'' +
                ", database='" + database + '\'' +
                '}';
    }
}
