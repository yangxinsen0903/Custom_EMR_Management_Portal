package com.sunbox.service;

import com.sunbox.domain.InfoClusterFullLogWithBLOBs;
import com.sunbox.domain.ResultMsg;

public interface INeoFullLogService {
    /**
     * 保存log数据
     * @param fullLogWithBLOBs
     * @return
     */
    ResultMsg saveLog(InfoClusterFullLogWithBLOBs fullLogWithBLOBs);
}
