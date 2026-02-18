package com.sunbox.sdpscale.controller;

import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpscale.constant.ScaleConstant;
import com.sunbox.sdpscale.mapper.ConfGroupElasticScalingRuleMapper;
import com.sunbox.sdpscale.service.RuleComputeService;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : [niyang]
 * @className : ScaleController
 * @description : [描述说明该类的功能]
 * @createTime : [2023/1/15 11:48 AM]
 */
@RestController
@RequestMapping("/scale")
public class ScaleController implements BaseCommonInterFace, ScaleConstant {
    @Autowired
    public RuleComputeService ruleComputeService;

    @Autowired
    public ConfGroupElasticScalingRuleMapper ruleMapper;

    /**
     * 弹性伸缩规则变更通知, 当弹性伸缩规则变更时, 需要从数据库重新加载规则
     * @return
     */
    @PostMapping("/metricChange")
    public ResultMsg metricChange() {
        return ruleComputeService.metricChangeNotify();
    }
}
