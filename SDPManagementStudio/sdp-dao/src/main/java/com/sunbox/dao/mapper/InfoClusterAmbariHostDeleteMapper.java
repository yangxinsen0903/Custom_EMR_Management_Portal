package com.sunbox.dao.mapper;

import com.sunbox.domain.InfoClusterAmbariHostDelete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface InfoClusterAmbariHostDeleteMapper {
    int deleteByPrimaryKey(Long id);

    int insert(InfoClusterAmbariHostDelete record);

    int insertSelective(InfoClusterAmbariHostDelete record);

    InfoClusterAmbariHostDelete selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(InfoClusterAmbariHostDelete record);

    int updateByPrimaryKey(InfoClusterAmbariHostDelete record);

    List<InfoClusterAmbariHostDelete> selectByClusterIdAndHostName(@Param("clusterId")String clusterId, @Param("hostName") String hostName);

    /**
     * 获取需要清理的host
     * 1. VM 在Azure已经销毁
     * 2. CreateTime < now()-Duration（秒）
     * @param duration 秒
     * @return
     */
    List<InfoClusterAmbariHostDelete> selectNeedClearHostsByCreateTime(@Param("duration") Integer duration);
}