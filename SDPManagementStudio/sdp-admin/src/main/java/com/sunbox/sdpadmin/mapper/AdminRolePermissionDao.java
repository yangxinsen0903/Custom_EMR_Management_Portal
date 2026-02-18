package com.sunbox.sdpadmin.mapper;

import com.sunbox.sdpadmin.core.model.AdminRolePermission;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminRolePermissionDao {

    int saveAll(List<AdminRolePermission> rolePermissions);

    int deleteByRoleId(String roleId);

    List<AdminRolePermission> getRolePermissionByRoleId(@Param("roleid") String roleid);
}
