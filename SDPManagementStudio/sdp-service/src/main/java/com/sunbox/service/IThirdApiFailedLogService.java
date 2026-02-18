package com.sunbox.service;

import com.sunbox.domain.InfoThirdApiFailedLog;
import com.sunbox.domain.InfoThirdApiFailedLogWithBLOBs;
import com.sunbox.domain.ResultMsg;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface IThirdApiFailedLogService {

    /**
     * 保存日志信息
     *
     * @param failedLogWithBLOBs
     * @return
     */
    ResultMsg saveFailedLog(InfoThirdApiFailedLogWithBLOBs failedLogWithBLOBs);

    /**
     * 根据查询条件查询
     *
     * @param queryParam
     * @return
     */
    List<InfoThirdApiFailedLog> queryFailedLogByParam(@Param("queryParam")Map queryParam);

    /**
     * 根据查询条件查询匹配的总条数
     *
     * @param queryParam
     * @return
     */
    Integer queryFailedLogCountByParam(@Param("queryParam")Map queryParam);

    /**
     * 根据主键查询详情
     *
     * @param id
     * @return
     */
    InfoThirdApiFailedLogWithBLOBs getFullLogById(@Param("id")Long id);

}
