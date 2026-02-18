package com.sunbox.sdpspot.constant;

public class RedisConst {
    public static final long EXPIRES_INFO_CLUSTER_VMS = 3600;

    public static final long EXPIRES_VM_EVICTION_EVENT = 1800;

    public static final long CD_REQUEST_SCALE_IN = 300;

    public static String keyInfoClusterVms(String vmName) {
        return "spot_info_vms:" + vmName;
    }

    public static String keyVmEvictionEvent(String clusterId, String vmName) {
        return "spot_event:" + clusterId + ":" + vmName;
    }

    public static String keyLiveFail(String clusterId, String vmName) {
        return "spot_live_fail:" + clusterId + ":" + vmName;
    }

    public static String keyPackageGroup(String clusterId, String groupId) {
        return "spot_package_group:" + clusterId + ":" + groupId;
    }

    public static String keyScaleInGroupCd(String clusterId, String groupId) {
        return "spot_sale_in_group_cd:" + clusterId + ":" + groupId;
    }

    public static String keyLockScaleGroup(String clusterId, String groupId) {
        return "spot_lock_scale_group:" + clusterId + ":" + groupId;
    }

    public static String keyLockScaleInGroup(String clusterId, String groupId) {
        return "spot_lock_scalein_group:" + clusterId + ":" + groupId;
    }

    public static String keyLockScaleOutGroup(String clusterId, String groupId) {
        return "spot_lock_scaleout_group:" + clusterId + ":" + groupId;
    }

    public static String keyLockScaleVmRole(String clusterId, String vmRole) {
        return "spot_lock_scale_vm_role:" + clusterId + ":" + vmRole;
    }

    public static String keyLockScaleOutVmRole(String clusterId, String vmRole) {
        return "spot_lock_scaleout_vm_role:" + clusterId + ":" + vmRole;
    }

    public static String keyLockCheckStoppedComponent() {
        return "sdp_lock_check_stopped_component";
    }
}