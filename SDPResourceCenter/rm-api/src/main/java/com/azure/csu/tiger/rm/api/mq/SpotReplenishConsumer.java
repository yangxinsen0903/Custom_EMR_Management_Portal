package com.azure.csu.tiger.rm.api.mq;

import com.azure.core.credential.TokenCredential;
import com.azure.csu.tiger.rm.api.dao.JobDao;
import com.azure.csu.tiger.rm.api.helper.AzureResourceGraphHelper;
import com.azure.csu.tiger.rm.api.helper.AzureResourceHelper;
import com.azure.csu.tiger.rm.api.jooq.tables.records.SdpRmJobsRecord;
import com.azure.csu.tiger.rm.api.response.GetVmInfoVo;
import com.azure.csu.tiger.rm.api.utils.ArmUtil;
import com.azure.csu.tiger.rm.api.utils.ConstantUtil;
import com.azure.csu.tiger.rm.api.utils.JsonUtil;
import com.azure.messaging.servicebus.*;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class SpotReplenishConsumer {

    private static final Logger logger = LoggerFactory.getLogger(SpotReplenishConsumer.class);

    @Autowired
    private TokenCredential tokenCredential;
    @Value("${azure.service-bus.replenish-spot.namespace}")
    private String namespace;
    @Value("${azure.service-bus.replenish-spot.event-to-rm-topic}")
    private String fromTopic;
    @Value("${azure.service-bus.replenish-spot.event-to-rm-subscription}")
    private String fromSubscription;

    @Value("${azure.service-bus.replenish-spot.rm-to-sdp-topic}")
    private String toTopic;

    private ServiceBusProcessorClient processorClient;

    private ServiceBusSenderClient senderClient;

    @Autowired
    private JobDao jobDao;
    @Autowired
    private AzureResourceGraphHelper azureResourceGraphHelper;
    @Autowired
    private AzureResourceHelper azureResourceHelper;
    @Autowired
    private ArmUtil armUtil;

    @PostConstruct
    public void init() {
        logger.info("Start to consume spot replenish message");

        String fullyQualifiedNamespace = String.format("%s.servicebus.windows.net", namespace);
        processorClient = new ServiceBusClientBuilder()
                .credential(tokenCredential)
                .fullyQualifiedNamespace(fullyQualifiedNamespace)
                .processor()
//                .disableAutoComplete()
                .topicName(fromTopic)
                .subscriptionName(fromSubscription)
                .processMessage(context -> asyncProcessMessage(context))
                .processError(context -> processError(context))
                .maxConcurrentCalls(5)
                .buildProcessorClient();
        processorClient.start();

        senderClient = new ServiceBusClientBuilder()
                .credential(tokenCredential)
                .fullyQualifiedNamespace(fullyQualifiedNamespace)
                .sender()
                .topicName(toTopic)
                .buildClient();
    }

//    private void asyncProcessMessage(ServiceBusReceivedMessageContext context) {
//        ServiceBusReceivedMessage message = context.getMessage();
//        try {
//            logger.info("Received message: {}", message.getMessageId());
//            // 模拟消息处理时间
//            Thread.sleep(20000); // 处理消息的模拟延迟
//
//            // 处理消息
//            // ...
//            // 不需要显式调用 context.complete()，因为 autoComplete 默认开启
//        } catch (Exception e) {
//            logger.error("Error processing message: {}", e.getMessage());
//            // 如果处理失败，抛出异常，消息不会被自动标记为 complete
//        }
//    }

    private void asyncProcessMessage(ServiceBusReceivedMessageContext context) {
        ServiceBusReceivedMessage message = context.getMessage();
        logger.info("Received message: {}, body: {}", message.getMessageId(), message.getBody());
        JsonObject asJsonObject = JsonParser.parseString(message.getBody().toString()).getAsJsonObject();
        String cluster = asJsonObject.getAsJsonObject("data").getAsJsonObject("resourceInfo").getAsJsonObject("tags").get("SYS_SDP_CLUSTER").getAsString();
        String group = asJsonObject.getAsJsonObject("data").getAsJsonObject("resourceInfo").getAsJsonObject("tags").get("SYS_SDP_GROUP").getAsString();
        String sysCreateBatch = asJsonObject.getAsJsonObject("data").getAsJsonObject("resourceInfo").getAsJsonObject("tags").get("SYS_CREATE_BATCH").getAsString();
        boolean isClusterDeletedOrDeleting = jobDao.isClusterDeletedOrDeleting(cluster);
        if (isClusterDeletedOrDeleting) {
            logger.info("Cluster {} is deleted or deleting, so this message will be ignored.", cluster);
            return;
        }
        List<SdpRmJobsRecord> inProgressJobs = jobDao.findCreateOrUpdateInProgressJob(cluster);
        boolean anyMatch = false;
        for (SdpRmJobsRecord job : inProgressJobs) {
            JsonObject jobArgs = JsonParser.parseString(job.getJobargs()).getAsJsonObject();
            JsonArray jsonElements = jobArgs.getAsJsonObject("RawRequest").getAsJsonArray("virtualMachineGroups");
            String clusterGroupBacth = jobArgs.get("SysCreateBatch").getAsString();
            for (JsonElement e : jsonElements.asList()) {
                if (e.getAsJsonObject().get("groupName").getAsString().equals(group)) {
                    if (Objects.equals(sysCreateBatch, clusterGroupBacth)) {
                        anyMatch = true;
                        break;
                    }
                }
            }
        }
        // 先判断当前有没有创建集群或者扩容集群的任务在进行中，并且事件中vm的SYS_CREATE_BATCH与创建、扩容任务中设置的SysCreateBatch一致
        // 如果一致，表明下次在查询job的时候，这个vm的信息会一并返回，此处不做处理
        // 如果不一致，则继续处理这个事件，获取vm的信息，发送消息给SDP
        if (anyMatch) {
            logger.info("There are in progress jobs for cluster: {}, group: {} and SYS CREATE BATCH is match. So this message will be ignored.", cluster, group);
            return;
        }
        logger.info("begin to get info of this vm");
        String resourceId = asJsonObject.getAsJsonObject("data").getAsJsonObject("resourceInfo").get("id").getAsString();
        String query = azureResourceGraphHelper.getCompleteQueryByIds(Lists.newArrayList(resourceId));
        List<GetVmInfoVo> vmInfos = Lists.newArrayList();
        int retryTimes = 0;
        while (retryTimes < 3) {
            retryTimes++;
            try {
                JsonArray queryResult = azureResourceGraphHelper.executeQuery(query, null);
                vmInfos = GetVmInfoVo.from(queryResult);
                if (!CollectionUtils.isEmpty(vmInfos)) {
                    break;
                }
                Thread.sleep(10000 * retryTimes);
            }catch (Exception e){
                logger.error("");
            }
        }
        if (CollectionUtils.isEmpty(vmInfos)) {
            logger.error("Can not get vm info for resourceId: {}", resourceId);
            return;
        }
        String subscriptionId = resourceId.split("/")[2];
        ArmUtil.setArmData(armUtil.getAzureResourceManager(subscriptionId));
        GetVmInfoVo vm = vmInfos.get(0);
        azureResourceHelper.registerVirtualMachinesToPrivateDnsWithRetry(vmInfos, vm.getTags().get(ConstantUtil.SYS_SDP_DNS));

        sendMessage(vm);
    }

    private void processError(ServiceBusErrorContext context) {
        logger.error("Error occurred: " + context.getException());

    }

    private void sendMessage(GetVmInfoVo message) {
        logger.info("Send message to SDP: {}", message);
        senderClient.sendMessage(new ServiceBusMessage(JsonUtil.obj2String(message)));
    }

    @PreDestroy
    public void close() {
        processorClient.close();
        senderClient.close();
    }


}
