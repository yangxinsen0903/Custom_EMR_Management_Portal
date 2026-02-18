package com.sunbox.domain.enums;

public enum ScaleMethods {
    SCALE_OUT(1, "扩容"),
    SCALE_IN(2, "缩容");

    private final Integer value;
    private final String text;

    ScaleMethods(Integer value, String text) {
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
