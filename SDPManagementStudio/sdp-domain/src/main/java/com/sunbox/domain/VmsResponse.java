package com.sunbox.domain;

import lombok.Data;

import java.util.Map;

/**
 * api/v1/vms/listAll/接口出参
 */
@Data
public class VmsResponse {

    private String name;

    private String hostName;

    private String dnsRecord;

    private String uniqueId;

    private String privateIp;

    private String zone;

    private Map<String,String> tags;

    private String priority;

    private String vmState;

    private String vmSize;
    /**
     * vm创建时间,不用VirtualMachineProfileTimeCreated
     */
    private String vmTimeCreated;

    //tag中一定会有
    public static final String SYS_SDP_CLUSTER="SYS_SDP_CLUSTER";



}
