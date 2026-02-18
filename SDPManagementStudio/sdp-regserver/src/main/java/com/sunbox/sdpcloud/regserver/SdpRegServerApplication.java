package com.sunbox.sdpcloud.regserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class SdpRegServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SdpRegServerApplication.class, args);
    }

}
