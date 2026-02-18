package com.sunbox.sdpadmin.filter;

import com.alibaba.fastjson.JSON;
import com.sunbox.domain.BaseUserRole;
import com.sunbox.sdpadmin.controller.annotation.PermissionLimit;
import com.sunbox.sdpadmin.model.shein.response.SheinResponseModel;
import com.sunbox.sdpadmin.service.LoginService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * 权限拦截器
 */
@Component
public class PermissionInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private LoginService loginService;

    /**
     *  放行的: 没加权限注解的,用户权限为空,有权限的.
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return super.preHandle(request, response, handler);
        }
        HandlerMethod method = (HandlerMethod) handler;
        PermissionLimit permission = method.getMethodAnnotation(PermissionLimit.class);
        if (permission == null) {
            //放行
            return super.preHandle(request, response, handler);
        }
        BaseUserRole baseUserRole = loginService.getUserRoleByRequest(request);
        if (baseUserRole == null || StringUtils.isEmpty(baseUserRole.getRoleCode())) {
            //放行
            return super.preHandle(request, response, handler);
        }
        String roleCode = baseUserRole.getRoleCode();
        String[] role = permission.role();
        if (role == null || role.length == 0) {
            //放行
            return super.preHandle(request, response, handler);
        } else {
            boolean res = Arrays.stream(role).anyMatch(roleCode::equalsIgnoreCase);
            if (res) {
                //放行
                return super.preHandle(request, response, handler);
            }
            PrintWriter writer = response.getWriter();
            SheinResponseModel sheinResponseModel = new SheinResponseModel();
            sheinResponseModel.setCode("403");
            sheinResponseModel.setMsg("Unauthorized access.");
            writer.write(JSON.toJSONString(sheinResponseModel));
            writer.flush();
            return false;
        }
    }

}
