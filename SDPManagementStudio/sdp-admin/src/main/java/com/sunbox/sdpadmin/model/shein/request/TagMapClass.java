package com.sunbox.sdpadmin.model.shein.request;

/**
 * 标签
 */
public class TagMapClass {
    private String childService;
    private String dep;
    private String env;
    private String _For;
    private String remark;
    private String service;
    private String svc;
    private String svcid;

    public String getChildService() { return childService; }
    public void setChildService(String value) { this.childService = value; }

    public String getDep() { return dep; }
    public void setDep(String value) { this.dep = value; }

    public String getEnv() { return env; }
    public void setEnv(String value) { this.env = value; }

    public String getFor() { return _For; }
    public void setFor(String value) { this._For = value; }

    public String getRemark() { return remark; }
    public void setRemark(String value) { this.remark = value; }

    public String getService() { return service; }
    public void setService(String value) { this.service = value; }

    public String getsvc() { return svc; }
    public void setsvc(String value) { this.svc = value; }

    public String getSvcid() { return svcid; }
    public void setSvcid(String value) { this.svcid = value; }
}