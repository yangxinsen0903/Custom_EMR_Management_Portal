package com.azure.csu.tiger.ansible.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.azure.csu.tiger"})
public class AnsibleAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnsibleAgentApplication.class, args);
	}

}
