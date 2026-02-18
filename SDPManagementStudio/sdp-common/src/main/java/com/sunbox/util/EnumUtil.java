package com.sunbox.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author : [niyang]
 * @className : EnumUtil
 * @description : [描述说明该类的功能]
 * @createTime : [2022/11/2 5:31 下午]
 */
public class EnumUtil {
    private static Map<Class, Object> map = new ConcurrentHashMap<>();
    private static String ENUM_CLASSPATH="java.lang.Enum";

    /**
     * 根据条件获取枚举对象
     *
     * @param className 枚举类
     * @param predicate 筛选条件
     * @param <T>
     * @return
     */
    public static <T> Optional<T> getEnumObject(Class<T> className, Predicate<T> predicate) {
        if (!className.isEnum()) {
            return null;
        }
        Object obj = map.get(className);
        T[] ts = null;
        if (obj == null) {
            ts = className.getEnumConstants();
            map.put(className, ts);
        } else {
            ts = (T[]) obj;
        }
        return Arrays.stream(ts).filter(predicate).findAny();
    }


}
