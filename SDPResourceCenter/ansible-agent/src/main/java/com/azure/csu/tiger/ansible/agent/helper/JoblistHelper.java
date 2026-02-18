package com.azure.csu.tiger.ansible.agent.helper;

import com.azure.csu.tiger.ansible.agent.config.ConstantsConfig;
import com.azure.csu.tiger.ansible.agent.dao.JobListDao;
import com.azure.csu.tiger.ansible.agent.jooq.tables.records.JoblistRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
public class JoblistHelper {

    @Autowired
    private JobListDao joblistDao;

    private static final Logger logger = LoggerFactory.getLogger(JoblistHelper.class);


    public String saveJob(JoblistRecord record) {
        if(record==null){
            logger.error("JobList is null:");
            throw new RuntimeException("JobList is null");
        }
        Integer status = record.getStatus();
        String jobId= record.getJobid();
        String jobResult = record.getJobresult();

        JoblistRecord existJob = joblistDao.findJob(jobId);
        if (existJob != null) {
            if (Objects.equals(existJob.getStatus(), ConstantsConfig.JOB_EXECUTE_STATUS_NOTSTART.getNumberValue())) {
                joblistDao.updateJobStatusAndAppName(status, record.getAppname(), jobId);
            } else if(!Objects.equals(existJob.getStatus(), record.getStatus())) {
                joblistDao.updateJobStatusAndResult(status, jobResult, jobId);
                logger.info("Update Job Status: JobID = {}, Status={}, JobResult={}", jobId, status, jobResult);
            }
        } else {
            joblistDao.createJob(record);
            jobId = record.getJobid();
        }
        logger.info("Joblist saved: {}", record.toString());

        return jobId;
    }
}
