package com.sunbox.sdpadmin.model.admin.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class PriceItem {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date effectiveDate;

    private Double unitPricePerHourUSD;
}
