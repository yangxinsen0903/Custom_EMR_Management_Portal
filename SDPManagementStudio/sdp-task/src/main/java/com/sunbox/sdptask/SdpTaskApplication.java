package com.sunbox.sdptask;

import com.sunbox.annotation.EnableRedisLock;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {"com.sunbox.controller", "com.sunbox.task", "com.sunbox.sdptask.**", "com.sunbox.service.**",})
@MapperScan(basePackages = {"com.sunbox.sdptask.mapper","com.sunbox.dao.mapper"})
@EnableFeignClients(basePackages = {"com.sunbox.sdpservice.service"})
@EnableRedisLock
@EnableAsync
@EnableScheduling
public class SdpTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(SdpTaskApplication.class, args);
    }

}
