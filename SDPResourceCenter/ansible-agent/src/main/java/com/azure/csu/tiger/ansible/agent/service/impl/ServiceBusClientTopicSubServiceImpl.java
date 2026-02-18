package com.azure.csu.tiger.ansible.agent.service.impl;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.csu.tiger.ansible.agent.helper.JoblistHelper;
import com.azure.csu.tiger.ansible.agent.helper.SerializationHelper;
import com.azure.csu.tiger.ansible.agent.model.AnsibleExecuteMsg;
import com.azure.csu.tiger.ansible.agent.service.AnsibleClientService;
import com.azure.csu.tiger.ansible.agent.service.ServiceBusClientTopicSubService;
import com.azure.messaging.servicebus.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class ServiceBusClientTopicSubServiceImpl implements ServiceBusClientTopicSubService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceBusClientTopicSubServiceImpl.class);

    @Value("${azure.servicebus.namespace}")
    private String namespace;

    @Value("${azure.servicebus.topicnameapi}")
    private String topicName;

    @Value("${azure.servicebus.subnameapi}")
    private String subName;

    @Value("${azure.servicebus.topicnamebak}")
    private String topicNameBak;

    @Autowired
    private JoblistHelper joblistHelper;

    @Autowired
    private TokenCredential defaultAzureCredential;

    @Autowired
    private AnsibleClientService ansibleClientService;

    private ServiceBusProcessorClient processorClient;

    private ServiceBusReceiverClient receiverClient;

    private ServiceBusSenderClient senderClient;


    @PostConstruct
    public void init() {

        this.processorClient = new ServiceBusClientBuilder()
                .fullyQualifiedNamespace(namespace)
                .credential(defaultAzureCredential)
                .processor()
                .topicName(topicName)
                .subscriptionName(subName)
                .disableAutoComplete()
                .processMessage(context -> processMessage(context))
                .processError(context -> processError(context))
//                .maxConcurrentCalls(5)
                .buildProcessorClient();
        receiverClient = new ServiceBusClientBuilder()
                .fullyQualifiedNamespace(namespace)
                .credential(defaultAzureCredential)
                .receiver()
                .topicName(topicName)
                .subscriptionName(subName)
                .buildClient();

        senderClient = new ServiceBusClientBuilder()
                .fullyQualifiedNamespace(namespace)
                .credential(defaultAzureCredential)
                .sender()
                .topicName(topicNameBak)
                .buildClient();

        try {
            receiveMessagesWithCre();
        } catch (InterruptedException e) {
            logger.error("Error occurred while receiving messages", e);
            throw new RuntimeException(e);
        }

    }

    private void sendMessageWithCre(ServiceBusMessage msg)
    {
        // send one message to the topic
        senderClient.sendMessage(msg);
        logger.info("Sent a single message, msg: " + msg.getBody().toString());
    }

    // handles received messages
    public void receiveMessagesWithCre() throws InterruptedException
    {

       logger.info("Starting the processor");
        processorClient.start();

    }


     private void processMessage(ServiceBusReceivedMessageContext context) {
         ServiceBusReceivedMessage message = context.getMessage();
         logger.info("Processing Message Start, message id: {}, body: {} ", message.getMessageId(), message.getBody().toString());

         ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
         ScheduledFuture<?> scheduledFuture = null;
         try {
             scheduledFuture = scheduler.scheduleAtFixedRate(() -> {
                 try {
                     receiverClient.renewMessageLock(message);
                     logger.info("Lock renewed for message: {}, lockToken: {}", message.getMessageId(), message.getLockToken());
                 } catch (Exception e) {
                     logger.error("Failed to renew lock for message: {}, lockToken: {}", message.getMessageId(), message.getLockToken(), e);
                 }
             }, 0, 30, TimeUnit.SECONDS); // 每30秒钟续租一次锁（根据实际需要调整）


            JsonSerializer jsp = JsonSerializerProviders.createInstance(true);
            Mono<AnsibleExecuteMsg> objectAsync = message.getBody().toObjectAsync(AnsibleExecuteMsg.class, jsp);
            AnsibleExecuteMsg ansibleExecuteMsg = objectAsync.block();


            AnsibleExecuteMsg nioMsg = new AnsibleExecuteMsg(ansibleExecuteMsg);

            List<String> plcd = ansibleClientService.preExecutePlayBook(nioMsg);

            ansibleClientService.runPlaybook(plcd, nioMsg);

            logger.info("Backup ansible job task to service bus : {}", nioMsg);
            sendMessageWithCre(new ServiceBusMessage(SerializationHelper.serializeToJson(nioMsg)));

            context.complete();
            logger.info("Processing Message End, message id: {} ", message.getMessageId());
        } catch (Exception e) {
            logger.error("Failed to process message, id: {}", message.getMessageId(), e);
            throw new RuntimeException(e);
        } finally {
             if (scheduledFuture != null) {
                 scheduledFuture.cancel(true); // 取消任务
             }
             scheduler.shutdown(); // 关闭线程池
             logger.info("Scheduler shutdown for message: {}", message.getMessageId());
         }

     }

     private void processError(ServiceBusErrorContext context) {
        logger.info("Error when receiving messages from namespace: {}. Entity: {}",
                context.getFullyQualifiedNamespace(), context.getEntityPath());

        if (!(context.getException() instanceof ServiceBusException)) {
            logger.info("Non-ServiceBusException occurred: {}", context.getException());
            return;
        }

        ServiceBusException exception = (ServiceBusException) context.getException();
        ServiceBusFailureReason reason = exception.getReason();

        if (reason == ServiceBusFailureReason.MESSAGING_ENTITY_DISABLED
                || reason == ServiceBusFailureReason.MESSAGING_ENTITY_NOT_FOUND
                || reason == ServiceBusFailureReason.UNAUTHORIZED) {
            logger.info("An unrecoverable error occurred. Stopping processing with reason {}: {}",
                    reason, exception.getMessage());
        } else if (reason == ServiceBusFailureReason.MESSAGE_LOCK_LOST) {
            logger.info("Message lock lost for message: {}", context.getException());
        } else if (reason == ServiceBusFailureReason.SERVICE_BUSY) {
            try {
                // Choosing an arbitrary amount of time to wait until trying again.
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                System.err.println("Unable to sleep for period of time");
            }
        } else {
            logger.info("Error source {}, reason {}, message: {}", context.getErrorSource(),
                    reason, context.getException());
        }
    }

}
