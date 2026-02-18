package com.sunbox.sdpadmin.strategy;

import com.sunbox.domain.ConfCluster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClusterCreationStrategyFactory {
    @Autowired
    DirectlyClusterCreateStrategyImpl directlyClusterCreateStrategy;

    @Autowired
    SplitClusterCreateStrategyImpl splitClusterCreateStrategy;

    private ClusterCreationStrategyFactory() {
    }

    public ClusterCreationStrategy create(ConfCluster.CreationMode creationMode) {
        if (creationMode.equals(ConfCluster.CreationMode.SPLIT)) {
            return splitClusterCreateStrategy;
        } else {
            return directlyClusterCreateStrategy;
        }
    }
}
