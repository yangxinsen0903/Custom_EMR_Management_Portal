package com.sunbox.annotation;

import com.sunbox.configuration.RedisLockConfiguration;
import com.sunbox.util.DistributedRedisLock;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({RedisLockConfiguration.class, DistributedRedisLock.class})
public @interface EnableRedisLock {
}
