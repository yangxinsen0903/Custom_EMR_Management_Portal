package com.sunbox.sdpcompose.enums;

public enum AmbariHostState {
    /**
     * New host state
     */
    INIT,
    /**
     * State when a registration request is received from the Host but
     * the host has not responded to its status update check.
     */
    WAITING_FOR_HOST_STATUS_UPDATES,
    /**
     * State when the server is receiving heartbeats regularly from the Host
     * and the state of the Host is healthy
     */
    HEALTHY,
    /**
     * State when the server has not received a heartbeat from the Host in the
     * configured heartbeat expiry window.
     */
    HEARTBEAT_LOST,
    /**
     * Host is in unhealthy state as reported either by the Host itself or via
     * any other additional means ( monitoring layer )
     */
    UNHEALTHY;
}