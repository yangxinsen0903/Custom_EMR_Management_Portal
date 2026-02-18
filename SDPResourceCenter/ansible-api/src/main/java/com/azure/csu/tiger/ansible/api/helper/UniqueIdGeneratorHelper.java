package com.azure.csu.tiger.ansible.api.helper;


import java.util.UUID;

public class UniqueIdGeneratorHelper {
    public static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }
}
