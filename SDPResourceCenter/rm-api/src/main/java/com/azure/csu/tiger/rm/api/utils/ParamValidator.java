package com.azure.csu.tiger.rm.api.utils;

import org.jooq.tools.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParamValidator {

    private static final String InvalidCharsRegex = "[~!@#\\$%\\^&\\*\\(\\)=\\+_\\[\\]\\{\\}\\\\|;:'\",<>\\./\\?]";

    public static boolean checkClusterName(String name) {
        if (StringUtils.isBlank(name)) {
            return false;
        }
        Matcher matcher = Pattern.compile(InvalidCharsRegex).matcher(name);
        if (matcher.find()) {
            return false;
        }
        return true;
    }
}
