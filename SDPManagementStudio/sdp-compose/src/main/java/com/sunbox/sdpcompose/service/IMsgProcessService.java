package com.sunbox.sdpcompose.service;

public interface IMsgProcessService {

    /**
     * 传输处理
     *
     * @param callback
     * @param message
     */
    void transforMessage(String callback,String message);
}
