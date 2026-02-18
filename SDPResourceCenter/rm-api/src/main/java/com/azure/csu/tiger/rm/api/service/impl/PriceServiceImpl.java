package com.azure.csu.tiger.rm.api.service.impl;

import com.azure.csu.tiger.rm.api.bo.SpotPriceBo;
import com.azure.csu.tiger.rm.api.exception.RmException;
import com.azure.csu.tiger.rm.api.helper.AzureResourceGraphHelper;
import com.azure.csu.tiger.rm.api.response.SpotEvictionRateReponse;
import com.azure.csu.tiger.rm.api.response.SpotPriceHistoryResponse;
import com.azure.csu.tiger.rm.api.response.SpotPriceItemVo;
import com.azure.csu.tiger.rm.api.response.SpotPriceResponse;
import com.azure.csu.tiger.rm.api.service.PriceService;
import com.azure.csu.tiger.rm.api.utils.HttpUtil;
import com.azure.csu.tiger.rm.api.utils.JsonUtil;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PriceServiceImpl implements PriceService {

    private static final Logger logger = LoggerFactory.getLogger(PriceServiceImpl.class);

    @Autowired
    private HttpUtil httpUtil;
    @Autowired
    private AzureResourceGraphHelper azureResourceGraphHelper;

    @Override
    public List<SpotPriceResponse> getSpotPrice(String region, List<String> skuNames) {
        String response = httpUtil.doGetSpotPrice(region, skuNames);
        JsonObject object = JsonParser.parseString(response).getAsJsonObject();
        if (object.has("Error")) {
            logger.error("Failed to get spot price, error: {}", response);
            throw new RmException(HttpStatus.BAD_REQUEST, response);
        }
        List<SpotPriceBo> items = JsonUtil.string2Obj(object.getAsJsonArray("Items").toString(), List.class, SpotPriceBo.class);
        Map<String, List<SpotPriceBo>> skuMap = items.stream().collect(Collectors.groupingBy(SpotPriceBo::getArmSkuName));
        return skuMap.entrySet().stream().map(entry -> {
            SpotPriceResponse vo = new SpotPriceResponse();
            vo.setVmSkuName(entry.getKey());
            entry.getValue().stream().forEach(item -> {
                String skuName = item.getSkuName();
                if (skuName.toLowerCase().endsWith("spot")) {
                    vo.setSpotUnitPricePerHourUSD(item.getUnitPrice());
                } else if (!skuName.toLowerCase().contains("low priority") && !skuName.toLowerCase().contains("spot")) {
                    vo.setOnDemandUnitPricePerHourUSD(item.getUnitPrice());
                }
            });
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SpotEvictionRateReponse> getSpotEvictionRate(String region, List<String> vmSkuList) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SpotResources ");
        queryBuilder.append("| where type =~ 'microsoft.compute/skuspotevictionrate/location' ");
        queryBuilder.append(String.format("| where sku.name in~ (%s) ", vmSkuList.stream().map(s -> String.format("'%s'", s)).collect(Collectors.joining(","))));
        queryBuilder.append(String.format("| where location =~ '%s' ", region));
        queryBuilder.append("| project skuName = tolower(sku.name), evictionRate = properties.evictionRate");
        JsonArray jsonArray = azureResourceGraphHelper.executeQuery(queryBuilder.toString(), null);
        Map<String, JsonElement> skuMap = jsonArray.asList().stream().collect(Collectors.toMap(
                item -> item.getAsJsonObject().get("skuName").getAsString(),
                item -> item));
        return vmSkuList.stream().map(i -> {
            String item = i.toLowerCase();
            SpotEvictionRateReponse vo = new SpotEvictionRateReponse();
            if(!skuMap.containsKey(item)) {
                vo.setVmSkuName(i);
                return vo;
            }
            String evictionRate = skuMap.get(item).getAsJsonObject().get("evictionRate").getAsString();
            vo.setVmSkuName(i);
            if (evictionRate != null) {
                if (evictionRate.contains("-")) {
                    String[] rates = evictionRate.split("-");
                    vo.setEvictionRateLowerPercentage(Integer.parseInt(rates[0]));
                    vo.setEvictionRateUpperPercentage(Integer.parseInt(rates[1]));
                } else if (evictionRate.endsWith("+")) {
                    vo.setEvictionRateLowerPercentage(Integer.parseInt(evictionRate.substring(0, evictionRate.length() - 1)));
                    vo.setEvictionRateUpperPercentage(null);
                } else {
                    vo.setEvictionRateLowerPercentage(null);
                    vo.setEvictionRateUpperPercentage(null);
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SpotPriceHistoryResponse> getSpotPriceHistory(String region, List<String> vmSkuList) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SpotResources ");
        queryBuilder.append("| where type =~ 'microsoft.compute/skuspotpricehistory/ostype/location' ");
        queryBuilder.append(String.format("| where sku.name in~ (%s) ", vmSkuList.stream().map(s -> String.format("'%s'", s)).collect(Collectors.joining(","))));
        queryBuilder.append(String.format("| where location =~ '%s' ", region));
        queryBuilder.append("| where properties.osType =~ 'linux' ");
        queryBuilder.append("| project skuName = tolower(sku.name), spotPrices = properties.spotPrices");
        JsonArray jsonArray = azureResourceGraphHelper.executeQuery(queryBuilder.toString(), null);
        Map<String, JsonElement> skuMap = jsonArray.asList().stream().collect(Collectors.toMap(
                item -> item.getAsJsonObject().get("skuName").getAsString(),
                item -> item));
        return vmSkuList.stream().map(i -> {
            String item = i.toLowerCase();
            SpotPriceHistoryResponse vo = new SpotPriceHistoryResponse();
            if (!skuMap.containsKey(item)) {
                vo.setVmSkuName(i);
                vo.setPriceItems(Lists.newArrayList());
                return vo;
            }
            JsonArray spotPrices = skuMap.get(item).getAsJsonObject().get("spotPrices").getAsJsonArray();
            vo.setVmSkuName(i);
            vo.setPriceItems(spotPrices.asList().stream().map(price -> {
                SpotPriceItemVo priceItem = new SpotPriceItemVo();
                priceItem.setEffectiveDate(Date.from(Instant.parse(price.getAsJsonObject().get("effectiveDate").getAsString())));
                priceItem.setUnitPricePerHourUSD(price.getAsJsonObject().get("priceUSD").getAsBigDecimal());
                return priceItem;
            }).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
    }
}
