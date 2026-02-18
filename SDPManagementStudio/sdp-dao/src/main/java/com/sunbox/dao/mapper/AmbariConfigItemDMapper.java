package com.sunbox.dao.mapper;

import com.sunbox.domain.AmbariConfigItemRequest;
import com.sunbox.domain.ambari.AmbariConfigItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *  在dao模块中的AmbariConfigItemMapper,另一个在compose模块, @Autowired
 */
@Mapper
@Repository
public interface AmbariConfigItemDMapper {
    int deleteByPrimaryKey(Long id);

    int insert(AmbariConfigItem record);

    int insertSelective(AmbariConfigItem record);

    AmbariConfigItem selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(AmbariConfigItem record);

    int updateByPrimaryKey(AmbariConfigItem record);

    List<String> selectComponentList();

    List<String> selectProfilesList();

    List<AmbariConfigItem> selectByPage(AmbariConfigItemRequest request);

    int selectTotalByPage(AmbariConfigItemRequest request);

    /**
     * 动态批量存储, 字段不为空时才存
     * @param itemList
     * @return
     */
    int insertBatch(@Param("itemList") List<AmbariConfigItem> itemList);


}