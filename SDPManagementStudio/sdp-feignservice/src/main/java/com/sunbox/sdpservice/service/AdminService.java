package com.sunbox.sdpservice.service;

import com.sunbox.domain.ResultMsg;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@FeignClient("IGS-ADMIN")
@RestController
public interface AdminService {

    @PostMapping("/admin/getUserInfoByToken")
    ResultMsg getUserInfoByToken(@RequestParam("token") String token);

}
