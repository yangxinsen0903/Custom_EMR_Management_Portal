package com.azure.csu.tiger.rm.api.dao;

import com.azure.csu.tiger.rm.api.jooq.tables.records.SdpRmJobsRecord;

import java.util.List;

public interface JobDao {

    void createJob(SdpRmJobsRecord jobRecord);

    SdpRmJobsRecord findJob(String jobId);

    void updateJobStatus(String status, String jobId);

    SdpRmJobsRecord findLatestJobByName(String name);

    List<SdpRmJobsRecord> findCreateOrUpdateInProgressJob(String cluster);

    boolean isClusterDeletedOrDeleting(String cluster);
}
