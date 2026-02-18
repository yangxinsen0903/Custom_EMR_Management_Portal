package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.InfoDelayMsg;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InfoDelayMsgMapper {
    int deleteByPrimaryKey(Long msgId);

    int insert(InfoDelayMsg record);

    int insertSelective(InfoDelayMsg record);

    InfoDelayMsg selectByPrimaryKey(Long msgId);

    int updateByPrimaryKeySelective(InfoDelayMsg record);

    int updateByPrimaryKeyWithBLOBs(InfoDelayMsg record);

    int updateByPrimaryKey(InfoDelayMsg record);

    List<InfoDelayMsg> selectLost();
}