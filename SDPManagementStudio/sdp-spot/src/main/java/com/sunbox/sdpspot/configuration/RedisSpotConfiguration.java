package com.sunbox.sdpspot.configuration;

import com.sunbox.sdpspot.util.DistributedRedisSpot;
import com.sunbox.util.DESUtil;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RedisSpotConfiguration {
    //region spot 所需要的redis连接客户端
    @Value("${mredis.spot.address}")
    private String redisSpotAddress;

    @Value("${mredis.spot.port}")
    private String redisSpotPort;

    @Value("${mredis.spot.password}")
    private String redisSpotPassword;

    @Value("${mredis.spot.idletimeout:30000}")
    private String redisSpotIdletimeout;

    @Value("${mredis.spot.db:1}")
    private String redisSpotdb;

    @Value("${mredis.spot.usessl}")
    private String redisSpotUsessl;
    //endregion

    @Bean
    @Qualifier("spotRedissonClient")
    public RedissonClient getRedissonForSpot(){
        LoggerFactory.getLogger(RedisSpotConfiguration.class).info("mredis.spot.config address:{}, port:{}, db:{}", redisSpotAddress, redisSpotPort, redisSpotdb);
        RedissonClient redisson = null;
        Config config = new Config();
        config.useSingleServer().setAddress("rediss://" + DESUtil.decrypt(redisSpotAddress)
                        + ":" + DESUtil.decrypt(redisSpotPort)).setPassword(DESUtil.decrypt(redisSpotPassword))
                .setDatabase(Integer.parseInt(redisSpotdb))
                .setIdleConnectionTimeout(Integer.parseInt(redisSpotIdletimeout))
                .setConnectTimeout(15000)
                .setTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(3000)
                .setConnectionPoolSize(5)
                .setConnectionMinimumIdleSize(2);

        redisson = Redisson.create(config);
        return redisson;
    }

    @Bean
    public DistributedRedisSpot distributedRedisSpot(@Qualifier("spotRedissonClient") RedissonClient redissonClient){
        return new DistributedRedisSpot(redissonClient);
    }
}
