package com.sunbox.sdpcompose.service.ambari.blueprint;

import com.sunbox.sdpcompose.service.ambari.enums.DBAppType;
import com.sunbox.sdpcompose.util.JacksonUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author: wangda
 * @date: 2022/12/11
 */
class DBConnectInfoTest {

    @Test
    void toConfigMap() {
        DBConnectInfo info = new DBConnectInfo();
        info.setAppType(DBAppType.HIVE_ENV)
                        .setDbName("lang");

        Map<String, Object> map = info.toConfigMap();
        System.out.println(JacksonUtils.toJson(map));
        assertEquals(map.get("hive_database_name"), "lang");

        info = new DBConnectInfo();
        info.setAppType(DBAppType.HIVE_SITE)
                .setDbName("lang")
                .setDriverClassName("driver")
                .setUserName("userName")
                .setPassword("password")
                .setConnectionUrl("jdbc:mysql://fadfadfasd?fadfad");

        map = info.toConfigMap();
        System.out.println(JacksonUtils.toJson(map));
        assertEquals(map.get("ambari.hive.db.schema.name"), "lang");
    }

}