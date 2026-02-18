package com.azure.csu.tiger.ansible.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.jooq.TableOptions;

//@Configuration
public class JacksonConfig {
//    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(TableOptions.class, new TableOptionsSerializer());
        mapper.registerModule(module);
        return mapper;
    }
}