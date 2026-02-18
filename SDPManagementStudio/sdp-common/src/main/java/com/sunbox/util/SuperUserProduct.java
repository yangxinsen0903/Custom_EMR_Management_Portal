package com.sunbox.util;

import java.util.UUID;

public enum SuperUserProduct {

    PRO_1("f2f646e4-26c5-464f-a977-c693cad95904", "电影2D/3D通兑票（2张）", "2020-01-03 10:00:00", "2020-01-03 17:00:00"),
    PRO_2("a2fae2ce-af46-4b89-8667-41afd0a5e84f", "齿科保健套餐", "2020-01-07 10:00:00", "2020-01-07 23:59:59"),
    PRO_3("727438cb-0686-44d7-b026-1865967d50f1", "爱奇艺黄金会员半年卡", "2020-01-09 12:00:00", "2020-01-09 23:59:59"),
    PRO_4("1c945d04-8b6f-4758-8aa8-cbc353931203", "蜻蜓FM会员半年卡", "2020-01-14 10:00:00", "2020-01-14 17:00:00"),
    PRO_5("71bc738f-2aff-4ef8-96c8-8a24055a53d9", "电影2D/3D通兑票（2张）", "2020-01-16 10:00:00", "2020-01-16 17:00:00"),
    PRO_6("597c8411-ee39-4ab1-825b-45f24e6cc158", "爱奇艺黄金会员半年卡", "2020-01-20 10:00:00", "2020-01-20 17:00:00"),
    PRO_7("6585456b-8b0a-4dd5-a3e7-9c1bf209dd84", "蜻蜓FM会员半年卡", "2020-01-22 10:00:00", "2020-01-22 17:00:00"),
    PRO_8("76872a5e-dfb7-4dcb-852a-2325c561ca4c", "机场贵宾厅", "2020-01-14 15:00:00", "2020-01-14 23:59:59");

    private String flag;
    private String name;
    private String startTime;
    private String endTime;

    SuperUserProduct(String flag, String name, String startTime, String endTime) {
        this.flag = flag;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
    }


    public static SuperUserProduct getDetail(String flag) {
        SuperUserProduct[] products = SuperUserProduct.values();
        for (SuperUserProduct userProduct : products) {
            if (flag.equals(userProduct.getFlag())) {
                return userProduct;
            }
        }
        return null;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public static void main(String[] args) {
        System.out.println(UUID.randomUUID().toString());
    }


}
