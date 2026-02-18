package com.sunbox.util;

/**
 * Created by DiaoWen on 2022/11/15
 */
public class TradeCodeUtil {

    //1:加油广东APP;2:加油中石化APP;3:广东石油微信公众号;4:易捷遥买小程序;5:广东易捷小程序(山水相逢);
    //6:石化优选小程序;7:加油中石化小程序;8:天猫旗舰店;9:大家来加油小程序;10:司机之家小程序;11:员工乐购小程序';
    public static String getTradeCode(Integer sourceOrder){
        String tradeCode = "";
        if(1 == sourceOrder){
            tradeCode = "APP";
        }
        if(2 == sourceOrder){
            tradeCode = "MWEB";
        }
        if(3 == sourceOrder){
            tradeCode = "JSAPI";
        }
        if(4 == sourceOrder||5 == sourceOrder||6 == sourceOrder||7 == sourceOrder
                ||9 == sourceOrder||10 == sourceOrder| 11 == sourceOrder){
            tradeCode = "JSAPI_XCX";
        }
        return tradeCode;
    }
}