package com.sunbox.domain;

import lombok.Data;

@Data
public class DestroyTaskRequest {

    private String clusterId;

    private String clusterName;

    private Integer pageIndex;

    private Integer pageSize;

    private String destroyStatus;


    //数据库分页
    private Integer pageStart;
    private Integer pageLimit;

    public void page(){
        int pageIndex= (this.pageIndex == null ? 1 : this.pageIndex);
        int pageSize= (this.pageSize == null ? 20 : this.pageSize);
        this.pageStart = (pageIndex-1) * pageSize;
        this.pageLimit = pageSize;
    }
}
