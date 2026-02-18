package com.azure.csu.tiger.ansible.agent.helper;

import com.azure.csu.tiger.ansible.agent.service.impl.AnsibleClientServiceImpl;
import com.ctc.wstx.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.azure.csu.tiger.ansible.agent.model.AnsibleExecuteMsg;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class AnsibleAgentHelper {

    private static final Logger logger = LoggerFactory.getLogger(AnsibleAgentHelper.class);

    @Autowired
    TemplateEngine templateEngine;

    @Value("${ansible.inventory.hosts.path}")
    private String ansibleInventoryPath;

    @Value("${ansible.inventory.hosts.name}")
    private String ansibleInventoryName;

    @Value("${ansible.cfg.path}")
    private String ansibleCfgPath;

    @Value("${ansible.cfg.name}")
    private String ansibleCfgName;

    @Value("${ansible.known.hosts.fullpathname}")
    private String ansibleKnowHostsName;

    @Value("${ansible.ssh.connection.timeout}")
    private String ansibleSshConnTimeout;

    @Value("${ansible.ssh.connection.retry}")
    private String ansibleSshConnRetry;

    @Value("${ansible.forks}")
    private String ansibleForks;

    @Value("${ansible.remote.port}")
    private String ansibleRemotePort;

    @Value("${ansible.playbookrootpath}")
    private String playbookRootPath;

    public List<String> createPlaybookCmd(){

        List<String> pCmdList = new ArrayList<>();

        pCmdList.add(playbookRootPath);

        pCmdList.add("-i");

        pCmdList.add(ansibleInventoryPath+ansibleInventoryName);

        return pCmdList;
    }

    public List<String> populateAuth(List<String> pCmdList,AnsibleExecuteMsg msg,String key){

        if (StringUtils.hasLength(msg.getUsername())) {
            pCmdList.add("-u");
            pCmdList.add(msg.getUsername());
        }

        pCmdList.add("--key-file");

        pCmdList.add(key);
         
        return pCmdList;

    }

    public List<String> populateExtraVars(List<String> pCmdList,AnsibleExecuteMsg msg){

        if(StringUtils.hasText(msg.getExtraVars())) {
            pCmdList.add("-e");
            String extraVars = msg.getExtraVars().trim();
            if (msg.getExtraVars().startsWith("-e")) {
                extraVars = extraVars.substring(2).trim();
            }
            if (extraVars.startsWith("\"") && extraVars.endsWith("\"")) {
                extraVars = extraVars.substring(1, extraVars.length() - 1).trim();
            }
            logger.info("ExtraVars: {}", extraVars);
            pCmdList.add(extraVars);
        }

        return pCmdList;
    }

    public void genAnsibleConfig()  throws IOException {

        Context context = new Context();
        context.setVariable("inventoryPath", ansibleInventoryPath);
        context.setVariable("ansibleForkNum", ansibleForks);
        context.setVariable("sshConnectionTimeout", ansibleSshConnTimeout);
        context.setVariable("sshConnectionRetry", ansibleSshConnRetry);
        context.setVariable("ansibleRemotePort", ansibleRemotePort);
        String output = templateEngine.process("ansibleConfigTemplate.cfg", context);
        FileHelper.createFile(ansibleCfgPath+ansibleCfgName,"cfg",true);
        try (
                BufferedWriter writer = new BufferedWriter(new FileWriter(ansibleCfgPath+ansibleCfgName))) {
                writer.write(output);
        }
    }

    public void genInventoryHosts(List<String> ipAddresses, String nodeName)  throws IOException {

//        Context context = new Context();
//
//        if(ipAddresses==null || ipAddresses.isEmpty() || nodeName==null || nodeName.isEmpty()) {
//            throw new IllegalArgumentException("ipAddresses or nodeName is null or empty");
//        }
//
//        context.setVariable("nodeName",nodeName);
//        context.setVariable("ips", ipAddresses);
//        String output = templateEngine.process("hosts", context);
//        System.out.println("output: "+output);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(nodeName);
        for(String ip : ipAddresses){
            stringBuffer.append(System.lineSeparator());
            stringBuffer.append(ip);
        }
        String output = stringBuffer.toString();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ansibleInventoryPath+ansibleInventoryName))) {
            writer.write(output);
        }
    }

    public void removeExistKnownHosts(){

        FileHelper.deleteFile(ansibleKnowHostsName);
    }
}
