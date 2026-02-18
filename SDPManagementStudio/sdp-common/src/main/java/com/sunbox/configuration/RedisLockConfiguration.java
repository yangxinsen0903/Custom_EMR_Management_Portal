package com.sunbox.configuration;

import com.sunbox.util.DESUtil;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RedisLockConfiguration {

    @Value("${mredis.lock.address}")
    private String redisLcokAddress;

    @Value("${mredis.lock.port}")
    private String port;

    @Value("${mredis.lock.password}")
    private String redisLockPassword;

    @Value("${mredis.lock.idletimeout:30000}")
    private String idletimeout;

    @Value("${mredis.lock.db:1}")
    private String redisdb;

    @Value("${mredis.usessl}")
    private String usessl;

    @Value("${mredis.connection.poolsize:10}")
    private Integer redisConnectionPoolSize;

    @Bean
    @Primary
    public RedissonClient getRedisson(){
        RedissonClient redisson = null;
        Config config = new Config();
        config.useSingleServer().setAddress("rediss://" + DESUtil.decrypt(redisLcokAddress)
                    + ":" + DESUtil.decrypt(port)).setPassword(DESUtil.decrypt(redisLockPassword))
                    .setDatabase(Integer.parseInt(redisdb))
                    .setIdleConnectionTimeout(Integer.parseInt(idletimeout))
                    .setConnectTimeout(15000)
                    .setConnectionMinimumIdleSize(5)
                    .setConnectionPoolSize(redisConnectionPoolSize)
                    .setTimeout(3000)
                    .setRetryAttempts(3)
                    .setRetryInterval(3000);

        redisson = Redisson.create(config);
        return redisson;
    }
}
