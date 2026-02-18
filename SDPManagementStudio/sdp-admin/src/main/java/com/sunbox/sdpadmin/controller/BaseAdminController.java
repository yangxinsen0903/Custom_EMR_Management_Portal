package com.sunbox.sdpadmin.controller;

import com.alibaba.fastjson.JSON;
import com.sunbox.domain.BaseUserInfo;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by yfq
 */
public class BaseAdminController implements BaseCommonInterFace {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private DistributedRedisLock redisLock;

    // @ModelAttribute
    // public void setReqAndRes(HttpServletRequest request, HttpServletResponse response) {
    //     if (response != null) {
    //         response.setCharacterEncoding("utf-8");
    //         response.setHeader("Cache-Control", "no-cache");
    //         response.setHeader("Pragma", "no-cache");
    //         response.setHeader("Expires", "-1");
    //         ServletUtils.setResponse(response);
    //     }
    //     ServletUtils.setRequest(request);
    // }

    // public HttpServletRequest request() {
    //     return ServletUtils.getRequest();
    // }

    // public HttpServletResponse response() {
    //     return ServletUtils.getResponse();
    // }

    public BaseUserInfo getUserInfo() {
        BaseUserInfo baseUserInfo = new BaseUserInfo();
        String token = null;

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return baseUserInfo;
        }

        for (Cookie cookie : cookies) {
            switch (cookie.getName()) {
                case "sdptoken":
                    token = cookie.getValue();
                default:
                    break;
            }
        }

        if (StringUtils.isNotBlank(token)) {
            String baseUserInfoJson = redisLock.getValue(token);
            if (StringUtils.isNotBlank(baseUserInfoJson)) {
                try {
                    baseUserInfo = JSON.parseObject(baseUserInfoJson, BaseUserInfo.class);
                } catch (Exception e) {
                    getLogger().info("BaseAdminController.getUserInfo baseUserInfo json conversion error. baseUserInfoJson: " + baseUserInfoJson);
                }
            }
        }
        return baseUserInfo;
    }
}
