package com.sunbox.sdpcloud.regserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/cloud")
public class MainController {

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/deal")
    public Object rest(
            @RequestParam("instanceName") String instanceName,
            @RequestParam("interfacename") String interfaceName,
            @RequestParam("paramName") String paramName,
            @RequestParam("paramValue") String paramValue
    ) {
        interfaceName = interfaceName.replaceAll("-","/");
        ResponseEntity<String> forObject = restTemplate.getForEntity("http://"+instanceName+"/"+interfaceName+"?"+paramName+"="+paramValue, String.class);
        return forObject.getBody();
    }

}
