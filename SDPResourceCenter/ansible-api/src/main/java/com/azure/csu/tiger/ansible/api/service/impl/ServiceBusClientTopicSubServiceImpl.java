package com.azure.csu.tiger.ansible.api.service.impl;

import com.azure.core.credential.TokenCredential;
import com.azure.csu.tiger.ansible.api.service.ServiceBusClientTopicSubService;
import com.azure.messaging.servicebus.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ServiceBusClientTopicSubServiceImpl implements ServiceBusClientTopicSubService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceBusClientTopicSubServiceImpl.class);

    @Value("${azure.servicebus.namespace}")
    private String namespace;

    @Value("${azure.servicebus.topicnameapi}")
    private String topicName;

    @Autowired
    private TokenCredential defaultAzureCredential;

    private ServiceBusSenderClient senderClient;

    @PostConstruct
    public void init(){
        senderClient = new ServiceBusClientBuilder()
                .fullyQualifiedNamespace(namespace)
                .credential(defaultAzureCredential)
                .sender()
                .topicName(topicName)
                .buildClient();
    }

    @PreDestroy
    public void close() {
        senderClient.close();
    }

    public void sendMessageBatchWithCre(List<ServiceBusMessage> listOfMessages)
    {
        // Creates an ServiceBusMessageBatch where the ServiceBus.
        ServiceBusMessageBatch messageBatch = senderClient.createMessageBatch();

        for (ServiceBusMessage message : listOfMessages) {
            if (messageBatch.tryAddMessage(message)) {
                continue;
            }

            // The batch is full, so we create a new batch and send the batch.
            senderClient.sendMessages(messageBatch);
            logger.info("Sent a batch of messages to the topic: " + topicName);

            // create a new batch
            messageBatch = senderClient.createMessageBatch();

            // Add that message that we couldn't before.
            if (!messageBatch.tryAddMessage(message)) {
                logger.error("Message is too large for an empty batch. Skipping. Max size: " + messageBatch.getMaxSizeInBytes());
            }
        }

        if (messageBatch.getCount() > 0) {
            senderClient.sendMessages(messageBatch);
            logger.info("Sent a batch of messages to the topic: " + topicName);
        }
    }

}
