package com.azure.csu.tiger.ansible.agent.service.impl;

import com.azure.csu.tiger.ansible.agent.config.ConstantsConfig;
import com.azure.csu.tiger.ansible.agent.dao.JobResultDao;
import com.azure.csu.tiger.ansible.agent.helper.*;
import com.azure.csu.tiger.ansible.agent.jooq.tables.records.JoblistRecord;
import com.azure.csu.tiger.ansible.agent.jooq.tables.records.JobresultsRecord;
import com.azure.csu.tiger.ansible.agent.model.AnsibleExecuteMsg;
import com.azure.csu.tiger.ansible.agent.service.AnsibleClientService;
import com.azure.csu.tiger.ansible.agent.model.ReturnValue;
import com.azure.csu.tiger.ansible.agent.service.ServiceBusClientTopicSubService;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class AnsibleClientServiceImpl implements AnsibleClientService {

    private static final Logger logger = LoggerFactory.getLogger(AnsibleClientServiceImpl.class);

    @Autowired
    public AnsibleAgentHelper ansibleAgentHelper;

    @Autowired
    private AzureBlobHelper azureBlobHelper;

    @Autowired
    private JoblistHelper joblistHelper;

    @Autowired
    private JobResultDao jobResultDao;

    @Autowired
    private AzureKeyVaultHelper azureKeyVaultHelper;

    @Autowired
    private SystemCommandHelper systemCommandHelper;

    @Value("${ansible.inventory.hosts.path}")
    private String inventoryHostsPath;

    @Value("${ansible.inventory.hosts.name}")
    private String inventoryHostsName;

    @Value("${ansible.inventory.nodename}")
    private String inventoryNodename;

    @Value("${ansible.privatekeyname}")
    private String privateKeyname;

    @Value("${ansible.keysecretname}")
    private String keySecretname;

    @Value("${host.pod-name}")
    private String podName;

    @Value("${host.pod-ip}")
    private String podIp;

    @Value("${spring.application.name}")
    private String appName;

    String playbookFullPathName, privateFullPathName, argString;
    Map<String, ReturnValue> result;
    Integer timeout;

    public String saveJoblist(AnsibleExecuteMsg msg,Integer jobExtStatus, String jobResult){

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
            record.setJobresult(StringUtils.hasText(jobResult) ? jobResult : "");
            record.setAppname(String.format("Pod Name: %s, Pod IP: %s, App Name: %s", podName, podIp, appName));
            record.setCreatetime(LocalDateTime.now());

            return joblistHelper.saveJob(record);

            
        } catch (Exception e) {
            logger.error("Save Joblist Failed: " + record.toString());
            //e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public void saveJobResult(String ansibleLog,String jobId){

        logger.info("Save Jobresult Start: "+jobId+"@@@@@@"+ansibleLog);
        JobresultsRecord record = new JobresultsRecord();
        //Save job message to db
        try {
            record.setJobid(jobId);
            record.setAnsiblelog(ansibleLog);
            record.setCreatetime(LocalDateTime.now());

            jobResultDao.createJobresult(record);

        } catch (Exception e) {
            logger.error("Save Joblist Failed: " + record.toString());
            //e.printStackTrace();
            throw new RuntimeException(e);
        }
    }    

    public void prepareFacilityFile(AnsibleExecuteMsg msg){

        try {
            String playUri = msg.getPlaybookUri();

            logger.info("Start Download Playbook file: "+playUri);

            //playbookFullPathName = azureBlobHelper.downloadBlobFile(FileHelper.extractFileName(playUri));
            playbookFullPathName = azureBlobHelper.downloadFileByHttpClient(playUri);

            List<String> scriptsUri = msg.getScriptFileUris();


            scriptsUri.forEach(scritUrl->{
                logger.info("Start Downlaod Scrit file: "+scritUrl);
                //azureBlobHelper.downloadBlobFile(FileHelper.extractFileName(scritUrl));
                azureBlobHelper.downloadFileByHttpClient(scritUrl);
            });

            logger.info("Start Downlaod private key file: "+privateKeyname);
            if (StringUtils.hasText(msg.getSshKeyVaultName()) && StringUtils.hasText(msg.getSshPrivateSecretName())) {
                privateFullPathName = azureKeyVaultHelper.getKeyValueFromKV(msg.getSshKeyVaultName(), msg.getSshPrivateSecretName());
            } else {
                privateFullPathName = azureKeyVaultHelper.getKeyValueFromKV(keySecretname);
            }
        } catch (Exception e) {
            logger.error("Down Load Facility Files Failed: "+e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void initHost(List<String> ips, String nodeName){
        try {
            ansibleAgentHelper.genAnsibleConfig();

            ansibleAgentHelper.genInventoryHosts(ips,nodeName);

            ansibleAgentHelper.removeExistKnownHosts();

        } catch (IOException e) {
            logger.info("Prepare Inventory Hosts, Ansible CFG, Know Hosts Failed: "+e.getMessage());
            throw new RuntimeException(e);
        }
        
    }
    public List<String> preExecutePlayBook(AnsibleExecuteMsg ansibleExeMsg){

           List<String> playbookCmd = new ArrayList<>();

            //Save Ansible job lists
            saveJoblist(ansibleExeMsg,ConstantsConfig.JOB_EXECUTE_STATUS_RUNNING.getNumberValue(), "");

           //Download Playbook, Shell Scripts, Privatekey
            prepareFacilityFile(ansibleExeMsg);

            //Init ansible inventory and cfg
            initHost(ansibleExeMsg.getNodeList(),inventoryNodename);

            playbookCmd = ansibleAgentHelper.createPlaybookCmd();
            playbookCmd.add(playbookFullPathName);

            playbookCmd=ansibleAgentHelper.populateAuth(playbookCmd, ansibleExeMsg, privateFullPathName);

            playbookCmd= ansibleAgentHelper.populateExtraVars(playbookCmd, ansibleExeMsg);

//            playbookCmd.add("-vvv");

            timeout = ansibleExeMsg.getTimeout();

            return playbookCmd;
    }


        @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 2000))
        public void runPlaybook(List<String> pbc,AnsibleExecuteMsg msg) {

            List<String> sOut = new ArrayList<>();

            try {

                sOut = systemCommandHelper.executeCommand(pbc, timeout);
                if (sOut.contains("Job Failed")) {
                    logger.info("Job Failed. Timeout reached after 15 minutes. Node IPs: {}", msg.getNodeList());
                }

                result = CommandExtResultHelper.parseCommandReturnValues(sOut);


            } catch (Exception e) {
                logger.error("Execute PlayBook Failed: %%%%%%%%%%%%%%%%"+"execute system command: "+pbc.stream().collect(Collectors.joining(" ")), e);
                throw new RuntimeException(e);
            }finally{
                String jobId;
                AtomicInteger jobStatus = new AtomicInteger(ConstantsConfig.JOB_EXEXUTE_STATUS_DONE.getNumberValue());
                JsonObject jobResult = new JsonObject();
                if(result != null && !result.values().isEmpty()){
                    msg.getNodeList().forEach(item->{
                        if(!result.get(item).isSuccess()) {
                            jobStatus.set(ConstantsConfig.JOB_EXEXUTE_STATUS_FAILED.getNumberValue());
                            jobResult.addProperty(item, "Failed");
                        } else {
                            jobResult.addProperty(item, "Success");
                        }
                    });

                    jobId = saveJoblist(msg, jobStatus.get(), jobResult.toString());

                }else{
                    msg.getNodeList().forEach(item->{
                        jobResult.addProperty(item, "Failed");
                    });
                    jobId = saveJoblist(msg,
                            ConstantsConfig.JOB_EXEXUTE_STATUS_FAILED.getNumberValue(),
                            jobResult.toString());

                }
                saveJobResult(sOut.stream().collect(Collectors.joining("\n")), jobId);

            }

        }

}
