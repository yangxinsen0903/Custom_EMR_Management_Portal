package com.azure.csu.tiger.rm.api.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@ApiModel
@ToString
@Data
@NoArgsConstructor
public class SpotInstanceRequest {

    private String region;

    private List<String> vmSkuNames;
}
