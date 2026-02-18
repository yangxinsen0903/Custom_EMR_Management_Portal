package com.sunbox.sdpcompose.service.ambari.enums;

/**
 * Ambari集群创建时的动作
 * @author: wangda
 * @date: 2022/12/7
 */
public enum ProvisionAction {
    /** 安装后并启动 */
    INSTALL_AND_START,
    /** 仅安装,不启动  */
    INSTALL_ONLY,
    /** 仅启动 */
    START_ONLY;
}
