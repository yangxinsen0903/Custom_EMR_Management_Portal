package com.azure.csu.tiger.rm.api.utils;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.nimbusds.jose.util.Pair;
import lombok.Data;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Data
public class HttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    private HttpClient httpClient;

    private TokenRequestContext context;

    @Autowired
    private TokenCredential tokenCredential;

    private String fleetUrl = "https://management.azure.com/subscriptions/%s/resourceGroups/%s/providers/Microsoft.AzureFleet/fleets/%s?api-version=2023-11-01-preview";

    private String spotPriceHost = "https://prices.azure.com/api/retail/prices?$filter=";
    private String spotPriceFilter = "serviceName eq 'Virtual Machines' and serviceFamily eq 'Compute' and currencyCode eq 'USD' and armRegionName eq '%s' and endswith(tolower(productName), 'windows') eq false and priceType eq 'Consumption' and (%s)";

    @PostConstruct
    private void init() {
        httpClient = HttpClients.createDefault();
        context = new TokenRequestContext().addScopes("https://management.azure.com/.default");
    }

    public Pair<Integer, String> doGetFleet(String resourceGroup, String fleetName) {
        HttpGet request = new HttpGet(String.format(fleetUrl, ArmUtil.getSubData(), resourceGroup, fleetName));
        String token = tokenCredential.getToken(context).block().getToken();

        request.addHeader("Authorization", "Bearer " + token);
        try {
            return httpClient.execute(request,
                    response -> {
                        String responseBody = EntityUtils.toString(response.getEntity());
                        logger.info(responseBody);
                        return Pair.of(Integer.valueOf(response.getCode()), responseBody);
                    });
        } catch (Exception e) {
            logger.error("Failed to get fleet, resource group: {}, fleet name: {}", resourceGroup, fleetName, e);
        }
        return null;
    }

    public int doDeleteFleet(String resourceGroup, String fleetName) {
        HttpDelete request = new HttpDelete(String.format(fleetUrl, ArmUtil.getSubData(), resourceGroup, fleetName));
        String token = tokenCredential.getToken(context).block().getToken();
        request.addHeader("Authorization", "Bearer " + token);
        try {
            httpClient.execute(request, response -> response.getCode());
        } catch (Exception e) {
            logger.error("Failed to delete fleet, resource group: {}, fleet name: {}", resourceGroup, fleetName, e);
        }
        return -1;
    }

    public String doGetSpotPrice(String region, List<String> skuNames) {
        StringBuilder skuNameFilter = new StringBuilder();
        int count = 1;
        for (String skuName : skuNames) {
            if (count == 1) {
                skuNameFilter.append("tolower(armSkuName) eq '").append(skuName.toLowerCase()).append("'");
            } else {
                skuNameFilter.append(" or tolower(armSkuName) eq '").append(skuName.toLowerCase()).append("'");
            }
            count++;
        }
        String encodeQuery = URLEncoder.encode(String.format(spotPriceFilter, region, skuNameFilter), StandardCharsets.UTF_8);
        String url = String.format("%s%s", spotPriceHost, encodeQuery);
        url = url.replace("+", "%20");
        logger.info("Get spot price url: {}", url);
        HttpGet request = new HttpGet(url);
        try {
            return httpClient.execute(request,
                    response -> EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            logger.error("Failed to get spot price, region: {}, sku names: {}", region, skuNames, e);
        }
        return null;
    }

}
