package com.sunbox.sdpadmin.controller;

import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpadmin.core.model.AdminRole;
import com.sunbox.sdpadmin.core.model.AdminUser;
import com.sunbox.sdpadmin.core.util.CookieUtil;
import com.sunbox.sdpadmin.service.AdminPermissionService;
import com.sunbox.sdpadmin.service.AdminRoleService;
import com.sunbox.sdpadmin.service.impl.LoginServiceImpl;
import com.sunbox.sdpadmin.service.impl.AdminUserServiceImpl;
import com.sunbox.sdpadmin.util.RedisUtil;
import com.sunbox.web.BaseCommon;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping("/admin/adminmng")
public class AdminUserController extends BaseCommon implements BaseCommonInterFace {

    @Autowired
    private AdminUserServiceImpl adminUserService;
    @Autowired
    private AdminRoleService adminRoleService;
    @Autowired
    private AdminPermissionService adminPermissionService;
    @Resource
    private RedisUtil redisUtil;

    @RequestMapping("/userpage")
    public String index(Model model) {

        return "/admin/authmng/adminUser";
    }

    @RequestMapping(value = "/createUser", method = RequestMethod.GET)
    @ResponseBody
    public ResultMsg createAdminUser(AdminUser adminUser) {
        ResultMsg resultMsg = new ResultMsg();
        if (adminUser == null || StringUtils.isEmpty(adminUser.getUsername())
                ) {
            resultMsg.setResult(false);
            resultMsg.setMsg("失败！");
            return resultMsg;
        }
        if (StringUtils.isEmpty(adminUser.getPassword())) {
            adminUser.setPassword(adminUser.getUsername());
        }
        adminUserService.save(adminUser);
        resultMsg.setResult(true);
        resultMsg.setMsg("成功！");
        return resultMsg;
    }

    @RequestMapping(value = "/updateUser", method = RequestMethod.GET)
    @ResponseBody
    public ResultMsg updateAdminUser(String id, String account) {

        ResultMsg resultMsg = new ResultMsg();
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(account)) {

            resultMsg.setResult(false);
            resultMsg.setMsg("参数错误！");
            return resultMsg;
        }

        adminUserService.update(id, account);

        resultMsg.setResult(true);
        resultMsg.setMsg("成功！");

