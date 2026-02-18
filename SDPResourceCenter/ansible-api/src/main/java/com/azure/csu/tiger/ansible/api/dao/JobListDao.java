package com.azure.csu.tiger.ansible.api.dao;

import com.azure.csu.tiger.ansible.api.jooq.tables.records.JoblistRecord;

import java.util.List;

public interface JobListDao {

    void createJob(JoblistRecord jobRecord);

    JoblistRecord findJob(String jobId);

    public List<JoblistRecord> fetchTransJobList(String transactionId);

    void updateJobStatus(int status, String jobId);
}
