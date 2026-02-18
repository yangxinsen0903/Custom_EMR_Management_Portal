package com.sunbox.domain.cluster;

import java.util.List;
import java.util.Map;

/**
 *  /workorder/callback 接口入参
 */

public class WorkorderCallbackRequest {
    private Long action_id;
    /**
     * agree，refuse，back，revoke
     */
    private String action_state;
    private String action_user_display_name;
    private String action_user_staff_id;
    private String action_user_uid;
    private String approving_time;
    private String callback;
    private List<String> callback_keys;
    /**
     * 描述
     */
    private String description;
    private String next_node;
    private String node;
    private List<Map<String,String>> records;
    private Long ticket_id;
    private String ticket_owner_user_uid;
    private String workflow_name;

    public Long getAction_id() {
        return action_id;
    }

    public void setAction_id(Long action_id) {
        this.action_id = action_id;
    }

    public String getAction_state() {
        return action_state;
    }

    public void setAction_state(String action_state) {
        this.action_state = action_state;
    }

    public String getAction_user_display_name() {
        return action_user_display_name;
    }

    public void setAction_user_display_name(String action_user_display_name) {
        this.action_user_display_name = action_user_display_name;
    }

    public String getAction_user_staff_id() {
        return action_user_staff_id;
    }

    public void setAction_user_staff_id(String action_user_staff_id) {
        this.action_user_staff_id = action_user_staff_id;
    }

    public String getAction_user_uid() {
        return action_user_uid;
    }

    public void setAction_user_uid(String action_user_uid) {
        this.action_user_uid = action_user_uid;
    }

    public String getApproving_time() {
        return approving_time;
    }

    public void setApproving_time(String approving_time) {
        this.approving_time = approving_time;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public List<String> getCallback_keys() {
        return callback_keys;
    }

    public void setCallback_keys(List<String> callback_keys) {
        this.callback_keys = callback_keys;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNext_node() {
        return next_node;
    }

    public void setNext_node(String next_node) {
        this.next_node = next_node;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public Long getTicket_id() {
        return ticket_id;
    }

    public void setTicket_id(Long ticket_id) {
        this.ticket_id = ticket_id;
    }

    public String getTicket_owner_user_uid() {
        return ticket_owner_user_uid;
    }

    public void setTicket_owner_user_uid(String ticket_owner_user_uid) {
        this.ticket_owner_user_uid = ticket_owner_user_uid;
    }

    public String getWorkflow_name() {
        return workflow_name;
    }

    public void setWorkflow_name(String workflow_name) {
        this.workflow_name = workflow_name;
    }

    public List<Map<String, String>> getRecords() {
        return records;
    }

    public void setRecords(List<Map<String, String>> records) {
        this.records = records;
    }

    @Override
    public String toString() {
        return "WorkorderCallbackRequest{" +
                "action_id=" + action_id +
                ", action_state='" + action_state + '\'' +
                ", action_user_display_name='" + action_user_display_name + '\'' +
                ", action_user_staff_id='" + action_user_staff_id + '\'' +
                ", action_user_uid='" + action_user_uid + '\'' +
                ", approving_time='" + approving_time + '\'' +
                ", callback='" + callback + '\'' +
                ", callback_keys=" + callback_keys +
                ", description='" + description + '\'' +
                ", next_node='" + next_node + '\'' +
                ", node='" + node + '\'' +
                ", records=" + records +
                ", ticket_id=" + ticket_id +
                ", ticket_owner_user_uid='" + ticket_owner_user_uid + '\'' +
                ", workflow_name='" + workflow_name + '\'' +
                '}';
    }
}
