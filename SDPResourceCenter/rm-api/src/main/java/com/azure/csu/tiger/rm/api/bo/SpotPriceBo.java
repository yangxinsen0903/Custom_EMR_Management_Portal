package com.azure.csu.tiger.rm.api.bo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class SpotPriceBo {

    private String armSkuName;

    private String skuName;

    private BigDecimal unitPrice;
}
