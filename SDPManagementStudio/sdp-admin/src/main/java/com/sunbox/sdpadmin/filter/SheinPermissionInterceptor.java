package com.sunbox.sdpadmin.filter;

import com.alibaba.fastjson.JSON;
import com.sunbox.dao.mapper.AuthKeyMapper;
import com.sunbox.domain.ApiAuthKey;
import com.sunbox.sdpadmin.controller.annotation.PermissionLimit;
import com.sunbox.sdpadmin.model.shein.response.SheinResponseModel;
import com.sunbox.service.consts.SheinParamConstant;
import com.sunbox.util.DistributedRedisLock;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;

@Component
public class SheinPermissionInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private DistributedRedisLock redisLock;
    @Autowired
    AuthKeyMapper authKeyMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return super.preHandle(request, response, handler);
        }
        HandlerMethod method = (HandlerMethod) handler;
        PermissionLimit permission = method.getMethodAnnotation(PermissionLimit.class);
        if(permission == null){
            //放行
            return super.preHandle(request, response, handler);
        }
        String[] sheinPermission = permission.sheinPermission();

        if (sheinPermission == null) {
            //放行
            return super.preHandle(request, response, handler);
        }

        boolean res = checkSheinPermission(request, response, handler, sheinPermission);
        if (res) {
            //放行
            return super.preHandle(request, response, handler);
        } else {
            PrintWriter writer = response.getWriter();
            SheinResponseModel sheinResponseModel = new SheinResponseModel();
            sheinResponseModel.setCode("403");
            sheinResponseModel.setMsg("Unauthorized access.");
            writer.write(JSON.toJSONString(sheinResponseModel));
            writer.flush();
            return false;
        }
    }

    // true 放行, false 拦截
    private boolean checkSheinPermission(HttpServletRequest request, HttpServletResponse response, Object handler, String[] sheinPermission) {
        if (sheinPermission == null || sheinPermission.length == 0) {
            return true;
        }
        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies == null){
            token = request.getHeader(SheinParamConstant.APITOKEN);
        }else {
            for (Cookie cookie : cookies) {
                if (SheinParamConstant.APITOKEN.equalsIgnoreCase(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        }

        if (StringUtils.isEmpty(token)) {
            return false;
        }
        boolean res = token.startsWith("t_");
        if (!res) {
            //放行
            return true;
        }
        String value = redisLock.getValue(token);
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        //ak 是否有效;3.读写权限是否正确
        List<ApiAuthKey> apiAuthKeys = authKeyMapper.selectAllByAk(value);
        if(CollectionUtils.isEmpty(apiAuthKeys)){
            return false;
        }
        String status = apiAuthKeys.get(0).getStatus();
        if (!SheinParamConstant.VALID.equalsIgnoreCase(status)) {
            return false;
        }
        String permiDB = apiAuthKeys.get(0).getPermission();
        String perInter = sheinPermission[0];
        // 数据库中是读,注解是读写,拦截
        if (SheinParamConstant.READ.equalsIgnoreCase(permiDB) && SheinParamConstant.READWRITE.equalsIgnoreCase(perInter)) {
            return false;
        }else {
            return true;
        }

    }

}
