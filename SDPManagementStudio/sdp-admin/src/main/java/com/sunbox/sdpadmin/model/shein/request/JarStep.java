package com.sunbox.sdpadmin.model.shein.request;

import java.util.List;

public class JarStep {
    /**
     * Jar执行参数
     */
    private List<String> args;
    /**
     * Jar包路径
     */
    private String jarName;

    public List<String> getArgs() { return args; }
    public void setArgs(List<String> value) { this.args = value; }

    public String getJarName() { return jarName; }
    public void setJarName(String value) { this.jarName = value; }
}