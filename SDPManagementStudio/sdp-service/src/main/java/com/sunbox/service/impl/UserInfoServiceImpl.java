package com.sunbox.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.sunbox.dao.mapper.BaseUserRoleMapper;
import com.sunbox.domain.BaseUserInfo;
import com.sunbox.domain.BaseUserRole;
import com.sunbox.service.IUserInfoService;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@Service
public class UserInfoServiceImpl implements IUserInfoService, BaseCommonInterFace {

    @Autowired
    private DistributedRedisLock redisLock;

    @Autowired
    private BaseUserRoleMapper baseUserRoleMapper;

    @Override
    public BaseUserInfo getUserRoleByRequest(HttpServletRequest request) {
        BaseUserInfo userInfoByToken = this.getUserInfoByRequest(request);
        return userInfoByToken;
    }

    @Override
    public BaseUserRole getUserRoleByUserName(String userName) {
        BaseUserRole baseUserRole = baseUserRoleMapper.selectRoleByUser(userName);
        return baseUserRole;
    }

    @Override
    public String getUserRoleStrByUserName(String userName) {
        BaseUserRole userRoleByUserName = getUserRoleByUserName(userName);
        if (userRoleByUserName == null || StrUtil.isEmpty(userRoleByUserName.getRoleCode())) {
            getLogger().error("UserInfoServiceImpl.getUserRoleStrByUserName userName is null:{} " + userName);
            return "";
        }
        return userRoleByUserName.getRoleCode();
    }

    public BaseUserInfo getUserInfoByRequest(HttpServletRequest request) {
        BaseUserInfo baseUserInfo = new BaseUserInfo();
        String token = null;
        if (request == null) return baseUserInfo;
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return baseUserInfo;
        for (Cookie cookie : cookies) {
            if ("sdptoken".equalsIgnoreCase(cookie.getName())) {
                token = cookie.getValue();
            }
        }
        BaseUserInfo userInfoByToken = this.getUserInfoByToken(token);
        if (userInfoByToken == null) return baseUserInfo;
        userInfoByToken.setPassword(null);
        return userInfoByToken;
    }

    public BaseUserInfo getUserInfoByToken(String token) {
        BaseUserInfo baseUserInfo = null;
        String baseUserInfoJson = redisLock.getValue(token);
        if (StringUtils.isNotBlank(baseUserInfoJson)) {
            try {
                baseUserInfo = JSON.parseObject(baseUserInfoJson, BaseUserInfo.class);
            } catch (Exception e) {
                getLogger().info("LoginService.getUserInfoByCookie baseUserInfo json conversion error. baseUserInfoJson: " + baseUserInfoJson);
            }
        }
        return baseUserInfo;
    }
}
