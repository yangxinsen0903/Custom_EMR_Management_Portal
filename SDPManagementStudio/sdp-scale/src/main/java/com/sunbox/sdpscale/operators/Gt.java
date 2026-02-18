package com.sunbox.sdpscale.operators;

import java.math.BigDecimal;

public class Gt implements IOperator {
    @Override
    public Boolean compareTo(BigDecimal left, BigDecimal right) {
        if (left == null) {
            left = BigDecimal.ZERO;
        }
        if (right == null) {
            right = BigDecimal.ZERO;
        }
        return left.compareTo(right) > 0;
    }
}
