package com.sunbox.sdpadmin.manager;

import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * 基于Redisson的全局限流量<br/>
 * 限流量的key保存在配置中心，Key名为：createCluster.rateLimiter.rate <br/>
 * 默认限流量为10
 * @date 2023/4/18
 */
@Service
public class CreateClusterRateLimiter {
    private Logger logger = Logger.getLogger(CreateClusterRateLimiter.class.getName());
    private String rateLimiterName = "createCluster.rateLimiter.rate";

    @Value("${createCluster.rateLimiter.rate:5}")
    public Long rate = 5L;

    RRateLimiter rateLimiter;

    @Autowired
    RedissonClient redissonClient;

    /**
     * 尝试获取令牌
     * @return true: 获取成功，false: 获取失败
     */
    public boolean tryAcquire() {
        if (Objects.isNull(rateLimiter)) {
            synchronized (this) {
                if (rateLimiter == null) {
                    logger.info("开始初始化限流器，限流器名称：createCluster.rateLimiter.rate，限流量为：" + rate);
                    rateLimiter = redissonClient.getRateLimiter(rateLimiterName);
                    rateLimiter.trySetRate(RateType.OVERALL, rate, 1, RateIntervalUnit.SECONDS);
                    RateLimiterConfig config = rateLimiter.getConfig();
                    if (!Objects.equals(config.getRate(), rate)) {
                        rateLimiter.setRate(RateType.OVERALL, rate, 1, RateIntervalUnit.SECONDS);
                    }
                    logger.info("限流器初始化完成");
                }
            }
        }
        return rateLimiter.tryAcquire(1);
    }

    public static void main(String[] args) {
        Config config = new Config();
        config.useSingleServer().setAddress("rediss://sunboxdev.redis.cache.windows.net:6380")
                .setConnectionPoolSize(10)
                .setConnectionMinimumIdleSize(2)
                .setDatabase(0);
        RedissonClient redissonClient = Redisson.create(config);

        CreateClusterRateLimiter limiter = new CreateClusterRateLimiter();
        limiter.redissonClient = redissonClient;

        boolean result = limiter.tryAcquire();

        System.out.println(result);
    }
}