        return resultMsg;
    }

    @RequestMapping(value = "/disableUser", method = RequestMethod.GET)
    @ResponseBody
    public ResultMsg disableAdminUser(HttpServletRequest request) {
        String id = request.getParameter("id");
        ResultMsg resultMsg = new ResultMsg();
        if (StringUtils.isEmpty(id)) {
            resultMsg.setResult(false);
            resultMsg.setMsg("参数错误！");
            return resultMsg;
        }
        adminUserService.disable(id);
        resultMsg.setResult(true);
        resultMsg.setMsg("成功！");

        return resultMsg;
    }

    @RequestMapping(value = "/enabledUser", method = RequestMethod.GET)
    @ResponseBody
    public ResultMsg enabledAdminUser(HttpServletRequest request) {
        String id = request.getParameter("id");
        ResultMsg resultMsg = new ResultMsg();
        if (StringUtils.isEmpty(id)) {
            resultMsg.setResult(false);
            resultMsg.setMsg("参数错误！");
            return resultMsg;
        }
        adminUserService.enabled(id);
        resultMsg.setResult(true);
        resultMsg.setMsg("成功！");

        return resultMsg;
    }

    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    @ResponseBody
    public ResultMsg resetPassword(String id) {

        ResultMsg resultMsg = new ResultMsg();

        if (StringUtils.isEmpty(id)) {
            resultMsg.setResult(false);
            resultMsg.setMsg("参数错误！");
            return resultMsg;
        }

//        adminUserService.resetPassword(id);
//
        resultMsg.setResult(true);
        resultMsg.setMsg("成功！");

        return resultMsg;
    }

    @RequestMapping(value = "/queryUser", method = RequestMethod.GET)
    @ResponseBody
    public ResultMsg QueryAdminUser(HttpServletRequest request) {
        // int page, int pageSize, String name, String tel
        String username = request.getParameter("username");
        String tel = request.getParameter("tel");
        String page = request.getParameter("page");
        String status = request.getParameter("status");
        int num = 0;
        if (page == null) {
            num = 1;
        } else {
            num = Integer.valueOf(request.getParameter("page"));
        }
        String size = request.getParameter("pageSize");
        int pageSize = 5;
        if (size != null) {
            pageSize = Integer.valueOf(size);
        }

        ResultMsg resultMsg = new ResultMsg();


        Map result = adminUserService.pageList(num, pageSize, username, tel, status);

        resultMsg.setRows((List<AdminUser>) result.get("list"));
        resultMsg.setTotal(Long.valueOf(result.get("count").toString()));
        resultMsg.setResult(true);
        resultMsg.setMsg("成功");

        return resultMsg;
    }

    @RequestMapping(value = "/assignRole", method = RequestMethod.POST)
    @ResponseBody
    public ResultMsg assignRole(String userId, List<String> roleIds) {

        ResultMsg resultMsg = new ResultMsg();
        if (StringUtils.isEmpty(userId)
                || roleIds == null
                || roleIds.size() <= 0) {

            resultMsg.setResult(false);
            resultMsg.setMsg("参数错误！");
            return resultMsg;
        }

        return adminUserService.assignRole(userId, roleIds);
    }

    @RequestMapping(value = "getRoleList", method = RequestMethod.GET)
    @ResponseBody
    public ResultMsg getRoleList(String userId) {

        ResultMsg resultMsg = new ResultMsg();
        if (StringUtils.isEmpty(userId)) {
            resultMsg.setResult(false);
            resultMsg.setMsg("参数错误！");
            return resultMsg;
        }

        List<Map<String, Object>> roleList = adminUserService.getRoleListByUserId(userId);
        resultMsg.setResult(true);
        resultMsg.setRows(roleList);
        resultMsg.setMsg("成功！");

        return resultMsg;
    }

    @RequestMapping("/adminrolepage")
    public String adminrolepage(Model model) {

        return "/admin/authmng/adminRole";
    }

    @ResponseBody
    @RequestMapping("queryallrole")
    public ResultMsg queryallrole(HttpServletRequest request) {
        String role = request.getParameter("role");
        String status = request.getParameter("status");
        String page = request.getParameter("page");
        String pageSize = request.getParameter("pageSize");
        Integer pagenum = 0;
        if (page != null) {
            pagenum = Integer.valueOf(page);
        }
        Integer size = 0;
        if (pageSize != null) {
            size = Integer.valueOf(pageSize);
        }
        ResultMsg resultMsg = new ResultMsg();


        Map result = adminRoleService.pageList(pagenum, size, role, status);

        resultMsg.setRows((List<AdminUser>) result.get("list"));
        resultMsg.setTotal(Long.valueOf(result.get("count").toString()));
        resultMsg.setResult(true);
        resultMsg.setMsg("成功");

        return resultMsg;
    }

    @RequestMapping("addrole")
    @ResponseBody
    public ResultMsg addrole(String role, String alias) {
        ResultMsg resultMsg = new ResultMsg();
        if (role == null || alias == null) {
            resultMsg.setResult(false);
            resultMsg.setMsg("新增失败");
            return resultMsg;
        }

        return adminRoleService.save(role, alias);
    }

    @RequestMapping("editrole")
    @ResponseBody
    public ResultMsg editrole(String role, String alias, String id) {
        AdminRole adminrole = new AdminRole();
        adminrole.setId(id);
        adminrole.setRole(role);
        adminrole.setAlias(alias);
        ResultMsg msg = new ResultMsg();
        if (role == null || alias == null || id == null) {
            msg.setResult(false);
            msg.setMsg("缺少参数");
            return msg;
        }
        return this.adminRoleService.update(adminrole);
    }

    //enabledRole disableRole
    @RequestMapping("enabledRole")
    @ResponseBody
    public ResultMsg enabledRole(String roleid) {
        ResultMsg resultMsg = new ResultMsg();
        if (roleid == null) {
            resultMsg.setResult(false);
            resultMsg.setMsg("缺少参数！");
            return resultMsg;
        }

        return adminRoleService.enabled(roleid);
    }

    @RequestMapping("disableRole")
    @ResponseBody
    public ResultMsg disableRole(String roleid) {
        ResultMsg resultMsg = new ResultMsg();
        if (roleid == null) {
            resultMsg.setResult(false);
            resultMsg.setMsg("缺少参数！");
            return resultMsg;
        }

        return adminRoleService.disable(roleid);
    }

    @RequestMapping("permissionpage")
    public String permissionpage(Model model) {

        return "/admin/authmng/adminPermission";
    }

    @RequestMapping(value = "getPermissionTreeData", method = RequestMethod.POST)
    @ResponseBody
    public ResultMsg getPermissionTreeData(HttpServletRequest request, String status, String rolid) {
        ResultMsg msg = new ResultMsg();
        String userid = "";
        String token = CookieUtil.getValue(request, LoginServiceImpl.LOGIN_ADMIN_IDENTITY_KEY);
        if (StringUtils.isNotEmpty(token)) {
            String user = redisUtil.getValue(token);
            if (StringUtils.isNotEmpty(user)) {
                JSONObject jsonObject = JSONObject.parseObject(user);
                userid = jsonObject.getString("id");
            }
        }
        if (StringUtils.isEmpty(userid)) {
            msg.setResult(false);
            msg.setMsg("登录超时，请重新登录!");
            return msg;
        }
        String nodelist = this.adminPermissionService.getPermissionTreeData(status, rolid, userid);
        msg.setResult(true);
        msg.setData(nodelist.replaceAll("\"nodes\":\\[],", ""));
        return msg;
    }

    // addadminPermission name alias menu url
    @RequestMapping("addadminPermission")
    @ResponseBody
    public ResultMsg addadminPermission(String pid, String name, String alias, String menu, String url, String status, Integer sortIndex, String icon) {
        return this.adminPermissionService.save(pid, name, alias, menu, url, status, sortIndex, icon);
    }

    @ResponseBody
    @RequestMapping("getPermissionById")
    public ResultMsg getPermissionById(String nodeid) {
        return this.adminPermissionService.getPermissionByid(nodeid);

    }

    @ResponseBody
    @RequestMapping("editadminPermission")
    public ResultMsg editadminPermission(String id, String name, String alias, String menu, String url, String status, Integer sortIndex, String pid, String icon) {
        return this.adminPermissionService.update(id, name, alias, menu, url, status, sortIndex, pid, icon);
    }

    @ResponseBody
    @RequestMapping("changePermissionStatus")
    public ResultMsg changePermissionStatus(String id, String status) {

        return this.adminPermissionService.updatePermissionStatus(id, status);
    }

    @RequestMapping(value = "getRolePermissionTree", method = RequestMethod.POST)
    @ResponseBody
    public ResultMsg getRolePermissionTree(String roleid) {
        String nodelist = this.adminPermissionService.getRolePermissionTreeData(roleid);
        ResultMsg msg = new ResultMsg();
        msg.setResult(true);
        msg.setData(nodelist);
        return msg;
    }

    @ResponseBody
    @RequestMapping(value = "editRolePermission", method = RequestMethod.POST)
    public ResultMsg editRolePermission(HttpServletRequest request) {
        String roleid = request.getParameter("roleid");
        String pids = request.getParameter("pids");
        ResultMsg msg = new ResultMsg();
        if (StringUtils.isEmpty(roleid)) {
            msg.setResult(false);
            msg.setMsg("参数错误");
            return msg;
        } else {
            List<String> pid = new ArrayList<String>();
            if (pids.length() > 3) {
                String t4 = pids.substring(0, pids.length() - 1);
                String temp[] = t4.split("#");
                for (String ts : temp) {
                    pid.add(ts);
                }
            }


            return adminRoleService.assignPremission(roleid, pid);
        }

    }

    @RequestMapping("getUserRoleTree")
    @ResponseBody
    public ResultMsg getUserRoleTree(String userid) {
        return this.adminUserService.getUserRoleTree(userid);
    }

    @ResponseBody
    @RequestMapping("editUserRole")
    public ResultMsg editUserRole(String userid, String roleids) {
        ResultMsg msg = new ResultMsg();
        if (StringUtils.isEmpty(userid) || StringUtils.isEmpty(roleids)) {
            msg.setResult(false);
            msg.setMsg("缺少参数");
            return msg;
        } else {
            List<String> roles = Arrays.asList(roleids.substring(0, roleids.length() - 1).split("#"));
            return this.adminUserService.assignRole(userid, roles);
        }
    }

    @ResponseBody
    @RequestMapping("dealResetpasword")
    public ResultMsg dealResetpasword(String userid, String repasswd) {
        return this.adminUserService.resetPassword(userid, repasswd);
    }

    @ResponseBody
    @RequestMapping("getPermission4ZTreeData")
    public ResultMsg getPermission4ZTreeData(String status) {
        ResultMsg msg = new ResultMsg();
        msg.setResult(true);
        msg.setData(this.adminPermissionService.getPermission4ZTreeData(status));

        return msg;
    }

    @ResponseBody
    @RequestMapping("getCurrentUsername")
    public ResultMsg getCurrentUsername(HttpServletRequest request) {
        ResultMsg msg = new ResultMsg();
        String token = CookieUtil.getValue(request, LoginServiceImpl.LOGIN_ADMIN_IDENTITY_KEY);
        if (StringUtils.isNotEmpty(token)) {
            String user = redisUtil.getValue(token);
            if (StringUtils.isNotEmpty(user)) {
                JSONObject jsonObject = JSONObject.parseObject(user);
                msg.setResult(true);
                msg.setData(jsonObject.getString("username"));
                return msg;
            }
        }
        msg.setResult(true);
        msg.setData("admin");
        return msg;
    }


    /**
     * 删除节点
     *
     * @param id
     * @return ResultMsg
     * @anther hzm
     * @Date 2020/4/26 13:30
     */
    @RequestMapping("deladminPermission")
    @ResponseBody
    public ResultMsg delAdminPermission(String id) {
        ResultMsg resultMsg = adminPermissionService.delAdminPermission(id);
        return resultMsg;
    }

    /**
     * 校验用户是否重复
     */
    @RequestMapping(value = "/validateUser", method = RequestMethod.GET)
    @ResponseBody
    public ResultMsg validateUser(HttpServletRequest request) {
        String userName = request.getParameter("username");
        AdminUser adminUser = adminUserService.getAdminUserByName(userName);
        ResultMsg resultMsg = new ResultMsg();
        if (adminUser == null) {
            resultMsg.setResult(false);
            resultMsg.setMsg("failed");
        } else {
            resultMsg.setResult(true);
            resultMsg.setMsg("success");
        }
        return resultMsg;
    }

    /**
     * 删除用户和用户角色关联关系
     */
    @RequestMapping(value = "/deleteUser", method = RequestMethod.GET)
    @ResponseBody
    public ResultMsg delAdminUser(HttpServletRequest request) {
        String id = request.getParameter("id");
        ResultMsg resultMsg = new ResultMsg();
        if (StringUtils.isEmpty(id)) {
            resultMsg.setResult(false);
            resultMsg.setMsg("参数错误！");
            return resultMsg;
        }
        adminUserService.delete(id);
        resultMsg.setResult(true);
        resultMsg.setMsg("成功！");
        return resultMsg;
    }

    /**
     * 查询所有菜单
     *
     * @param status 是否可用
     * @param roleid 角色id
     * @param pid    父id
     * @return 菜单集合
     */
    @RequestMapping(value = "getPermissionList", method = RequestMethod.GET)
    @ResponseBody
    public ResultMsg getPermissionList(String status, String roleid, String pid) {
        return adminPermissionService.adminPermissionService(status, roleid, pid);
    }

    /**
     * 查询所有图标
     * @param
     * @Author GaoZhen
     * @Date 2020/9/15 0015
     * @return
     */
    @RequestMapping(value = "queryIconList", method = RequestMethod.POST)
    @ResponseBody
    public ResultMsg queryIconList() {
        return adminPermissionService.queryIconList();
    }

    /**
     * 上传图片
     * @param file
     * @return
     */
    @ResponseBody
    @RequestMapping("/uploadIndexImg")
    public String uploadIndexImg(@RequestParam("txt_file") MultipartFile file) {

//        // 获取项目路径
//        File directory = new File("");// 参数为空
//        String courseFile = null;
//        try {
//            courseFile = directory.getCanonicalPath();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        String resourcesPath = this.getClass().getClassLoader().getResource("resources").getPath();
        //getLogger().info("uploadIndexImg=====================>resourcesPath:"+resourcesPath);

        return null;
//        Map<String, Object> map = new HashMap<>();
//        if (file != null) {
//            // 原文件名
//            String originalFileName = file.getOriginalFilename();
//            // 文件后缀
//            String suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
//            // 新文件名
//            String fileNewName = UUID.randomUUID() + suffix;
//            //E:\workspace\igscloud\igs-admin\src\main\resources\public\adminlte\bower_components\bootstrap\img\img8.png
//            String path = courseFile+"/igs-admin/src/main/resources/public/adminlte/bower_components/bootstrap/img/";
//            File localFile = new File(path);
//            File dir = new File(path, fileNewName);
//            //判断上传路径存不存在不存在就创建
//            if (!localFile.exists()){
//                localFile.mkdirs();
//            }
//            try {
//                file.transferTo(dir);
//                map.put("success", "上传成功！");
//                return JSON.toJSONString(map);
//            } catch (IllegalStateException e) {
//                map.put("error", "图片不合法");
//                return JSON.toJSONString(map);
//            } catch (IOException e) {
//                map.put("error", "图片不合法");
//                return JSON.toJSONString(map);
//            }
//        } else {
//            map.put("error", "图片不合法");
//            return JSON.toJSONString(map);
//        }
    }


}
