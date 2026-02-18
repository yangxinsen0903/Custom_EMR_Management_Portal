/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.sdpadmin.controller;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.sunbox.domain.DiffVm;
import com.sunbox.domain.SystemEvent;
import com.sunbox.sdpadmin.controller.annotation.PermissionLimit;
import com.sunbox.sdpadmin.service.OpsService;
import com.sunbox.service.ISystemEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 检查VM差异性Controller<br/>
 * 1. SDP与Yarn的差异<br/>
 * 2. SDP与Azure的差异<br/>
 * @author wangda
 * @date 2023/7/25
 */
@Slf4j
@RestController
public class DiffVmController {

    @Autowired
    private OpsService opsService;

    @Autowired
    private ISystemEventService systemEventService;

    @RequestMapping(value = "/ops/yarnDiffVm", produces = "text/plain; version=0.0.4; charset=utf-8")
    @PermissionLimit(role = {"Maintainer","Administrator"})
    public String yarnDiffVm(@RequestParam(value = "format", required = false) String format) {
        try {
            List<DiffVm> diffVms = opsService.generateSdpYarnDiffVms();
            if (StrUtil.equalsIgnoreCase(format, "json")) {
                return JSONArray.toJSONString(diffVms);
            } else {
                StringBuilder sb = new StringBuilder();
                if (diffVms.size() > 0) {
                    sb.append(diffVms.get(0).toCSVHeader()).append("\n");
                }
                for (DiffVm diffVm : diffVms) {
                    sb.append(diffVm.toCSVLine()).append("\n");
                }
                return sb.toString();
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return ExceptionUtil.stacktraceToString(ex);
        }
    }

    @RequestMapping(value = "/ops/systemEvent", produces = "text/plain; version=0.0.4; charset=utf-8")
    @PermissionLimit(role = {"Maintainer","Administrator"})
    public String systemEvent(@RequestParam(value = "format", required = false) String format,
                              @RequestParam(value = "page", required = false) Integer page,
                              @RequestParam(value = "size", required = false) Integer size) {
        log.info("收到查询系统事件请求, format={}, page={}, size={}", format, page, size);
        if (Objects.isNull(page)) {
            page = 1;
        }
        if (Objects.isNull(size)) {
            size = 100;
        }

        try {
            List<SystemEvent> systemEvents = systemEventService.querySystemEvent(page, size);
            if (StrUtil.equalsIgnoreCase(format, "json")) {
                return JSONArray.toJSONString(systemEvents);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(SystemEvent.toCSVHeader()).append("\n");

                for (SystemEvent event : systemEvents) {
                    sb.append(event.toCSVLine()).append("\n");
                }
                return sb.toString();
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return ExceptionUtil.stacktraceToString(ex);
        }

    }
}
