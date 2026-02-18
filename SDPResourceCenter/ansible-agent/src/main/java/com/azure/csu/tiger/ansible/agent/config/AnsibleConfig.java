package com.azure.csu.tiger.ansible.agent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
public class AnsibleConfig {

    @Value("${ansible.inventory.hosts.path}")
    private String ansibleInventoryPath;

    @Value("${ansible.inventory.hosts.name}")
    private String ansibleInventoryName;

    @Value("${ansible.cfg.path}")
    private String ansibleCfgPath;

    @Value("${ansible.cfg.name}")
    private String ansibleCfgName;

    @Value("${ansible.known.hosts.fullpathname}")
    private String ansibleKnowHostsName;

    @Value("${ansible.ssh.connection.timeout}")
    private String ansibleSshConnTimeout;

    @Value("${ansible.ssh.connection.retry}")
    private String ansibleSshConnRetry;

    @Value("${ansible.forks}")
    private String ansibleForks;

    @Value("${ansible.remote.port}")
    private String ansibleRemotePort;




    @Bean
    public TemplateEngine templateEngine(){

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setTemplateMode("TEXT");
        TemplateEngine tTemplateEngine = new TemplateEngine();
        tTemplateEngine.setTemplateResolver(templateResolver);
        return tTemplateEngine;
    }

}
