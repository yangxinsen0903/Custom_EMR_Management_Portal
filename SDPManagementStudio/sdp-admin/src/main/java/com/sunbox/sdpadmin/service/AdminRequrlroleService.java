package com.sunbox.sdpadmin.service;

import com.sunbox.sdpadmin.core.model.AdminRequrlrole;

import java.util.List;

public interface AdminRequrlroleService {

    int deleteByPrimaryKey(String id);

    int insert(AdminRequrlrole record);

    int insertSelective(AdminRequrlrole record);

    List<AdminRequrlrole> selectAll();

    AdminRequrlrole selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(AdminRequrlrole record);

    int updateByPrimaryKey(AdminRequrlrole record);
}
