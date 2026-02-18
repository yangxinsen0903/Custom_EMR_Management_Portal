package com.sunbox.sdpcompose;


import com.sunbox.annotation.EnableKeyVault;
import com.sunbox.annotation.EnableRedisLock;
import com.sunbox.runtime.RuntimeGlobal;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {"com.sunbox.controller", "com.sunbox.task","com.sunbox.dao","com.sunbox.service", "com.sunbox.sdpcompose.**"})
@MapperScan(basePackages = {"com.sunbox.sdpcompose.mapper","com.sunbox.dao.mapper"})
@EnableFeignClients(basePackages = {"com.sunbox.sdpservice.service"})
@EnableRedisLock
@EnableKeyVault
@EnableScheduling
public class SdpComposeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SdpComposeApplication.class, args);
        RuntimeGlobal.ThirdBlocked = true;
    }

}