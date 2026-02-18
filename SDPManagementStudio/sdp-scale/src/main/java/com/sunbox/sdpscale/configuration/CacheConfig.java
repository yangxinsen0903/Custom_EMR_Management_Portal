package com.sunbox.sdpscale.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sunbox.domain.ResultMsg;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author : [niyang]
 * @className : CacheConfig
 * @description : [描述说明该类的功能]
 * @createTime : [2023/7/19 10:24 PM]
 */
@Configuration
public class CacheConfig {

    @Value("${sdp.cache.cluster.resourcemanager.time:2592000}")
    private Long sdpCacheClusterResourceManagerTime;

    @Value("${sdp.cache.cluster.resourcemanager.initialCapacity:1000}")
    private int sdpCacheClusterResourceManagerInitialCapacity;

    @Value("${sdp.cache.cluster.resourcemanager.maximumSize:10000}")
    private int sdpCacheClusterResourceManagerMaximumSize;

    /**
     *  ResourceManager地址缓存
     * @return
     */
    @Bean(value = "rmCache")
    public Cache<String, List<String>> metaCache() {
        return Caffeine.newBuilder()
                // 设置最后一次写入或访问后经过固定时间过期
                .expireAfterWrite(sdpCacheClusterResourceManagerTime, TimeUnit.SECONDS)
                // 初始的缓存空间大小
                .initialCapacity(sdpCacheClusterResourceManagerInitialCapacity)
                // 缓存的最大条数
                .maximumSize(sdpCacheClusterResourceManagerMaximumSize)
                .build();
    }
}
