package com.sunbox.dao.mapper;

import com.sunbox.domain.InfoClusterFullLogWithBLOBs;
import com.sunbox.domain.InfoClusterInfoCollectLog;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.util.List;

@Mapper
@Repository
public interface InfoClusterInfoCollectLogMapper {

    int insertSelective(@NotNull InfoClusterInfoCollectLog infoClusterInfoCollectLog);
    InfoClusterInfoCollectLog selectByPrimaryKey(@NotNull String id);
    List<InfoClusterInfoCollectLog> selectAll(InfoClusterInfoCollectLog infoClusterInfoCollectLog);
    Long count(InfoClusterInfoCollectLog infoClusterInfoCollectLog);
    int updateByPrimaryKey(@NotNull InfoClusterInfoCollectLog infoClusterInfoCollectLog);

    int insert(InfoClusterFullLogWithBLOBs record);

}
