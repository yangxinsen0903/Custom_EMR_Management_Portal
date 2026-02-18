package com.azure.csu.tiger.rm.api.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@ApiModel
@Data
@NoArgsConstructor
public class ListVmResponse {

    private List<GetVmInfoVo> datas;

    private Integer pageNo;

    private Integer pageSize;

    private Integer count;

    public static ListVmResponse from(List<GetVmInfoVo> datas, Integer pageNo, Integer pageSize) {
        ListVmResponse response = new ListVmResponse();
        response.setDatas(datas);
        response.setPageNo(pageNo);
        response.setPageSize(pageSize);
        response.setCount(datas.size());
        return response;
    }
}
