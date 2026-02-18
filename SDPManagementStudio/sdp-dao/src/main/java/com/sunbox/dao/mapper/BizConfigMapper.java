/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.dao.mapper;

import com.sunbox.domain.BizConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 业务配置(BizConfig)的Mapper接口
 * @author wangda
 * @date 2024/7/12
 */
@Mapper
@Repository
public interface BizConfigMapper {
    /**
     * 保存一条新的配置
     * @param record
     * @return
     */
    int insert(BizConfig record);

    /**
     * 查询所有的配置
     * @return
     */
    List<BizConfig> selectAll();

    /**
     * 根据分类和Key查询记录
     * @param category
     * @param cfgKey
     * @return
     */
    List<BizConfig> selectByCategoryAndKey(@Param("category") String category, @Param("cfgKey") String cfgKey);

    /**
     * 更新一个配置,如果要更新销毁参数,需要同步更新缓存:updateLimitConfig
     * @param record
     * @return
     */
    int updateByPrimaryKey(BizConfig record);

    /**
     * 删除一个配置
     * @param id
     * @return
     */
    int deleteById(@Param("id") Long id);
    //如果要更新销毁参数,需要同步更新缓存:updateLimitConfig
    int updateConfByCfgkey(BizConfig record);

    List<BizConfig> selectByCfgKey( @Param("cfgKeys") List<String>  cfgKeys);

}
