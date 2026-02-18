package com.sunbox.sdpspot.mapper;

import com.sunbox.domain.ConfClusterHostGroup;
import com.sunbox.domain.InfoClusterVm;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfClusterHostGroupMapper {
    List<ConfClusterHostGroup> listByPurchaseType(@Param("purchaseType") int purchaseType, @Param("state") int state);

    int countByPurchaseType(@Param("purchaseType") int purchaseType, @Param("state") int state);

    ConfClusterHostGroup findByClusterIdAndGroupId(@Param("clusterId") String clusterId, @Param("groupId") String groupId);

    int countByClusterIdAndVmRoleAndPurchaseTypeNeState(@Param("clusterId") String clusterId,
                                                        @Param("vmRole") String vmRole,
                                                        @Param("purchaseType") int purchaseType,
                                                        @Param("state1") int state1,
                                                        @Param("state2") int state2,
                                                        @Param("state3") int state3);
}
