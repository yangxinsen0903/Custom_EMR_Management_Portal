package com.azure.csu.tiger.ansible.agent.dao;

import com.azure.csu.tiger.ansible.agent.jooq.tables.records.JobresultsRecord;

import java.util.List;

public interface JobResultDao {

    void createJobresult(JobresultsRecord jobresultRecord);

    JobresultsRecord findJobresult(String jobId);

    void updateJobresult(String ansibleLog, String jobId);
}
