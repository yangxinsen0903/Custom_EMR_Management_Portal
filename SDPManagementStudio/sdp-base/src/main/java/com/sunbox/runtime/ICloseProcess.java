package com.sunbox.runtime;

import com.sunbox.web.BaseCommonInterFace;

public interface ICloseProcess {

    //处理关闭事件
    default boolean closeProcess(){
        return true;
    };
}
