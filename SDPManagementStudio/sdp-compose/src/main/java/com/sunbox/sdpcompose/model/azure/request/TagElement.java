package com.sunbox.sdpcompose.model.azure.request;

public class TagElement {
    /**
     * 标签Key
     */
    private String name;
    /**
     * 标签值
     */
    private String value;

    public String getName() { return name; }
    public void setName(String value) { this.name = value; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    @Override
    public String toString() {
        return "TagElement{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}