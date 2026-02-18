package com.sunbox.dao.mapper;

import com.sunbox.domain.MetaDataItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
* @description 针对表【meta_data_item(元数据条目表)】的数据库操作Mapper
* @createDate 2024-05-31 09:36:57
*/
@Mapper
@Repository
public interface MetaDataItemMapper {
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    MetaDataItem queryById(Long id);

    /**
     * 统计总行数
     *
     * @param metaDataItem 查询条件
     * @return 总行数
     */
    long count(MetaDataItem metaDataItem);

    /**
     * 新增数据
     *
     * @param metaDataItem 实例对象
     * @return 影响行数
     */
    int insert(MetaDataItem metaDataItem);


    /**
     * 修改数据
     *
     * @param metaDataItem 实例对象
     * @return 影响行数
     */
    int update(MetaDataItem metaDataItem);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    List<MetaDataItem> selectMetaDataList(MetaDataItem item);

    /**
     * 获取元数据data
     * @param item
     * @return
     */
    List<String> selectMetaData(MetaDataItem item);

    /**
     * 统计行数
     * @return
     */
    @Select("select count(*) from meta_data_item where type != #{type} and region =#{region}")
    long countBy(String type,String region);

}
