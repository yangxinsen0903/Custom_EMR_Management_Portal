package com.sunbox.sdpadmin.service;

import com.sunbox.domain.*;
import com.sunbox.sdpadmin.model.admin.request.BaseUserInfoRequest;
import com.sunbox.sdpadmin.model.admin.request.LoginData;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface LoginService {

    ResultMsg login(HttpServletRequest request, HttpServletResponse response, LoginData loginData);

    ResultMsg userLogout(HttpServletRequest request, HttpServletResponse response);

    ResultMsg getUserList(BaseUserInfoRequest baseUserInfoRequest);

    ResultMsg createUser(BaseUserInfoRequest baseUserInfo);

    ResultMsg updatePassword(LoginData loginData);

    ResultMsg deleteUser(String username);

    ResultMsg getUserInfoByCookie(HttpServletRequest request, HttpServletResponse response);

    ReturnT<String> logout(HttpServletRequest request, HttpServletResponse response);

    BaseUserInfo getUserInfoByToken(String token);

    /**
     *  通过HttpServletRequest获取用户信息(无密码)
     * @param request
     * @return
     */
    BaseUserInfo getUserInfoByRequest(HttpServletRequest request);

    /**
     *  通过HttpServletRequest获取用户角色信息
     * @param request
     * @return
     */
    BaseUserRole getUserRoleByRequest(HttpServletRequest request);


}
