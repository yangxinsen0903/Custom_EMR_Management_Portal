/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.sdptask.controller;

import com.sunbox.sdptask.task.VmStatementTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangda
 * @date 2023/8/21
 */
@RestController
public class TestController {

    @Autowired
    VmStatementTask task ;

    @GetMapping("/testVmDiff")
    public String testVmDiff() {
        task.start();
        return "testVmDiff";
    }
}
