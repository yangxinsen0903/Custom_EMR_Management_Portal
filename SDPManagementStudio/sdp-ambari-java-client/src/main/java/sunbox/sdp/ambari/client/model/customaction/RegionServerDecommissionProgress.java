package sunbox.sdp.ambari.client.model.customaction;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author: wangda
 * @date: 2023/2/26
 */
public class RegionServerDecommissionProgress {
    @SerializedName("href")
    private String href;

    @SerializedName("requestSchedule")
    private Map requestSchedule;

    public boolean isProcessCompleted() {
        List<Map<String, ?>> requestResult = getRequestResult();
        boolean isAllCompleted = true;
        for (Map<String, ?> map : requestResult) {
            String requestStatus = (String)map.get("request_status");
            if (StrUtil.isEmpty(requestStatus) || Objects.equals(requestStatus, "IN_PROGRESS")) {
                isAllCompleted = false;
                break;
            }
        }
        return isAllCompleted;
    }

    public boolean isDecommissionSuccess() {
        boolean isProcessCompleted = isProcessCompleted();
        if (!isProcessCompleted) {
            // 没完成,返回不成功
            return false;
        }

        List<Map<String, ?>> requestResult = getRequestResult();
        boolean isAllSuccess = true;
        for (Map<String, ?> map : requestResult) {
            String requestStatus = (String)map.get("request_status");
            if (Objects.equals(requestStatus, "COMPLETED")) {
                continue;
            }
            if (Objects.equals(requestStatus, "FAILED")) {
                String responseMessage = (String)map.get("response_message");
                if (StrUtil.isEmpty(responseMessage) ||
                responseMessage.indexOf("not in STARTED state") == -1) {
                    isAllSuccess = false;
                }
            }
        }
        return isAllSuccess;
    }

    List<Map<String, ?>> getRequestResult() {
        List<Map<String, ?>> result=Lists.newArrayList();
        // 第一层: batch -> Object
        if (Objects.isNull(requestSchedule)) {
            return result;
        }
        Map batchMap = (Map) requestSchedule.get("batch");

        // 第二层: batch_requests -> List
        if (Objects.isNull(batchMap)) {
            return result;
        }
        result = (List<Map<String, ?>>)batchMap.get("batch_requests");
        return  result;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public Map getRequestSchedule() {
        return requestSchedule;
    }

    public void setRequestSchedule(Map requestSchedule) {
        this.requestSchedule = requestSchedule;
    }
}
