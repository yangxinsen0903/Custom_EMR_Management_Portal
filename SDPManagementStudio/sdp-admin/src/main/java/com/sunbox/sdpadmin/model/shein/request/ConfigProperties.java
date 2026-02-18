package com.sunbox.sdpadmin.model.shein.request;

import lombok.Data;

import java.util.Map;

@Data
public class ConfigProperties {

    private String confItemName;

    private Map<String, Object> confs;
}
