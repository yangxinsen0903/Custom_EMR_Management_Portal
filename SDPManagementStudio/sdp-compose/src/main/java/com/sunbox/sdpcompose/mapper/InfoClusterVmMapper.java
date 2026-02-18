package com.sunbox.sdpcompose.mapper;

import com.sunbox.domain.InfoClusterVm;
import com.sunbox.domain.InfoClusterVmKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

@Repository
public interface InfoClusterVmMapper {
    int deleteByPrimaryKey(InfoClusterVmKey key);

    int insert(InfoClusterVm record);

    int insertBatch(@Param("vms") List<InfoClusterVm> vms);

    int insertSelective(InfoClusterVm record);

    InfoClusterVm selectByPrimaryKey(InfoClusterVmKey key);

    List<InfoClusterVm> selectByClusterId(String clusterId);

    List<InfoClusterVm> selectByClusterIdAndRole(@Param("clusterId") String clusterId,
                                                 @Param("role") String role);

    List<InfoClusterVm> selectByClusterIdAndRoleAndState(@Param("clusterId") String clusterId,
                                                         @Param("role") String role,
                                                         @Param("state") Integer state);

    List<InfoClusterVm> selectByClusterIdAndGroupName(@Param("clusterId") String clusterId,
                                                      @Param("groupName") String groupName);

    List<InfoClusterVm> selectByClusterIdAndGroupNameAndState(@Param("clusterId") String clusterId,
                                                              @Param("groupName") String groupName,
                                                              @Param("state") Integer state);
    List<InfoClusterVm> selectByClusterIdAndGroupNameAndSkuNameAndState(@Param("clusterId") String clusterId,
                                                              @Param("groupName") String groupName,
                                                              @Param("skuName") String vmSku,
                                                              @Param("state") Integer state);
    List<InfoClusterVm> selectByClusterIdAndGroupNameAndStates(@Param("clusterId") String clusterId,
                                                              @Param("groupName") String groupName,
                                                              @Param("state1") Integer state1,
                                                              @Param("state2") Integer state2);

    /**
     * 根据集群ID和扩容任务ID获取机器列表
     *
     * @param clusterId 集群ID
     * @param taskId    扩容任务ID
     * @return
     */
    List<InfoClusterVm> selectByClusterIdAndScaleOutTaskId(@Param("clusterId") String clusterId,
                                                           @Param("taskId") String taskId);


    List<InfoClusterVm> selectByClusterIdAndScaleOutTaskIdAndState(@Param("clusterId") String clusterId,
                                                           @Param("taskId") String taskId,
                                                           @Param("state") Integer state);

    List<InfoClusterVm> selectByClusterIdAndScaleOutTaskIdAndStates(@Param("clusterId") String clusterId,
                                                                   @Param("taskId") String taskId,
                                                                   @Param("state1") Integer state1,
                                                                   @Param("state2") Integer state2);

    /**
     * 根据集群ID和缩容任务ID获取机器列表
     *
     * @param clusterId 集群ID
     * @param taskId    缩容任务ID
     * @return
     */
    List<InfoClusterVm> selectByClusterIdAndScaleInTaskId(@Param("clusterId") String clusterId,
                                                          @Param("taskId") String taskId);

    /**
     * 获取集群指定groupName当前正在运行可以scalein的机器
     *
     * @param clusterId
     * @param groupName
     * @param cnt
     * @return
     */
    List<InfoClusterVm> selectVmsByGroupNameForScaleIn(@Param("clusterId") String clusterId,
                                                       @Param("groupName") String groupName,
                                                       @Param("cnt") int cnt);


    int updateByPrimaryKeySelective(InfoClusterVm record);

    int updateByPrimaryKey(InfoClusterVm record);

    int updateVMStateByClusterId(@Param("clusterId") String clusterId, @Param("state") int state);

    int updateVMStateByClusterIdAndVMName(@Param("clusterId") String clusterId,
                                          @Param("vmName") String vmName,
                                          @Param("state") int state);

    int updateScaleinTaskIdByClusterIdAndVmNames(@Param("clusterId") String clusterId,
                                                 @Param("vmNames") List<String> vmNames,
                                                 @Param("taskId") String taskid);

    /**
     * 批量更新 vm state
     * @param clusterId 集群ID
     * @param vmNames VM names
     * @param state 集群状态
     * @return
     */
    int batchUpdateVMState(@Param("clusterId") String clusterId,
                           @Param("vmNames") List<String> vmNames,
                           @Param("state") Integer state);

    /**
     * 根据集群clusterid+vmips+扩容任务id 获取 vm信息
     *
     * @param clusterId
     * @param vmIps
     * @param taskId
     * @return
     */
    List<InfoClusterVm> getVMListByClusterIdAndIpsAndScaleOutTaskId(@Param("clusterId") String clusterId,
                                                                    @Param("vmIps") List<String> vmIps,
                                                                    @Param("taskId") String taskId);

