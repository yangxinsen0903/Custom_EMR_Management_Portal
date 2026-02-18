package com.azure.csu.tiger.rm.api.utils;

import com.azure.csu.tiger.rm.api.dao.JobDao;
import com.azure.csu.tiger.rm.api.jooq.tables.records.SdpRmJobsRecord;
import com.azure.csu.tiger.rm.api.request.CreateVmsRequest;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DnsUtil {

    @Autowired
    private JobDao jobDao;

    public String getPrivateDnsZone(String clusterName) {
        SdpRmJobsRecord job = jobDao.findLatestJobByName(ConstantUtil.getClusterDeploymentName(clusterName));
        JsonObject rawRequest = JsonParser.parseString(job.getJobargs()).getAsJsonObject().get("RawRequest").getAsJsonObject();
        CreateVmsRequest request = JsonUtil.string2Obj(rawRequest.toString(), CreateVmsRequest.class);
        String dnsName = request.getVirtualMachineGroups().get(0).getVirtualMachineSpec().getBaseProfile().getHostNameSuffix();
        return dnsName;
    }
}
