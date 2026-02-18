package com.sunbox.domain.cluster;

import lombok.Data;

import java.util.List;
@Data
public class NodeOperator {
    private String node_name;
    private List<String> operators;


}
