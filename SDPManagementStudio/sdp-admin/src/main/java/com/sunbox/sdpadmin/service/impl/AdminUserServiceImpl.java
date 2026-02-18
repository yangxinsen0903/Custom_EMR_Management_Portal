package com.sunbox.sdpadmin.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.ResultMsg;

import com.sunbox.domain.ReturnT;
import com.sunbox.sdpadmin.core.model.AdminRole;
import com.sunbox.sdpadmin.core.model.AdminUser;
import com.sunbox.sdpadmin.core.model.AdminUserRole;
import com.sunbox.sdpadmin.mapper.AdminRoleDao;
import com.sunbox.sdpadmin.mapper.AdminUserDao;
import com.sunbox.sdpadmin.mapper.AdminUserRoleDao;
import com.sunbox.sdpadmin.service.AdminUserService;
import com.sunbox.util.EncryptionUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Resource
    private AdminUserDao adminUserDao;

    @Resource
    private AdminUserRoleDao userRoleDao;

    @Resource
    private AdminRoleDao roleDao;

    @Resource
    private AdminUserRoleDao  adminUserRoleDao;

    @Override
    public ReturnT<String> save(AdminUser user) {


        user.setStatus(1);
        user.setCreatetime(new Date());
        user.setId(UUID.randomUUID().toString());
        user.setPassword(EncryptionUtil.MD5(user.getPassword()));
        int a = adminUserDao.save(user);
        if (a > 0) {
            return new ReturnT<>(ReturnT.SUCCESS_CODE, "新增成功");
        } else {
            return new ReturnT<>(ReturnT.FAIL_CODE, "新增失败");
        }

    }

    @Override
    public ReturnT<String> update(String id, String name) {

        AdminUser adminUser = adminUserDao.getAdminUserById(id);
        if (adminUser == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "对象不存在");
        }

        adminUser.setUsername(name);
        adminUserDao.update(adminUser);
        return null;
    }


    @Override
    public ReturnT<String> disable(String id) {
        AdminUser adminUser = adminUserDao.getAdminUserById(id);
        if (adminUser == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "对象不存在");
        }

        adminUser.setStatus(0);
        adminUserDao.update(adminUser);
        return null;
    }

    @Override
    public ReturnT<String> delete(String id) {
        AdminUser adminUser = adminUserDao.getAdminUserById(id);
        List<AdminUserRole> adminUserRoleList = adminUserRoleDao.getUserRoleListByUserId(id);
        if (adminUser == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "用户对象不存在");
        }
        if (adminUserRoleList.size() > 0) {
            adminUserRoleDao.deleteByUserId(id);
        }
        adminUserDao.delete(id);
        return null;
    }

    @Override
    public ReturnT<String> enabled(String id) {
        AdminUser adminUser = adminUserDao.getAdminUserById(id);
        if (adminUser == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "对象不存在");
        }

        adminUser.setStatus(1);
        adminUserDao.update(adminUser);
        return null;
    }

    @Override
    public ResultMsg resetPassword(String userid,String password) {
        ResultMsg msg=new ResultMsg();
        AdminUser adminUser = adminUserDao.getAdminUserById(userid);
        if (adminUser == null) {
            msg.setResult(false);
            msg.setMsg("对象不存在或参数错误");
            return msg;
        }

        adminUser.setPassword(EncryptionUtil.MD5(password));
        int a=adminUserDao.update(adminUser);
        if(a==0){
            msg.setResult(false);
            msg.setMsg("重置失败");
            return msg;
        }else{
            msg.setResult(true);
            msg.setMsg("重置成功");
            return msg;
        }
    }


    @Override
    public Map<String, Object> pageList(int page, int pageSize, String name, String tel,String status) {
        List<AdminUser> adminUsers = adminUserDao.pageList(page, pageSize, name, tel,status);
        int count = adminUserDao.pageListCount(page, pageSize, name, tel,status);

        Map<String, Object> map = new HashMap<>();
        map.put("list", adminUsers);
        map.put("count", count);

        return map;
    }

    @Override
    public ResultMsg assignRole(String userId, List<String> roleIds) {

        ResultMsg resultMsg = new ResultMsg();
        List<AdminUserRole> userRoles = new ArrayList<>();

        int deleteResult = userRoleDao.deleteByUserId(userId);
        if (deleteResult < 0) {
            resultMsg.setResult(false);
            resultMsg.setMsg("分配失败！");
            return resultMsg;
        }
        for (String roleId : roleIds) {
            AdminUserRole userRole = new AdminUserRole();
            userRole.setUserid(userId);
            userRole.setRid(roleId);
            userRoles.add(userRole);
        }
        int saveResult = userRoleDao.saveAll(userRoles);
        if (saveResult <= 0) {
            resultMsg.setResult(false);
            resultMsg.setMsg("指派失败！");
            return resultMsg;
        }

        resultMsg.setResult(true);
        resultMsg.setMsg("成功");
        return resultMsg;
    }

    @Override
    public List<Map<String, Object>> getRoleListByUserId(String userId) {

        List<AdminUserRole> userRoles = userRoleDao.getUserRoleListByUserId(userId);
        List<AdminRole> roles = roleDao.getRoleList();

        List<Map<String, Object>> roleList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        boolean isAssign = false;
        for (AdminRole role : roles) {
            isAssign = false;
            for (AdminUserRole userRole : userRoles) {
                if (role.getId().equals(userRole.getRid())) {
                    isAssign = true;
                }
            }
            map.put("isAssign", isAssign);
            map.put("userId", role.getId());
            map.put("alias", role.getAlias());
            map.put("role", role.getRole());
            roleList.add(map);
        }
        return roleList;

    }
    @Override
    public ResultMsg getUserRoleTree(String userid){
        List<AdminRole> roleist=this.roleDao.getRoleList();
        List<AdminUserRole> userolrList=this.userRoleDao.getUserRoleListByUserId(userid);
        JSONArray array=new JSONArray();
        for(AdminRole ap:roleist){
            JSONObject obj2=new JSONObject();
            obj2.put("id",ap.getId());
            obj2.put("name",ap.getRole());
            obj2.put("pId","0");
            for(AdminUserRole adrp:userolrList){
                if(adrp.getRid().equals(ap.getId())){
                    obj2.put("checked",true);
                    break;
                }
            }
            array.add(obj2);
        }
        ResultMsg msg=new ResultMsg();
        msg.setResult(true);
        msg.setData(array.toJSONString());
        return msg;
    }
    @Override
    public AdminUser verifyLoginInfo(Map<String, Object> map) {
        return adminUserDao.verifyLoginInfo(map);
    }

    @Override
    public AdminUser verifyLoginInfoWithoutPassword(Map<String, Object> map) {
        return adminUserDao.verifyLoginInfo(map);
    }

    @Override
    public AdminUser getAdminUserById(String id) {
        return adminUserDao.getAdminUserById(id);
    }

    @Override
    public AdminUser getAdminUserByName(String username) {
        return adminUserDao.getAdminUserByName(username);
    }
}
