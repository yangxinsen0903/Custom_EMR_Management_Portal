package com.azure.csu.tiger.ansible.agent.helper;


import com.azure.csu.tiger.ansible.agent.service.impl.AnsibleClientServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerializationHelper {
    private static final Logger logger = LoggerFactory.getLogger(SerializationHelper.class);


    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String serializeToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("An error occurred while serializing object", e);
            throw new RuntimeException(e);
        }
    }
}
