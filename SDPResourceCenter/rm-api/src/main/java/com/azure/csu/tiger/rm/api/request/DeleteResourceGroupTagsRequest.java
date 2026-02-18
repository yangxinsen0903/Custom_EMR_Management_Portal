package com.azure.csu.tiger.rm.api.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@ApiModel
@ToString
@Data
@NoArgsConstructor
public class DeleteResourceGroupTagsRequest {

    private String apiVersion;

    private String transactionId;

    private List<String> tagNames;
}
