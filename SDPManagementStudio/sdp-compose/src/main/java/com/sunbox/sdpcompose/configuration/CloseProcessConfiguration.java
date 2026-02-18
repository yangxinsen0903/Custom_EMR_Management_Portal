package com.sunbox.sdpcompose.configuration;

import com.sunbox.runtime.CloseEventPorcess;
import com.sunbox.runtime.ICloseProcess;
import com.sunbox.web.BaseCommon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author : [niyang]
 * @className : CloseProcessConfiguration
 * @description : [描述说明该类的功能]
 * @createTime : [2023/8/2 3:09 PM]
 */
@Configuration
public class CloseProcessConfiguration extends BaseCommon {

    @Autowired
    private ICloseProcess closeProcess;

    @Bean
    public boolean setClose(){
        CloseEventPorcess.setCloseProcess(closeProcess);
        getLogger().info("设置处理事件完成。");
        return true;
    }
}
