package com.sunbox.sdpcompose.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @Description TODO
 * @Author shishicheng
 * @Date 2023/3/16 19:29
 */
@Configuration
public class ThreadLocalUtils {

    public static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = new ThreadLocal<>();

    @Bean
    public ThreadLocal<Map<String, Object>> getThreadLocal() {
        return THREAD_LOCAL;
    }
}
