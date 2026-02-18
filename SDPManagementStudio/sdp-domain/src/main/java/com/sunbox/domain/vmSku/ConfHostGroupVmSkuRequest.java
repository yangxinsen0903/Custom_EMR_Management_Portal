package com.sunbox.domain.vmSku;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Date;

/**
 * 实例组SKU表(ConfHostGroupVmSku)实体类
 *
 * @author makejava
 * @since 2024-08-05 10:44:41
 */
@Data
public class ConfHostGroupVmSkuRequest implements Serializable {
    /**
     * 实例组配置ID,conf_cluster_vm表主键
     */
    @NotEmpty
    private String vmConfId;


    private Integer pageIndex;
    private Integer pageSize;

    private Integer pageStart;
    private Integer pageLimit;

    public void page(){
        int pageIndex= (this.pageIndex == null ? 1 : this.pageIndex);
        int pageSize= (this.pageSize == null ? 20 : this.pageSize);
        this.pageStart = (pageIndex-1) * pageSize;
        this.pageLimit = pageSize;
    }

}

