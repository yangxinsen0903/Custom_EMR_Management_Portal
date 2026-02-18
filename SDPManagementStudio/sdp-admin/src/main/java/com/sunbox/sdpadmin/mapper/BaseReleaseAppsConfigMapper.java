package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.BaseReleaseAppsConfig;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface BaseReleaseAppsConfigMapper {
    int deleteByPrimaryKey(String releaseVersion);

    int insert(BaseReleaseAppsConfig record);

    int insertSelective(BaseReleaseAppsConfig record);

    BaseReleaseAppsConfig selectByPrimaryKey(@Param("releaseVersion") String releaseVersion, @Param("appConfigClassification") String appConfigClassification);

    int updateByPrimaryKeySelective(BaseReleaseAppsConfig record);

    int updateByPrimaryKey(BaseReleaseAppsConfig record);

    List<BaseReleaseAppsConfig> selectByObject(Map params);

    List<BaseReleaseAppsConfig> selectAll(@Param("releaseVersion") String releaseVersion);
}