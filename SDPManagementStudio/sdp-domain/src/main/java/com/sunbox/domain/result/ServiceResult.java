package com.sunbox.domain.result;

import com.sunbox.domain.ResultMsg;

public class ServiceResult {
    public static final ServiceResult OK = new ServiceResult(true, null);
    private boolean success;

    private String message;
    private Exception exception;

    private ServiceResult() {
    }

    public ServiceResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Exception getException() {
        return exception;
    }

    public static ServiceResult success() {
        ServiceResult result = new ServiceResult();
        result.success = true;
        return result;
    }

    public static ServiceResult failure(String message) {
        ServiceResult result = new ServiceResult();
        result.success = false;
        result.message = message;
        return result;
    }

    public static ServiceResult failure(Exception e) {
        ServiceResult result = new ServiceResult();
        result.success = false;
        result.message = e.getMessage();
        result.exception = e;
        return result;
    }

    public ResultMsg toResultMsg(){
        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setResult(this.success);
        resultMsg.setErrorMsg(this.message);
        return resultMsg;
    }
}
