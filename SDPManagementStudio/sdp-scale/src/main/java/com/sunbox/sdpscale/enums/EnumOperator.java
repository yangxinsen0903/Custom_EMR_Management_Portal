package com.sunbox.sdpscale.enums;

import com.sunbox.sdpscale.operators.*;

import java.math.BigDecimal;
import java.util.Objects;

public enum EnumOperator {
    gt(">", "大于", new Gt()),
    lt("<", "小于", new It()),
    gte(">=", "大于等于", new Gte()),
    lte("<=", "小于等于", new Ite());

    private String operator;
    private String text;
    private IOperator compare;

    EnumOperator(String operator, String text, IOperator compare) {
        this.operator = operator;
        this.text = text;
        this.compare = compare;
    }

    public Boolean compare(BigDecimal left, BigDecimal right) {
        return compare.compareTo(left, right);
    }

    public static EnumOperator valueOfOperator(String oper) {
        for (EnumOperator value : values()) {
            if (Objects.equals(oper, value.operator)) {
                return value;
            }
        }
        throw new RuntimeException("指标计算符号不存在," + oper);
    }
}
