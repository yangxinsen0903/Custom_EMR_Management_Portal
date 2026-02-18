package com.sunbox.sdpadmin.controller;

import com.sunbox.domain.ApiAuthKey;
import com.sunbox.domain.AuthRequest;
import com.sunbox.domain.ResultMsg;
import com.sunbox.service.AuthKeyService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/admin/api")
public class AuthKeyController {

    @Resource
    private AuthKeyService authKeyService;

    /**
     * 查询全部的auth列表
     * @param authRequest
     * @return
     */
    @RequestMapping(value = "/queryauthkeylist", method = RequestMethod.POST)
    public ResultMsg queryAuthKeyList(@RequestBody AuthRequest authRequest) {
        return authKeyService.queryAllAuthKeyList(authRequest);
    }

    /**
     * 创建authKey
     * @param authKey
     * @return
     */
    @RequestMapping(value = "/createauthkey", method = RequestMethod.POST)
    public ResultMsg createAuthKey(@RequestBody ApiAuthKey authKey,HttpServletRequest request) {
        return authKeyService.createAuthInfo(authKey,request);
    }

    /**
     * 修改authKey
     * @param authKey
     * @return
     */
    @RequestMapping(value = "/updateauthkey", method = RequestMethod.POST)
    public ResultMsg updateAuthKey(@RequestBody ApiAuthKey authKey,HttpServletRequest request) {
        return authKeyService.updateAuthKey(authKey,request);
    }

    /**
     * 删除authKey
     * @param authKey
     * @return
     */
    @RequestMapping(value = "/deleteauthkey", method = RequestMethod.POST)
    public ResultMsg deleteAuthKey(@RequestBody ApiAuthKey authKey) {
        return authKeyService.deleteAuthKey(authKey);
    }

    /**
     * 查询当前登录用户所属的数据中心
     * @param
     * @return
     */
    @RequestMapping(value = "/getRegionsForCurrentUser", method = RequestMethod.GET)
    public ResultMsg getRegionsForCurrentUser(HttpServletRequest request ) {
        return authKeyService.getRegionsForCurrentUser(request);
    }

}
