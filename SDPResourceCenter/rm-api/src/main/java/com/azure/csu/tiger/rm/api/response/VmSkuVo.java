package com.azure.csu.tiger.rm.api.response;

import com.azure.resourcemanager.compute.models.ComputeSku;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel
@Data
@NoArgsConstructor
public class VmSkuVo {

    public static VmSkuVo from(ComputeSku sku) {
        VmSkuVo vo = new VmSkuVo();

        return vo;
    }
}
