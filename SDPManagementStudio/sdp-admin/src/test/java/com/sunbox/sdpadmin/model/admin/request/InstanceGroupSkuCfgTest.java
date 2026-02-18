package com.sunbox.sdpadmin.model.admin.request;/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author wangda
 * @date 2024/9/20
 */
class InstanceGroupSkuCfgTest {

    @Test
    void isMixedLVmSku() {
        InstanceGroupSkuCfg cfg = new InstanceGroupSkuCfg();
        cfg.setSkuNames(Arrays.asList("Standard_L8s_v3", "Standard_E4as_v5", "Standard_D8ds_v5"));
        Assertions.assertTrue(cfg.isMixedLVmSku());

        cfg.setSkuNames(Arrays.asList("Standard_E4as_v5", "Standard_D8ds_v5"));
        Assertions.assertFalse(cfg.isMixedLVmSku());
    }
}