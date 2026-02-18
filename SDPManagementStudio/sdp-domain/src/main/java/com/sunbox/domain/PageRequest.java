package com.sunbox.domain;
/**
 * 分页
 * @author : gmq
 */
public class PageRequest {
    private Integer pageIndex;
    private Integer pageSize;

    private Integer getPageIndex() {
        return this.pageIndex == null ? 1 : this.pageIndex;
    }

    private Integer getPageSize() {
        return this.pageSize == null ? 20 : this.pageSize;
    }

    private Integer getPageStart() {
        return (getPageIndex() - 1) * getPageSize();
    }
    //  -----------
    private void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    private void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    // 计算分页,用于sql
    public void page(){
        this.pageIndex=getPageStart();
        this.pageSize=getPageSize();
    }

}
