package com.azure.csu.tiger.rm.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ConstantUtil {
    public static String SYS_SDP_CLUSTER = "SYS_SDP_CLUSTER";

    public static String SYS_SDP_GROUP = "SYS_SDP_GROUP";

    public static String SYS_CREATE_BATCH = "SYS_CREATE_BATCH";

    public static String SYS_SDP_DNS = "SYS_SDP_DNS";

    public static String SDP_CLUSTER_RG_PREFIX;

    public static String SDP_CLUSTER_FLEET_PREFIX;

    private static final Logger logger = LoggerFactory.getLogger(ConstantUtil.class);

    @Value("${sdp.cluster.prefix.rg}")
    private String sdpClusterRgPrefix;

    @Value("${sdp.cluster.prefix.fleet}")
    private String sdpClusterFleetPrefix;

    @PostConstruct
    private void init() {
        SDP_CLUSTER_RG_PREFIX = sdpClusterRgPrefix;
        SDP_CLUSTER_FLEET_PREFIX = sdpClusterFleetPrefix;
        logger.info("SDP cluster rg prefix is: {}", SDP_CLUSTER_RG_PREFIX);
        logger.info("SDP cluster fleet prefix is: {}", SDP_CLUSTER_FLEET_PREFIX);
    }

    public static String getClusterDeploymentName(String clusterName) {
        return String.format("deploy-append-vms-%s", clusterName);
    }

    public static String getUpdateDataDiskSizeDeployName(String clusterName, String jobArgs) {
        return String.format("deploy-%s-resize-disk-%s", clusterName, md5Hex(jobArgs).substring(0,3));
    }

    public static String getUpdateDataDiskIopsMbpsDeployName(String clusterName, String jobArgs) {
        return String.format("deploy-%s-update-iops-mbps-%s", clusterName, md5Hex(jobArgs).substring(0,3));
    }

    public static String buildCreateVirtualMachinesJobId(String clusterName)
    {
        return String.format("create-cluster-%s-%s", clusterName, new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date()));
    }

    public static String getAppendVirtualMachinesDeployName(String clusterName, String jobArgs) {
        return String.format("append-cluster-%s-%s", clusterName, md5Hex(jobArgs).substring(0,3));
    }

    public static String buildAppendVirtualMachinesJobId(String clusterName, String jobArgs)
    {
        return String.format("append-cluster-%s-%s-%s", clusterName, md5Hex(jobArgs), new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date()));
    }

    public static String buildDeleteResourceGroupJobId(String resourceGroupName)
    {
        return String.format("del-rg-%s-%s", resourceGroupName, new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date()));
    }

    public static String buildDeleteResourceGroupJobName(String resourceGroupName)
    {
        return String.format("del-rg-%s", resourceGroupName);
    }

    public static String buildUpdateDataDiskSizeJobId(String clusterName, String jobArgs)
    {
        return String.format("update-vm-disk-size-%s-%s-%s", clusterName, md5Hex(jobArgs), new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date()));
    }

    public static String buildUpdateDataIopsMbpsJobId(String clusterName, String jobArgs)
    {
        return String.format("update-vm-disk-iops-mbps-%s-%s-%s", clusterName, md5Hex(jobArgs), new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date()));
    }

    public static String buildDeleteVirtualMachineJobId(String vmName)
    {
        return String.format("delete-%s-%s", vmName, new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date()));
    }

    public static String buildDeleteVirtualMachineJobName(String vmName)
    {
        return String.format("delete-%s", vmName);
    }

    public static String buildDeleteVirtualMachinesJobId(String clusterName, String jobArgs)
    {
        String vmNameCheckSum = md5Hex(jobArgs);
        return String.format("delete-cluster-%s-vms-%s-%s", clusterName, vmNameCheckSum, new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date()));
    }

    public static String buildDeleteVirtualMachinesJobName(String clusterName, String jobArgs)
    {
        String vmNameCheckSum = md5Hex(jobArgs);
        return String.format("delete-cluster-%s-vms-%s", clusterName, vmNameCheckSum);
    }

    public static String buildDeleteClusterGroupJobId(String clusterName, String groupName)
    {
        return String.format("delete-cluster-%s-group-%s-%s", clusterName, groupName, new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date()));
    }

    public static String buildDeleteClusterGroupJobName(String clusterName, String groupName)
    {
        return String.format("delete-cluster-%s-group-%s", clusterName, groupName);
    }

    public static String buildFleetName(String clusterName, String groupName) {
        return String.format("%s-%s-%s", SDP_CLUSTER_FLEET_PREFIX, clusterName.toLowerCase(), groupName.toLowerCase());
    }

    public static String buildComputerNamePrefix(String clusterName, String groupName) {
        return String.format("%s-%s-", clusterName.toLowerCase(), ConstantUtil.getShortGroupName(groupName.toLowerCase()));
    }

    public static String getResourceGroupName(String clusterName) {
        return String.format("rg-%s-%s", SDP_CLUSTER_RG_PREFIX, clusterName);
    }

    public static String getClusterName(String resourceGroupName) {
        return resourceGroupName.replace(String.format("rg-%s-", SDP_CLUSTER_RG_PREFIX), "");
    }

    public static String getShortGroupName(String groupName) {
        if (groupName.toLowerCase().startsWith("ambari")) {
            return "amb";
        } else if (groupName.toLowerCase().startsWith("master")) {
            return "mst";
        } else if (groupName.toLowerCase().startsWith("core")) {
            return "cor";
        } else if (groupName.toLowerCase().startsWith("task")) {
            return "tsk";
        } else {
            return groupName.toLowerCase();
        }
    }

    public static Integer AZURE_THROTTLE_RETRY_TIMES_DEFAULT = 10;

    public static Integer AZURE_THROTTLE_WAIT_TIME_SECONDS_DEFAULT = 10;

    public static String md5Hex(String input) {
        try {
        // 获取MD5摘要算法实例
        MessageDigest md = MessageDigest.getInstance("MD5");
        // 计算输入字符串的哈希值
        byte[] hashBytes = md.digest(input.getBytes());
        // 将字节数组转换为十六进制字符串
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
