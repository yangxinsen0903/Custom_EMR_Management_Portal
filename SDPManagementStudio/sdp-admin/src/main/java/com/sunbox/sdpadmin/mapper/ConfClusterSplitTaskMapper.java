package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.ConfClusterSplitTask;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfClusterSplitTaskMapper {
    int insertSelective(ConfClusterSplitTask record);
}
