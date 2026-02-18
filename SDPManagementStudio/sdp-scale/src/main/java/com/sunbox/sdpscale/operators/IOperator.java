package com.sunbox.sdpscale.operators;

import java.math.BigDecimal;

public interface IOperator {
    Boolean compareTo(BigDecimal left, BigDecimal right);
}
