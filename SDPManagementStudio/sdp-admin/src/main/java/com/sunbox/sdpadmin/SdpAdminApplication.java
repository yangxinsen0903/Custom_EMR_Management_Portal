package com.sunbox.sdpadmin;


import com.sunbox.annotation.EnableKeyVault;
import com.sunbox.annotation.EnableRedisLock;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@ComponentScan(basePackages = {"com.sunbox.controller", "com.sunbox.task","com.sunbox.dao","com.sunbox.service","com.sunbox.sdpadmin.**"})
@MapperScan(basePackages = {"com.sunbox.sdpadmin.mapper","com.sunbox.dao.mapper"})
@EnableFeignClients(basePackages = {"com.sunbox.sdpservice.service"})
@EnableTransactionManagement
@EnableKeyVault
@EnableRedisLock
@ServletComponentScan
@EnableScheduling
public class SdpAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(SdpAdminApplication.class, args);
    }

}
