package com.sunbox.service;

import com.sunbox.domain.BaseUserInfo;
import com.sunbox.domain.BaseUserRole;

import javax.servlet.http.HttpServletRequest;

public interface IUserInfoService {

    /**
     *  通过HttpServletRequest获取用户信息
     * @param request
     * @return
     */
    BaseUserInfo getUserRoleByRequest(HttpServletRequest request);

    BaseUserRole getUserRoleByUserName(String userName);

    String getUserRoleStrByUserName(String userName);

    BaseUserInfo getUserInfoByRequest(HttpServletRequest request) ;

}
