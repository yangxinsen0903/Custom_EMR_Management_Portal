package com.azure.csu.tiger.rm.task.service;

import com.azure.csu.tiger.rm.task.dao.JobDao;
import com.azure.csu.tiger.rm.task.jooq.tables.records.SdpRmJobsRecord;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class ScanUnCompletedJob {

    private static final Logger logger = LoggerFactory.getLogger(ScanUnCompletedJob.class);

    @Autowired
    private JobDao jobDao;

    private HttpClient httpClient;

    @Value("${rm.api.url}")
    private String rmApiUrl;

    @PostConstruct
    private void init() {
        httpClient = HttpClients.createDefault();
    }

    @Scheduled(cron = "*/30 * * * * *")
    public void scanUnCompletedJob() {
        logger.info("Scan uncompleted job");
        List<SdpRmJobsRecord> inProgressJobs = jobDao.findInProgressJob();
        if (inProgressJobs.size() == 0) {
            logger.info("No in progress job");
            return;
        } else {
            logger.info("Find {} In progress job", inProgressJobs.size());
        }
        inProgressJobs.forEach( job -> {
            logger.info("Job {} is in progress", job.getJobid());
            String subscriptionId = JsonParser.parseString(job.getJobargs()).getAsJsonObject().get("subscriptionId").getAsString();
            findJobStatus(job.getJobid(), subscriptionId);
        });

    }

    private void findJobStatus(String jobId, String subscriptionId) {
        HttpGet request = new HttpGet(String.format("%s/%s", rmApiUrl, jobId));
        request.addHeader("subscriptionId", subscriptionId);
        try {
            httpClient.execute(request,
                    response -> {
                        if (response.getCode() == 200) {
                            logger.info("Find job status successful.");
                        } else {
                            jobDao.updateJobStatus("Unknown", jobId);
                            logger.error("Find job status failed, response code: {}", response.getCode());
                        }
                        return null;
                    });
        } catch (Exception e) {
            logger.error("Find job status error", e);
        }
    }
}
