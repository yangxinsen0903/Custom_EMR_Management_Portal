package com.azure.csu.tiger.rm.api.dao.impl;

import com.azure.csu.tiger.rm.api.dao.JobDao;
import com.azure.csu.tiger.rm.api.enums.JobStatus;
import com.azure.csu.tiger.rm.api.enums.JobType;
import com.azure.csu.tiger.rm.api.jooq.Tables;
import com.azure.csu.tiger.rm.api.jooq.tables.records.SdpRmJobsRecord;
import com.azure.csu.tiger.rm.api.utils.ConstantUtil;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static org.jooq.impl.DSL.*;

@Repository
public class JobDaoImpl implements JobDao {

    @Autowired
    private DSLContext context;

    @Override
    public void createJob(SdpRmJobsRecord record) {
        if (record == null) {
            return;
        }
        context.insertInto(Tables.SDP_RM_JOBS)
                .set(record)
                .execute();
    }

    @Override
    public SdpRmJobsRecord findJob(String jobId) {
        if (jobId == null) {
            return null;
        }
        return context.select().from(Tables.SDP_RM_JOBS)
                .where(Tables.SDP_RM_JOBS.JOBID.eq(jobId))
                .fetchOneInto(SdpRmJobsRecord.class);
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

    @Override
    public SdpRmJobsRecord findLatestJobByName(String name) {
        return context.select().from(Tables.SDP_RM_JOBS)
                .where(Tables.SDP_RM_JOBS.NAME.eq(name))
                .orderBy(Tables.SDP_RM_JOBS.ID.desc())
                .limit(1)
                .fetchOneInto(SdpRmJobsRecord.class);
    }

    @Override
    public List<SdpRmJobsRecord> findCreateOrUpdateInProgressJob(String cluster) {
        return context.select().from(Tables.SDP_RM_JOBS)
                .where(Tables.SDP_RM_JOBS.JOBID.like("create-cluster-"+cluster+"-%").or(Tables.SDP_RM_JOBS.JOBID.like("append-cluster-"+cluster+"-%")))
                .and(field("JSON_EXTRACT(JSON_EXTRACT(jobArgs, '$.RawRequest'), '$.clusterName')").eq(cluster))
                .and(Tables.SDP_RM_JOBS.STATUS.eq(JobStatus.Started.name()))
                .fetchInto(SdpRmJobsRecord.class);
    }

    @Override
    public boolean isClusterDeletedOrDeleting(String cluster) {
        String rgName = ConstantUtil.getResourceGroupName(cluster);
        SdpRmJobsRecord record = context.select().from(Tables.SDP_RM_JOBS)
                .where(
                        or(
                            and(
                                Tables.SDP_RM_JOBS.JOBID.like("del-rg-"+ rgName+"-%"),
                                field("JSON_EXTRACT(jobArgs, '$.resourceGroup')").eq(rgName)
                            )
                            ,and(
                                Tables.SDP_RM_JOBS.JOBID.like("create-cluster-"+cluster+"-%"),
                                field("JSON_EXTRACT(JSON_EXTRACT(jobArgs, '$.RawRequest'), '$.clusterName')").eq(cluster)
                            )
                            ,and(
                                Tables.SDP_RM_JOBS.JOBID.like("append-cluster-"+cluster+"-%"),
                                field("JSON_EXTRACT(JSON_EXTRACT(jobArgs, '$.RawRequest'), '$.clusterName')").eq(cluster)
                            )
                        )
                )
                .orderBy(Tables.SDP_RM_JOBS.ID.desc())
                .limit(1)
                .fetchOneInto(SdpRmJobsRecord.class);
        if (record == null) {
            return true;
        }
        String jobType = record.getType();
        return jobType.equals(JobType.DeleteResourceGroup.name());
    }
}
