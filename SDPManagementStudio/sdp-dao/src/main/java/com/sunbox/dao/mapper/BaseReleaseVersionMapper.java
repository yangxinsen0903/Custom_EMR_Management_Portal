package com.sunbox.dao.mapper;

import com.sunbox.domain.BaseReleaseVersion;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BaseReleaseVersionMapper {
    int deleteByPrimaryKey(String releaseVersion);

    int insert(BaseReleaseVersion record);

    int insertSelective(BaseReleaseVersion record);

    BaseReleaseVersion selectByPrimaryKey(String releaseVersion);

    int updateByPrimaryKeySelective(BaseReleaseVersion record);

    int updateByPrimaryKey(BaseReleaseVersion record);

    List<BaseReleaseVersion> selectAll();
}