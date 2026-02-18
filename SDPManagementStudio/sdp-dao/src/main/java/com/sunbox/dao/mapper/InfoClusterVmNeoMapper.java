package com.sunbox.dao.mapper;

import com.sunbox.domain.InfoClusterVm;
import com.sunbox.domain.InfoClusterVmWithConf;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface InfoClusterVmNeoMapper {
    /**
     * InfoClusterVm.VM_RUNNING
     * @param state
     * @return
     */
    List<InfoClusterVmWithConf> selectByState(@Param("clusterState") Integer clusterState,
                                              @Param("state") Integer state,
                                              @Param("region") String region);

    @Deprecated
    InfoClusterVm queryByClusterId(String clusterId);

    List<InfoClusterVm> queryByClusterIdAndVmRole(String clusterId, String vmRole);

    int deleteByPrimaryKey(@Param("clusterId") String clusterId, @Param("vmName") String vmName);

    int insert(InfoClusterVm record);

    int insertBatch(@Param("vms") List<InfoClusterVm> vms);

    int insertSelective(InfoClusterVm record);

    InfoClusterVm selectByPrimaryKey(@Param("clusterId") String clusterId, @Param("vmName") String vmName);

    List<InfoClusterVm> selectByClusterId(String clusterId);

    List<InfoClusterVm> selectByClusterIdAndState(@Param("clusterId")String clusterId, @Param("states")List<Integer> states);

    InfoClusterVm selectOneByClusterId(String clusterId);

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
    InfoClusterVm selectOneByClusterIdAndGroupNameAndState(@Param("clusterId") String clusterId,
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
     * 查询缩容的VM, 不加状态判断, 因为缩容的状态不确定
     * @param clusterId
     * @param scaleinTaskId
     * @return
     */
    List<InfoClusterVm> selectVmsByScaleInTaskId(@Param("clusterId") String clusterId, @Param("scaleinTaskId") String scaleinTaskId);


    /**
     * 清理infoClusterVm表上存储的scalingInTaskId
     * @param clusterId
     * @param scaleinTaskId
     */
    void cleanScaleinTaskId(@Param("clusterId") String clusterId,
                            @Param("scaleinTaskId") String scaleinTaskId);

    List<InfoClusterVm> selectRunningVMsWithOutVmId();

    int updateVMId(Map<String,String> updateItem);

    List<InfoClusterVm> selectByClusterIdAndGroupNameAndStates(@Param("clusterId") String clusterId,
                                                               @Param("groupName") String groupName,
                                                               @Param("state1") Integer state1,
                                                               @Param("state2") Integer state2);
    List<InfoClusterVm> getVMCountByStateGroupByRole(@Param("clusterId") String clusterId, @Param("state") Integer state);

    int selectCountByGroupNameAndState(@Param("clusterId") String clusterId,
                                       @Param("groupName") String groupName,
                                       @Param("state") Integer state);

    List<InfoClusterVm> selectVMListByCluIds(@Param("clusterIdList") List<String> clusterIdList);

    int selectByVmName( @Param("vmName") String vmName);

}