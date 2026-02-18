package com.azure.csu.tiger.rm.api.helper;

import com.azure.csu.tiger.rm.api.response.GetVmInfoVo;
import com.azure.resourcemanager.resourcegraph.ResourceGraphManager;
import com.azure.resourcemanager.resourcegraph.models.QueryRequest;
import com.azure.resourcemanager.resourcegraph.models.QueryRequestOptions;
import com.azure.resourcemanager.resourcegraph.models.QueryResponse;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Data
public class AzureResourceGraphHelper {

    private static final Logger logger = LoggerFactory.getLogger(AzureResourceGraphHelper.class);

    @Autowired
    private ResourceGraphManager resourceGraphManager;

    private String vmQueryByFleet;

    private String vmQueryBySubNet;

    private String vmQueryByIds;

    @PostConstruct
    private void init() {
        try {
            vmQueryByFleet = getArgQuery("arg_query_vm_by_fleet");
            vmQueryBySubNet = getArgQuery("arg_query_vm_by_subnet");
            vmQueryByIds = getArgQuery("arg_query_vm_by_id");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getArgQuery(String queryName) throws IOException {
        InputStream resourceAsStream = ClassLoader.getSystemResourceAsStream(String.format("arg-query/%s.txt", queryName));
        String value = new String(resourceAsStream.readAllBytes(), StandardCharsets.UTF_8);
//        ClassPathResource resource = new ClassPathResource(String.format("arg-query/%s.txt", queryName));
//        String value = FileUtils.readFileToString(resource.getFile(), "UTF-8");
        return value;
    }

    public String getCompleteQueryByFleet(List<String> fleetNames, String sysCreateBatch) {
        String queryFormat = this.getVmQueryByFleet();
        String containOption = fleetNames.stream().map(i -> String.format("\"%s\"", i)).collect(Collectors.joining(","));
        String query = String.format(queryFormat, containOption, sysCreateBatch);
        return query;
    }

    public String getCompleteQueryByIds(List<String> ids) {
        String queryFormat = this.getVmQueryByIds();
        String containOption = ids.stream().map(i -> String.format("\"%s\"", i)).collect(Collectors.joining(","));
        String query = String.format(queryFormat, containOption);
        return query;
    }

    public JsonArray executeQuery(String query, String subscriptionId) {
        logger.info("Execute query: {}", query);
        QueryRequest queryRequest = new QueryRequest()
                .withQuery(query)
                .withOptions(new QueryRequestOptions().withTop(100));
        if (subscriptionId != null) {
            queryRequest.withSubscriptions(Collections.singletonList(subscriptionId));
        }
        Gson gson = new Gson();
        String skipToken = null;
        JsonArray result = new JsonArray();
        do {
            if (skipToken != null) {
                queryRequest.withOptions(new QueryRequestOptions().withSkipToken(skipToken).withTop(100));
            }
            QueryResponse response = resourceGraphManager.resourceProviders().resources(queryRequest);
            logger.info("Query response: {}", gson.toJson(response.data()));
            JsonArray datas = JsonParser.parseString(gson.toJson(response.data())).getAsJsonArray();
            result.addAll(datas);
            skipToken = response.skipToken();
            logger.info("Skip token: {}", skipToken);
        } while (skipToken != null);
        return result;
    }

}
