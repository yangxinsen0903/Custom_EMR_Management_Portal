/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.dao.mapper;

import com.sunbox.dao.query.VmEventQueryParam;
import com.sunbox.domain.VmEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * VmEvent的数据库映射接口
 * @author wangda
 * @date 2024/7/14
 */
@Mapper
@Repository
public interface VmEventMapper {

    /**
     * 新增一个事件
     **/
    int insert(VmEvent vmEvents);

    /**
     * 更新
     **/
    int updateById(VmEvent vmEvents);

    /**
     * 查询 根据主键 id 查询
     **/
    VmEvent selectById(int id);

    /**
     * 查询 分页查询
     **/
    List<VmEvent> selectPageList(@Param("param") VmEventQueryParam param);

    /**
     * 查询 分页查询 count
     **/
    int selectPageListCount(@Param("param") VmEventQueryParam param);

    /**
     * 根据VMName和事件类型, 查询事件通知
     * @param vmName
     * @param eventType
     * @return
     */
    VmEvent selectByVmNameAndEventType(@Param("vmName") String vmName, @Param("eventType") String eventType);

}
