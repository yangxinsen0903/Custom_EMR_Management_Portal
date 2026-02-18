package com.sunbox.sdpservice.service;

import com.sunbox.domain.ResultMsg;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@FeignClient("IGS-USER")
@RestController
@RequestMapping("/user/admin")
public interface AdminUserService {

    /**
     * 获取服务端公钥
     * @return
     */
    @RequestMapping("/generateBase64PublicKey")
    Map<String, String> generateBase64PublicKey();

    @PostMapping("/userLogin")
    ResultMsg userLogin(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        @RequestParam("timest") String timest);




    @PostMapping("/getOrgUserInfoByUserId")
    ResultMsg getOrgUserInfoByUserId(@RequestParam("username") String username);
}
