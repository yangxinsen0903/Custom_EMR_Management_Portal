package com.sunbox.sdpcompose.manager;

import com.sunbox.sdpcompose.enums.IntegrationSystem;
import com.sunbox.sdpcompose.provider.AnsibleIntegrationProviderImpl;
import com.sunbox.sdpcompose.provider.IntegrationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AnalysisManager {
    @Autowired
    AnsibleIntegrationProviderImpl ansibleIntegrationProvider;

    public String collectLog(IntegrationSystem system, Object data){
        return createIntegrationProvider(system).collectLog(data);
    }

    public IntegrationProvider createIntegrationProvider(IntegrationSystem system){
        if(system.equals(IntegrationSystem.ANSIBLE)){
            return ansibleIntegrationProvider;
        }
        throw new IllegalArgumentException("无效的系统类型");
    }
}
