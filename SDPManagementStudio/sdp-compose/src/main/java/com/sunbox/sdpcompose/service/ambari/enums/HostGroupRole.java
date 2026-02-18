package com.sunbox.sdpcompose.service.ambari.enums;

import org.bouncycastle.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 主机组角色
 *
 * @author: wangda
 * @date: 2022/12/5
 */
public enum HostGroupRole {
    /**
     * Ambari类型 , 用于布署主节点
     */
    AMBARI,
    /**
     * Master类型, 用于布署主节点，非集群布署情况下，MASTER节点可以部署Ambari
     */
    MASTER,
    /**
     * Core类型, 用于布署存储节点
     */
    CORE,
    /**
     * Task类型, 用于布署计算节点
     */
    TASK;

    private static Logger logger = LoggerFactory.getLogger(HostGroupRole.class);

    public static HostGroupRole parse(String name){
        if (Objects.isNull(name)) {
            return null;
        }
        name = Strings.toUpperCase(name);
        try {
            return valueOf(name);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }
}
