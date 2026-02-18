package com.azure.csu.tiger.rm.api.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ApiModel
@ToString
@Data
@NoArgsConstructor
public class VmSizeProfile {

    private String name;

    private Integer rank;
}
