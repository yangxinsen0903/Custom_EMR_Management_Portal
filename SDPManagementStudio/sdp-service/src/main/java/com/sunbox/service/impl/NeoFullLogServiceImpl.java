package com.sunbox.service.impl;

import com.sunbox.dao.mapper.InfoClusterInfoCollectLogMapper;
import com.sunbox.domain.InfoClusterFullLogWithBLOBs;
import com.sunbox.domain.ResultMsg;
import com.sunbox.service.INeoFullLogService;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NeoFullLogServiceImpl implements INeoFullLogService , BaseCommonInterFace {

    @Autowired
    private InfoClusterInfoCollectLogMapper fullLogMapper;

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
}
