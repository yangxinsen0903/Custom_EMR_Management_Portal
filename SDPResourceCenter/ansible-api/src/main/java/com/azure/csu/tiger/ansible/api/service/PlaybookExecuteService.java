package com.azure.csu.tiger.ansible.api.service;

import com.azure.csu.tiger.ansible.api.model.AnsibleExecuteMsg;
import com.azure.csu.tiger.ansible.api.bo.AnsibleTransResponse;

import java.util.List;

public interface PlaybookExecuteService {

    public void saveAnsibleControlLog(String reqquestMessage,String responseString,String transactionId,int status);

    public List<String> sendMessageToServiceBusTopic(AnsibleExecuteMsg ansibleExecuteMsg);

    public AnsibleTransResponse fetchTransactionResult(String transactionId);
}
