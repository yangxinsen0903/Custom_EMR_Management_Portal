package com.azure.csu.tiger.rm.api.response;

import com.azure.csu.tiger.rm.api.utils.ConstantUtil;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApiModel
@Data
@NoArgsConstructor
public class GetGroupInfoVo {

    private String groupName;

    private Integer count;

    private List<GetVmInfoVo> virtualMachines;

    public static List<GetGroupInfoVo> from(List<GetVmInfoVo> vmInfos) {
        Map<String, List<GetVmInfoVo>> map = vmInfos.stream().collect(Collectors.groupingBy(GetVmInfoVo::getFleetName));
        List<GetGroupInfoVo> groupInfo = Lists.newArrayList();
        map.entrySet().forEach(entry -> {
            GetGroupInfoVo vo = new GetGroupInfoVo();
            vo.setGroupName(entry.getValue().get(0).getTags().get(ConstantUtil.SYS_SDP_GROUP));
            vo.setCount(entry.getValue().size());
            vo.setVirtualMachines(entry.getValue());
            groupInfo.add(vo);
        });
        return groupInfo;
    }
}
