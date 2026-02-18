package com.sunbox.sdpadmin.service;

import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.ReturnT;
import com.sunbox.sdpadmin.core.model.AdminUser;

import java.util.List;
import java.util.Map;

public interface AdminUserService {

    ReturnT<String> save(AdminUser user);

    ReturnT<String> update(String id, String name);

    ReturnT<String> disable(String id);

    ReturnT<String> enabled(String id);

    ReturnT<String> delete(String id);


    ResultMsg resetPassword(String userid,String password);

    Map<String, Object> pageList(int page, int pageSize, String name,String tel,String status);


    ResultMsg assignRole(String userId, List<String> roleIds);

    List<Map<String, Object>> getRoleListByUserId(String userId);
    ResultMsg getUserRoleTree(String userid);

    AdminUser verifyLoginInfo(Map<String,Object> map);

    AdminUser verifyLoginInfoWithoutPassword(Map<String,Object> map);

    AdminUser getAdminUserById(String id);

    AdminUser getAdminUserByName(String username);
}
