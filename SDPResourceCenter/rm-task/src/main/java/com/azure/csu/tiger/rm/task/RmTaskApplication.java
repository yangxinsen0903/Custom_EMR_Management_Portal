package com.azure.csu.tiger.rm.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.azure.csu.tiger"})
public class RmTaskApplication {

	public static void main(String[] args) {
		SpringApplication.run(RmTaskApplication.class, args);
	}

}
