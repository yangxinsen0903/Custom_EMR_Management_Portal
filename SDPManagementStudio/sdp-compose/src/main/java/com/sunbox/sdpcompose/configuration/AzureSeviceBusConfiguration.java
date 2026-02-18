package com.sunbox.sdpcompose.configuration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.azure.messaging.servicebus.*;
import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpcompose.producer.ProducerCache;
import com.sunbox.sdpcompose.properties.AzureServiceBusProperties;
import com.sunbox.sdpcompose.service.IMsgProcessService;
import com.sunbox.sdpcompose.util.BeanMethod;
import com.sunbox.sdpcompose.util.SpringContextUtil;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;

import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * @author : [niyang]
 * @className : AzureSeviceBusConfiguration
 * @description : [描述说明该类的功能]
 * @createTime : [2022/11/29 8:09 PM]
 */
@Configuration
@ConditionalOnProperty(prefix = "azure.servicebus", value = "enable", matchIfMissing = true)
@EnableConfigurationProperties(AzureServiceBusProperties.class)
public class AzureSeviceBusConfiguration implements BaseCommonInterFace {

    @Autowired
    private SpringContextUtil app;

    @Value("${message.compose:servicebus}")
    private String messagecompose;

    @Autowired
    private IMsgProcessService msgProcessService;

    @Bean
    public boolean AzureMQSenderClient(@Qualifier("AzureServiceBusProperties")
                                       AzureServiceBusProperties azureServiceBusProperties) {

        if (!messagecompose.equalsIgnoreCase("servicebus")) {
            return false;
        }

        JSONArray jsonArray = JSON.parseArray(azureServiceBusProperties.getConfigs());
        if (null == jsonArray) {
            return false;
        }
        getLogger().info("开始初始化ServiceBus配置, 共有{}个ServiceBus配置.", jsonArray.size());
        jsonArray.stream().filter(Objects::nonNull).forEach(item -> {
            JSONObject js = (JSONObject) item;
            if (ProducerCache.producers == null) {
                ProducerCache.producers = new HashMap<>();
            }
            if (ProducerCache.consumers == null) {
                ProducerCache.consumers = new HashMap<>();
            }

            if (js.getString("type").equalsIgnoreCase("producer")) {
                getLogger().info("开始初始化ServiceBus producer: {}", js.getString("name"));
                if (ProducerCache.producers != null &&
                        !ProducerCache.producers.containsKey(js.getString("name"))) {
                    //生产者客户端
                    ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
                            .connectionString(js.getString("connectionString"))
                            .sender()
                            .topicName(js.getString("topicName"))
                            .buildClient();
                    ProducerCache.producers.put(js.getString("name"), senderClient);
                    getLogger().info("Starting ServiceBus the producer:" + js.getString("name"));
                }
            }

            if (js.getString("type").equalsIgnoreCase("consumer")) {
                getLogger().info("开始初始化ServiceBus consumer: {}", js.getString("name"));
                Consumer<ServiceBusReceivedMessageContext> messageProcessor = context -> {
                    ServiceBusReceivedMessage message = context.getMessage();

                    if (azureServiceBusProperties.isShowMessage()) {
                        getLogger().info("receive Azure Service Message message:"
                                + message.getMessageId()
                                + "，messagebody：" + message.getBody());
                    }
                    transforMessage(js.getString("callback"), message.getBody() + "");
                };

                Consumer<ServiceBusErrorContext> errorHandler = context -> {
                    getLogger().error("Error when receiving messages: ", context.getException());
                };

                ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
                        .connectionString(js.getString("connectionString"))
                        .processor()
                        .topicName(js.getString("topicName"))
                        .subscriptionName(js.getString("subName"))
                        .processMessage(messageProcessor)
                        .processError(errorHandler)
                        .buildProcessorClient();

                processorClient.start();
                ProducerCache.consumers.put(js.getString("name"), processorClient);
                getLogger().info("Starting ServiceBus the consumer:" + js.getString("name"));
            }
        });
        return true;
    }

    public void transforMessage(String callback, String message) {
        msgProcessService.transforMessage(callback, message);
    }


}
