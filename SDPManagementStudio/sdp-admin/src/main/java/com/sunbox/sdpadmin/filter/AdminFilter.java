package com.sunbox.sdpadmin.filter;

import com.alibaba.fastjson.JSON;
import com.sunbox.domain.BaseUserInfo;
import com.sunbox.domain.ResultMsg;
import com.sunbox.sdpadmin.core.model.AdminRequrlrole;
import com.sunbox.sdpadmin.service.AdminRequrlroleService;
import com.sunbox.sdpadmin.util.RedisUtil;
import com.sunbox.sdpadmin.util.UrlMatchUtils;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@WebFilter(urlPatterns = "/admin/*",filterName = "adminFilter")
public class AdminFilter implements Filter,BaseCommonInterFace {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private AdminRequrlroleService adminRequrlroleService;

    @Autowired
    private DistributedRedisLock redisLock;

    // 不需要过滤的地址
    private static Set<String> greenUrlSet = new HashSet<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 配置不需要过滤的地址
        greenUrlSet.add("/admin/login");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        request.setCharacterEncoding("utf-8");
        String url = httpServletRequest.getRequestURI(); // 请求路由地址

        httpServletResponse.addHeader("Access-Control-Allow-Origin", "*");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", "*");
        httpServletResponse.addHeader("Access-Control-Allow-Methods", "GET, POST");
        httpServletResponse.addHeader("Access-Control-Max-Age", "1800");

        String requestURI = httpServletRequest.getRequestURI();

        boolean iswhite=false;
        for (String uri : greenUrlSet){
            if (requestURI.startsWith(uri)) {
                iswhite=true;
                break;
            }
        }

        if (!iswhite) {
            BaseUserInfo baseUserInfo = null;
            String token = null;

            Cookie[] cookies = httpServletRequest.getCookies();
            if (cookies == null) {
                PrintWriter writer = httpServletResponse.getWriter();
                ResultMsg resMessage = new ResultMsg();
                resMessage.setResult(false);
                resMessage.setRetcode("404");
                resMessage.setMsg(new String("Please login again".getBytes(), StandardCharsets.UTF_8));
                writer.write(JSON.toJSONString(resMessage));
                writer.flush();
                return;
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
                        getLogger().info("AdminFilter.doFilter baseUserInfo json conversion error. baseUserInfoJson: " + baseUserInfoJson);
                    }
                }
            }

            if (baseUserInfo == null) {
                // 重定向到登录页
                PrintWriter writer = httpServletResponse.getWriter();
                ResultMsg resMessage = new ResultMsg();
                resMessage.setResult(false);
                resMessage.setRetcode("404");
                resMessage.setErrorMsg(new String("Please login again".getBytes(), StandardCharsets.UTF_8));
                           writer.write(JSON.toJSONString(resMessage));
                writer.flush();
                return;
            }
        }
        chain.doFilter(httpServletRequest, httpServletResponse);
    }

    @Override
    public void destroy() {

    }

    private String getIPAddress(HttpServletRequest request) {
        String ip = null;

        //X-Forwarded-For：Squid 服务代理
        String ipAddresses = request.getHeader("X-Forwarded-For");

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //Proxy-Client-IP：apache 服务代理
            ipAddresses = request.getHeader("Proxy-Client-IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //WL-Proxy-Client-IP：weblogic 服务代理
            ipAddresses = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //HTTP_CLIENT_IP：有些代理服务器
            ipAddresses = request.getHeader("HTTP_CLIENT_IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //X-Real-IP：nginx服务代理
            ipAddresses = request.getHeader("X-Real-IP");
        }

        //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (ipAddresses != null && ipAddresses.length() != 0) {
            ip = ipAddresses.split(",")[0];
        }

        //还是不能获取到，最后再通过request.getRemoteAddr();获取
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /*
     * 返回true表示次请求需要登陆，返回false表示不需要登陆直接放行
     * anno 无需登陆，authc 需要登陆
     * */
    private boolean checkURIIsReqestLogin(String url) {
        String key = "mch_admin_request_url_check";
        redisUtil.del(key);
        String content = redisUtil.getValue(key);
        List<AdminRequrlrole> requrlroles = new ArrayList<AdminRequrlrole>();
        if (StringUtils.isNotEmpty(content)) {
            List<AdminRequrlrole> rediscontent = JSON.parseArray(content, AdminRequrlrole.class);
            requrlroles.addAll(rediscontent);
        } else {
            requrlroles.addAll(adminRequrlroleService.selectAll());
            redisUtil.save(key, JSON.toJSONString(requrlroles));
        }
        AdminRequrlrole matchrequrlrole = null;
        for (AdminRequrlrole requrlrole : requrlroles) {
            if (UrlMatchUtils.wildcardStarMatch(requrlrole.getRequrl(), url)) {
                //如果uri匹配到，则直接结束循环
                matchrequrlrole = requrlrole;
                break;
            }
        }
        if (matchrequrlrole != null) {
            //不需要登陆
            if ("anno".equals(matchrequrlrole.getIslogin())) {
                return false;
            }
            //需要登陆
            if ("authc".equals(matchrequrlrole.getIslogin())) {
                return true;
            }
        }
        return true;
    }

    private void tologin(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        httpServletRequest.setAttribute("loginmessage", "您没有登陆，请登陆后继续您的操作！");
        httpServletRequest.getRequestDispatcher("/adminv2/toLogin").forward(httpServletRequest, httpServletResponse);
    }

}
