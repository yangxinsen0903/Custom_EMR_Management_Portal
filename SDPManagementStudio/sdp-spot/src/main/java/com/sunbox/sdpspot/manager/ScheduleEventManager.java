package com.sunbox.sdpspot.manager;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.InfoClusterVm;
import com.sunbox.sdpspot.constant.RedisConst;
import com.sunbox.sdpspot.data.LiveFailData;
import com.sunbox.sdpspot.mapper.InfoClusterVmMapper;
import com.sunbox.sdpspot.model.ScheduleEvent;
import com.sunbox.sdpspot.model.ScheduleEvents;
import com.sunbox.sdpspot.model.VmEvictionEvent;
import com.sunbox.sdpspot.util.DistributedRedisSpot;
import com.sunbox.web.BaseCommonInterFace;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class ScheduleEventManager implements BaseCommonInterFace {
    @Autowired
    private DistributedRedisSpot spotRedissonClient;

    @Autowired
    private InfoClusterVmMapper infoClusterVmMapper;

    public LiveFailData getLiveFailData(String clusterId, String vmName) {
        String redisLiveFailKey = RedisConst.keyLiveFail(clusterId, vmName);
        try {
            String valueAsString = spotRedissonClient.getValueAsString(redisLiveFailKey);
            if (StringUtils.isEmpty(valueAsString)) {
                return new LiveFailData(clusterId, vmName, 0);
            }
            return JSONUtil.toBean(valueAsString, LiveFailData.class);
        } catch (Exception e) {
            return new LiveFailData(clusterId, vmName, 2);
        }
    }

    public boolean setLiveFailData(LiveFailData liveFailData) {
        try {
            String redisLiveFailKey = RedisConst.keyLiveFail(liveFailData.getClusterId(), liveFailData.getVmName());
            spotRedissonClient.saveString(redisLiveFailKey, JSONUtil.toJsonStr(liveFailData), 60 * 60);
            return true;
        } catch (Exception e) {
            getLogger().error("setLiveFailData error,data:{}", liveFailData, e);
            return false;
        }
    }

    public void deleteLiveFailData(LiveFailData liveFailData) {
        String redisLiveFailKey = RedisConst.keyLiveFail(liveFailData.getClusterId(), liveFailData.getVmName());
        try {
            spotRedissonClient.delete(redisLiveFailKey);
        } catch (Exception ignore) {

        }
    }

    public VmEvictionEvent findEvictionEventFromHttp(InfoClusterVm infoClusterVm, int port, LiveFailData liveFailData) {
        String vmName = infoClusterVm.getVmName();
        String hostName = infoClusterVm.getHostName();
        String internalIp = infoClusterVm.getInternalip();
        String clusterId = infoClusterVm.getClusterId();

        String body = httpGet(internalIp, port, liveFailData);
        if (StringUtils.isEmpty(body)) {
            liveFailData.setFailedCount(liveFailData.getFailedCount() + 1);
            return null;
        }

        try {
            ScheduleEvents scheduleEvents = JSONObject.parseObject(body, ScheduleEvents.class);
            if (scheduleEvents == null) {
                liveFailData.setFailedCount(liveFailData.getFailedCount() + 1);
                return null;
            }

            //判断是否被ip是否被其它集群使用
            InfoClusterVm latestInfoClusterVm = infoClusterVmMapper.findTop1IdByInternalIpOrderByCreatedTimeDesc(internalIp, InfoClusterVm.VM_RUNNING);
            if (latestInfoClusterVm == null) {
                liveFailData.setFailedCount(liveFailData.getFailedCount() + 1);
                return null;
            } else if (!StringUtils.equals(latestInfoClusterVm.getClusterId(), clusterId)
                    || !StringUtils.equals(latestInfoClusterVm.getVmName(), vmName)
                    || !StringUtils.equals(latestInfoClusterVm.getHostName(), hostName)) {
                getLogger().error("虚拟机ip已经被其它实例占用,逐出当前虚拟机,原虚拟机:{},新虚拟机:{}", infoClusterVm, latestInfoClusterVm);
                VmEvictionEvent vmEvictionEvent = new VmEvictionEvent();
                vmEvictionEvent.setVmName(vmName);
                vmEvictionEvent.setHostname(hostName);
                vmEvictionEvent.setEvictTime(new Date());
                vmEvictionEvent.setRemaining(0);
                vmEvictionEvent.setTime(new Date());
                vmEvictionEvent.setDeleted(false);
                return vmEvictionEvent;
            }

            if (scheduleEvents.getEvents() == null) {
                liveFailData.setFailedCount(liveFailData.getFailedCount() + 1);
                return null;
            }

            for (ScheduleEvent event : scheduleEvents.getEvents()) {
                if (StringUtils.equalsIgnoreCase(event.getEventType(), "PREEMPT")) {
                    VmEvictionEvent vmEvictionEvent = new VmEvictionEvent();
                    vmEvictionEvent.setVmName(vmName);
                    vmEvictionEvent.setHostname(hostName);
                    vmEvictionEvent.setEvictTime(event.getNotBefore());
                    vmEvictionEvent.setRemaining((int) ((new Date().getTime() - event.getNotBefore().getTime()) / 1000));
                    vmEvictionEvent.setTime(new Date());
                    vmEvictionEvent.setDeleted(false);
                    return vmEvictionEvent;
                }
            }
        } catch (Exception ignored) {
        }

        VmEvictionEvent vmEvictionEvent = new VmEvictionEvent();
        vmEvictionEvent.setVmName(vmName);
        vmEvictionEvent.setHostname(hostName);
        vmEvictionEvent.setEvictTime(null);
        vmEvictionEvent.setRemaining(null);
        vmEvictionEvent.setTime(new Date());
        vmEvictionEvent.setDeleted(false);
        return vmEvictionEvent;
    }

    private String httpGet(String ip, int port, LiveFailData liveFailData) {
        String url = "http://" + ip + ":" + port + "/events";
        int timeout = 1;
        if (liveFailData != null) {
            timeout = 1 + liveFailData.getFailedCount();
        }

        Request request = null;
        Response response = null;
        OkHttpClient okHttpClient = null;
        try {
            okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(timeout, TimeUnit.SECONDS)
                    .readTimeout(timeout, TimeUnit.SECONDS)
                    .build();
            request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            Call call = okHttpClient.newCall(request);
            response = call.execute();
            if (response.isSuccessful()) {
                getLogger().info("http get test pass, url:{}, code:{},timeout:{}", url, response.code(), timeout);
                return response.body().string();
            }
            if (response.code() == HttpStatus.NOT_FOUND.value()) {
                getLogger().info("http get test pass with NOT_FOUND, url:{}, code:{},timeout:{}", url, response.code(), timeout);
                return null;
            }
            getLogger().warn("http get test not pass, url:{}, code:{},timeout:{}", url, response.code(), timeout);
            return null;
        } catch (Exception e) {
            getLogger().error("http get test error, url:{}, ex:{},timeout:{}", url, e.getMessage(), timeout);
            return null;
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {

                }
            }
        }
    }
}
