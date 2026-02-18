package com.sunbox.dao.mapper;

import com.sunbox.domain.BaseUserRegion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 *  用户和数据中心, base_user_region的user_id存储base_user_info的user_name
 */
@Mapper
@Repository
public interface BaseUserRegionMapper {

    int insert(BaseUserRegion userRegion);

    int deleteUserRegion(String userId);

    int batchInsertRegion(@Param("userRegionList") List<BaseUserRegion> userRegionList);

    List<Map<String, Object>> selectRegionByUserIds(@Param("userIdList")List<String> userIdList);

    // 根据用户id查询数据中心的id, 再查询数据中心的region和type, 再掉接口.
    List<Map<String, Object>> selectRegionSimpleByUserIds(@Param("userIdList")List<String> userIdList);

    List<String> listUserRegion(@Param("userId")String userId);

}
