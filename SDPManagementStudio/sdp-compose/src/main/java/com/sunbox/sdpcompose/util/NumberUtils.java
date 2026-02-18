package com.sunbox.sdpcompose.util;

public class NumberUtils {
    public static Integer toInteger(String integerString, Integer defaultValue) {
        try {
            return Integer.parseInt(integerString);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }
}
