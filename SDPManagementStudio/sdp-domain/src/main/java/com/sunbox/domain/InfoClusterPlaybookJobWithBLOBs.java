package com.sunbox.domain;

public class InfoClusterPlaybookJobWithBLOBs extends InfoClusterPlaybookJob {
    private String nodeList;

    private String scriptFileUri;

    public String getNodeList() {
        return nodeList;
    }

    public void setNodeList(String nodeList) {
        this.nodeList = nodeList == null ? null : nodeList.trim();
    }

    public String getScriptFileUri() {
        return scriptFileUri;
    }

    public void setScriptFileUri(String scriptFileUri) {
        this.scriptFileUri = scriptFileUri == null ? null : scriptFileUri.trim();
    }

    @Override
    public String toString() {
        return "InfoClusterPlaybookJobWithBLOBs{" +
                "nodeList='" + nodeList + '\'' +
                ", scriptFileUri='" + scriptFileUri + '\'' +
                '}';
    }
}