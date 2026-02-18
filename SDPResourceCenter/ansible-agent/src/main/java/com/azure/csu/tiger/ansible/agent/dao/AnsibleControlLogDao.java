package com.azure.csu.tiger.ansible.agent.dao;

import com.azure.csu.tiger.ansible.agent.jooq.tables.records.AnsiblecontrollogRecord;

import java.util.List;

public interface AnsibleControlLogDao {

    void createAnsiblecontrollogRecord(AnsiblecontrollogRecord ansiblecontrollogRecord );

    List<AnsiblecontrollogRecord> findAnsiblecontrollogRecord(String transactionId ,Integer jobId);

    void updateAnsiblecontrollogRecordStatus(Integer status, Integer Id);
}
