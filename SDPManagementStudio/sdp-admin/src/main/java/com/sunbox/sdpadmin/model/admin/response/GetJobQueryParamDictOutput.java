package com.sunbox.sdpadmin.model.admin.response;

import java.util.List;

public class GetJobQueryParamDictOutput {
    private List<KvItem> jobNames;

    private List<KvItem> jobStates;

    public List<KvItem> getJobNames() {
        return jobNames;
    }

    public void setJobNames(List<KvItem> jobNames) {
        this.jobNames = jobNames;
    }

    public List<KvItem> getJobStates() {
        return jobStates;
    }

    public void setJobStates(List<KvItem> jobStates) {
        this.jobStates = jobStates;
    }

    public static class KvItem{
        private String key;
        private String value;

        public KvItem(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
