package com.sunbox.configuration;

import com.sunbox.runtime.RuntimeManager;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MainConfiguration {

    @Bean
    @LoadBalanced
    public RestTemplate loadBalanced(){
        return new RestTemplate();
    }

    @Primary
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public RuntimeManager runtimeManager(ConfigurableApplicationContext applicationContext){
        RuntimeManager runtimeManager = new RuntimeManager();
        runtimeManager.initiate(applicationContext);
        return runtimeManager;
    }
}
