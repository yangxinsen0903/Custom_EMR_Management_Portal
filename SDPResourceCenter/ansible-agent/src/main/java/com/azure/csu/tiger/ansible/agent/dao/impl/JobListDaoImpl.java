package com.azure.csu.tiger.ansible.agent.dao.impl;

import com.azure.csu.tiger.ansible.agent.dao.JobListDao;
import com.azure.csu.tiger.ansible.agent.jooq.Tables;
import com.azure.csu.tiger.ansible.agent.jooq.tables.records.JoblistRecord;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JobListDaoImpl implements JobListDao {

    @Autowired
    private DSLContext context;

    @Override
    public void createJob(JoblistRecord record) {
        if (record == null) {
            return;
        }
        context.insertInto(Tables.JOBLIST)
                .set(record)
                .execute();
    }

    @Override
    public JoblistRecord findJob(String jobId) {
        if (jobId == null) {
            return null;
        }

        return context.select().from(Tables.JOBLIST)
                .where(Tables.JOBLIST.JOBID.eq(jobId))
                .fetchOneInto(JoblistRecord.class);

    }

    @Override
    public List<JoblistRecord> fetchTransJobList(String transactionId) {
        if (transactionId == null) {
            return null;
        }

        return context.select().from(Tables.JOBLIST)
                .where(Tables.JOBLIST.TRANSCATIONID.eq(transactionId))
                .fetchInto(JoblistRecord.class);

    }

    @Override
    public void updateJobStatusAndResult(int status, String jobResult, String jobId) {

        context.update(Tables.JOBLIST)
                .set(Tables.JOBLIST.STATUS, status)
                .set(Tables.JOBLIST.JOBRESULT, jobResult)
                .where(Tables.JOBLIST.JOBID.eq(jobId))
                .execute();
    }

    @Override
    public void updateJobStatusAndAppName(int status, String appName, String jobId) {
        context.update(Tables.JOBLIST)
                .set(Tables.JOBLIST.STATUS, status)
                .set(Tables.JOBLIST.APPNAME, appName)
                .where(Tables.JOBLIST.JOBID.eq(jobId))
                .execute();
    }
}
