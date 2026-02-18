package com.sunbox.sdpadmin.controller;

import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpadmin.core.model.AdminPermission;
import com.sunbox.sdpadmin.core.util.CookieUtil;
import com.sunbox.sdpadmin.service.impl.LoginServiceImpl;
import com.sunbox.sdpadmin.service.impl.AdminPermissionServiceImpl;
import com.sunbox.sdpadmin.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/permission")
public class AdminPermissionController {

    @Resource
    private AdminPermissionServiceImpl permissionService;

    @Resource
    private RedisUtil redisUtil;

    @RequestMapping(value = "/createPermission", method = RequestMethod.POST)
    @ResponseBody
    public ResultMsg createPermission(AdminPermission permission) {

        ResultMsg resultMsg = new ResultMsg();

        if (permission == null || StringUtils.isEmpty(permission.getName())) {
              resultMsg.setResult(false);
              resultMsg.setMsg("参数错误");
              return resultMsg;
        }

//        int result = permissionService.save(permission);
        int result =0;
        if (result <= 0) {
            resultMsg.setResult(false);
            resultMsg.setMsg("创建失败");
            return resultMsg;
        }

        resultMsg.setResult(true);
        resultMsg.setMsg("创建成功");

        return resultMsg;
    }

    @RequestMapping(value = "/updatePermission", method = RequestMethod.POST)
    @ResponseBody
    public ResultMsg updatePermission(AdminPermission permission) {

        ResultMsg resultMsg = new ResultMsg();

        if (permission == null
                || StringUtils.isEmpty(permission.getId())
                || StringUtils.isEmpty(permission.getName())) {
            resultMsg.setResult(false);
            resultMsg.setMsg("参数错误");
            return resultMsg;
        }

//        int result = permissionService.update(permission);
        int result=0;
        if (result <= 0) {
            resultMsg.setResult(false);
            resultMsg.setMsg("创建失败");
            return resultMsg;
        }

        resultMsg.setResult(true);
        resultMsg.setMsg("修改成功");

        return resultMsg;
    }

    @RequestMapping(value = "/disablePermission", method = RequestMethod.POST)
    @ResponseBody
    public ResultMsg disablePermission(String id) {

        ResultMsg resultMsg = new ResultMsg();

        if (StringUtils.isEmpty(id)) {
            resultMsg.setResult(false);
            resultMsg.setMsg("参数错误");
            return resultMsg;
        }

        int result = permissionService.disable(id);
        if (result <= 0) {
            resultMsg.setResult(false);
            resultMsg.setMsg("禁用失败");
            return resultMsg;
        }

        resultMsg.setResult(true);
        resultMsg.setMsg("禁用成功");

        return resultMsg;
    }

    @RequestMapping(value = "/enabledPermission", method = RequestMethod.POST)
    @ResponseBody
    public ResultMsg enabledPermission(String id) {

        ResultMsg resultMsg = new ResultMsg();

        if (StringUtils.isEmpty(id)) {
            resultMsg.setResult(false);
            resultMsg.setMsg("参数错误");
            return resultMsg;
        }

        int result = permissionService.enabled(id);
        if (result <= 0) {
            resultMsg.setResult(false);
            resultMsg.setMsg("启用失败");
            return resultMsg;
        }

        resultMsg.setResult(true);
        resultMsg.setMsg("启用成功");

        return resultMsg;
    }

    @RequestMapping(value = "/queryPermission", method = RequestMethod.GET)
    @ResponseBody
    public ResultMsg queryPermission(int page, int pageSize, String name) {
        ResultMsg resultMsg = new ResultMsg();
        if (page <= 0 || pageSize <= 0) {
            resultMsg.setResult(false);
            resultMsg.setMsg("参数错误！");
            return resultMsg;
        }

        Map<String, Object> result = permissionService.queryPermission(page, pageSize, name);
        resultMsg.setRows((List<AdminPermission>) result.get("list"));
        resultMsg.setTotal((Long) result.get("count"));
        resultMsg.setResult(true);
        resultMsg.setMsg("成功");

        return resultMsg;
    }

    @RequestMapping(value = "/queryMenu", method = RequestMethod.GET)
    @ResponseBody
    public ResultMsg queryMenu(HttpServletRequest request,String pid) {
        ResultMsg resultMsg = new ResultMsg();
        String token = CookieUtil.getValue(request, LoginServiceImpl.LOGIN_ADMIN_IDENTITY_KEY);
        if(StringUtils.isNotEmpty(token)){
            String user = redisUtil.getValue(token);
            if(StringUtils.isNotEmpty(user)){
                JSONObject jsonObject = JSONObject.parseObject(user);
                String userid = jsonObject.getString("id");
                //根据用户id查询用户权限
                Map<String,Object> map = new HashMap<>();
                map.put("userid",userid);
                map.put("pid",pid);
                resultMsg = permissionService.selectMenuByPid(map);
                return resultMsg;
            }
        }
        resultMsg.setResult(false);
        resultMsg.setMsg("登录用户信息不存在!");
        return resultMsg;
    }


    @RequestMapping(value = "/queryMenuWithJson", method = RequestMethod.GET)
    @ResponseBody
    public ResultMsg queryMenuWithJson(HttpServletRequest request) {
        ResultMsg resultMsg = new ResultMsg();
       /* String token = CookieUtil.getValue(request, LoginService.LOGIN_ADMIN_IDENTITY_KEY);
        if(StringUtils.isNotEmpty(token)){
            String user = redisUtil.getValue(token);
            if(StringUtils.isNotEmpty(user)){
                JSONObject jsonObject = JSONObject.parseObject(user);
                String userid = jsonObject.getString("id");
                //根据用户id查询用户权限
                Map<String,Object> map = new HashMap<>();
                map.put("userid",userid);
                resultMsg = permissionService.selectMenuByPid(map);
                return resultMsg;
            }
        }*/
       String userid="f957994e-53cf-490e-839e-6dccae7bfac9";
        Map<String,Object> map = new HashMap<>();
        map.put("userid",userid);
        resultMsg = permissionService.queryMenuWithJson(map);
        return resultMsg;
        /*resultMsg.setResult(false);
        resultMsg.setMsg("登录用户信息不存在!");
        return resultMsg;*/
    }
}
