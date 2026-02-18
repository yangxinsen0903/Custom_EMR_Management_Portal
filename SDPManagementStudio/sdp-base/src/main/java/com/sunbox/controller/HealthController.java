package com.sunbox.controller;

import com.sunbox.runtime.RuntimeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;

@RestController
@RequestMapping("/health")
public class HealthController {
    @Autowired
    RuntimeManager runtimeManager;

    @GetMapping("/ready")
    public String ready() {
        return "OK";
    }

    @GetMapping("/status")
    public String status() {
        if(runtimeManager.isApiRequestBlocked()){
            throw new HttpServerErrorException(HttpStatus.BAD_REQUEST);
        }
        return "OK";
    }
}
