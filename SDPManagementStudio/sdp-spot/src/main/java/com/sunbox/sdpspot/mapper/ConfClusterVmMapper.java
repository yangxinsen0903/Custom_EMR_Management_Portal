package com.sunbox.sdpspot.mapper;

import com.sunbox.domain.ConfClusterVm;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfClusterVmMapper {
    List<ConfClusterVm> listByClusterId(@Param("clusterId") String clusterId,
                                        @Param("groupId") String groupId,
                                        @Param("purchaseType") int purchaseType);

    List<ConfClusterVm> listByPurchaseType(@Param("purchaseType") int purchaseType, @Param("state") int state);

    int countByPurchaseType(@Param("purchaseType") int purchaseType, @Param("state") int state);

    ConfClusterVm findByClusterIdAndGroupId(@Param("clusterId") String clusterId, @Param("groupId") String groupId);

    ConfClusterVm selectByClusterIdAndVmConfId(@Param("clusterId") String clusterId, @Param("vmConfId") String vmConfId);
}