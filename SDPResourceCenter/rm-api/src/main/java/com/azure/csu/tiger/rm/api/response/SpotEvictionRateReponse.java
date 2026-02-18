package com.azure.csu.tiger.rm.api.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@ApiModel
@Data
@NoArgsConstructor
public class SpotEvictionRateReponse {

    private String vmSkuName;

    private Integer evictionRateLowerPercentage;

    private Integer evictionRateUpperPercentage;
}
