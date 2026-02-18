package com.sunbox.dao.mapper;

import com.sunbox.domain.BaseImageScripts;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BaseImageScriptsMapper {
    int deleteByPrimaryKey(String imgScriptId);

    int insert(BaseImageScripts record);

    int insertSelective(BaseImageScripts record);

    BaseImageScripts selectByPrimaryKey(String imgScriptId);

    int updateByPrimaryKeySelective(BaseImageScripts record);

    int updateByPrimaryKey(BaseImageScripts record);

    List<BaseImageScripts> getAllByImgIdAndRunTiming(String imgId,String runTiming);

    List<BaseImageScripts> getAllByImgId(@Param("imgId")String imgId,
                                         @Param("pageStart") Integer pageStart,
                                         @Param("pageLimit") Integer pageLimit);
    int countByImgId(@Param("imgId")String imgId);
}