package com.sunbox.sdpadmin.model.admin.response;

import lombok.Data;

import java.util.List;

@Data
public class SpotPriceHistory {

    private String skuName;

    private List<PriceItem> priceItems;
}
