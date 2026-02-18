package com.sunbox.sdpcompose.service.ambari.blueprint;

import com.sunbox.sdpcompose.service.ambari.enums.DBAppType;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 数据库连接信息, 用于设置大数据组件配置中的数据库信息.与 <code>DBAppType</code> 搭配使用
 * @author: wangda
 * @date: 2022/12/9
 */
public class DBConnectInfo {

    /** 应用类型 */
    private DBAppType appType;

    /** 驱动类名 */
    private String driverClassName;

    /** 数据库连接信息 */
    private String connectionUrl;

    /** 数据库名 */
    private String dbName;

    /** 用户名 */
    private String userName;

    /** 密码 */
    private String password;

    /**
     * 转换为配置
     * @return
     */
    public Map<String, Object> toConfigMap() {
        if (Objects.isNull(appType)) {
            throw new RuntimeException("数据库应用类型不能为空");
        }

        Map<String, Object> configMap = new HashMap<>();
        if (StringUtils.isNotBlank(driverClassName) && StringUtils.isNotBlank(appType.getDriverName())) {
            configMap.put(appType.getDriverName(), driverClassName);
        }

        if (StringUtils.isNotBlank(connectionUrl) && StringUtils.isNotBlank(appType.getConnectionUrl())) {
            configMap.put(appType.getConnectionUrl(), connectionUrl);
        }

        if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(appType.getUserName())) {
            configMap.put(appType.getUserName(), userName);
        }

        if (StringUtils.isNotBlank(password) && StringUtils.isNotBlank(appType.getPassword())) {
            configMap.put(appType.getPassword(), password);
        }

        if (StringUtils.isNotBlank(dbName) && StringUtils.isNotBlank(appType.getDbName())) {
            configMap.put(appType.getDbName(), dbName);
        }

        // 如果设置了dbName, 而且当前配置是hive-site,则生成一个Ambari的配置
        if (Objects.equals(appType, DBAppType.HIVE_SITE) && StringUtils.isNotBlank(dbName)) {
            // Ambari中使用这个变量，显示到页面上。专门针对HIVE_SITE处理。
            configMap.put("ambari.hive.db.schema.name", dbName);
        }

        return configMap;
    }

    /**
     * 获限配置类型,即配置文件的名称
     * @return
     */
    public String getConfigType() {
        return this.appType.getConfigType();
    }

    public DBAppType getAppType() {
        return appType;
    }

    public DBConnectInfo setAppType(DBAppType appType) {
        this.appType = appType;
        return this;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public DBConnectInfo setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
        return this;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public DBConnectInfo setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public DBConnectInfo setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public DBConnectInfo setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getDbName() {
        return dbName;
    }

    public DBConnectInfo setDbName(String dbName) {
        this.dbName = dbName;
        return this;
    }
}
