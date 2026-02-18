package com.sunbox.domain;

import java.util.Date;

public class JobResults {
    private Integer id;

    private String jobid;

    private Date createtime;

    private String ansiblelog;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getJobid() {
        return jobid;
    }

    public void setJobid(String jobid) {
        this.jobid = jobid == null ? null : jobid.trim();
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public String getAnsiblelog() {
        return ansiblelog;
    }

    public void setAnsiblelog(String ansiblelog) {
        this.ansiblelog = ansiblelog == null ? null : ansiblelog.trim();
    }

    @Override
    public String toString() {
        return "JobResults{" +
                "id=" + id +
                ", jobid='" + jobid + '\'' +
                ", createtime=" + createtime +
                ", ansiblelog='" + ansiblelog + '\'' +
                '}';
    }
}