package com.azure.csu.tiger.ansible.api.helper;


import com.fasterxml.jackson.databind.ObjectMapper;

public class SerializationHelper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String serializeToJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
