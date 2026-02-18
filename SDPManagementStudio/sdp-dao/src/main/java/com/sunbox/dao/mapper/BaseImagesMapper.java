package com.sunbox.dao.mapper;

import com.sunbox.domain.BaseImages;
import org.springframework.stereotype.Repository;


@Repository
public interface BaseImagesMapper {
    int deleteByPrimaryKey(String imgId);

    int insert(BaseImages record);

    int insertSelective(BaseImages record);

    BaseImages selectByPrimaryKey(String imgId);

    int updateByPrimaryKeySelective(BaseImages record);

    int updateByPrimaryKey(BaseImages record);
}