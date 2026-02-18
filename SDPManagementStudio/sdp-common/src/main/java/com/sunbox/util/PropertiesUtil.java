/**
 * 58.com Inc.
 * Copyright (c) 2005-2015 All Rights Reserved.
 */
package com.sunbox.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

/**
 * 
 * @author yangjian
 * @version $Id: PropertiesUtil.java, v 0.1 2015-8-7 下午2:33:20 yangjian Exp $
 */
public class PropertiesUtil {

    public static PropertiesUtil props = new PropertiesUtil();
    public static Properties properties = null;
    //public static Properties props = null;

    static{
        Resource moderes = new ClassPathResource("/conf/system.properties");
        try {
            Properties modeprop = PropertiesLoaderUtils.loadProperties(moderes);
            String filename="/conf/prop.properties";
            if("true".equals(modeprop.get("debug"))){
                filename="/conf/debug.prop.properties";
            }
            Resource resource = new ClassPathResource(filename);
            //props = PropertiesLoaderUtils.loadProperties(resource);
            properties = PropertiesLoaderUtils.loadProperties(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        String value;
        try {
            value = properties.getProperty(key);
            value = DESUtil.decrypt(value);
        } catch (Exception e) {
            value = "";
        }

        return value;
    }

    public static String getEcryptProperty(String key) {
        String value;
        try {
            //value = properties.getProperty(key);
            value = DESUtil.encrypt(key,"caB2dfD4E5F60708");
        } catch (Exception e) {
            value = "";
        }

        return value;
    }
}
