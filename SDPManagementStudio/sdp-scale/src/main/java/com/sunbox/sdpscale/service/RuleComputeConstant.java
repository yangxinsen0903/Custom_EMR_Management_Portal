package com.sunbox.sdpscale.service;

public interface RuleComputeConstant {
    /**
     * 规则验证通过
     */
    int rule_validate_pass = 1;
    int rule_validate_fail = 0;
    int scale_type_in = 0;
    int scale_type_out = 1;
    int task_runing = 1;
    int task_create_failure = 0;
    /**
     * 规则停用
     */
    int rule_isValid_disabled = 0;
    /**
     * 规则启用
     */
    int rule_isValid_enable = 1;
    /**
     * 任务失败需要停止规则
     */
    String task_create_failure_stop_collect = "404";
}
