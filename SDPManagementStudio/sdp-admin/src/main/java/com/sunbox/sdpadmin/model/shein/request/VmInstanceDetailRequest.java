package com.sunbox.sdpadmin.model.shein.request;

import lombok.Data;

@Data
public class VmInstanceDetailRequest {
    /**
     * 数据中心。传参范围：nous | us | eur
     */
    private String dc;
    private String clusterId;
    private String vmName;
    /**
     * 带域名的VM HostName，vmName,hostName,ip三选一
     */
    private String hostName;
    /**
     * VM的IP地址，vmName,hostName,ip三选一
     */
    private String ip;
    /**
     * VM的状态，1：运行中；-1：已删除；-10：删除中；-99：状态未知
     */
    private Integer state;
    /**
     * 实例组名称，多个实例组名称，用半角逗号连接，如：ambari,master
     */
    private String groupName;
}
