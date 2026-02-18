package com.sunbox.sdpcompose.util;

import com.sunbox.sdpcompose.configuration.AzureSeviceBusConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author : [niyang]
 * @className : SpringContextUtil
 * @description : [描述说明该类的功能]
 * @createTime : [2022/11/30 4:45 PM]
 */
@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static  ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }

    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    public static <T> T getBean(Class<T> clz) {
        return applicationContext.getBean(clz);
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

}
