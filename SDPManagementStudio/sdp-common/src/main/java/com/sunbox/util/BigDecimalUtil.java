package com.sunbox.util;

import java.math.BigDecimal;

public class BigDecimalUtil {

    /**
     * 提供精确加法计算的add方法
     * @param value1 被加数
     * @param value2 加数
     * @return 两个参数的和
     */
    public static double add(double value1,double value2){
        BigDecimal b1 = new BigDecimal(Double.valueOf(value1));
        BigDecimal b2 = new BigDecimal(Double.valueOf(value2));
        return b1.add(b2).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 提供精确乘法运算的mul方法
     * @param value1 被乘数
     * @param value2 乘数
     * @return 两个参数的积
     */
     public static double mul(double value1,double value2){
         BigDecimal b1 = new BigDecimal(Double.valueOf(value1));
         BigDecimal b2 = new BigDecimal(Double.valueOf(value2));
         return b1.multiply(b2).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
     }

}
