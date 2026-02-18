package com.sunbox.dao.mapper;

import com.sunbox.domain.ApiAuthKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface AuthKeyMapper {

    int insert(ApiAuthKey record);

    List<ApiAuthKey> selectAllByName(@Param("name") String name, @Param("pageIndex") int pageIndex, @Param("pageSize") int pageSize);

    int selectCountByName(@Param("name") String name);

    int updateByPrimaryKey(ApiAuthKey record);

    int deleteByPrimaryKey(Long id);

    List<ApiAuthKey> selectAllByAk(@Param("accessKey") String accessKey);

    List<ApiAuthKey> selectAllExpire( );


}
