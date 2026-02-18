package com.sunbox.service;

import com.sunbox.domain.ApiAuthKey;
import com.sunbox.domain.AuthRequest;
import com.sunbox.domain.ResultMsg;

import javax.servlet.http.HttpServletRequest;

public interface AuthKeyService {


    /**
     * 查询全部的auth列表
     */
    ResultMsg queryAllAuthKeyList(AuthRequest authRequest);

    /**
     * 创建auth信息
     * @param authKey
     * @return
     */
    ResultMsg createAuthInfo(ApiAuthKey authKey,HttpServletRequest request);

    ResultMsg updateAuthKey(ApiAuthKey authKey,HttpServletRequest request);

    ResultMsg deleteAuthKey(ApiAuthKey authKey);

    /**
     * 查询当前登录用户所属的数据中心
     * @param request
     * @return
     */
    ResultMsg getRegionsForCurrentUser(HttpServletRequest request);
}
