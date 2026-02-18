package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.BaseReleaseApps;
import com.sunbox.domain.BaseReleaseAppsKey;
import org.springframework.stereotype.Repository;

@Repository
public interface BaseReleaseAppsMapper {
    int deleteByPrimaryKey(BaseReleaseAppsKey key);

    int insert(BaseReleaseApps record);

    int insertSelective(BaseReleaseApps record);

    BaseReleaseApps selectByPrimaryKey(BaseReleaseAppsKey key);

    int updateByPrimaryKeySelective(BaseReleaseApps record);

    int updateByPrimaryKey(BaseReleaseApps record);
}