package com.azure.csu.tiger.rm.api.service;

import com.azure.csu.tiger.rm.api.response.SpotEvictionRateReponse;
import com.azure.csu.tiger.rm.api.response.SpotPriceHistoryResponse;
import com.azure.csu.tiger.rm.api.response.SpotPriceResponse;

import java.io.IOException;
import java.util.List;

public interface PriceService {

    List<SpotPriceResponse> getSpotPrice(String region, List<String> skuNames);

    List<SpotEvictionRateReponse> getSpotEvictionRate(String region, List<String> vmSkuList);

    List<SpotPriceHistoryResponse> getSpotPriceHistory(String region, List<String> vmSkuList);

}
