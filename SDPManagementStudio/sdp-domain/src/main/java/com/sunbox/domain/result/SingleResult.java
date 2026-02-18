package com.sunbox.domain.result;

import com.sunbox.domain.ResultMsg;

public class SingleResult<T>{
    private T data;

    private boolean success;

    private String message;
    private Exception exception;

    public T getData() {
        return data;
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

    public static <E> SingleResult<E> success(E data){
        SingleResult<E> result = new SingleResult<>();
        result.success = true;
        result.data = data;
        return result;
    }

    public static <E> SingleResult<E> failure(String message){
        SingleResult<E> result = new SingleResult<>();
        result.success = false;
        result.data = null;
        result.message = message;
        return result;
    }

    public static <E> SingleResult<E> failure(Exception e){
        SingleResult<E> result = new SingleResult<>();
        result.success = false;
        result.data = null;
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
