package com.sunbox.sdpadmin.model.shein.response;

public class SheinResponseModel {
    /**
     * 响应码，200：已提交创建集群任务
     */
    private String code;
    /**
     * 结果数据
     */
    private Object info;
    /**
     * 响应消息
     */
    private String msg;

    public static final String Request_Success="200";

    public static final String Request_ConditionError="500";

    public static final String Request_NoData="404";

    public static final String Request_Failed="-1";

    public static final String Request_Timeout="403";

    public static final String MSG_Success="success";


    public String getCode() { return code; }
    public void setCode(String value) { this.code = value; }

    public Object getInfo() {
        return info;
    }

    public void setInfo(Object info) {
        this.info = info;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "SheinResponseModel{" +
                "code='" + code + '\'' +
                ", info=" + info +
                ", msg='" + msg + '\'' +
                '}';
    }
}