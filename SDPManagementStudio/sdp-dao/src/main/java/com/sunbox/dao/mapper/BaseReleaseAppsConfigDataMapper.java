package com.sunbox.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface BaseReleaseAppsConfigDataMapper {

    List<String> selectAll(@Param("releaseVersionList") List<String> releaseVersionList) ;

}
