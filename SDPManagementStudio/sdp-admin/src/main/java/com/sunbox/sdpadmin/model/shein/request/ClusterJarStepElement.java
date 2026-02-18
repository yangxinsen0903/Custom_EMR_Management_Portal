package com.sunbox.sdpadmin.model.shein.request;

public class ClusterJarStepElement {
    /**
     * 失败后操作，CONTINUE
     */
    private String actionOnFailure;
    private JarStep jarStep;

    public String getActionOnFailure() { return actionOnFailure; }
    public void setActionOnFailure(String value) { this.actionOnFailure = value; }

    public JarStep getJarStep() { return jarStep; }
    public void setJarStep(JarStep value) { this.jarStep = value; }
}