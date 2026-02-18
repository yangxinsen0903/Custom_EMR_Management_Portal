package com.azure.csu.tiger.ansible.api.service;

import com.azure.messaging.servicebus.ServiceBusMessage;

import java.util.List;

public interface ServiceBusClientTopicSubService {

    //public void receiveMessagesWithConnStr()  throws InterruptedException;

    //public void sendMessageWithCre();

    //public void receiveMessagesWithCre() throws InterruptedException;

    public void sendMessageBatchWithCre(List<ServiceBusMessage> listOfMessages);

    //public void sendMessageBatchWithConnStr(List<ServiceBusMessage> listOfMessages);
}
