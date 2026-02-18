package com.sunbox.sdpspot;

import com.sunbox.annotation.EnableRedisLock;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {"com.sunbox.controller",  "com.sunbox.task","com.sunbox.dao","com.sunbox.service","com.sunbox.sdpspot.**"})
@MapperScan(basePackages = {"com.sunbox.sdpspot.mapper","com.sunbox.dao.mapper"})
@EnableFeignClients(basePackages = {"com.sunbox.sdpservice.service"})
@EnableRedisLock
@EnableAsync
@EnableScheduling
public class SdpSpotApplication {

    public static void main(String[] args) {
        SpringApplication.run(SdpSpotApplication.class, args);
    }

}
