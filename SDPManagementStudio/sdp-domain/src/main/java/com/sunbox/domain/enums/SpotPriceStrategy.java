package com.sunbox.domain.enums;

import java.util.Objects;

public enum SpotPriceStrategy {
    MARKET(1), // 按市场价的比例
    QUOTE(2),  // 直接出价
    ;
    private int id;

    SpotPriceStrategy(int id) {
        this.id = id;
    }

    public static boolean validate(Integer priceStrategy) {
        SpotPriceStrategy[] values = SpotPriceStrategy.values();
        for (SpotPriceStrategy value : values) {
            if (Objects.equals(value.getId(), priceStrategy)) {
                return true;
            }
        }
        return false;
    }

    public boolean equalValue(Integer priceStrategy) {
        return Objects.equals(id, priceStrategy);
    }

    public int getId() {
        return id;
    }
}