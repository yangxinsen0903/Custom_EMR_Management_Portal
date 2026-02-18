package com.sunbox.dao.mapper;

import com.sunbox.domain.azure.AzureVmtraceInfo;
import com.sunbox.domain.azure.AzureVmtraceInfoRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *  azure_cleaned_vms_record ,Azure端僵尸机清理任务,
 *  vm_name唯一约束
 */
@Mapper
@Repository
public interface AzureVmtraceInfoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(AzureVmtraceInfo record);

    int insertSelective(AzureVmtraceInfo record);

    AzureVmtraceInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(AzureVmtraceInfo record);

    int updateByPrimaryKey(AzureVmtraceInfo record);

    List<AzureVmtraceInfo> selectByPage(AzureVmtraceInfoRequest request);

    int selectTotal(AzureVmtraceInfoRequest request);

    /**
     * 根据vmName查询, vm_name唯一约束
     * @param vmName
     * @return
     */
    List<AzureVmtraceInfo> selectByVMName(String vmName);

    int updateVmsDelResponseByClusterName(@Param("vmsDelResponse")String vmsDelResponse, @Param("clusterName")String clusterName);
}