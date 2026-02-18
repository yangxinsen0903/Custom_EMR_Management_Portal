package com.azure.csu.tiger.rm.api.controller;

import com.azure.csu.tiger.rm.api.exception.RmException;
import com.azure.csu.tiger.rm.api.request.SpotInstanceRequest;
import com.azure.csu.tiger.rm.api.response.SpotEvictionRateReponse;
import com.azure.csu.tiger.rm.api.response.SpotPriceHistoryResponse;
import com.azure.csu.tiger.rm.api.response.SpotPriceResponse;
import com.azure.csu.tiger.rm.api.service.PriceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Api(tags="Spot Price rest api")
@RequestMapping("/api/v1/price")
@RestController
public class PriceController {

    private static final Logger logger = LoggerFactory.getLogger(PriceController.class);

    @Autowired
    private PriceService priceService;

    @ApiOperation(value = "查询Spot价格")
    @PostMapping(path = "/spotInstance", produces = {"application/json"})
    public ResponseEntity<List<SpotPriceResponse>> getSpotInstance(@RequestBody SpotInstanceRequest request) {
        if (request.getRegion() == null || CollectionUtils.isEmpty(request.getVmSkuNames())) {
            throw new RmException(HttpStatus.BAD_REQUEST, "region and vmSkuNames are required");
        }
        List<SpotPriceResponse> spotPrice = priceService.getSpotPrice(request.getRegion(), request.getVmSkuNames());
        return ResponseEntity.ok(spotPrice);
    }

    @ApiOperation(value = "查询Spot驱逐率")
    @PostMapping(path = "/spotEvictionRate", produces = {"application/json"})
    public ResponseEntity<List<SpotEvictionRateReponse>> getSpotEvictionRate(@RequestBody SpotInstanceRequest request) {
        if (request.getRegion() == null || CollectionUtils.isEmpty(request.getVmSkuNames())) {
            throw new RmException(HttpStatus.BAD_REQUEST, "region and vmSkuNames are required");
        }
        List<SpotEvictionRateReponse> spotEvictionRate = priceService.getSpotEvictionRate(request.getRegion(), request.getVmSkuNames());
        return ResponseEntity.ok(spotEvictionRate);
    }

    @ApiOperation(value = "查询Spot价格历史")
    @PostMapping(path = "/spotPriceHistory", produces = {"application/json"})
    public ResponseEntity<List<SpotPriceHistoryResponse>> getSpotPriceHistory(@RequestBody SpotInstanceRequest request) {
        if (request.getRegion() == null || CollectionUtils.isEmpty(request.getVmSkuNames())) {
            throw new RmException(HttpStatus.BAD_REQUEST, "region and vmSkuNames are required");
        }
        List<SpotPriceHistoryResponse> response = priceService.getSpotPriceHistory(request.getRegion(), request.getVmSkuNames());
        return ResponseEntity.ok(response);
    }
}
