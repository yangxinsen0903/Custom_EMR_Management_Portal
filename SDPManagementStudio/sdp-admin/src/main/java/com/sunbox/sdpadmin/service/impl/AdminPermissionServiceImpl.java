package com.sunbox.sdpadmin.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpadmin.core.model.AdminPermission;
import com.sunbox.sdpadmin.core.model.AdminRolePermission;
import com.sunbox.sdpadmin.mapper.AdminPermissionDao;
import com.sunbox.sdpadmin.mapper.AdminRolePermissionDao;
import com.sunbox.sdpadmin.model.MenuModel;
import com.sunbox.sdpadmin.service.AdminPermissionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminPermissionServiceImpl implements AdminPermissionService {

    @Resource
    private AdminPermissionDao permissionDao;
    @Resource
    private AdminRolePermissionDao adminRolePermissionDao;


    @Override
    public ResultMsg save(String pid, String name, String alias, String menu, String url, String status, Integer sortIndex, String icon) {
        if (StringUtils.isEmpty(pid)) {
            pid = "9";
        }
        ResultMsg msg = new ResultMsg();
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(alias) || StringUtils.isEmpty(menu) || StringUtils.isEmpty(pid)) {
            msg.setResult(false);
            msg.setMsg("参数的错误");
            return msg;
        } else {
            AdminPermission record = new AdminPermission();
            if (sortIndex == null) {
                int sortIndex1 = permissionDao.getSortIndex();
//                if(sortIndex1 == 0){
                record.setSortIndex(sortIndex1 + 1);
//                }else {
//                    record.setSortIndex(sortIndex1);
//                }
            } else {
                int i = permissionDao.selectSortIndex(sortIndex);
                if (i > 0) {//之前有这个序号，这个序号之后全部加1
                    int rowCount = permissionDao.updateSortIndex(sortIndex);
                    if (rowCount > 0) {
                        record.setSortIndex(sortIndex);
                    } else {
                        msg.setResult(false);
                        msg.setMsg("新增失败");
                        return msg;
                    }
                } else {
                    record.setSortIndex(sortIndex);
                }
            }

            record.setId(UUID.randomUUID().toString().replaceAll("-", ""));
            record.setCreatedate(new Date());
            record.setName(name);
            record.setAlias(alias);
            record.setUrl(url);
            record.setMenu(Integer.valueOf(menu));
            record.setStatus(StringUtils.isNotEmpty(status) ? Integer.parseInt(status) : 1);
            record.setPid(pid);
            record.setReqesttype(1);
            record.setIcon(icon);
            int a = this.permissionDao.save(record);
            if (a <= 0) {
                msg.setResult(false);
                msg.setMsg("新增失败");
                return msg;
            } else {
                msg.setResult(true);
                msg.setMsg("新增成功");
                return msg;
            }
        }
    }

    @Override
    public ResultMsg update(String id, String name, String alias, String menu, String url, String status, Integer sortIndex, String pid, String icon) {
        ResultMsg msg = new ResultMsg();
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(alias) || StringUtils.isEmpty(menu)) {
            msg.setResult(false);
            msg.setMsg("参数的错误");
            return msg;
        } else {
            AdminPermission record = new AdminPermission();
            int i = permissionDao.selectSortIndex(sortIndex);
            if (i > 0) {//之前有这个序号，这个序号之后全部加1
                int rowCount = permissionDao.updateSortIndex(sortIndex);
                if (rowCount > 0) {
                    record.setSortIndex(sortIndex);
                } else {
                    msg.setResult(false);
                    msg.setMsg("更新失败");
                    return msg;
                }
            } else {
                record.setSortIndex(sortIndex);
            }
            record.setId(id);
            record.setName(name);
            record.setAlias(alias);
            record.setUrl(url);
            record.setMenu(Integer.valueOf(menu));
            record.setStatus(StringUtils.isNotEmpty(status) ? Integer.parseInt(status) : 1);
            record.setPid(pid);
            record.setIcon(icon);
            int a = this.permissionDao.update(record);
            if (a <= 0) {
                msg.setResult(false);
                msg.setMsg("更新失败");
                return msg;
            } else {
                msg.setResult(true);
                msg.setMsg("更新成功");
                return msg;
            }

        }
    }

    @Override
    public int disable(String id) {

        AdminPermission permission = permissionDao.getPermissionById(id);

        permission.setStatus(0);

        return permissionDao.update(permission);
    }

    @Override
    public int enabled(String id) {
        AdminPermission permission = permissionDao.getPermissionById(id);

        permission.setStatus(1);

        return permissionDao.update(permission);
    }

    @Override
    public List<Map<String, Object>> getPermissionList() {

        List<AdminPermission> adminPermissions = permissionDao.getPermissionList(null, null, null);
        List<AdminPermission> pList = adminPermissions.stream()
                .filter(permission -> StringUtils.isEmpty(permission.getPid()))
                .collect(Collectors.toList());

        List<AdminPermission> cList = adminPermissions.stream()
                .filter(permission -> !StringUtils.isEmpty(permission.getPid()))
                .collect(Collectors.toList());

        List<Map<String, Object>> maps = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        for (AdminPermission permission : pList) {
            for (AdminPermission adminPermission : cList) {
                if (permission.getId().equals(adminPermission.getPid())) {
                    map.put("permission", permission);
                    map.put("child", getChild(permission.getId(), cList));
                    maps.add(map);
                }
            }
        }
        return maps;
    }

    private List<Map<String, Object>> getChild(String id, List<AdminPermission> cList) {
        List<Map<String, Object>> maps = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        for (AdminPermission permission : cList) {
            if (id.equals(permission.getPid())) {
                map.put("permission", permission);
                map.put("child", getChild(permission.getId(), cList));
                maps.add(map);
            }
        }
        return maps;
    }

    @Override
    public Map<String, Object> queryPermission(int page, int pageSize, String name) {
        List<AdminPermission> list = permissionDao.pageList(page, pageSize, name);
        int count = permissionDao.pageCount(page, pageSize, name);

        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("count", count);
        return map;
    }

    public String getPermissionTreeDataBak(String status, String roleid) {

        List<AdminPermission> perlist = this.permissionDao.getPermissionList(status, roleid, null);
        JSONObject obj = new JSONObject();
        for (AdminPermission record : perlist) {
            if (record.getPid().equals("0")) {
                obj.put("text", record.getName());
                obj.put("id", record.getId());
                obj.put("selectable", "false");
                obj.put("icon", "glyphicon glyphicon-stop");
                break;
            }
        }
        if (!obj.isEmpty()) {
            obj.put("nodes", putchildnode(obj.get("id").toString(), perlist));
        }
        return obj.toJSONString();
    }

    @Override
    public String getPermissionTreeData(String status, String roleid, String userid) {
        //List<AdminPermission> perlist=this.permissionDao.getPermissionListByUserid(status,userid);
        List<AdminPermission> perlist = this.permissionDao.getPermissionList(status, roleid, "");
        List<JSONObject> list = new ArrayList<>();
        for (AdminPermission record : perlist) {
            JSONObject obj = new JSONObject();
            if (record.getPid().equals("9")) {
                obj.put("text", record.getName());
                obj.put("id", record.getId());
                obj.put("selectable", "false");
                obj.put("icon", "glyphicon glyphicon-stop");
                list.add(obj);
            }
        }
        if (list.size() > 0) {
            for (JSONObject model : list) {
                model.put("nodes", putchildnode(model.get("id").toString(), perlist));
            }
        }
        return JSON.toJSON(list).toString();
    }

    private JSONArray putchildnode(String nodeid, List<AdminPermission> nodelist) {
        JSONArray array = new JSONArray();
        for (AdminPermission tt : nodelist) {
            if (tt.getPid().equals(nodeid)) {
                JSONObject obj = new JSONObject();
                obj.put("text", tt.getName());
                obj.put("id", tt.getId());
                obj.put("icon", "glyphicon glyphicon-stop");
                if (tt.getStatus() == 0) {
                    obj.put("backColor", "#a1a3a6");
                }
                obj.put("selectable", "false");

                obj.put("nodes", putchildnode(tt.getId(), nodelist));
                array.add(obj);
            }
        }
        return array;
    }

    @Override
    public ResultMsg getPermissionByid(String id) {
        ResultMsg msg = new ResultMsg();
        AdminPermission record = this.permissionDao.getPermissionById(id);
        msg.setResult(true);
        msg.setData(record);
        return msg;
    }

    @Override
    public ResultMsg updatePermissionStatus(String id, String status) {
        ResultMsg msg = new ResultMsg();
        if (id == null || status == null) {
            msg.setResult(false);
            msg.setMsg("缺少参数，更新失败");
            return msg;

        } else {
            if (status.equals("0")) {
                List<AdminPermission> perlist = this.permissionDao.getPermissionList("1", null, id);
                if (perlist.size() > 0) {
                    msg.setMsg("该节点下面还有状态正常的节点，不能停用");
                    msg.setResult(false);
                    return msg;
                }
            }

            AdminPermission record = new AdminPermission();
            record.setId(id);
            record.setStatus(Integer.valueOf(status));
            int a = this.permissionDao.update(record);
            if (a <= 0) {
                msg.setMsg("更新失败");
                msg.setResult(false);
                return msg;
            } else {
                msg.setResult(true);
                msg.setMsg("更新成功");
                return msg;
            }
        }
    }

    @Override
    public String getRolePermissionTreeData(String roleid) {
        List<AdminPermission> perlist = this.permissionDao.getPermissionList("1", "", null);
        List<AdminRolePermission> roleplist = this.adminRolePermissionDao.getRolePermissionByRoleId(roleid);

        JSONArray array = new JSONArray();
        for (AdminPermission ap : perlist) {
            JSONObject obj2 = new JSONObject();
            obj2.put("id", ap.getId());
            obj2.put("name", ap.getName());
            obj2.put("pId", ap.getPid());
            if (ap.getPid().equals("0")) {
                obj2.put("open", "true");
            }
            if (ap.getStatus() == 0) {
                obj2.put("chkDisabled", "true");
            }
            for (AdminRolePermission adrp : roleplist) {
                if (adrp.getPid().equals(ap.getId())) {
                    obj2.put("checked", true);
                    break;
                }
            }
            array.add(obj2);
        }
        return array.toJSONString();

    }

    private JSONArray putchildnode4role(String nodeid, List<AdminPermission> nodelist, List<AdminRolePermission> roleplist) {
        JSONArray array = new JSONArray();
        for (AdminPermission tt : nodelist) {
            if (tt.getPid().equals(nodeid)) {
                JSONObject obj = new JSONObject();
                obj.put("text", tt.getName());
                obj.put("id", tt.getId());
                obj.put("selectable", "true");
                obj.put("icon", "glyphicon glyphicon-stop");
                for (AdminRolePermission rolep : roleplist) {
                    if (tt.getId().equals(rolep.getPid())) {
                        JSONObject t3 = new JSONObject();
                        t3.put("checked", "true");
                        t3.put("selected", "true");
                        t3.put("expanded", "true");
                        obj.put("state", t3);
                    }
                }

                obj.put("nodes", putchildnode4role(tt.getId(), nodelist, roleplist));
                array.add(obj);
            }
        }
        return array;
    }

    @Override
    public String getPermission4ZTreeData(String status) {
        List<AdminPermission> perlist = this.permissionDao.getPermissionList(status, null, null);
        JSONArray array = new JSONArray();
        for (AdminPermission ap : perlist) {
            JSONObject obj2 = new JSONObject();
            obj2.put("id", ap.getId());
            obj2.put("name", ap.getName());
            obj2.put("pId", ap.getPid());
            if (ap.getPid().equals("root")) {
                obj2.put("open", "true");
            }
            if (ap.getStatus() == 0) {
                obj2.put("chkDisabled", "true");
            }
            array.add(obj2);
        }
        return array.toJSONString();
    }

    @Override
    public List<AdminPermission> selectByParams(Map<String, Object> map) {
        return permissionDao.selectByParams(map);
    }

    @Override
    public List<AdminPermission> selectMenu(Map<String, Object> map) {
        return permissionDao.selectMenu(map);
    }

    @Override
    public ResultMsg selectMenuByPid(Map<String, Object> map) {
        ResultMsg resultMsg = new ResultMsg();
        //判断pid是否为空
        String pid = map.get("pid").toString();
        if (StringUtils.isEmpty(pid)) {
            map.put("pid", "9");
        }
        map.put("rid", map.get("userid"));
        List<AdminPermission> menList = permissionDao.selectMenu(map);
        resultMsg.setResult(true);
        resultMsg.setMsg("获取菜单列表成功");
        resultMsg.setData(menList);
        return resultMsg;
    }

    @Override
    public ResultMsg queryMenuWithJson(Map<String, Object> map) {
        List<MenuModel> menuModelList = new ArrayList<>();
        ResultMsg resultMsg = new ResultMsg();
        //判断pid是否为空
        map.put("pid", "9");
        map.put("rid", map.get("userid"));
        List<AdminPermission> menList = permissionDao.selectMenu(map);
        MenuModel m = new MenuModel();
        m.setId("xtsy");
        m.setPid("0");
        m.setUrl("/adminv2/welcome");
        m.setIcon("icon-home");
        m.setName("系统首页");
        menuModelList.add(m);
        if (menList.size() > 0) {
            MenuModel menuModel = null;
            for (AdminPermission men : menList) {
                menuModel = new MenuModel();
                menuModel.setId(men.getId());
                menuModel.setPid("0");
                menuModel.setUrl(StringUtils.isEmpty(men.getUrl()) ? "" : men.getUrl());
                String icon = men.getIcon();
                if(StringUtils.isNotEmpty(icon)){
                    menuModel.setIcon(men.getIcon());
                }else {
                    menuModel.setIcon("iconfont icon-wenzhang");
                }
                menuModel.setName(men.getName());

                menuModelList.add(menuModel);
                //根据id去查询子菜单
                Map<String, Object> params = new HashMap<>();
                params.put("rid", map.get("userid"));
                params.put("pid", men.getId());
                List<AdminPermission> childList = permissionDao.selectMenu(params);
                if (childList.size() > 0) {
                    MenuModel childModel = null;
                    for (AdminPermission child : childList) {
                        childModel = new MenuModel();
                        childModel.setId(child.getId());
                        childModel.setPid(men.getId());
                        childModel.setUrl(StringUtils.isEmpty(child.getUrl()) ? "" : child.getUrl());
                        childModel.setIcon("");
                        childModel.setName(child.getName());
                        menuModelList.add(childModel);
                    }
                }
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("data", menuModelList);
        resultMsg.setResult(true);
        resultMsg.setMsg("获取菜单列表成功");
        resultMsg.setData(result);
        return resultMsg;
    }


    @Override
    public ResultMsg delAdminPermission(String id) {
        ResultMsg resultMsg = new ResultMsg();
        //查询pid为id的字段
        int rowCount = permissionDao.selectByPid(id);
        if (rowCount > 0) {
            //该节点有子类节点
            resultMsg.setMsg("请先删除子节点");
            resultMsg.setResult(false);
            return resultMsg;
        } else {
            //该节点没有子节点
            //判断status为0，不可用
            int status = permissionDao.selectByStatus(id);
            if (status > 0) {//状态不可用
                //删除--将status更新为2
                int i = permissionDao.updateStatusById(id, 2);
                if (i > 0) {
                    resultMsg.setMsg("删除成功");
                    resultMsg.setResult(true);
                    return resultMsg;
                } else {
                    resultMsg.setMsg("删除失败");
                    resultMsg.setResult(false);
                    return resultMsg;
                }
            } else {
                //状态可用
                resultMsg.setMsg("该节点是启用状态，不能删除！");
                resultMsg.setResult(false);
                return resultMsg;
            }

        }

    }

    @Override
    public List<AdminPermission> queryChildPermission(String id) {
        return permissionDao.queryChildPermission(id);
    }

    /**
     * 查询所有菜单,用户生成 tree
     *
     * @param status 是否可用
     * @param roleid 角色id
     * @param pid    父id
     * @return 菜单集合
     */
    @Override
    public ResultMsg adminPermissionService(String status, String roleid, String pid) {
        ResultMsg msg = new ResultMsg();
        List<AdminPermission> permissionList = permissionDao.getPermissionList(status, roleid, pid);
        // 增加根节点
        AdminPermission adminPermission = new AdminPermission();
        adminPermission.setId("9");
        adminPermission.setPid("0");
        adminPermission.setName("根节点");
        permissionList.add(adminPermission);
        msg.setData(permissionList);
        msg.setResult(Boolean.TRUE);
        return msg;
    }

    @Override
    public ResultMsg queryIconList() {
        ResultMsg resultMsg = new ResultMsg();

        return resultMsg;
    }

}

