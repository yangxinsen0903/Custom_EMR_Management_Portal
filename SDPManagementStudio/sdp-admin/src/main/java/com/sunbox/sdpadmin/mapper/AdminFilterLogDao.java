package com.sunbox.sdpadmin.mapper;



import com.sunbox.sdpadmin.core.model.AdminFilterLog;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminFilterLogDao {
    int deleteByPrimaryKey(String id);

    int insert(AdminFilterLog record);

    int insertSelective(AdminFilterLog record);

    AdminFilterLog selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(AdminFilterLog record);

    int updateByPrimaryKey(AdminFilterLog record);

    List<AdminFilterLog> queryFilterLog(AdminFilterLog vmAdminFilterLog);
}