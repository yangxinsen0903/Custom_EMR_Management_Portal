package com.sunbox.sdpcompose.service.ambari;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.sunbox.domain.AppNode;
import com.sunbox.sdpcompose.service.ambari.blueprint.Blueprint;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * @author: wangda
 * @date: 2022/12/6
 */
class BlueprintTest {

    @Test
    void setStackInfo_名称不规范() {
        Blueprint blueprint = new Blueprint();
        try {
            blueprint.setStackInfo("Cluster01", "SDP1234");
            fail("应该抛出异常 ");
        } catch (RuntimeException ex) {
            assertTrue(true);
        }
    }

    @Test
    void setStackInfo_名称规范() {
        AppNode appNode = JSONUtil.toBean("{\"appAttempts\":{\"appAttempt\":[{\"id\":1,\"startTime\":1677526250000,\"finishedTime\":0,\"containerId\":\"container_1677524237263_0006_01_000001\",\"nodeHttpAddress\":\"sdp-elegant-test-sit-cor-0002.sit.sdp.com:8042\",\"nodeId\":\"sdp-elegant-test-sit-cor-0002.sit.sdp.com:45454\",\"logsLink\":\"http://sdp-elegant-test-sit-cor-0002.sit.sdp.com:8042/node/containerlogs/container_1677524237263_0006_01_000001/spark\",\"blacklistedNodes\":\"\",\"nodesBlacklistedBySystem\":\"\",\"appAttemptId\":\"appattempt_1677524237263_0006_000001\",\"exportPorts\":\"null\"}]}}", AppNode.class);
        List<String> nodes = new ArrayList<>();
        if (appNode.getAppAttempts() != null && CollUtil.isNotEmpty(appNode.getAppAttempts().getAppAttempt())) {
            for (AppNode.AppAttempt appAttempt : appNode.getAppAttempts().getAppAttempt()) {
                List<String> split = StrUtil.split(appAttempt.getNodeId(), ":");
                nodes.add(CollUtil.getFirst(split));
            }
        }

        Console.log(nodes);
    }
}