package com.sunbox.util;

import cn.hutool.core.util.StrUtil;

public class SdpExceptionUtil {
    public static void wrapRuntimeAndThrow(String format, Object... msg) {
        throw new RuntimeException(StrUtil.format(format, msg));
    }

    public static void wrapRuntimeAndThrow(String msg) {
        throw new RuntimeException(msg);
    }
}
