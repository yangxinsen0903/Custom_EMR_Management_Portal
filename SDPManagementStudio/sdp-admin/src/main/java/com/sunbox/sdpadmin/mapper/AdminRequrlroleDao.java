package com.sunbox.sdpadmin.mapper;

import com.sunbox.sdpadmin.core.model.AdminRequrlrole;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminRequrlroleDao {
    int deleteByPrimaryKey(String id);

    int insert(AdminRequrlrole record);

    int insertSelective(AdminRequrlrole record);

    List<AdminRequrlrole> selectAll();

    AdminRequrlrole selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(AdminRequrlrole record);

    int updateByPrimaryKey(AdminRequrlrole record);
}