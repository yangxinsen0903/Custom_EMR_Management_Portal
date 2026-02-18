package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.ConfTagKeys;

import java.util.List;

public interface ConfTagKeysMapper {
    int insert(ConfTagKeys record);

    int insertSelective(ConfTagKeys record);

    List<ConfTagKeys> selectAll();
}