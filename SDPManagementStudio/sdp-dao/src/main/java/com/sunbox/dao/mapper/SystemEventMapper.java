package com.sunbox.dao.mapper;

import com.sunbox.domain.InfoClusterVmClearLog;
import com.sunbox.domain.SystemEvent;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemEventMapper {

    SystemEvent selectByPrimaryKey(String systemEventId);

    int deleteByPrimaryKey(String systemEventId);

    List<SystemEvent> selectByPage(int limit, int size);

    int insert(SystemEvent record);

}