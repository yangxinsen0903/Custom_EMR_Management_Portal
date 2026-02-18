package com.sunbox.service.scale.strategy;

import com.sunbox.domain.ConfScalingTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ComposeStrategyFactory {
    @Autowired
    DeleteTaskVmScaleTaskStrategyImpl deleteTaskVmAdjustScaleTaskStrategy;

    @Autowired
    SpotScaleTaskStrategyImpl spotAdjustScaleTaskStrategy;

    @Autowired
    ManualScaleTaskStrategyImpl manualAdjustScaleTaskStrategy;

    @Autowired
    ScalingScaleTaskStrategyImpl scalingAdjustScaleTaskStrategy;

    @Autowired
    CompleteEvictVmScaleTaskStrategyImpl completeEvictVmScaleTaskStrategy;

    public ScaleTaskStrategy createScaleTaskStrategy(ConfScalingTask task){
        if(task.getOperatiionType().equals(ConfScalingTask.Operation_type_delete_Task_Vm)){
            return deleteTaskVmAdjustScaleTaskStrategy;
        } else if(Objects.equals(task.getOperatiionType(), ConfScalingTask.Operation_type_spot)){
            return spotAdjustScaleTaskStrategy;
        } else if(Objects.equals(task.getOperatiionType(), ConfScalingTask.Operation_type_Scaling)){
            return scalingAdjustScaleTaskStrategy;
        } else if(Objects.equals(task.getOperatiionType(), ConfScalingTask.Operation_type_Complete_Evict_Vm)){
            return completeEvictVmScaleTaskStrategy;
        } else {
            return manualAdjustScaleTaskStrategy;
        }
    }
}
