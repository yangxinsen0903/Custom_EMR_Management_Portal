package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.BaseUserInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 *  base_user_region的user_id存储base_user_info的user_name,
 *  base_user_role的user_id存储base_user_info的user_name
 */
@Repository
public interface BaseUserInfoMapper {

    List<BaseUserInfo> selectAllByPrimaryKey(@Param("userId") String userId);

    int selectAllCountByUsername(@Param("username") String username, @Param("realName") String realName);

    List<BaseUserInfo> selectAllByUsername(@Param("username") String username, @Param("realName") String realName,
                                           @Param("pageIndex") int pageIndex, @Param("pageSize") int pageSize);

    int deleteByPrimaryKey(String userId);

    int insert(BaseUserInfo record);

    int insertSelective(BaseUserInfo record);

    BaseUserInfo selectByPrimaryKey(String userId);

    BaseUserInfo selectByUsername(String username);

    int updateByPrimaryKeySelective(BaseUserInfo record);

    int updateByPrimaryKey(BaseUserInfo record);

    /**
     * 根据各种查询条件，（分页），分组查出所有的id
     * @param inParam
     * @return
     */
    List<Map<String, String>> selectAllUserId(Map<String, Object> inParam);

    int selectCountByUserRole(Map<String, Object> inParam);

    //base_user_region的user_id存储base_user_info的user_name,
    // base_user_role的user_id存储base_user_info的user_name
    List<BaseUserInfo> selectAllByPrimaryKeys(@Param("userIdList")List<String> userIdList);

 }