package com.sunbox.sdpadmin.mapper;

import com.sunbox.sdpadmin.core.model.AdminRole;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminRoleDao {

    int save(AdminRole role);

    int update(AdminRole role);

    AdminRole getRoleById(@Param("id") String id);

    List<AdminRole> getRoleList();

    List<AdminRole> pageList(@Param("page") int page,
                             @Param("pageSize") int pageSize,
                            @Param("status") String status,
                             @Param("role") String role);

    int pageListCount(
                  @Param("status") String status,
                 @Param("role") String role);
}
