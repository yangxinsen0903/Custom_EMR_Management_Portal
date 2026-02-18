package com.azure.csu.tiger.ansible.agent.config;

public enum ConstantsConfig {

    JOB_STATUS_SUCCESS(0),
    JOB_STATUS_FAIL(1),

    PLAYBOOK_TYPE_SDP("1"),
    PLAYBOOK_TYPE_CUSTOM("2"),

    LOGlEVEL_DEFAULT(0),

    JOB_EXECUTE_STATUS_NOTSTART(0),
    JOB_EXECUTE_STATUS_RUNNING(1),
    JOB_EXEXUTE_STATUS_DONE(2),
    JOB_EXEXUTE_STATUS_FAILED(3);

    private final String value;

    private final int numberValue;

    ConstantsConfig(String value) {
        this.value = value;
        this.numberValue = -1;
        // Default for string values
    }

    ConstantsConfig(int numberValue) {
        this.value = null;
        this.numberValue = numberValue;
    }

    public String getStringValue() {
        return value;
    }

    public int getNumberValue() {
        return numberValue;
    }
}

