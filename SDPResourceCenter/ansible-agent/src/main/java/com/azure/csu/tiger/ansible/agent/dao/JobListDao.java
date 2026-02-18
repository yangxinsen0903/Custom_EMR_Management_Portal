package com.azure.csu.tiger.ansible.agent.dao;

import com.azure.csu.tiger.ansible.agent.jooq.tables.records.JoblistRecord;

import java.util.List;

public interface JobListDao {

    void createJob(JoblistRecord jobRecord);

    JoblistRecord findJob(String jobId);

    public List<JoblistRecord> fetchTransJobList(String transactionId);

    void updateJobStatusAndResult(int status, String jobResult, String jobId);

    void updateJobStatusAndAppName(int status, String appName, String jobId);
}
