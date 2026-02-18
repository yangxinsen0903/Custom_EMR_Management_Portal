package com.sunbox.sdpadmin.mapper;

import com.sunbox.sdpadmin.core.model.AdminPermission;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AdminPermissionDao {

    int save(AdminPermission permission);

    int update(AdminPermission permission);

    int remove(String id);

    AdminPermission getPermissionById(String id);

    List<AdminPermission> getPermissionList(@Param("status")String status,@Param("roleid") String roleid,@Param("pid")String pid);

    List<AdminPermission> getPermissionListByUserid(@Param("status")String status,@Param("userid") String userid);

    List<AdminPermission> pageList(int page, int pageSize, String name);

    int pageCount(int page, int pageSize, String name);

    List<AdminPermission> selectByParams(Map<String,Object> map);

    List<AdminPermission> selectMenu(Map<String,Object> map);

    int getSortIndex();

    int selectSortIndex(Integer sortIndex);

    int updateSortIndex(Integer sortIndex);

    int selectByPid(String id);

    int selectByStatus(String id);


    int updateStatusById(@Param("id") String id,@Param("status") Integer status);

    /**
     * 获取子节点
     * @param id
     * @return
     */
    List<AdminPermission> queryChildPermission(String id);
}
