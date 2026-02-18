package com.azure.csu.tiger.rm.api.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ApiModel
@ToString
@Data
@NoArgsConstructor
public class SpotProfile {

    private Integer capacity;

    private Integer minCapacity;

    private String maxPricePerVM;

    private String allocationStrategy;

    private String evictionPolicy;

    private boolean maintain;
}
