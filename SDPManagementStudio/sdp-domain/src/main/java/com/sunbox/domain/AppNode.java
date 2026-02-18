package com.sunbox.domain;

import java.util.List;

public class AppNode {
    private AppAttempts appAttempts;

    public void setAppAttempts(AppAttempts appAttempts) {
        this.appAttempts = appAttempts;
    }

    public AppAttempts getAppAttempts() {
        return appAttempts;
    }

    public static class AppAttempts {

        private List<AppAttempt> appAttempt;

        public void setAppAttempt(List<AppAttempt> appAttempt) {
            this.appAttempt = appAttempt;
        }

        public List<AppAttempt> getAppAttempt() {
            return appAttempt;
        }
    }

    public static class AppAttempt {

        private String nodeId;
        private String nodeHttpAddress;
        private long startTime;
        private int id;
        private String logsLink;
        private String containerId;

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeHttpAddress(String nodeHttpAddress) {
            this.nodeHttpAddress = nodeHttpAddress;
        }

        public String getNodeHttpAddress() {
            return nodeHttpAddress;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public void setLogsLink(String logsLink) {
            this.logsLink = logsLink;
        }

        public String getLogsLink() {
            return logsLink;
        }

        public void setContainerId(String containerId) {
            this.containerId = containerId;
        }

        public String getContainerId() {
            return containerId;
        }
    }
}
