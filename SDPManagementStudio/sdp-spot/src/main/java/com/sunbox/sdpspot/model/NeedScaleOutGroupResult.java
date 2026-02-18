package com.sunbox.sdpspot.model;

public class NeedScaleOutGroupResult {
    private boolean needScale;
    private int requireCount;

    public NeedScaleOutGroupResult(boolean needScale, int requireCount) {
        this.needScale = needScale;
        this.requireCount = requireCount;
    }

    public boolean isNeedScale() {
        return needScale;
    }

    public int getRequireCount() {
        return requireCount;
    }
}
