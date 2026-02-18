package com.sunbox.runtime;

import org.springframework.stereotype.Service;

/**
 * @author : [niyang]
 * @className : CloseEventPorcess
 * @description : [描述说明该类的功能]
 * @createTime : [2023/8/2 1:50 PM]
 */
public class CloseEventPorcess {
    private  static ICloseProcess closeProcesss;

    public static ICloseProcess getCloseProcess() {
        return closeProcesss;
    }

    public  static void setCloseProcess(ICloseProcess closeProcess) {
        closeProcesss = closeProcess;
    }
}
