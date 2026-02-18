package com.sunbox.dao.mapper;


import com.sunbox.domain.ConfigDetail;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface ConfigDetailMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ConfigDetail record);

    int insertSelective(ConfigDetail record);

    ConfigDetail selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ConfigDetail record);

    int updateByPrimaryKey(ConfigDetail record);

    ConfigDetail selectByaKey(String akey);

    int updateByaKey(ConfigDetail record);

}