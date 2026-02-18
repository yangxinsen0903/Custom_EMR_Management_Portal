package com.sunbox.sdpcompose.service.impl;

import com.sunbox.domain.InfoClusterFullLogWithBLOBs;
import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpcompose.mapper.InfoClusterFullLogMapper;
import com.sunbox.sdpcompose.service.IFullLogService;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author : [niyang]
 * @className : FullLogServiceImpl
 * @description : [sdp与外部接口交互日志记录]
 * @createTime : [2023/1/14 1:19 PM]
 */
@Service
public class FullLogServiceImpl  implements IFullLogService, BaseCommonInterFace {

    @Autowired
    private InfoClusterFullLogMapper fullLogMapper;

    /**
     * 保存log数据
     *
     * @param fullLogWithBLOBs
     * @return
     */
    @Override
    public ResultMsg saveLog(InfoClusterFullLogWithBLOBs fullLogWithBLOBs) {
        ResultMsg msg=new ResultMsg();
        try {
            fullLogMapper.insert(fullLogWithBLOBs);
            msg.setResult(true);
        }catch (Exception e){
            msg.setResult(false);
            getLogger().error("保存fullLog异常",e);
        }
        return msg;
    }

    /**
     * 获取SDP外部接口交互日志列表，不包含参数和响应数据报文
     *
     * @param clusterid 集群ID
     * @return
     */
    @Override
    public ResultMsg getLogListWithOutBlobs(String clusterid) {
        return null;
    }

    /**
     * 获取单条日志数据，包含请求参数和响应报文
     *
     * @param logId 日志ID
     * @return
     */
    @Override
    public ResultMsg getLogWithBolbs(String logId) {
        return null;
    }

    /**
     * 定时删除指定时间之前的日志数据
     *
     * @param responseTime
     * @return
     */
    @Override
    public ResultMsg deleteLogsByResponseTime(Date responseTime) {
        ResultMsg resultMsg = new ResultMsg();
        try {
            fullLogMapper.deleteByResponseTime(responseTime);
            getLogger().info("FullLogServiceImpl.deleteLogsByResponseTime success. responseTime: {}", responseTime.getTime());
            resultMsg.setResult(true);
        } catch (Exception e) {
            resultMsg.setResult(false);
            getLogger().error("删除fullLog异常: ", e);
        }
        return resultMsg;
    }
}
