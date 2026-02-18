package com.azure.csu.tiger.rm.api.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@ApiModel
@Data
@NoArgsConstructor
public class SpotPriceHistoryResponse {

    private String vmSkuName;

    private List<SpotPriceItemVo> priceItems;
}
