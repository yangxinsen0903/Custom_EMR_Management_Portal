package com.sunbox.sdpcompose.service;


import com.sunbox.domain.InfoClusterFullLogWithBLOBs;
import com.sunbox.domain.ResultMsg;

import java.util.Date;

/**
 * sdp与外部接口交互日志记录
 */
public interface IFullLogService {

    /**
     * 保存log数据
     * @param fullLogWithBLOBs
     * @return
     */
    ResultMsg saveLog(InfoClusterFullLogWithBLOBs fullLogWithBLOBs);

    /**
     * 获取SDP外部接口交互日志列表，不包含参数和响应数据报文
     * @param clusterid 集群ID
     * @return
     */
    ResultMsg getLogListWithOutBlobs(String clusterid);

    /**
     * 获取单条日志数据，包含请求参数和响应报文
     *
     * @param logId 日志ID
     * @return
     */
    ResultMsg getLogWithBolbs(String logId);

    /**
     * 定时删除指定时间之前的日志数据
     */
    ResultMsg deleteLogsByResponseTime(Date responseTime);
}
