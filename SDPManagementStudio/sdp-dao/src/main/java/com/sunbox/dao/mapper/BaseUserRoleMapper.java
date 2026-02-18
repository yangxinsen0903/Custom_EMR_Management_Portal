package com.sunbox.dao.mapper;

import com.sunbox.domain.BaseUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *  用户和角色, base_user_role的user_id存储base_user_info的user_name
 */
@Mapper
@Repository
public interface BaseUserRoleMapper {

    int insert(BaseUserRole record);

    int updateUserRole(BaseUserRole userRole);

    BaseUserRole selectRoleByUser(@Param("userId") String userId);

    // base_user_role的user_id存储base_user_info的user_name
    List<BaseUserRole> selectAllByPrimaryKeys(@Param("userIdList")List<String> userIdList);

    int deleteByUserId(String userId);


}
