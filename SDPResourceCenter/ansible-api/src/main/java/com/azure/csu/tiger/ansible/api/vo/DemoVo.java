package com.azure.csu.tiger.ansible.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DemoVo {

    @JsonProperty("uid")
    private Long userId;

    private Long skuId;

    private Long skuNum;

    /**
     * 商品价格
     */
    private Long price;

    /**
     * 商品名称
     */
    private String name;

}
