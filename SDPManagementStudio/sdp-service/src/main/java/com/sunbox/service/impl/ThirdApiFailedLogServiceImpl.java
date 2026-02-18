package com.sunbox.service.impl;

import com.sunbox.dao.mapper.InfoThirdApiFailedLogMapper;
import com.sunbox.domain.InfoThirdApiFailedLog;
import com.sunbox.domain.InfoThirdApiFailedLogWithBLOBs;
import com.sunbox.domain.ResultMsg;
import com.sunbox.service.IThirdApiFailedLogService;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author : [niyang]
 * @className : ThirdApiFailedLogServiceImpl
 * @description : [描述说明该类的功能]
 * @createTime : [2023/8/3 5:25 PM]
 */
@Service
public class ThirdApiFailedLogServiceImpl implements IThirdApiFailedLogService, BaseCommonInterFace {

    @Autowired
    private InfoThirdApiFailedLogMapper thirdApiFailedLogMapper;

    /**
     * 保存日志信息
     *
     * @param failedLogWithBLOBs
     * @return
     */
    @Override
    public ResultMsg saveFailedLog(InfoThirdApiFailedLogWithBLOBs failedLogWithBLOBs) {
        ResultMsg resultMsg = new ResultMsg();
        try {
            thirdApiFailedLogMapper.insert(failedLogWithBLOBs);
            resultMsg.setResult(true);
        }catch (Exception e){
            getLogger().error("保存失败日志异常，",e);
            resultMsg.setErrorMsg(ExceptionUtils.getStackTrace(e));
            resultMsg.setResult(false);
        }
        return resultMsg;
    }

    /**
     * 根据查询条件查询
     *
     * @param queryParam
     * @return
     */
    @Override
    public List<InfoThirdApiFailedLog> queryFailedLogByParam(Map queryParam) {
        return null;
    }

    /**
     * 根据查询条件查询匹配的总条数
     *
     * @param queryParam
     * @return
     */
    @Override
    public Integer queryFailedLogCountByParam(Map queryParam) {
        return null;
    }

    /**
     * 根据主键查询详情
     *
     * @param id
     * @return
     */
    @Override
    public InfoThirdApiFailedLogWithBLOBs getFullLogById(Long id) {
        return thirdApiFailedLogMapper.selectByPrimaryKey(id);
    }
}
