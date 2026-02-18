package com.sunbox.sdpcompose.model.azure.response;

import java.io.Serializable;

/**
 * @Description: sku response
 * @Title: BaseSkuResponse
 * @Package: com.sunbox.sdpcompose.model.azure.response
 * @Author: wangshihao
 * @Copyright: 版权
 * @CreateTime: 2022/12/6 19:07
 */
public class BaseSkuResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 名称
     * */
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