    /**
     * 根据集群clusterid+vm hostname+扩容任务ID 获取vm信息
     * @param clusterId
     * @param hostNames
     * @param taskId
     * @return
     */
    List<InfoClusterVm> getVMListByClusterIdAndHostNamesAndScaleOutTaskId(@Param("clusterId") String clusterId,
                                                                        @Param("hostNames") List<String> hostNames,
                                                                        @Param("taskId") String taskId);

    /**
     * 查询当前集群各实例组正在运行的VM数量
     *
     * @return
     */
    List<HashMap> getGroupNameCount(@Param("clusterId") String clusterId);

    InfoClusterVm getVmCountByGroupName(@Param("clusterId") String clusterId, @Param("groupName") String groupName);

    /**
     * 获取集群某个角色的vm数量
     *
     * @param clusterId 集群ID
     * @param vmRole    集群角色
     * @param state     vm运行状态
     * @return
     */
    int getClusterInstanceCountByVmRoleAndState(@Param("clusterId") String clusterId,
                                                @Param("vmRole") String vmRole,
                                                @Param("state") Integer state);


    /**
     * 查询当前集群各实例角色正在运行的VM数量
     *
     * @return
     */
    List<HashMap> getVMRoleNameCount(@Param("clusterId") String clusterId);

    int updateVmsStatusByScaleInTaskId(@Param("clusterId") String clusterId,
                                       @Param("taskId") String taskId,
                                       @Param("state") int state);


    int updateGroupId(InfoClusterVm updateInfoClusterVm);

    List<InfoClusterVm> selectVmsByTaskId(InfoClusterVm infoClusterVm);

    List<InfoClusterVm> selectByScaleInTaskId(@Param("clusterId") String clusterId,
                                              @Param("scaleInTaskId") String scaleInTaskId);

    List<InfoClusterVm> selectByScaleInTaskIdAndState(@Param("clusterId") String clusterId,
                                              @Param("scaleInTaskId") String scaleInTaskId,
                                                      @Param("state") Integer state);

    Integer countByScaleOutTaskIdAndState(@Param("clusterId") String clusterId,
                                                      @Param("scaleOutTaskId") String scaleOutTaskId,
                                                      @Param("state") Integer state);

    Integer countByClusterIdAndGroupNameAndState(@Param("clusterId") String clusterId,
                                                 @Param("groupName") String groupName,
                                                 @Param("state") Integer state);

    List<InfoClusterVm> selectAllVmsByRoleAndState(@Param("vmRole") String vmRole,
                                                   @Param("state") Integer state);

    List<InfoClusterVm> selectAllAvaliableAmbari();

    InfoClusterVm selectVMByClusterIdAndHostName(@Param("clusterId") String clusterId,
                                                  @Param("hostName") String hostName);


    /**
     * 获取集群创建时某个角色的vm数量
     *
     * @param clusterId
     * @param vmRole
     * @return
     */
    int getCreateVmCountByVmRole(@Param("clusterId") String clusterId,
                                 @Param("vmRole") String vmRole);

    /**
     *  根据扩容任务ID，变更vm 工程模式
     *
     * @param scaleOutTaskId
     * @param clusterId
     * @param maintenanceMode
     * @return
     */
    int updateMaintenanceModeByScaleoutTaskId(@Param("scaleOutTaskId") String scaleOutTaskId,
                                              @Param("clusterId") String clusterId,
                                              @Param("maintenanceMode") Integer maintenanceMode);

    /**
     * 根据缩容任务ID，变更vm工程模式
     *
     * @param scaleInTaskId
     * @param clusterId
     * @param maintenanceMode
     * @return
     */
    int updateMaintenanceModeByScaleinTaskId(@Param("scaleInTaskId") String scaleInTaskId,
                                             @Param("clusterId") String clusterId,
                                             @Param("maintenanceMode") Integer maintenanceMode);


    void cleanScaleInTaskId(@Param("clusterId") String clusterId,
                            @Param("groupName") String groupName,
                            @Param("taskId") String taskId);
    /**
     * 集群创建完成后，更新vm加入集群时间
     *
     * @param clusterId
     * @return
     */
    int updateJoinClusterTimeOnCompleteCreate(@Param("clusterId") String clusterId);

    /**
     * 集群扩容完成后，更新vm加入集群时间
     *
     * @param clusterId
     * @param taskId
     * @return
     */
    int updateJoinClusterTimeOnCompleteScaleOut(@Param("clusterId") String clusterId,@Param("taskId") String taskId);

    List<InfoClusterVm> selectRunningVmsByClusterIdAndGroupName(@Param("clusterId")String clusterId, @Param("groupName")String groupName);

}