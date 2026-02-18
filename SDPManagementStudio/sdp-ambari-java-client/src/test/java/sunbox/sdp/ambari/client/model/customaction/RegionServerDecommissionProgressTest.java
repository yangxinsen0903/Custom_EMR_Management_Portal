package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author: wangda
 * @date: 2023/2/26
 */
public class RegionServerDecommissionProgressTest {

    @Test
    public void getRequestResult() {
        RegionServerDecommissionProgress progress = buildProgress();
        List<Map<String, ?>> requestResult = progress.getRequestResult();
        System.out.println(requestResult.size());
    }

    @Test
    public void isProcessCompleted() {
        RegionServerDecommissionProgress progress = buildProgress();
        boolean processCompleted = progress.isProcessCompleted();
        Assert.assertTrue(processCompleted);
    }

    @Test
    public void isDecommissionSuccess() {
        RegionServerDecommissionProgress progress = buildProgress();
        boolean processCompleted = progress.isDecommissionSuccess();
        Assert.assertTrue(processCompleted);
    }

    private RegionServerDecommissionProgress buildProgress() {
        String response = "{\n" +
                "  \"href\": \"http://20.172.10.47:8765/api/v1/clusters/sdpU0QTE36BxTk/request_schedules/32\",\n" +
                "  \"requestSchedule\": {\n" +
                "    \"authenticated_user\": 1.0,\n" +
                "    \"batch\": {\n" +
                "      \"batch_requests\": [\n" +
                "        {\n" +
                "          \"order_id\": 0.0,\n" +
                "          \"request_type\": \"POST\",\n" +
                "          \"request_uri\": \"/clusters/sdpU0QTE36BxTk/requests\",\n" +
                "          \"request_body\": \"{\\\"Requests/resource_filters\\\":[{\\\"service_name\\\":\\\"HBASE\\\",\\\"component_name\\\":\\\"HBASE_MASTER\\\"}],\\\"RequestInfo\\\":{\\\"context\\\":\\\"Decommission RegionServer - Turn drain mode on \\\",\\\"exclusive\\\":\\\"true\\\",\\\"operation_level\\\":{\\\"cluster_name\\\":\\\"sdpU0QTE36BxTk\\\",\\\"level\\\":\\\"CLUSTER\\\"},\\\"parameters\\\":{\\\"excluded_hosts\\\":\\\"sdp-u0qte36bxtk-cor-0002.dev.sdp.com,sdp-u0qte36bxtk-cor-0003.dev.sdp.com\\\",\\\"slave_type\\\":\\\"HBASE_REGIONSERVER\\\"},\\\"command\\\":\\\"DECOMMISSION\\\"}}\",\n" +
                "          \"request_status\": \"FAILED\",\n" +
                "          \"return_code\": 500.0,\n" +
                "          \"response_message\": \"An internal system exception occurred: Component HBASE_REGIONSERVER on host sdp-u0qte36bxtk-cor-0002.dev.sdp.com cannot be decommissioned as its not in STARTED state. Aborting the whole request.\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"order_id\": 1.0,\n" +
                "          \"request_type\": \"PUT\",\n" +
                "          \"request_uri\": \"/clusters/sdpU0QTE36BxTk/hosts/sdp-u0qte36bxtk-cor-0002.dev.sdp.com/host_components/HBASE_REGIONSERVER\",\n" +
                "          \"request_body\": \"{\\\"RequestInfo\\\":{\\\"context\\\":\\\"Decommission RegionServer - Stop RegionServer: sdp-u0qte36bxtk-cor-0002.dev.sdp.com\\\",\\\"exclusive\\\":true,\\\"operation_level\\\":{\\\"cluster_name\\\":\\\"sdpU0QTE36BxTk\\\",\\\"level\\\":\\\"HOST_COMPONENT\\\",\\\"service_name\\\":\\\"HBASE\\\",\\\"host_name\\\":\\\"sdp-u0qte36bxtk-cor-0002.dev.sdp.com\\\"}},\\\"Body\\\":{\\\"HostRoles\\\":{\\\"state\\\":\\\"INSTALLED\\\"}}}\",\n" +
                "          \"request_status\": \"COMPLETED\",\n" +
                "          \"return_code\": 200.0\n" +
                "        },\n" +
                "        {\n" +
                "          \"order_id\": 2.0,\n" +
                "          \"request_type\": \"PUT\",\n" +
                "          \"request_uri\": \"/clusters/sdpU0QTE36BxTk/hosts/sdp-u0qte36bxtk-cor-0003.dev.sdp.com/host_components/HBASE_REGIONSERVER\",\n" +
                "          \"request_body\": \"{\\\"RequestInfo\\\":{\\\"context\\\":\\\"Decommission RegionServer - Stop RegionServer: sdp-u0qte36bxtk-cor-0003.dev.sdp.com\\\",\\\"exclusive\\\":true,\\\"operation_level\\\":{\\\"cluster_name\\\":\\\"sdpU0QTE36BxTk\\\",\\\"level\\\":\\\"HOST_COMPONENT\\\",\\\"service_name\\\":\\\"HBASE\\\",\\\"host_name\\\":\\\"sdp-u0qte36bxtk-cor-0003.dev.sdp.com\\\"}},\\\"Body\\\":{\\\"HostRoles\\\":{\\\"state\\\":\\\"INSTALLED\\\"}}}\",\n" +
                "          \"request_status\": \"COMPLETED\",\n" +
                "          \"return_code\": 200.0\n" +
                "        },\n" +
                "        {\n" +
                "          \"order_id\": 3.0,\n" +
                "          \"request_type\": \"POST\",\n" +
                "          \"request_uri\": \"/clusters/sdpU0QTE36BxTk/requests\",\n" +
                "          \"request_body\": \"{\\\"Requests/resource_filters\\\":[{\\\"service_name\\\":\\\"HBASE\\\",\\\"component_name\\\":\\\"HBASE_MASTER\\\"}],\\\"RequestInfo\\\":{\\\"service_name\\\":\\\"HBASE\\\",\\\"component_name\\\":\\\"HBASE_MASTER\\\",\\\"context\\\":\\\"Decommission RegionServer - Turn drain mode off \\\",\\\"operation_level\\\":{\\\"cluster_name\\\":\\\"sdpU0QTE36BxTk\\\",\\\"level\\\":\\\"CLUSTER\\\"},\\\"parameters\\\":{\\\"excluded_hosts\\\":\\\"sdp-u0qte36bxtk-cor-0002.dev.sdp.com,sdp-u0qte36bxtk-cor-0003.dev.sdp.com\\\",\\\"mark_draining_only\\\":true,\\\"slave_type\\\":\\\"HBASE_REGIONSERVER\\\"},\\\"command\\\":\\\"DECOMMISSION\\\"}}\",\n" +
                "          \"request_status\": \"COMPLETED\",\n" +
                "          \"return_code\": 202.0\n" +
                "        }\n" +
                "      ],\n" +
                "      \"batch_settings\": {\n" +
                "        \"batch_separation_in_seconds\": 1.0,\n" +
                "        \"task_failure_tolerance_limit\": 0.0\n" +
                "      }\n" +
                "    },\n" +
                "    \"cluster_name\": \"sdpU0QTE36BxTk\",\n" +
                "    \"create_time\": \"2023-02-26 19:09:31\",\n" +
                "    \"create_user\": \"admin\",\n" +
                "    \"id\": 32.0,\n" +
                "    \"last_execution_status\": \"COMPLETED\",\n" +
                "    \"status\": \"COMPLETED\",\n" +
                "    \"update_time\": \"2023-02-26 19:09:54\",\n" +
                "    \"update_user\": \"admin\"\n" +
                "  }\n" +
                "}";
        Gson gson = new Gson();
        RegionServerDecommissionProgress progress = gson.fromJson(response, RegionServerDecommissionProgress.class);
        return progress;
    }
}