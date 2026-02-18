package com.sunbox.dao.mapper;

import com.sunbox.domain.InfoThirdApiFailedLog;
import com.sunbox.domain.InfoThirdApiFailedLogWithBLOBs;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface InfoThirdApiFailedLogMapper {
    int deleteByPrimaryKey(Long id);

    int insert(InfoThirdApiFailedLogWithBLOBs record);

    int insertSelective(InfoThirdApiFailedLogWithBLOBs record);

    InfoThirdApiFailedLogWithBLOBs selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(InfoThirdApiFailedLogWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(InfoThirdApiFailedLogWithBLOBs record);

    int updateByPrimaryKey(InfoThirdApiFailedLog record);

    List<InfoThirdApiFailedLog> getListByParam(Map param);

    Integer getCountByParam(Map param);

}