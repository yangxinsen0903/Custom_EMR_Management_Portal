package com.sunbox.sdpcompose.service.ambari.enums;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author: wangda
 * @date: 2023/1/1
 */
class ServiceTest {

    @Test
    void getComponents() {
        List<BDComponent> components = BDService.ZOOKEEPER.getComponents();
        Assertions.assertEquals(2, components.size());
    }

    @Test
    void getComponents_过滤场景() {
        List<BDComponent> components = BDService.HDFS.getComponents(HAScene.HA);
        Assertions.assertEquals(5, components.size());

        components = BDService.HDFS.getComponents(HAScene.NON_HA);
        Assertions.assertEquals(4, components.size());

        components = BDService.HDFS.getComponents(HAScene.ALL);
        Assertions.assertEquals(3, components.size());
    }

    @Test
    void testContains_字符串名字() {
        boolean exists = BDService.HDFS.contains("namenode");
        assertTrue(exists);
    }

    @Test
    void testContains_组件枚举() {
        boolean exists = BDService.HDFS.contains(BDComponent.NAMENODE);
        assertTrue(exists);

        exists = BDService.HDFS.contains(BDComponent.HBASE_CLIENT);
        assertFalse(exists);
    }

}