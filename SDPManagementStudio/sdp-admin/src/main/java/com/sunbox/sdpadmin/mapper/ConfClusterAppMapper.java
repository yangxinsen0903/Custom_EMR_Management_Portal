package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.ConfClusterApp;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfClusterAppMapper {
    int deleteByPrimaryKey(@Param("clusterId") String clusterId, @Param("appName") String appName);

    int insert(ConfClusterApp record);

    int insertSelective(ConfClusterApp record);

    ConfClusterApp selectByPrimaryKey(@Param("clusterId") String clusterId, @Param("appName") String appName);

    int updateByPrimaryKeySelective(ConfClusterApp record);

    int updateByPrimaryKey(ConfClusterApp record);

    List<ConfClusterApp> selectByObject(ConfClusterApp confClusterApp);
}