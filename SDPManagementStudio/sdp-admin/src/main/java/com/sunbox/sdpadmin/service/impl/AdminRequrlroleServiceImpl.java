package com.sunbox.sdpadmin.service.impl;

import com.sunbox.sdpadmin.core.model.AdminRequrlrole;
import com.sunbox.sdpadmin.mapper.AdminRequrlroleDao;
import com.sunbox.sdpadmin.service.AdminRequrlroleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AdminRequrlroleServiceImpl implements AdminRequrlroleService {
    @Resource
    private AdminRequrlroleDao adminRequrlroleDao;

    @Override
    public int deleteByPrimaryKey(String id) {
        return 0;
    }

    @Override
    public int insert(AdminRequrlrole record) {
        return 0;
    }

    @Override
    public int insertSelective(AdminRequrlrole record) {
        return 0;
    }

    @Override
    public List<AdminRequrlrole> selectAll() {
        return adminRequrlroleDao.selectAll();
    }

    @Override
    public AdminRequrlrole selectByPrimaryKey(String id) {
        return null;
    }

    @Override
    public int updateByPrimaryKeySelective(AdminRequrlrole record) {
        return 0;
    }

    @Override
    public int updateByPrimaryKey(AdminRequrlrole record) {
        return 0;
    }
}
