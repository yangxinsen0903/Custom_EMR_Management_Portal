package com.sunbox.sdpadmin.service;


import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpadmin.core.model.AdminRole;

import java.util.List;
import java.util.Map;

public interface AdminRoleService {

    ResultMsg save(String role, String alias);

    ResultMsg update(AdminRole role);

    ResultMsg disable(String id);

    ResultMsg enabled(String id);

    ResultMsg assignPremission(String roleId, List<String> permissionIds);

    Map<String, Object> pageList(int page, int pageSize, String role,String status);

}
