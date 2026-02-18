package com.azure.csu.tiger.ansible.agent.dao.impl;

import com.azure.csu.tiger.ansible.agent.dao.JobListDao;
import com.azure.csu.tiger.ansible.agent.dao.JobResultDao;
import com.azure.csu.tiger.ansible.agent.jooq.Tables;
import com.azure.csu.tiger.ansible.agent.jooq.tables.records.JobresultsRecord;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JobResultDaoImpl implements JobResultDao {

    @Autowired
    private DSLContext context;

    @Override
    public void createJobresult(JobresultsRecord record) {
        if (record == null) {
            return;
        }
        context.insertInto(Tables.JOBRESULTS)
                .set(record)
                .execute();
    }

    @Override
    public JobresultsRecord findJobresult(String jobId) {
        if (jobId == null) {
            return null;
        }

        return context.select().from(Tables.JOBRESULTS)
                .where(Tables.JOBRESULTS.JOBID.eq(jobId))
                .fetchOneInto(JobresultsRecord.class);

    }


    @Override
    public void updateJobresult(String ansibleLog, String jobId) {

        context.update(Tables.JOBRESULTS)
                .set(Tables.JOBRESULTS.ANSIBLELOG, ansibleLog)
                .where(Tables.JOBRESULTS.JOBID.eq(jobId))
                .execute();
    }
}
