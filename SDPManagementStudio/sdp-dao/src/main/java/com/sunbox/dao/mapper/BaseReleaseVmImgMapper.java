package com.sunbox.dao.mapper;

import com.sunbox.domain.BaseReleaseVmImg;
import com.sunbox.domain.images.ImageResponse;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BaseReleaseVmImgMapper {
    int deleteByPrimaryKey(@Param("releaseVersion") String releaseVersion, @Param("vmRole") String vmRole);

    int insert(BaseReleaseVmImg record);

    int insertSelective(BaseReleaseVmImg record);

    BaseReleaseVmImg selectByPrimaryKey(@Param("releaseVersion") String releaseVersion, @Param("vmRole") String vmRole);

    List<BaseReleaseVmImg> selectOneByImgId(@Param("imgId") String imgId);

    List<BaseReleaseVmImg> selectAllStackVersion();

    int updateByPrimaryKeySelective(BaseReleaseVmImg record);

    int updateByPrimaryKey(BaseReleaseVmImg record);

    List<ImageResponse> listImageByReleaseVersion(@Param("releaseVersion") String releaseVersion,
                                                  @Param("pageStart") Integer pageStart,
                                                  @Param("pageLimit") Integer pageLimit);

    int countImageByReleaseVersion(@Param("releaseVersion") String releaseVersion);
}