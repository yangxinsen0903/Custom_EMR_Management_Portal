package com.sunbox.domain;

import lombok.Data;

import java.util.*;
@Data
public class AmbariConfigAddRequest {
    private List<String> stackCode;
    private  List<String> itemType;
    private List<String> serviceCode;
    private String key;
    private String value;
    private Integer isContentProp;
    private Integer isDynamic;
    private String dynamicType;
    private String state;
    private String configTypeCode;
    private Long id ;
    private String componentCode;


}
