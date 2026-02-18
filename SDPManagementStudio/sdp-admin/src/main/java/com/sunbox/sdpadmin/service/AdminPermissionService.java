package com.sunbox.sdpadmin.service;

import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpadmin.core.model.AdminPermission;

import java.util.List;
import java.util.Map;

public interface AdminPermissionService {

    ResultMsg save(String pid, String name, String alias, String menu, String url, String status, Integer sortIndex, String icon);

    ResultMsg update(String id, String name, String alias, String menu, String url, String status, Integer sortIndex, String pid, String icon);

    int disable(String id);

    int enabled(String id);

    List<Map<String, Object>> getPermissionList();

    Map<String, Object> queryPermission(int page, int pageSize, String name);

    String getPermissionTreeData(String status, String roleid, String userid);

    ResultMsg getPermissionByid(String id);

    ResultMsg updatePermissionStatus(String id, String status);

    String getRolePermissionTreeData(String roleid);

    String getPermission4ZTreeData(String status);

    List<AdminPermission> selectByParams(Map<String, Object> map);

    List<AdminPermission> selectMenu(Map<String, Object> map);

    ResultMsg selectMenuByPid(Map<String, Object> map);

    ResultMsg queryMenuWithJson(Map<String, Object> map);

    /**
     * 删除节点
     *
     * @param id
     * @return ResultMsg
     * @auther hzm
     */
    ResultMsg delAdminPermission(String id);

    /**
     * 获取子节点
     *
     * @param id
     * @return
     */
    List<AdminPermission> queryChildPermission(String id);

    /**
     * 查询所有菜单
     *
     * @param status 是否可用
     * @param roleid 角色id
     * @param pid    父id
     * @return 菜单集合
     */
    ResultMsg adminPermissionService(String status, String roleid, String pid);

    /**
     * 获取所有图标
     * @return
     */
    ResultMsg queryIconList();
}
