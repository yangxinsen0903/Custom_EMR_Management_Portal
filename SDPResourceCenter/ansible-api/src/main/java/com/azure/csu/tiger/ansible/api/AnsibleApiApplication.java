package com.azure.csu.tiger.ansible.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.azure.csu.tiger"})
public class AnsibleApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnsibleApiApplication.class, args);
	}

}
