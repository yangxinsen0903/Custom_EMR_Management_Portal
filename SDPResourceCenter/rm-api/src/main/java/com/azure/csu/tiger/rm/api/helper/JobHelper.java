package com.azure.csu.tiger.rm.api.helper;

import com.azure.csu.tiger.rm.api.dao.JobDao;
import com.azure.csu.tiger.rm.api.jooq.tables.records.SdpRmJobsRecord;
import com.azure.csu.tiger.rm.api.service.impl.VirtualMachineServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
public class JobHelper {

    @Autowired
    private JobDao jobDao;

    private static final Logger logger = LoggerFactory.getLogger(JobHelper.class);

    public void saveJob(String jobId, String deploymentName, String type, String status, String request ) {
        SdpRmJobsRecord record = new SdpRmJobsRecord();
        record.setJobid(jobId);
        record.setName(deploymentName);
        record.setType(type);
        record.setStatus(status);
        record.setJobargs(request);
        record.setCreatedtime(LocalDateTime.now());
        SdpRmJobsRecord existJob = jobDao.findJob(record.getJobid());
        if (existJob != null && Objects.equals(existJob.getStatus(), record.getStatus())) {
            jobDao.updateJobStatus(status, jobId);
            return;
        } else {
            jobDao.createJob(record);
        }
        logger.info("Job saved: {}", record.toString());
    }
}
