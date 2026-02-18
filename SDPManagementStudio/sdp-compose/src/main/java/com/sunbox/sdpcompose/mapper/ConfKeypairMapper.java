package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.ConfKeypair;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfKeypairMapper {
    int deleteByPrimaryKey(String keypairId);

    int insert(ConfKeypair record);

    int insertSelective(ConfKeypair record);

    ConfKeypair selectByPrimaryKey(String keypairId);

    int updateByPrimaryKeySelective(ConfKeypair record);

    int updateByPrimaryKey(ConfKeypair record);
}