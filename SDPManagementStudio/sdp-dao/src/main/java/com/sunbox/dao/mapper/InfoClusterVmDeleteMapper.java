package com.sunbox.dao.mapper;

import com.sunbox.domain.InfoClusterVmDelete;
import com.sunbox.domain.InfoClusterVmDeleteDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface InfoClusterVmDeleteMapper {
    int deleteByPrimaryKey(Long id);

    int insert(InfoClusterVmDelete record);

    int insertSelective(InfoClusterVmDelete record);

    InfoClusterVmDelete selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(InfoClusterVmDelete record);

    int updateByPrimaryKey(InfoClusterVmDelete record);

    List<InfoClusterVmDelete> getByClusterIdAndVmName(String clusterId, String vmName);

    List<InfoClusterVmDelete> getNeedDeleteVMDeletes(Integer limitCount);

    List<InfoClusterVmDelete> getDeletingVMs();

    List<InfoClusterVmDelete> getSendRequestTimeOutVms(@Param("date") Date date);

    List<InfoClusterVmDelete> getDeletingTimeOutVms(@Param("date")Date date);

    List<InfoClusterVmDelete> getNeedReleaseFreeze(@Param("date") Date date);

    List<InfoClusterVmDeleteDetail> queryByTime(@Param("region") String region,
                                                @Param("planName") String planName,
                                                @Param("planId") String planId,
                                                @Param("clusterId") String clusterId,
                                                @Param("clusterName") String clusterName,
                                                @Param("hostName") String hostName,
                                                @Param("status") Integer status,
                                                @Param("beginDate") Date beginDate,
                                                @Param("endDate") Date endDate,
                                                @Param("pageStart") Integer pageStart,
                                                @Param("pageLimit") Integer pageSize);

    Long countByTime(@Param("region") String region,
                     @Param("planName") String planName,
                     @Param("planId") String planId,
                     @Param("clusterId") String clusterId,
                     @Param("clusterName") String clusterName,
                     @Param("hostName") String hostName,
                     @Param("status") Integer status,
                     @Param("beginDate") Date beginDate,
                     @Param("endDate") Date endDate);

    /**
     * 获取集群中虚拟机删除计划的汇总信息
     * <ol>
     * <li>'frozen': 已冻结 </li>
     * <li>'waitDelete': 待删除 </li>
     * <li>'deleteRequest': 删除请求已发送</li>
     * <li>'deleting': 删除中</li>
     * <li>'deleted': 已删除</li>
     * </ol>
     * @return
     */
    List<Map> vmCleanSummary(@Param("region")String region);
}