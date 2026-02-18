package com.azure.csu.tiger.rm.api.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@ApiModel
@Data
@NoArgsConstructor
public class SpotPriceItemVo {

    private Date effectiveDate;

    private BigDecimal unitPricePerHourUSD;


}
