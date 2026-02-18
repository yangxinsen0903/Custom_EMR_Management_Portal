package com.sunbox.sdpadmin.service.impl;

import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpadmin.core.model.AdminRole;
import com.sunbox.sdpadmin.core.model.AdminRolePermission;
import com.sunbox.sdpadmin.mapper.AdminRoleDao;
import com.sunbox.sdpadmin.mapper.AdminRolePermissionDao;
import com.sunbox.sdpadmin.service.AdminRoleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class AdminRoleServiceImpl implements AdminRoleService {

    @Resource
    private AdminRoleDao roleDao;

    @Resource
    private AdminRolePermissionDao rolePermissionDao;

    @Override
    public ResultMsg save(String role, String alias) {
        AdminRole adminrole=new AdminRole();
        adminrole.setRole(role);
        adminrole.setAlias(alias);
        adminrole.setStatus(1);
        adminrole.setId(UUID.randomUUID().toString().replaceAll("-",""));
        adminrole.setCreateDate(new Date());
        int a=roleDao.save(adminrole);
        ResultMsg msg=new ResultMsg();
        if(a>0){
            msg.setMsg("新增成功");
            msg.setResult(true);
        }else{
            msg.setMsg("新增失败");
            msg.setResult(false);
        }
        return msg;
    }

    @Override
    public ResultMsg update(AdminRole role) {
        ResultMsg msg=new ResultMsg();
        AdminRole adminRole = roleDao.getRoleById(role.getId());
        adminRole.setAlias(role.getAlias());
        adminRole.setRole(role.getRole());
        int a= roleDao.update(adminRole);
        if(a<=0){
            msg.setMsg("修改失败");
            msg.setResult(false);
        }else{
            msg.setMsg("修改成功");
            msg.setResult(true);
        }
        return msg;
    }

    @Override
    public ResultMsg disable(String id) {
        ResultMsg msg=new ResultMsg();
        AdminRole role = roleDao.getRoleById(id);
        role.setStatus(0);
        int a=roleDao.update(role);
        if(a<=0){
            msg.setMsg("停用失败");
            msg.setResult(false);
        }else{
            msg.setMsg("停用成功");
            msg.setResult(true);
        }
        return msg;

    }

    @Override
    public ResultMsg enabled(String id) {
        ResultMsg msg=new ResultMsg();
        AdminRole role = roleDao.getRoleById(id);
        role.setStatus(1);
        int a= roleDao.update(role);
        if(a<=0){
            msg.setMsg("启用失败");
            msg.setResult(false);
        }else{
            msg.setMsg("启用成功");
            msg.setResult(true);
        }
        return msg;
    }

    @Override
    public ResultMsg assignPremission(String roleId, List<String> permissionIds) {
        ResultMsg msg=new ResultMsg();
        int deleteResult = rolePermissionDao.deleteByRoleId(roleId);

        if (deleteResult < 0) {
            msg.setResult(false);
            return msg;
        }

        List<AdminRolePermission> rolePermissions = new ArrayList<>();
        if(permissionIds.size()==0){
            msg.setMsg("处理成功");
            msg.setResult(true);
            return  msg;
        }
        for (String permissionId : permissionIds) {
            AdminRolePermission rolePermission = new AdminRolePermission();
            rolePermission.setPid(permissionId);
            rolePermission.setRid(roleId);
            rolePermissions.add(rolePermission);
        }
        int a=rolePermissionDao.saveAll(rolePermissions);
        if(a<=0){
            msg.setMsg("授权失败");
            msg.setResult(false);
        }else{
            msg.setMsg("授权成功");
            msg.setResult(true);
        }
        return  msg;
    }

    @Override
    public Map<String, Object> pageList(int page, int pageSize, String role,String status) {

        Map<String, Object> map = new HashMap<>();

        List<AdminRole> roles = roleDao.pageList(page, pageSize,status, role);
        int count = roleDao.pageListCount(status,role);

        map.put("list", roles);
        map.put("count", count);
        return map;
    }

}
