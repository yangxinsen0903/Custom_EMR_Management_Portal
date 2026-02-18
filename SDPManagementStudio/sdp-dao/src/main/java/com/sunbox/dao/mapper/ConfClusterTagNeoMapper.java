package com.sunbox.dao.mapper;

import com.sunbox.domain.ConfClusterTag;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ConfClusterTagNeoMapper {

    List<ConfClusterTag> selectByObject(ConfClusterTag confClusterTag);


}
