package com.azure.csu.tiger.ansible.agent.service;

import com.azure.csu.tiger.ansible.agent.model.AnsibleExecuteMsg;

import java.util.List;

public interface AnsibleClientService {

        public void prepareFacilityFile(AnsibleExecuteMsg msg);

        public void initHost(List<String> hostsIp, String nodeName);

        public List<String> preExecutePlayBook(AnsibleExecuteMsg ansibleExeMsg);

        public void runPlaybook(List<String> playbookCmd,AnsibleExecuteMsg msg);
}
