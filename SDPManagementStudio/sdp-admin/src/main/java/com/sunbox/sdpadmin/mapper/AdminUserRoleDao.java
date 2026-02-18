package com.sunbox.sdpadmin.mapper;

import com.sunbox.sdpadmin.core.model.AdminUserRole;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminUserRoleDao {

    int saveAll(List<AdminUserRole> list);

    int deleteByUserId(String userId);

    List<AdminUserRole> getUserRoleListByUserId(@Param("userId") String userId);
}
