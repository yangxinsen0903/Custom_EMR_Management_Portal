package com.sunbox.sdpvmsr;

import com.sunbox.annotation.EnableRedisLock;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com.sunbox.controller", "com.sunbox.sdpvmsr.**","com.sunbox.task","com.sunbox.dao","com.sunbox.service.**"})
@MapperScan(basePackages = {"com.sunbox.sdpvmsr.mapper","com.sunbox.dao.mapper"})
@EnableFeignClients(basePackages = {"com.sunbox.sdpservice.service"})
@EnableRedisLock
public class SdpVmsrApplication {

    public static void main(String[] args) {
        SpringApplication.run(SdpVmsrApplication.class, args);
    }

}
