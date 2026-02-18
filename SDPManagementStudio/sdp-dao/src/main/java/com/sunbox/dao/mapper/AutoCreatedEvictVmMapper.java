package com.sunbox.dao.mapper;

import com.sunbox.domain.AutoCreatedEvictVm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Mapper
@Repository
public interface AutoCreatedEvictVmMapper {
    /**
     * 按主键查询
     * @param id
     * @return
     */
    AutoCreatedEvictVm selectByPrimaryKey(@Param("id") Long id);

    /**
     * 更新一个VM的处理状态
     * @param id 主键ID
     * @param state 状态
     */
    void updateState(@Param("id") Long id, @Param("state") String state);

    /**
     * 查询VM列表
     * @param beginTime
     * @param endTime
     * @param states
     * @return
     */
    List<AutoCreatedEvictVm> selectByTimeAndState(@Param("beginTime") Date beginTime, @Param("endTime") Date endTime,
                                                  @Param("states") List<String> states);

    /**
     * 保存一个VM
     * @param entity
     * @return
     */
    int insert(AutoCreatedEvictVm entity);

    /**
     * 根据vmid查询VM
     * @param vmid
     * @return
     */
    AutoCreatedEvictVm selectByVmId(@Param("vmid") String vmid);

}