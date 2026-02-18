package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.BaseClusterOperationTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface BaseClusterOperationTemplateMapper {
    int deleteByPrimaryKey(String templateId);

    int insert(BaseClusterOperationTemplate record);

    int insertSelective(BaseClusterOperationTemplate record);

    BaseClusterOperationTemplate selectByPrimaryKey(String templateId);

    int updateByPrimaryKeySelective(BaseClusterOperationTemplate record);

    int updateByPrimaryKey(BaseClusterOperationTemplate record);

    List<BaseClusterOperationTemplate> selectByCondition(Map paramMap);
}