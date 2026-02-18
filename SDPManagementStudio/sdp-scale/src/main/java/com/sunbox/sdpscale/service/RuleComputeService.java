package com.sunbox.sdpscale.service;

import com.sunbox.domain.Metric;
import com.sunbox.domain.ResultMsg;

import java.util.List;

public interface RuleComputeService {
    ResultMsg reloadScalingRule();

    ResultMsg compute(List<Metric> metricList);

    ResultMsg metricChangeNotify();
}
