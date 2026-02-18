package com.azure.csu.tiger.rm.api.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ApiModel
@ToString
@Data
@NoArgsConstructor
public class RegularProfile {

    private Integer capacity;

    private Integer minCapacity;

    private String allocationStrategy;
}
