package com.sunbox.sdpadmin.controller;

import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpadmin.controller.annotation.PermissionLimit;
import com.sunbox.sdpadmin.model.admin.request.BaseUserInfoRequest;
import com.sunbox.sdpadmin.model.admin.request.LoginData;
import com.sunbox.sdpadmin.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class LoginController {

    @Autowired
    private LoginService loginService;

    @RequestMapping(value = "/admin/login")
    @ResponseBody
    public ResultMsg login(HttpServletRequest request, HttpServletResponse response, @RequestBody LoginData loginData) {
        ResultMsg resultMsg = loginService.login(request, response, loginData);
        return resultMsg;
    }

    @RequestMapping(value = "/admin/logout")
    @ResponseBody
    public ResultMsg logout(HttpServletRequest request, HttpServletResponse response) {
        ResultMsg resultMsg = loginService.userLogout(request, response);
        return resultMsg;
    }

    /**
     * 获取全部用户信息
     */
    @RequestMapping(value = "/admin/userList")
    @PermissionLimit(role = {"Administrator"})
    @ResponseBody
    public ResultMsg getUserList(@RequestBody BaseUserInfoRequest baseUserInfoRequest) {
        ResultMsg resultMsg = loginService.getUserList(baseUserInfoRequest);
        return resultMsg;
    }

    /**
     * 创建用户
     */
    @RequestMapping(value = "/admin/createUser", method = RequestMethod.POST)
    @PermissionLimit(role = {"Administrator"})
    @ResponseBody
    public ResultMsg createUser(@RequestBody BaseUserInfoRequest baseUserInfo) {
        ResultMsg resultMsg = loginService.createUser(baseUserInfo);
        return resultMsg;
    }

    /**
     * 修改密码(修改用户信息)
     */
    @RequestMapping(value = "/admin/updatePassword")
    @PermissionLimit(role = {"Administrator"})
    @ResponseBody
    public ResultMsg updatePassword(@RequestBody LoginData loginData) {
        ResultMsg resultMsg = loginService.updatePassword(loginData);
        return resultMsg;
    }

    /**
     * 删除用户
     */
    @RequestMapping(value = "/admin/deleteUser")
    @PermissionLimit(role = {"Administrator"})
    @ResponseBody
    public ResultMsg deleteUser(@RequestBody LoginData loginData) {
        ResultMsg resultMsg = loginService.deleteUser(loginData.getUserName());
        return resultMsg;
    }

    /**
     * 根据cookie获取用户信息
     */
    @RequestMapping(value = "/admin/getUserInfoByCookie")
    @ResponseBody
    public ResultMsg getUserInfoByCookie(HttpServletRequest request, HttpServletResponse response) {
        ResultMsg resultMsg = loginService.getUserInfoByCookie(request, response);
        return resultMsg;
    }

}
