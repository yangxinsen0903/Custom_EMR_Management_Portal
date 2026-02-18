package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.BaseClusterOperationTemplateActivity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BaseClusterOperationTemplateActivityMapper {
    int deleteByPrimaryKey(String activityId);

    int insert(BaseClusterOperationTemplateActivity record);

    int insertSelective(BaseClusterOperationTemplateActivity record);

    BaseClusterOperationTemplateActivity selectByPrimaryKey(String activityId);

    int updateByPrimaryKeySelective(BaseClusterOperationTemplateActivity record);

    int updateByPrimaryKey(BaseClusterOperationTemplateActivity record);

    /**
     * 根据模版id获取执行活动列表
     * @param templateId
     * @return
     */
    List<BaseClusterOperationTemplateActivity> selectByTemplateId(String templateId);

}