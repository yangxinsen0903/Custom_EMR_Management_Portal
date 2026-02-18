package com.sunbox.util;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by DiaoWen on 2022/8/19
 *
 * 数据转换工具类
 */
public class DozerUtil {

    private static Mapper dozerMapper = new DozerBeanMapper();

    /**
     * @Description: 单个对象复制及类型转换
     * @Param: s 数据对象
     * @param: clz 复制目标类型
     * @Return: {@link T}
     * @author: DiaoWen
     * @date: 2022/8/19
     */
    public static <T, S> T convert(final S s, Class<T> clz) {
        return s == null ? null : dozerMapper.map(s, clz);
    }

    /**
     * @Description: list复制
     * @Param: s 数据对象
     * @param:  clz 复制目标类型
     * @Return: {@link List <T>}
     * @author: DiaoWen
     * @date: 2022/8/19
     */
    public static <T, S> List<T> convert(List<S> s, Class<T> clz) {
        return s == null ? null : s.stream().map(vs -> dozerMapper.map(vs, clz)).collect(Collectors.toList());
    }

    /**
     * @Description: set复制
     * @Param: s 数据对象
     * @param: clz 复制目标类型
     * @Return: {@link Set <T>}
     * @author: DiaoWen
     * @date: 2022/8/19
     */
    public static <T, S> Set<T> convert(Set<S> s, Class<T> clz) {
        return s == null ? null : s.stream().map(vs -> dozerMapper.map(vs, clz)).collect(Collectors.toSet());
    }

    /**
     * @Description: 数组复制
     * @Param: s 数据对象
     * @param: clz 复制目标类型
     * @Return: {@link T []}
     * @author: DiaoWen
     * @date: 2022/8/19
     */
    public static <T, S> T[] convert(S[] s, Class<T> clz) {
        if (s == null) {
            return null;
        }
        T[] arr = (T[]) Array.newInstance(clz, s.length);
        for (int i = 0; i < s.length; i++) {
            arr[i] = dozerMapper.map(s[i], clz);
        }
        return arr;
    }
}