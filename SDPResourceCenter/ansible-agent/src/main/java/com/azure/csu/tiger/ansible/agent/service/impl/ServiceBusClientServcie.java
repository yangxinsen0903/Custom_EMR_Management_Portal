package com.azure.csu.tiger.ansible.agent.service.impl;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.csu.tiger.ansible.agent.model.AnsibleExecuteMsg;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.*;
import com.azure.messaging.servicebus.models.SubQueue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ServiceBusClientServcie {

    // Service Bus连接字符串
    private static final String connectionString = "Endpoint";

    // Service Bus队列名称
    private static final String queueName = "ansibletask";

    //Topic Name
    private static final String topicName = "ansibletask";


    public static void messageReceiverHandler() throws JsonProcessingException {

        //serviceBusSenderHandler();

        //serviceBusReceiverTopic();
        serviceBusReceiverHandler();
        //serviceBusReceiver4RD();
    }

    private static void serviceBusReceiverHandlerEasy(){
        // 接收并处理队列中的消息
//        ServiceBusReceiverClient receiverClient = clientBuilder.receiver().queueName(queueName).buildClient();
//        for (ServiceBusReceivedMessage message : receiverClient.receiveMessages(1)) {
//            System.out.println("Received message: " + message.getBody().toString());
//            // 处理消息后，确认消息已被处理
//            receiverClient.complete(message);
//        }
//
//        // 关闭ServiceBusClient
//        // senderClient.close();
//        receiverClient.close();
    }

    private static void serviceBusSenderHandler(){
        //创建ServiceBusBuilder
        ServiceBusClientBuilder clientBuilder = new ServiceBusClientBuilder().connectionString(connectionString);

        // 发送处理队列中的消息
        try (ServiceBusSenderClient senderClient = clientBuilder.sender().topicName(topicName).buildClient()) {

            AnsibleExecuteMsg aem = new AnsibleExecuteMsg();
            aem.setTransactionId("123456");

            aem.setJobId("1123456778");

            List<String> nodelist = new ArrayList<>(Arrays.asList("127.0.0.1","127.0.0.1","127.0.0.1"));
            aem.setNodeList(nodelist);

            aem.setPlaybookType(1);

            aem.setPlaybookUri("/home/sdp/playbook/bandwidth.yaml");

            List<String> scriptList = new ArrayList<>(Arrays.asList("list.sh","cd.sh","pwd.sh"));
            aem.setScriptFileUris(scriptList);

            aem.setExtraVars("");

            aem.setUsername("sdp");

            aem.setTimeout(100);

            ObjectMapper mapper = new ObjectMapper();
            String message = mapper.writeValueAsString(aem);
            ServiceBusMessage sbm = new ServiceBusMessage(message).setSessionId("sessionidsample");
            senderClient.sendMessage(sbm);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    //Topic Handler
    private static void serviceBusReceiverTopicHandler(){

        ServiceBusClientBuilder clientBuilder = new ServiceBusClientBuilder().connectionString(connectionString);


        // Function that gets called whenever a message is received.

        Consumer<ServiceBusReceivedMessageContext> processMessage = context -> {
            final ServiceBusReceivedMessage message = context.getMessage();
            // Randomly complete or abandon each message. Ideally, in real-world scenarios, if the business logic
            // handling message reaches desired state such that it doesn't require Service Bus to redeliver
            // the same message, then context.complete() should be called otherwise context.abandon().
            final boolean success = Math.random() < 0.5;
            if (success) {
                try {
                    System.out.println("&&&&&&&&&&&-"+message.getMessageId());

                    JsonSerializer jsp = JsonSerializerProviders.createInstance(true);
                    Mono<AnsibleExecuteMsg> objectAsync = message.getBody().toObjectAsync(AnsibleExecuteMsg.class, jsp);

                    System.out.println("%%%%%%%%%%%%"+objectAsync.toString());


                    context.complete();
                } catch (RuntimeException error) {
                    System.out.printf("Completion of the message %s failed.%n Error: %s%n",
                            message.getMessageId(), error);
                }
            } else {
                try {
                    System.out.println("!!!!!!!!!!!!!!!!!-"+message.getMessageId());
                    JsonSerializer jsp = JsonSerializerProviders.createInstance(true);
                    Mono<AnsibleExecuteMsg> objectAsync = message.getBody().toObjectAsync(AnsibleExecuteMsg.class, jsp);

                    System.out.println("%%%%%%%%%%%%"+objectAsync.toString());
                    context.abandon();
                } catch (RuntimeException error) {
                    System.out.printf("Abandoning of the message %s failed.%nError: %s%n",
                            message.getMessageId(), error);
                }
            }
        };

// Sample code that gets called if there's an error
        Consumer<ServiceBusErrorContext> processError = errorContext -> {
            if (errorContext.getException() instanceof ServiceBusException exception) {

                System.out.printf("Error source: %s, reason %s%n", errorContext.getErrorSource(),
                        exception.getReason());
            } else {
                System.out.printf("Error occurred: %s%n", errorContext.getException());
            }
        };

        //TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

// Create the processor client via the builder and its sub-builder
// 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        try (ServiceBusProcessorClient processorClient = clientBuilder
                .processor()
                .topicName(topicName)
                .subscriptionName("job1")
                .subQueue(SubQueue.DEAD_LETTER_QUEUE)
                //.queueName(queueName)
                //.receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
                //.disableAutoComplete()  // Make sure to explicitly opt in to manual settlement (e.g. complete, abandon).
                .processMessage(processMessage)
                .processError(processError)
                //.disableAutoComplete()
                .buildProcessorClient()) {

// Starts the processor in the background. Control returns immediately.
            processorClient.start();
        }

// Stop processor and dispose when done processing messages.
        //processorClient.stop();
        //processorClient.close();
    }


    private static void serviceBusReceiverHandler(){

        ServiceBusClientBuilder clientBuilder = new ServiceBusClientBuilder().connectionString(connectionString);


// Function that gets called whenever a message is received.

        Consumer<ServiceBusReceivedMessageContext> processMessage = context -> {
            final ServiceBusReceivedMessage message = context.getMessage();
            // Randomly complete or abandon each message. Ideally, in real-world scenarios, if the business logic
            // handling message reaches desired state such that it doesn't require Service Bus to redeliver
            // the same message, then context.complete() should be called otherwise context.abandon().
            final boolean success = Math.random() < 0.5;
            if (success) {
                try {
                    System.out.println("&&&&&&&&&&&-"+message.getMessageId());

                    JsonSerializer jsp = JsonSerializerProviders.createInstance(true);
                    Mono<AnsibleExecuteMsg> objectAsync = message.getBody().toObjectAsync(AnsibleExecuteMsg.class, jsp);

                    System.out.println("%%%%%%%%%%%%"+objectAsync.toString());


                    context.complete();
                } catch (RuntimeException error) {
                    System.out.printf("Completion of the message %s failed.%n Error: %s%n",
                            message.getMessageId(), error);
                }
            } else {
                try {
                    System.out.println("!!!!!!!!!!!!!!!!!-"+message.getMessageId());
                    JsonSerializer jsp = JsonSerializerProviders.createInstance(true);
                    Mono<AnsibleExecuteMsg> objectAsync = message.getBody().toObjectAsync(AnsibleExecuteMsg.class, jsp);

                    System.out.println("%%%%%%%%%%%%"+objectAsync.toString());
                    context.abandon();
                } catch (RuntimeException error) {
                    System.out.printf("Abandoning of the message %s failed.%nError: %s%n",
                            message.getMessageId(), error);
                }
            }
        };

// Sample code that gets called if there's an error
        Consumer<ServiceBusErrorContext> processError = errorContext -> {
            if (errorContext.getException() instanceof ServiceBusException exception) {

                System.out.printf("Error source: %s, reason %s%n", errorContext.getErrorSource(),
                        exception.getReason());
            } else {
                System.out.printf("Error occurred: %s%n", errorContext.getException());
            }
        };

        //TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

// Create the processor client via the builder and its sub-builder
// 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        try (ServiceBusProcessorClient processorClient = clientBuilder
                .processor()
                //.topicName(topicName)
                //.subscriptionName("job1")
                //.subQueue(SubQueue.DEAD_LETTER_QUEUE)
                .queueName(queueName)
                //.receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
                .disableAutoComplete()  // Make sure to explicitly opt in to manual settlement (e.g. complete, abandon).
                .processMessage(processMessage)
                .processError(processError)
                .disableAutoComplete()
                .buildProcessorClient()) {

// Starts the processor in the background. Control returns immediately.
            processorClient.start();
        }

// Stop processor and dispose when done processing messages.
        //processorClient.stop();
        //processorClient.close();
    }

 private static void serviceBusReceiver4RD(){
     ServiceBusClientBuilder clientBuilder = new ServiceBusClientBuilder().connectionString(connectionString);

     // Function that gets called whenever a message is received.
     Consumer<ServiceBusReceivedMessageContext> processMessage = context -> {
         final ServiceBusReceivedMessage message = context.getMessage();
         System.out.printf("Processing message. Session: %s, Sequence #: %s. Contents: %s%n",
                 message.getSessionId(), message.getSequenceNumber(), message.getBody());

         JsonSerializer jsp = JsonSerializerProviders.createInstance(true);
         Mono<AnsibleExecuteMsg> objectAsync = message.getBody().toObjectAsync(AnsibleExecuteMsg.class, jsp);

         System.out.println("%%%%%%%%%%%%"+objectAsync.toString());


     };

// Sample code that gets called if there's an error
     Consumer<ServiceBusErrorContext> processError = errorContext -> {
         if (errorContext.getException() instanceof ServiceBusException) {
             ServiceBusException exception = (ServiceBusException) errorContext.getException();

             System.out.printf("Error source: %s, reason %s%n", errorContext.getErrorSource(),
                     exception.getReason());
         } else {
             System.out.printf("Error occurred: %s%n", errorContext.getException());
         }
     };

     //TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

// Create the processor client via the builder and its sub-builder
// 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
// 'disableAutoComplete()' will opt in to manual settlement (e.g. complete, abandon).
     ServiceBusProcessorClient processorClient = clientBuilder
             //.credential(fullyQualifiedNamespace, tokenCredential)
             .processor()
             .queueName(queueName)
             //.receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
             .processMessage(processMessage)
             .processError(processError)
             .disableAutoComplete()
             .buildProcessorClient();

// Starts the processor in the background. Control returns immediately.
     processorClient.start();

// Stop processor and dispose when done processing messages.
     processorClient.stop();
     processorClient.close();
 }

 private static void serviceBusReceiverTopic(){

     TokenCredential credential = new DefaultAzureCredentialBuilder().build();

// 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
// 'disableAutoComplete' indicates that users will explicitly settle their message.
     ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
             .credential("ansible1.servicebus.windows.net", credential)
             .receiver() // Use this for session or non-session enabled queue or topic/subscriptions
             .topicName(topicName)
             .subscriptionName("job1")
             .subQueue(SubQueue.DEAD_LETTER_QUEUE)
             .buildClient();


     ServiceBusReceivedMessage receivedMessage = receiver.peekMessage();
     while (!(receivedMessage ==null)){

         JsonSerializer jsp = JsonSerializerProviders.createInstance(true);
         Mono<AnsibleExecuteMsg> objectAsync = receivedMessage.getBody().toObjectAsync(AnsibleExecuteMsg.class, jsp);

         System.out.println("%%%%%%%%%%%%"+objectAsync.toString());
         receivedMessage = receiver.peekMessage();

     }
// When users are done with the receiver, dispose of the receiver.
// Clients should be long-lived objects as they require resources
// and time to establish a connection to the service.
     receiver.close();
 }
}
