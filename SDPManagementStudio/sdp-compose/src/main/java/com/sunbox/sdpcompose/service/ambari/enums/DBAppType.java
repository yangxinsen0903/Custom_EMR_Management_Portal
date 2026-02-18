package com.sunbox.sdpcompose.service.ambari.enums;

/**
 * 应用数据库的类型, 区分Hive,Ambari等应用
 * @author: wangda
 * @date: 2022/12/9
 */
public enum DBAppType {
    HIVE_SITE("hive-site",
            "javax.jdo.option.ConnectionDriverName",
            "javax.jdo.option.ConnectionURL",
            "ambari.hive.db.schema.name",
            "javax.jdo.option.ConnectionUserName",
            "javax.jdo.option.ConnectionPassword"),
    HIVE_ENV("hive-env",
            "",
            "",
            "hive_database_name",
            "", ""),
    AMBARI("", "", "", "", "", "");

    DBAppType(String configType, String driverName, String connectionUrl, String dbName, String userName, String password) {
        this.configType = configType;
        this.connectionUrl = connectionUrl;
        this.driverName = driverName;
        this.userName = userName;
        this.password = password;
        this.dbName = dbName;
    }

    private String configType;

    private String connectionUrl;

    private String driverName;

    private String dbName;

    private String userName;

    private String password;

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
}
