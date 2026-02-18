package com.azure.csu.tiger.rm.task.dao.impl;

import com.azure.csu.tiger.rm.task.dao.JobDao;
import com.azure.csu.tiger.rm.task.enums.JobStatus;
import com.azure.csu.tiger.rm.task.jooq.Tables;
import com.azure.csu.tiger.rm.task.jooq.tables.records.SdpRmJobsRecord;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class JobDaoImpl implements JobDao {

    @Autowired
    private DSLContext context;

    @Override
    public List<SdpRmJobsRecord> findInProgressJob() {
        return context.select().from(Tables.SDP_RM_JOBS)
                .where(Tables.SDP_RM_JOBS.STATUS.eq(JobStatus.Started.name()))
                .fetchInto(SdpRmJobsRecord.class);
    }

    @Override
    public void updateJobStatus(String status, String jobId) {
        if (status == null) {
            return;
        }
        context.update(Tables.SDP_RM_JOBS)
                .set(Tables.SDP_RM_JOBS.STATUS, status)
                .set(Tables.SDP_RM_JOBS.LASTMODIFIEDTIME, LocalDateTime.now())
                .where(Tables.SDP_RM_JOBS.JOBID.eq(jobId))
                .execute();
    }
}
