package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.BaseReleaseApps;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface BaseReleaseAppsMapper {
    int deleteByPrimaryKey(@Param("releaseVersion") String releaseVersion, @Param("appName") String appName);

    int insert(BaseReleaseApps record);

    int insertSelective(BaseReleaseApps record);

    BaseReleaseApps selectByPrimaryKey(@Param("releaseVersion") String releaseVersion, @Param("appName") String appName);

    int updateByPrimaryKeySelective(BaseReleaseApps record);

    int updateByPrimaryKey(BaseReleaseApps record);

    List<BaseReleaseApps> selectByObject(Map params);

    List<BaseReleaseApps> selectClusterReleaseLabels(@Param("releaseLabelPrefix") String releaseLabelPrefix,
                                                     @Param("appVer") String appVer,
                                                     @Param("appName") String appName);
}