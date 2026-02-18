package com.sunbox.domain.enums;

/**
 * 0未开始 1执行中 2 执行成功 3执行失败
 */
public enum SpotGroupScaleTaskStates {
    WAITING(0, "未开始"),
    EXECUTING(1, "执行中"),
    EXECUTE_SUCCESS(2, "执行成功"),
    EXECUTE_FAILURE(3, "执行失败");

    private final Integer value;
    private final String text;

    SpotGroupScaleTaskStates(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    public Integer getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
