package com.sunbox.sdpadmin.model.shein.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class SheinClusterCfg {

    @JsonProperty("cfgItemName")
    private String classification;

    @JsonProperty("cfgs")
    private Map<String, Object> cfg;
}
