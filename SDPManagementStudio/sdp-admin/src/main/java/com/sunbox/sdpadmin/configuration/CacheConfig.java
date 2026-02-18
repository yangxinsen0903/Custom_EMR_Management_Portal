package com.sunbox.sdpadmin.configuration;

import com.alibaba.fastjson.JSONObject;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sunbox.domain.ResultMsg;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author : [niyang]
 * @className : CacheConfig
 * @description : [描述说明该类的功能]
 * @createTime : [2023/7/19 10:24 PM]
 */
@Configuration
public class CacheConfig {

    @Value("${sdp.cache.meta.time:1800}")
    private Long sdpCacheMetaTime;

    @Value("${sdp.cache.meta.initialCapacity:100}")
    private int sdpCacheMetaInitialCapacity;

    @Value("${sdp.cache.meta.maximumSize:10000}")
    private int sdpCacheMetaMaximumSize;

    /**
     *  Azure 元数据缓存adminApi接口使用
     * @return
     */
    @Bean(value = "metaCache")
    public Cache<String, ResultMsg> metaCache() {
        return Caffeine.newBuilder()
                // 设置最后一次写入或访问后经过固定时间过期
                .expireAfterWrite(sdpCacheMetaTime, TimeUnit.SECONDS)
                // 初始的缓存空间大小
                .initialCapacity(sdpCacheMetaInitialCapacity)
                // 缓存的最大条数
                .maximumSize(sdpCacheMetaMaximumSize)
                .build();
    }

    /**
     *  缓存compose元数据接口使用
     * @return
     */
    @Bean(value = "composeMetaCache")
    public Cache<String, ResultMsg> composeMetaCache() {
        return Caffeine.newBuilder()
                // 设置最后一次写入或访问后经过固定时间过期
                .expireAfterWrite(sdpCacheMetaTime, TimeUnit.SECONDS)
                // 初始的缓存空间大小
                .initialCapacity(sdpCacheMetaInitialCapacity)
                // 缓存的最大条数
                .maximumSize(sdpCacheMetaMaximumSize)
                .build();
    }


    /**
     *  缓存竞价实例元数据接口使用
     * @return
     */
    @Bean(value = "spotInstanceCache")
    public Cache<String, ResultMsg> spotInstanceCache() {
        return Caffeine.newBuilder()
                // 设置最后一次写入或访问后经过固定时间过期
                .expireAfterWrite(sdpCacheMetaTime, TimeUnit.SECONDS)
                // 初始的缓存空间大小
                .initialCapacity(sdpCacheMetaInitialCapacity)
                // 缓存的最大条数
                .maximumSize(sdpCacheMetaMaximumSize)
                .build();
    }

    /**
     *  缓存SKU信息
     * @return
     */
    @Bean(value = "skuCache")
    public Cache<String, JSONObject> skuCache() {
        return Caffeine.newBuilder()
                // 设置最后一次写入或访问后经过固定时间过期
                .expireAfterWrite(sdpCacheMetaTime, TimeUnit.SECONDS)
                // 初始的缓存空间大小
                .initialCapacity(sdpCacheMetaInitialCapacity)
                // 缓存的最大条数
                .maximumSize(sdpCacheMetaMaximumSize)
                .build();
    }


}
