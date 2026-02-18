package com.sunbox.sdpspot.mapper;

import com.sunbox.domain.InfoClusterVm;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InfoClusterVmMapper {
    List<InfoClusterVm> listByPurchaseType(int purchaseType, int state);

    List<InfoClusterVm> listByClusterIdAndGroupIdAndPurchaseType(@Param("clusterId") String clusterId,
                                                                 @Param("groupId") String groupId,
                                                                 @Param("purchaseType") int purchaseType,
                                                                 @Param("state") int state);

    long countByPurchaseType(int purchaseType, int state);

    List<InfoClusterVm> listByVmName(String vmName, int purchaseType, int state);

    InfoClusterVm findByVmName(@Param("vmName") String vmName,
                               @Param("purchaseType") int purchaseType,
                               @Param("state") int state);

    int countByClusterIdAndGroupIdAndPurchaseType(@Param("clusterId") String clusterId,
                                                  @Param("groupId") String groupId,
                                                  @Param("purchaseType") int purchaseType,
                                                  @Param("state") int state);

    int countByClusterIdAndGroupIdAndPurchaseTypeAndStates(@Param("clusterId") String clusterId,
                                                           @Param("groupId") String groupId,
                                                           @Param("purchaseType") int purchaseType,
                                                           @Param("state1") int state1,
                                                           @Param("state2") int state2);

    int countByClusterIdAndGroupIdAndPurchaseTypeAndState(@Param("clusterId") String clusterId,
                                                           @Param("groupId") String groupId,
                                                           @Param("purchaseType") int purchaseType,
                                                           @Param("state") int state);

    int updateHealthCheckTime(InfoClusterVm record);

    InfoClusterVm findTop1IdByInternalIpOrderByCreatedTimeDesc(@Param("internalIp") String internalIp,
                                                               @Param("state") int state);

    InfoClusterVm findOne(@Param("clusterId") String clusterId,
                          @Param("groupId") String groupId,
                          @Param("vmName") String vmName);
}
