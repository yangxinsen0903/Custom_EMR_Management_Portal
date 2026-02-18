package com.sunbox.sdpcompose.provider;

import com.sunbox.web.BaseCommonInterFace;

public interface IntegrationProvider extends BaseCommonInterFace {
    /**
     * 收集日志
     * @param param
     * @return
     */
    String collectLog(Object param);
}
