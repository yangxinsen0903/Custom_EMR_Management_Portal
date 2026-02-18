package com.sunbox.sdptask.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class OperationPlanUtils {
    public static String getStateText(Integer state, Double percent) {
        if (state == null) {
            return "未知";
        }

        String percentText = null;
        if (percent == null) {
            percentText = "0%";
        } else {
            percentText = percent.intValue() + "%";
        }

        switch (state) {
            case 0:
                return "已创建";
            case 1:
                return "执行中(" + percentText + ")";
            case 2:
                return "执行完成";
            case -1:
                return "执行超时(" + percentText + ")";
            case -2:
                return "执行失败(" + percentText + ")";
            default:
                return "未知" + state + "(" + percentText + ")";
        }
    }

    public static String getPlanName(String jobName) {
        List<GetJobQueryParamDictOutput.KvItem> kvItems = getJobQueryParamDict().getJobNames();

        for (GetJobQueryParamDictOutput.KvItem kvItem : kvItems) {
            if (StringUtils.equalsIgnoreCase(kvItem.getKey(), jobName)) {
                return kvItem.getValue();
            }
        }
        return jobName;
    }

    public static GetJobQueryParamDictOutput getJobQueryParamDict() {
        GetJobQueryParamDictOutput output = new GetJobQueryParamDictOutput();
        output.setJobNames(new ArrayList<>());
        output.getJobNames().add(new GetJobQueryParamDictOutput.KvItem("create_cluster", "创建集群"));
        output.getJobNames().add(new GetJobQueryParamDictOutput.KvItem("destroy_cluster", "销毁集群"));
        output.getJobNames().add(new GetJobQueryParamDictOutput.KvItem("restart_service", "重启服务"));
        output.getJobNames().add(new GetJobQueryParamDictOutput.KvItem("clean_exception_vm", "清理异常VM"));
        output.getJobNames().add(new GetJobQueryParamDictOutput.KvItem("collect_log", "日志收集"));
        output.getJobNames().add(new GetJobQueryParamDictOutput.KvItem("scale_out_part", "磁盘扩容"));
        output.getJobNames().add(new GetJobQueryParamDictOutput.KvItem("scale_out_split", "增量创建扩容"));
        output.getJobNames().add(new GetJobQueryParamDictOutput.KvItem("scale_out_scaling", "弹性扩容"));
        output.getJobNames().add(new GetJobQueryParamDictOutput.KvItem("scale_out_add_group", "新增实例组"));
        output.getJobNames().add(new GetJobQueryParamDictOutput.KvItem("scale_out_manual", "手动扩容"));
        output.getJobNames().add(new GetJobQueryParamDictOutput.KvItem("scale_out_spot", "竞价买入"));
        output.getJobNames().add(new GetJobQueryParamDictOutput.KvItem("scale_in_scaling", "弹性缩容"));
        output.getJobNames().add(new GetJobQueryParamDictOutput.KvItem("scale_in_manual", "手动缩容"));
        output.getJobNames().add(new GetJobQueryParamDictOutput.KvItem("scale_in_spot", "竞价逐出"));
        output.getJobNames().add(new GetJobQueryParamDictOutput.KvItem("scale_in_delete_vm", "删除实例"));
        output.getJobNames().add(new GetJobQueryParamDictOutput.KvItem("scale_in_delete_group", "删除实例组"));
        output.getJobNames().add(new GetJobQueryParamDictOutput.KvItem("runuserscript", "用户自定义脚本"));

        output.setJobStates(new ArrayList<>());
        output.getJobStates().add(new GetJobQueryParamDictOutput.KvItem("0", "已创建"));
        output.getJobStates().add(new GetJobQueryParamDictOutput.KvItem("1", "执行中"));
        output.getJobStates().add(new GetJobQueryParamDictOutput.KvItem("2", "执行完成"));
        output.getJobStates().add(new GetJobQueryParamDictOutput.KvItem("-1", "执行超时"));
        output.getJobStates().add(new GetJobQueryParamDictOutput.KvItem("-2", "执行失败"));
        return output;
    }
}
