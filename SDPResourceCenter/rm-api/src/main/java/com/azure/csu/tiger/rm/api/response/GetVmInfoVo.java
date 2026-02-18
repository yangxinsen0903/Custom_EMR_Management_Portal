package com.azure.csu.tiger.rm.api.response;

import com.azure.csu.tiger.rm.api.utils.ConstantUtil;
import com.azure.csu.tiger.rm.api.utils.JsonUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiModel
@Data
@ToString
@NoArgsConstructor
public class GetVmInfoVo {

    private String name;

    private String hostName;

    private String dnsRecord;

    private String uniqueId;

    private String privateIp;

    private String zone;

    private Map<String, String> tags;

    private String priority;

    private String vmTimeCreated;

    private String vmState;

    private String vmSize;

    @JsonIgnore
    private String fleetName;

    public static GetVmInfoVo from(JsonObject o) {
        GetVmInfoVo vo= new GetVmInfoVo();
        vo.setName(o.get("vmName").getAsString());
        vo.setDnsRecord(o.get("computerName").getAsString());
        vo.setHostName(o.get("computerName").getAsString());
        if (o.get("vmTags").getAsJsonObject().has(ConstantUtil.SYS_SDP_DNS)) {
            String dns = o.get("vmTags").getAsJsonObject().get(ConstantUtil.SYS_SDP_DNS).getAsString();
            if (!vo.getHostName().endsWith(dns)) {
                vo.setHostName(vo.getHostName() + "." + dns);
            }
            if (vo.getDnsRecord().endsWith(dns)){
                vo.setDnsRecord(vo.getDnsRecord().substring(0, vo.getDnsRecord().lastIndexOf("." + dns)));
            }
        }
        vo.setPrivateIp(o.get("privateIpAddress").getAsString());
        vo.setZone(o.get("vmZones").getAsJsonArray().get(0).getAsString());
        vo.setTags(JsonUtil.string2Obj(o.get("vmTags").getAsJsonObject().toString(), HashMap.class));
        if (o.has("fleetName")) {
            vo.setFleetName(o.get("fleetName").getAsString());
        }
        vo.setUniqueId(o.get("vmId").getAsString());
        vo.setPriority(o.get("vmPriority").getAsString());
        vo.setVmTimeCreated(o.get("vmTimeCreated").getAsString());
        vo.setVmState(o.get("vmState").getAsString());
        vo.setVmSize(o.get("vmSize").getAsString());
        return vo;
    }

    public static List<GetVmInfoVo> from (JsonArray array) {
        List<GetVmInfoVo> vos = Lists.newArrayList();
        for(JsonElement e : array.asList()) {
            vos.add(from(e.getAsJsonObject()));
        }
        return vos;
    }

}
