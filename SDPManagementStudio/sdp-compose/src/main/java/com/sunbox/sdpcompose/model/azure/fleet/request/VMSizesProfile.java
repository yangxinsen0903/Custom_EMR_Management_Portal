package com.sunbox.sdpcompose.model.azure.fleet.request;

public class VMSizesProfile {
    private String name;
    private Integer rank;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    @Override
    public String toString() {
        return "VMSizesProfile{" +
                "name='" + name + '\'' +
                ", rank=" + rank +
                '}';
    }
}
