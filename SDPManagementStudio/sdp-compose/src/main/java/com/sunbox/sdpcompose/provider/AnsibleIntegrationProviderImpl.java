package com.sunbox.sdpcompose.provider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.JobResults;
import com.sunbox.sdpcompose.mapper.JobResultsMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AnsibleIntegrationProviderImpl implements IntegrationProvider {
    public static final int STATUS_FAIL = 3;

    @Autowired
    JobResultsMapper jobResultsMapper;

    @Override
    public String collectLog(Object param) {
        getLogger().info("begin collect log,param:{}", param);
        if (param == null) {
            getLogger().error("collect log error,param is null");
            return null;
        }
        if (!JSONArray.class.isAssignableFrom(param.getClass())) {
            getLogger().error("collect log error,param type is not JSONArray");
            return null;
        }
        StringBuilder logBuilder = new StringBuilder();
        JSONArray jsonArray = (JSONArray) param;
        for (int index = 0; index < jsonArray.size(); index++) {
            Object jobObject = jsonArray.get(index);
            if (jobObject == null) {
                continue;
            }

            if (!JSONObject.class.isAssignableFrom(jobObject.getClass())) {
                continue;
            }

            JSONObject jobJsonObj = (JSONObject) jobObject;
            String jobID = null;
            Integer jobStatus = null;
            try {
                jobID = jobJsonObj.getString("jobID");
                jobStatus = jobJsonObj.getInteger("jobStatus");
            } catch (Exception e) {
                getLogger().error("get jobID and jobStatus error,param:{}", param, e);
                continue;
            }

            if (jobStatus == null || StringUtils.isEmpty(jobID)) {
                getLogger().error("jobID and jobStatus is invalid,jobID:{},jobStatus:{}", jobID, jobStatus);
                continue;
            }

            if (jobStatus != STATUS_FAIL) {
                continue;
            }

            String collectLog = selectJobLogByJobId(jobID);
            if (StringUtils.isEmpty(collectLog)) {
                continue;
            }
            logBuilder.append(collectLog);
        }

        if (logBuilder.length() == 0) {
            return null;
        }
        getLogger().info("collect log finish,log:{}", logBuilder);
        if (logBuilder.length() > 60 * 1025) {
            return logBuilder.substring(0, 60 * 1024);
        }
        return logBuilder.toString();
    }

    private String selectJobLogByJobId(String jobID) {
        try {
            JobResults jobResults = jobResultsMapper.selectByJobId(jobID);
            if (jobResults == null) {
                return null;
            }
            if (StringUtils.isEmpty(jobResults.getAnsiblelog())) {
                return null;
            }
            return jobResults.getAnsiblelog();
        } catch (Exception e) {
            getLogger().error("selectJobLogByJobId error jobId:{},message:{}", jobID, e.getMessage());
            return null;
        }
    }
}
