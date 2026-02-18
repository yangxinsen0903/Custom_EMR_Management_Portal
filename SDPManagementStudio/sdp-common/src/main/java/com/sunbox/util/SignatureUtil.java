package com.sunbox.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Arrays;
import java.util.Map;


public class SignatureUtil {

    /**
     * 生成 package 字符串
     *
     * @param map
     * @param paternerKey
     * @return
     */
    public static String generatePackage(Map<String, String> map, String paternerKey) {
        String sign = generateSign(map, paternerKey);
        Map<String, String> tmap = MapUtil.order(map);
        String s2 = MapUtil.mapJoin(tmap, false, true);
        return s2 + "&sign=" + sign;
    }

    /**
     * 生成sign MD5 加密 toUpperCase
     *
     * @param map
     * @param paternerKey
     * @return
     */
    public static String generateSign(Map<String, String> map, String paternerKey) {
        //去给map key排序  a  b  c  d  e  f
        Map<String, String> tmap = MapUtil.order(map);
        //删除sgin ，签名不参与算法
         if (tmap.containsKey("sign")) {
            tmap.remove("sign");
        }
       // System.out.println("temp - remove key = sgin："+tmap.toString());
        String str = MapUtil.mapJoin(tmap, false, false);
        //System.out.println("解读Map  参数串连："+str);

        return DigestUtils.md5Hex(str + "&key=" + paternerKey).toUpperCase();
    }
    /**
     * 生成sign MD5 加密 toUpperCase key首字母大写
     *
     * @param map
     * @param paternerKey
     * @return
     */
    public static String generateSignWithKey(Map<String, String> map, String paternerKey) {
        //去给map key排序  a  b  c  d  e  f
        Map<String, String> tmap = MapUtil.order(map);
        //删除sgin ，签名不参与算法
         if (tmap.containsKey("sign")) {
            tmap.remove("sign");
        }
        //System.out.println("temp - remove key = sgin："+tmap.toString());
        String str = MapUtil.mapJoin(tmap, false, false);
        //System.out.println("解读Map  参数串连："+str);

        return DigestUtils.md5Hex(str + "&Key=" + paternerKey).toUpperCase();
    }

    /**
     * 生成 paySign
     *
     * @param map
     * @param paySignKey
     * @return
     */
    public static String generatePaySign(Map<String, String> map, String paySignKey) {
        if (paySignKey != null) {
            map.put("appkey", paySignKey);
        }
        Map<String, String> tmap = MapUtil.order(map);
        String str = MapUtil.mapJoin(tmap, true, false);
        return DigestUtils.shaHex(str);
    }




}
