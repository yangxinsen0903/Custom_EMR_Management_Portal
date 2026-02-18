package com.sunbox.sdpadmin.model.admin.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Shein Cmdb返回来的Info字段对象
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SheinCmdbInfo {
    private Integer count;

    @JsonProperty("page_total")
    private Integer pageTotal;

    @JsonProperty("page_size")
    private Integer pageSize;

    private List<SheinCmdbInfoItem> results;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getPageTotal() {
        return pageTotal;
    }

    public void setPageTotal(Integer pageTotal) {
        this.pageTotal = pageTotal;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public List<SheinCmdbInfoItem> getResults() {
        return results;
    }

    public void setResults(List<SheinCmdbInfoItem> results) {
        this.results = results;
    }
}