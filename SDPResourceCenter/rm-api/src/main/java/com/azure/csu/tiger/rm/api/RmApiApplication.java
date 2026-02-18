package com.azure.csu.tiger.rm.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.azure.csu.tiger"})
public class RmApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(RmApiApplication.class, args);
	}

}
