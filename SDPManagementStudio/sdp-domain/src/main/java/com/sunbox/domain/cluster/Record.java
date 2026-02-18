package com.sunbox.domain.cluster;

import lombok.Data;

import java.util.List;
@Data
public class Record  {
    private List<Field> fields;
    /**
     * 从1开始，0给全局变量使用
     */
    private Integer sequence;


}

