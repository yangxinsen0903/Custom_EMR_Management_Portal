package com.azure.csu.tiger.ansible.agent.dao.impl;

import com.azure.csu.tiger.ansible.agent.jooq.Tables;
import com.azure.csu.tiger.ansible.agent.jooq.tables.records.AnsiblecontrollogRecord;
import com.azure.csu.tiger.ansible.agent.dao.AnsibleControlLogDao;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AnsibleControlLogDaoImpl implements AnsibleControlLogDao {

    @Autowired
    private DSLContext context;

    @Override
    public void createAnsiblecontrollogRecord(AnsiblecontrollogRecord record) {
        if (record == null) {
            return;
        }
        context.insertInto(Tables.ANSIBLECONTROLLOG)
                .set(record)
                .execute();
    }

    @Override
    public List<AnsiblecontrollogRecord> findAnsiblecontrollogRecord(String transactionId, Integer status) {
        if (transactionId == null) {
            return null;
        }

        return context.select().from(Tables.ANSIBLECONTROLLOG)
                .where(Tables.ANSIBLECONTROLLOG.TRANSACTIONID.eq(transactionId).and(Tables.ANSIBLECONTROLLOG.STATUS.eq(status)))
                .fetchInto(AnsiblecontrollogRecord.class);

    }

    @Override
    public void updateAnsiblecontrollogRecordStatus(Integer status, Integer Id) {

        context.update(Tables.ANSIBLECONTROLLOG)
                .set(Tables.ANSIBLECONTROLLOG.STATUS, status)
                .where(Tables.ANSIBLECONTROLLOG.ID.eq(Id))
                .execute();
    }
}
