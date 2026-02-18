package com.azure.csu.tiger.rm.api.config;

import com.azure.csu.tiger.rm.api.exception.RmException;
import com.azure.csu.tiger.rm.api.utils.ArmUtil;
import com.azure.resourcemanager.AzureResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class WebMvcConfig {

    private static final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);

    @Autowired
    private ArmUtil armUtil;

    @Bean
    public HandlerInterceptor getHandlerInterceptor() {

        return new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                if (!request.getRequestURI().contains("/api/")) {
                    return true;
                }
                if (request.getRequestURI().contains("/api/v1/metas/supportedSubscriptionList")) {
                    return true;
                }
                String subscriptionId = request.getHeader("subscriptionId");
                if (subscriptionId == null) {
                    throw new RmException(HttpStatus.BAD_REQUEST, "subscriptionId is required in header.");
                }
                ArmUtil.setSubData(subscriptionId);
                logger.info("subscriptionId: {}, request uri: {}", subscriptionId, request.getRequestURI());
                AzureResourceManager azureResourceManager = armUtil.getAzureResourceManager(subscriptionId);
                ArmUtil.setArmData(azureResourceManager);
                return true;
            }

            @Override
            public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
                ArmUtil.clear();
            }

            @Override
            public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

            }
        };
    }

    @Bean
    public WebMvcConfigurer getWebMvcConfigurer(HandlerInterceptor interceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(interceptor);
            }
        };
    }
}
