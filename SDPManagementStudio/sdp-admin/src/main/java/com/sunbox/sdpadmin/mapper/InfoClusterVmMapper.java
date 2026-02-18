package com.sunbox.sdpadmin.mapper;

import com.sunbox.domain.InfoClusterVm;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public interface InfoClusterVmMapper {
    int deleteByPrimaryKey(@Param("clusterId") String clusterId, @Param("vmName") String vmName);

    int insert(InfoClusterVm record);

    int insertSelective(InfoClusterVm record);

    InfoClusterVm selectByPrimaryKey(@Param("clusterId") String clusterId, @Param("vmName") String vmName);

    int updateByPrimaryKeySelective(InfoClusterVm record);

    int updateByPrimaryKey(InfoClusterVm record);

    List<Map> selectByObject(Map params);

    /**
     * 查询一个集群中正在运行的所有主机，不包括维护状态的主机
     * @param clusterId 集群ID
     * @param vmRoles 角色列表
     * @return 主机列表
     */
    List<Map> selectAllRunningVms(String clusterId, List<String> vmRoles);

    List<Map> selectByObjectNotScaleoutTask(Map params);

    List<Map> selectByObjectInnerScalingTask(Map params);

    List<HashMap<String, Object>> getVMlistByParam(Map param);

    int countByClusterIdAndGroupIdAndState(@Param("clusterId") String clusterId,
                                           @Param("groupId") String groupId,
                                           @Param("state") Integer state);

    int getVMlistCountByParam(Map param);

    List<InfoClusterVm> selectByClusterIdAndGroupNameAndState(@Param("clusterId") String clusterId,
                                                              @Param("groupName") String groupName,
                                                              @Param("state") Integer state);

    /**
     * 查询当前集群各实例组正在运行的VM数量
     *
     * @return
     */
    List<HashMap> getGroupNameCount(@Param("clusterId") String clusterId);

    List<InfoClusterVm> getVMCountByState(@Param("clusterId") String clusterId, @Param("state") Integer state);

    List<InfoClusterVm> selectVmsByTaskId(@Param("clusterId") String clusterId, @Param("scaleoutTaskId") String scaleoutTaskId);

    List<InfoClusterVm> selectRunningVmsByTaskId(@Param("clusterId") String clusterId, @Param("scaleoutTaskId") String scaleoutTaskId, @Param("state") Integer state);

    /**
     * 清理infoClusterVm表上存储的scalingInTaskId
     * @param clusterId
     * @param scaleinTaskId
     */
    void cleanScaleinTaskId(@Param("clusterId") String clusterId,
                            @Param("scaleinTaskId") String scaleinTaskId);

    List<InfoClusterVm> selectRunningVMsWithOutVmId();

    int updateVMId(Map<String,String> updateItem);

    List<HashMap<String, Object>> selectVmInstanceDetail(@Param("region") String region,
                                                         @Param("clusterId") String clusterId,
                                                         @Param("vmName") String vmName,
                                                         @Param("hostName") String hostName,
                                                         @Param("internalIp") String ip,
                                                         @Param("state") Integer state,
                                                         @Param("groupNameList") List<String> groupNameList);

}