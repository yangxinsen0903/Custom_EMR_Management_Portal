package com.sunbox.util;

/**
 * 计算距离
 *
 * @author fanxnw
 * @date 2020年7月24日
 */
public class CalculationDistance {

    /**
     * 平均半径,单位：m；不是赤道半径。赤道为6378左右
     */
    private static final double EARTH_RADIUS = 6371393;

    public static double getDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        // 经纬度（角度）转弧度。弧度用作参数，以调用Math.cos和Math.sin
        // A经弧度
        double radiansAX = Math.toRadians(lng1);
        // A纬弧度
        double radiansAY = Math.toRadians(lat1);
        // B经弧度
        double radiansBX = Math.toRadians(lng2);
        // B纬弧度
        double radiansBY = Math.toRadians(lat2);

        // 公式中“cosβ1cosβ2cos（α1-α2）+sinβ1sinβ2”的部分，得到∠AOB的cos值
        double cos = Math.cos(radiansAY) * Math.cos(radiansBY) * Math.cos(radiansAX - radiansBX)
                + Math.sin(radiansAY) * Math.sin(radiansBY);
        // 反余弦值
        double acos = Math.acos(cos);
        // 最终结果 米
        return EARTH_RADIUS * acos;
    }

}
