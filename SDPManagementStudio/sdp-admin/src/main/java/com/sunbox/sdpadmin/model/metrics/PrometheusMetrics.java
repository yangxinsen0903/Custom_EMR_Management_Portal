package com.sunbox.sdpadmin.model.metrics;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Prometheus指标对象
 * @date 2023/6/13
 */
public class PrometheusMetrics {
    private String help = "";
    private String type = "";

    private String name = "";

    private List<String> labelNames = new ArrayList<>();

    private List<String> contents = new ArrayList<>();

    /**
     * 构造一个新的Counter指标
     * @param name 指标名称
     * @param help 指标描述
     * @param labelNames 标签名称列表
     * @return Counter指标对象
     */
    public static PrometheusMetrics newCounter(String name, String help, List<String> labelNames) {
        PrometheusMetrics metrics = new PrometheusMetrics();
        metrics.name = name;
        metrics.help = help;
        metrics.type = "counter";
        metrics.labelNames = labelNames;
        return metrics;
    }

    /**
     * 添加一个指标项
     * @param subName 子指标名称
     * @param labels 标签值列表
     * @param val 指标值
     */
    public void addMetrics(String subName, List<String> labels, Double val) {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        if (StrUtil.isNotEmpty(subName)) {
            sb.append("_").append(subName);
        }

        if (CollectionUtil.isNotEmpty(labelNames)) {
            if (CollectionUtil.isEmpty(labels) || labels.size() != labelNames.size()) {
                throw new RuntimeException("labelNames和labels的数量不一致");
            }
            sb.append("{");
            for (int i = 0; i < labelNames.size(); i++) {
                String labelName = labelNames.get(i);
                String label = labels.get(i);
                if (StrUtil.isNotEmpty(label)) {
                    sb.append(labelName).append("=\"").append(label).append("\"");
                    if (i != labelNames.size() - 1) {
                        sb.append(",");
                    }
                }
            }
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append("}");
        }

        sb.append(" ").append(val);
        contents.add(sb.toString());
    }

    /**
     * 将指标对象转换为字符串，可以向客户端输出
     * @return
     */
    @Override
    public String toString() {
        return toStringBuilder().toString();
    }

    public StringBuilder toStringBuilder() {
        StringBuilder result = new StringBuilder();
        result.append("# HELP ").append(name).append(" ").append(help).append("\n")
                .append("# TYPE ").append(name).append(" ").append(type).append("\n");
        for (String content : contents) {
            result.append(content).append("\n");
        }
        return result;
    }

    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getLabelNames() {
        return labelNames;
    }

    public void setLabelNames(List<String> labelNames) {
        this.labelNames = labelNames;
    }
}
