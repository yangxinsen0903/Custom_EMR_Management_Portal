package com.azure.csu.tiger.ansible.api.service.impl;

import com.azure.csu.tiger.ansible.api.config.ConstantsConfig;
import com.azure.csu.tiger.ansible.api.dao.AnsibleControlLogDao;
import com.azure.csu.tiger.ansible.api.dao.JobListDao;
import com.azure.csu.tiger.ansible.api.jooq.tables.records.AnsiblecontrollogRecord;
import com.azure.csu.tiger.ansible.api.jooq.tables.records.JoblistRecord;
import com.azure.csu.tiger.ansible.api.model.AnsibleExecuteMsg;
import com.azure.csu.tiger.ansible.api.service.ServiceBusClientTopicSubService;
import com.azure.csu.tiger.ansible.api.bo.AgentExecuteDTO;
import com.azure.csu.tiger.ansible.api.bo.AnsibleTransResponse;
import com.azure.csu.tiger.ansible.api.helper.NodeListHelper;
import com.azure.csu.tiger.ansible.api.helper.SerializationHelper;
import com.azure.csu.tiger.ansible.api.helper.UniqueIdGeneratorHelper;
import com.azure.csu.tiger.ansible.api.service.PlaybookExecuteService;
import com.azure.csu.tiger.ansible.api.vo.AnsibleExtMsg;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.google.common.collect.Lists;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlaybookExecuteServiceImpl implements PlaybookExecuteService {

    private static final Logger logger = LoggerFactory.getLogger(PlaybookExecuteServiceImpl.class);

    @Value("${ansible.agentnum}")
    private int agentNum;

    @Autowired
    AnsibleControlLogDao ansibleControlLogDao;

    @Autowired
    ServiceBusClientTopicSubService serviceBusCTSService;

    @Autowired
    JobListDao jobListDao;

    public void saveAnsibleControlLog(String reqquestMessage,String responseString,String transactionId,int status){
        logger.info("Save Ansible ControlLog start: @@@@@@");
        AnsiblecontrollogRecord ansiblecontrollogRecord = new AnsiblecontrollogRecord();

        ansiblecontrollogRecord.setRequestmsg(reqquestMessage);
        ansiblecontrollogRecord.setResponsemsg(responseString);
        ansiblecontrollogRecord.setTransactionid(transactionId);
        ansiblecontrollogRecord.setStatus(status);
        ansiblecontrollogRecord.setLoglevel(ConstantsConfig.LOGlEVEL_DEFAULT.getNumberValue());
        ansiblecontrollogRecord.setCreatetime(LocalDateTime.now());
        ansibleControlLogDao.createAnsiblecontrollogRecord(ansiblecontrollogRecord);
        logger.info("Save Ansible ControlLog end: $$$$$$ "+ansiblecontrollogRecord.toString());
    }

    public List<String> getRetryJobList(AnsibleExtMsg ansibleExecuteMsg){

        //Todo

        return new ArrayList<>();
    }

    public List<String> sendMessageToServiceBusTopic(AnsibleExecuteMsg ansibleExecuteMsg){
        List<String> jobList = Lists.newArrayList();

        //Todo getRetryJobList

        logger.info("Send Ansible Execute Mesage to ServiceBus Start: @@@@@@");
        List<ServiceBusMessage> listOfMessages = new ArrayList<>();
        List<List<String>> nodeList = NodeListHelper.partition(ansibleExecuteMsg.getNodeList(),agentNum);

        nodeList.forEach(sublist -> {

            logger.info("Group Execute Job: $$$$$$ Sublist of size {}: {}", sublist.size(), sublist);

            AnsibleExecuteMsg tAnsibleExtMsg = ansibleExecuteMsg.copy();
            String jobId = UniqueIdGeneratorHelper.generateUniqueId();
            tAnsibleExtMsg.setJobId(jobId);
            tAnsibleExtMsg.setNodeList(sublist);
            jobList.add(jobId);
            try {
                listOfMessages.add(new ServiceBusMessage(SerializationHelper.serializeToJson(tAnsibleExtMsg)));
            } catch (Exception e) {
                logger.error("Populate JSON Message Exception: {}", e.getMessage());
                e.printStackTrace();
            }
            saveJobList(tAnsibleExtMsg, ConstantsConfig.JOB_EXECUTE_STATUS_NOTSTART.getNumberValue());
        });

        serviceBusCTSService.sendMessageBatchWithCre(listOfMessages);
        logger.info("Send Ansible Execute Mesage to ServiceBus End: $$$$$$ {}", listOfMessages);
        return jobList;
    }

    public AnsibleTransResponse fetchTransactionResult(String transactionId){

        logger.info("Get JOB Status Start: $$$$$$ "+transactionId);
        AnsibleTransResponse ansibleTransResponse = new AnsibleTransResponse();
        List<JoblistRecord> joblistRecords = jobListDao.fetchTransJobList(transactionId);
        List<AgentExecuteDTO> ansibleTransJobList = new ArrayList<>();

        joblistRecords.forEach(item->{

            ModelMapper modelMapper = new ModelMapper();
            AgentExecuteDTO tMapMsg = modelMapper.map(item,AgentExecuteDTO.class);

            ansibleTransJobList.add(tMapMsg);
            ansibleTransResponse.setTransactionId(item.getTranscationid());

        });

        ansibleTransResponse.setJobList(ansibleTransJobList);

        logger.info("Get JOB Status End: $$$$$$ "+transactionId);

        return ansibleTransResponse;
    }

    public String saveJobList(AnsibleExecuteMsg msg,Integer jobExtStatus){

        logger.info("Save Joblist Start: "+msg.toString());
        JoblistRecord record = null;
        //Save job message to db
        try {
            record = new JoblistRecord();
            record.setTranscationid(msg.getTransactionId());
            record.setHosts(msg.getNodeList().toString());
            record.setStatus(jobExtStatus);
            record.setJobid(msg.getJobId());
            record.setMessage(SerializationHelper.serializeToJson(msg));
            record.setJobresult("");
            record.setCreatetime(LocalDateTime.now());

            jobListDao.createJob(record);

            return record.getJobid();
        } catch (Exception e) {
            logger.error("Save Joblist Failed: " + record.toString());
            //e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
