package com.sunbox.dao.mapper;

import com.sunbox.domain.InfoCluster;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface InfoClusterNeoMapper {

  InfoCluster selectByPrimaryKey(String clusterId);

}
