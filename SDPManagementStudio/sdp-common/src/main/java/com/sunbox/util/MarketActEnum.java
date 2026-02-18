package com.sunbox.util;

import org.apache.commons.compress.utils.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum MarketActEnum {
    ACT_TYPE_QYD("1", "4331"),//4331 19652
    ACT_TYPE_CYD("2", "4332"),//4332 19653
    ACT_TYPE_FYD("3", "4330"),//4330 19654
    ACT_TYPE_JDD("4", "4333");//4333 19658

    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    MarketActEnum(String name, String value){
         this.name=name;
         this.value=value;
    }
    public static List toList() {
        List list = Lists.newArrayList();
        for (MarketActEnum typeEnum : MarketActEnum.values()) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("value", typeEnum.getValue());
            map.put("name", typeEnum.getName());
            list.add(map);
        }
        return list;
    }

    public static String getValue(String value) {
        switch (value) {
            case "1":
                return ACT_TYPE_QYD.value;
            case "2":
                return ACT_TYPE_CYD.value;
            case "3":
                return ACT_TYPE_FYD.value;
            case "4":
                return ACT_TYPE_JDD.value;
            default:
                return null;
        }
    }

   public static void main(String[] args){
        System.out.println(MarketActEnum.getValue("1"));
    }

}
