package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.BaseDictionary;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BaseDictionaryMapper {
    int deleteByPrimaryKey(Integer dictId);

    int insert(BaseDictionary record);

    int insertSelective(BaseDictionary record);

    BaseDictionary selectByPrimaryKey(Integer dictId);

    int updateByPrimaryKeySelective(BaseDictionary record);

    int updateByPrimaryKey(BaseDictionary record);

    List<BaseDictionary> selectBaseDictionary(BaseDictionary baseDictionary);
}