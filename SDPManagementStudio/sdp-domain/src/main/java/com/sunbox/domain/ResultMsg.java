package com.sunbox.domain;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.List;

public class ResultMsg<T> implements Serializable {
    /**
     *
     */
//	private static final long serialVersionUID = 1L;
    private boolean result;//处理状态
    private Object data;//其他数据
    private String msg;//消息
    private List<?> rows;//bootstarp分页设置每一行数据
    private Long total = 0L;//分页设置总数 todo 默认为零 不要改
    private String retcode;//状态码
    private List<?> footer;//jquery datagrid 页脚
    private String errorMsg;
    private String accode;
    private Integer actimes; //acode有效期
    private String bizid;
    private String ext1;
    private String ext2;
    private T resTypeData;

    public static final String NOT_FOUNT = "400";
    public static final String ERROR = "500";
    public static final String DEFUALT = "0";

    public void setResultSucces(String msg) {
        this.result = true;
        this.msg = msg;
    }

    public void setResultFail(String msg) {
        this.result = false;
        this.msg = msg;
        this.errorMsg = msg;
    }

    public String getBizid() {
        return bizid;
    }

    public void setBizid(String bizid) {
        this.bizid = bizid;
    }

    public String getExt1() {
        return ext1;
    }

    public void setExt1(String ext1) {
        this.ext1 = ext1;
    }

    public String getExt2() {
        return ext2;
    }

    public void setExt2(String ext2) {
        this.ext2 = ext2;
    }

    public Integer getActimes() {
        return actimes;
    }

    public void setActimes(Integer actimes) {
        this.actimes = actimes;
    }

    private boolean needmsgcode;//是否需要短信验证码

    public boolean isNeedmsgcode() {
        return needmsgcode;
    }

    public boolean getNeedmsgcode() {
        return needmsgcode;
    }

    public void setNeedmsgcode(boolean needmsgcode) {
        this.needmsgcode = needmsgcode;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public String getAccode() {
        return accode;
    }

    public void setAccode(String accode) {
        this.accode = accode;
    }

    public boolean isResult() {
        return result;
    }

    @JsonIgnore
    public boolean isSuccess(){
        return result;
    }

    public boolean getResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<?> getRows() {
        return rows;
    }

    public void setRows(List<?> rows) {
        this.rows = rows;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(long total2) {
        this.total = total2;
    }

    public String getRetcode() {
        if (StrUtil.isEmpty(retcode)) {
            retcode = DEFUALT;
        }
        return retcode;
    }

    public void setRetcode(String retcode) {
        this.retcode = retcode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public List<?> getFooter() {
        return footer;
    }

    public void setFooter(List<?> footer) {
        this.footer = footer;
    }

    public static ResultMsg FAILURE(String msg) {
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResultFail(msg);
        return resultMsg;
    }

    public static ResultMsg FAILURE(String msg, String retcode) {
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResultFail(msg);
        resultMsg.setRetcode(retcode);
        return resultMsg;
    }

    public static ResultMsg SUCCESS(String msg) {
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResultSucces(msg);
        return resultMsg;
    }

    public static ResultMsg SUCCESS() {
        return SUCCESS(StrUtil.EMPTY);
    }

    public T getResTypeData() {
        return resTypeData;
    }

    public void setResTypeData(T resTypeData) {
        this.resTypeData = resTypeData;
    }

    public static ResultMsg SUCCESS(Object data) {
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(true);
        resultMsg.setData(data);
        return resultMsg;
    }

    public static ResultMsg SUCCESST(Object data) {
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(true);
        resultMsg.setResTypeData(data);
        return resultMsg;
    }

    @Override
    public String toString() {
        return "{" +
                "\"result\":" + result +
                ", \"data\":" + data +
                ", \"msg\":\"" + msg + '"' +
                ", \"rows\":" + rows +
                ", \"total\":" + total +
                ", \"retcode\":\"" + retcode + '"' +
                ", \"footer\":" + footer +
                ", \"errorMsg\":\"" + errorMsg + '"' +
                ", \"accode\":\"" + accode + '"' +
                ", \"actimes\":" + actimes +
                ", \"bizid\":\"" + bizid + '"' +
                ", \"ext1\":\"" + ext1 + '"' +
                ", \"ext2\":\"" + ext2 + '"' +
                ", \"resTypeData\":" + resTypeData +
                ", \"needmsgcode\":" + needmsgcode +
                "}";
    }
}
