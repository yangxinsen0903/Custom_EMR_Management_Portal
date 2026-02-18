package com.azure.csu.tiger.rm.task.dao;


import com.azure.csu.tiger.rm.task.jooq.tables.records.SdpRmJobsRecord;

import java.util.List;

public interface JobDao {

    List<SdpRmJobsRecord> findInProgressJob();

    void updateJobStatus(String status, String jobId);
}
