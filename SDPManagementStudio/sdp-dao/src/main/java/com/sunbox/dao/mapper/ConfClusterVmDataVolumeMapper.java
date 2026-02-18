package com.sunbox.dao.mapper;

import com.sunbox.domain.ConfClusterVmDataVolume;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
@Repository
public interface ConfClusterVmDataVolumeMapper {
    int deleteByPrimaryKey(String volumeConfId);

    int insert(ConfClusterVmDataVolume record);

    int insertSelective(ConfClusterVmDataVolume record);

    ConfClusterVmDataVolume selectByPrimaryKey(String volumeConfId);

    int updateByPrimaryKeySelective(ConfClusterVmDataVolume record);

    int updateByPrimaryKey(ConfClusterVmDataVolume record);

    int updateByVmConfId(ConfClusterVmDataVolume record);

    int insertBatch(@Param("vmdatas") List<ConfClusterVmDataVolume> vmdatas);

    List<ConfClusterVmDataVolume> selectByVmConfId(String vmConfId);

    List<ConfClusterVmDataVolume> selectByVmConfIds(@Param("vmConfIds") Set<String> vmConfIds);

}