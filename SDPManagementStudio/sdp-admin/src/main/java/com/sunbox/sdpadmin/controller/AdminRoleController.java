package com.sunbox.sdpadmin.controller;

import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpadmin.core.model.AdminRole;
import com.sunbox.sdpadmin.service.AdminRoleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/adminrole")
public class AdminRoleController {

    @Resource
    private AdminRoleService roleService;

    @RequestMapping(value = "/createRole", method = RequestMethod.POST)
    @ResponseBody
    public ResultMsg createRole(AdminRole role) {

        ResultMsg resultMsg = new ResultMsg();
        if (role == null || StringUtils.isEmpty(role.getRole())) {
            resultMsg.setResult(false);
            resultMsg.setMsg("参数错误！");
            return resultMsg;
        }

        role.setId(UUID.randomUUID().toString());
        role.setStatus(1);
        role.setCreateDate(new Date());

        int result =0;
        if (result <= 0) {
            resultMsg.setResult(false);
            resultMsg.setMsg("创建失败！");
            return resultMsg;
        }

        resultMsg.setResult(true);
        resultMsg.setMsg("成功！");

        return resultMsg;
    }

    @RequestMapping(value = "/updateRole", method = RequestMethod.POST)
    @ResponseBody
    public ResultMsg updateRole(AdminRole role) {

        ResultMsg resultMsg = new ResultMsg();
        if (role == null
                || StringUtils.isEmpty(role.getId())
                || StringUtils.isEmpty(role.getRole())) {
            resultMsg.setResult(false);
            resultMsg.setMsg("参数错误！");
            return resultMsg;
        }

        int result = 0;

        if (result <= 0) {
            resultMsg.setResult(false);
            resultMsg.setMsg("创建失败！");
            return resultMsg;
        }

        resultMsg.setResult(true);
        resultMsg.setMsg("成功！");

        return resultMsg;
    }

    @RequestMapping(value = "/disableRole", method = RequestMethod.POST)
    @ResponseBody
    public ResultMsg disableRole(String id) {

        ResultMsg resultMsg = new ResultMsg();
        if (StringUtils.isEmpty(id)) {
            resultMsg.setResult(false);
            resultMsg.setMsg("参数错误！");
            return resultMsg;
        }

        int result = 0;

        if (result <= 0) {
            resultMsg.setResult(false);
            resultMsg.setMsg("创建失败！");
            return resultMsg;
        }

        resultMsg.setResult(true);
        resultMsg.setMsg("成功！");

        return resultMsg;
    }

    @RequestMapping(value = "/enabledRole", method = RequestMethod.POST)
    @ResponseBody
    public ResultMsg enabledRole(String id) {

        ResultMsg resultMsg = new ResultMsg();
        if (StringUtils.isEmpty(id)) {
            resultMsg.setResult(false);
            resultMsg.setMsg("参数错误！");
            return resultMsg;
        }

        int result = 0;

        if (result <= 0) {
            resultMsg.setResult(false);
            resultMsg.setMsg("创建失败！");
            return resultMsg;
        }

        resultMsg.setResult(true);
        resultMsg.setMsg("成功！");

        return resultMsg;
    }

    @RequestMapping(value = "/queryRole", method = RequestMethod.GET)
    @ResponseBody
    public ResultMsg queryRole(int page, int pageSize, String alias, String role) {

        ResultMsg resultMsg = new ResultMsg();
        if (page <= 0 || pageSize <= 0) {
            resultMsg.setResult(false);
            resultMsg.setMsg("参数错误！");
            return resultMsg;
        }

        Map<String, Object> result = roleService.pageList(page, pageSize, alias, role);
        resultMsg.setRows((List<AdminRole>) result.get("list"));
        resultMsg.setTotal((Long) result.get("count"));
        resultMsg.setResult(true);
        resultMsg.setMsg("成功");

        return resultMsg;
    }

    @RequestMapping(value = "/assignPermission", method = RequestMethod.POST)
    @ResponseBody
    public ResultMsg assignPermission(String roleId, List<String> permissionIds) {

        ResultMsg resultMsg = new ResultMsg();
        if (StringUtils.isEmpty(roleId) || permissionIds == null || permissionIds.size() <= 0) {
            resultMsg.setResult(false);
            resultMsg.setMsg("参数错误！");
            return resultMsg;
        }

//        int result = roleService.assignPremission(roleId, permissionIds);
        int result=0;
        if (result <= 0) {
            resultMsg.setResult(false);
            resultMsg.setMsg("指派失败");
            return resultMsg;
        }

        resultMsg.setResult(true);
        resultMsg.setMsg("成功！");

        return resultMsg;
    }
}
