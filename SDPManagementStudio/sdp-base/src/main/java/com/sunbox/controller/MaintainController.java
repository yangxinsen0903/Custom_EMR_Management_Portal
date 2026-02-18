package com.sunbox.controller;

import com.sunbox.model.TestConnectTool;
import com.sunbox.runtime.RuntimeManager;
import com.sunbox.web.BaseCommonInterFace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/maintain")
public class MaintainController implements BaseCommonInterFace {
    @Autowired
    RuntimeManager runtimeManager;

    @GetMapping("/stop")
    public String stop(HttpServletRequest httpServletRequest) {
        runtimeManager.stopAndWait();
        return "OK";
    }

    @PostMapping(value = "/checkconnect")
    public Object checkconnect(@RequestBody String jsonStr,
                               HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse) {
        return new TestConnectTool(jsonStr, httpServletResponse).check();
    }
}