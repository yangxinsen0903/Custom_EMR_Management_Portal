package com.sunbox.sdpadmin.mapper;

import com.sunbox.sdpadmin.core.model.AdminUser;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AdminUserDao {

    int save(AdminUser user);

    int update(AdminUser user);

    AdminUser getAdminUserById(@Param("id") String id);

    AdminUser verifyLoginInfo(Map<String,Object> map);

    List<AdminUser> pageList(@Param("page") int page,
                             @Param("pageSize") int pageSize,
                             @Param("username") String username, @Param("tel") String tel,@Param("status")String status);

    int pageListCount(@Param("page") int page,
                  @Param("pageSize") int pageSize,
                  @Param("username") String username,@Param("tel") String tel,@Param("status")String status);

    AdminUser getAdminUserByName(@Param("username") String username);

    void delete(@Param("id") String id);
}
