package com.sunbox.sdpadmin.filter;

import com.alibaba.fastjson.JSON;
import com.sunbox.domain.BaseUserInfo;
import com.sunbox.sdpadmin.model.shein.response.SheinResponseModel;
import com.sunbox.servletWrapper.RequestWrapper;
import com.sunbox.servletWrapper.ResponseWrapper;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.web.BaseCommonInterFace;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@WebFilter(urlPatterns = "/sheinapi/*", filterName = "sheinApiFilter")
public class SheinApiFilter implements Filter, BaseCommonInterFace {

    @Autowired
    private DistributedRedisLock redisLock;

    @Value("${shein.api.token:85611fe214304e9dd15dbd10db6227ea}")
    private String sheinApiToken;

    // 不需要过滤的地址
    private static Set<String> greenUrlSet = new HashSet<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 配置不需要过滤的地址
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        long start = System.currentTimeMillis();
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        RequestWrapper requestWrapper = new RequestWrapper(httpServletRequest);
        ResponseWrapper responseWrapper = new ResponseWrapper(httpServletResponse);

        servletRequest.setCharacterEncoding("utf-8");
//        String url = httpServletRequest.getRequestURI(); // 请求路由地址

        httpServletResponse.addHeader("Access-Control-Allow-Origin", "*");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", "*");
        httpServletResponse.addHeader("Access-Control-Allow-Methods", "GET, POST");
        httpServletResponse.addHeader("Access-Control-Max-Age", "1800");

        String requestURI = httpServletRequest.getRequestURI();

        // 打印请求内容
        getLogger().info("TraceSheinApi Enter:{}, Method:{}, RequestBody:{}",
                requestURI, requestWrapper.getMethod(), requestWrapper.getBody());

        boolean iswhite = false;
        for (String uri : greenUrlSet) {
            if (requestURI.startsWith(uri)) {
                iswhite = true;
                break;
            }
        }

        if (!iswhite) {
            BaseUserInfo baseUserInfo = null;
            String token = null;

            Cookie[] cookies = httpServletRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    switch (cookie.getName()) {
                        case "apitoken":
                            token = cookie.getValue();
                        default:
                            break;
                    }
                }
            }

            if (StringUtils.isEmpty(token)) {
                token = httpServletRequest.getHeader("apitoken");
            }

            if (StringUtils.isEmpty(token)) {
                PrintWriter writer = httpServletResponse.getWriter();
                SheinResponseModel sheinResponseModel = new SheinResponseModel();
                sheinResponseModel.setCode("403");
                sheinResponseModel.setMsg("Unauthorized access.");
                writer.write(JSON.toJSONString(sheinResponseModel));
                writer.flush();
                return;
            }
            // 兼容老token, 新token: t_ + token
            if (StringUtils.isNotBlank(token) && (token.equals(sheinApiToken) || token.startsWith("t_"))) {
                baseUserInfo = new BaseUserInfo();
            }else{
                getLogger().info("sheinApiToken Error: {}", token);
            }

            if (baseUserInfo == null) {
                // 重定向到登录页
                PrintWriter writer = httpServletResponse.getWriter();
                SheinResponseModel sheinResponseModel = new SheinResponseModel();
                sheinResponseModel.setCode("403");
                sheinResponseModel.setMsg("Unauthorized access.");
                writer.write(JSON.toJSONString(sheinResponseModel));
                writer.flush();
                return;
            }
        }
        filterChain.doFilter(requestWrapper, responseWrapper);
        long elapse = System.currentTimeMillis() - start;
        getLogger().info("TraceSheinApi Exit: {} ms, {}, Method:{}",
                elapse, requestURI, requestWrapper.getMethod());
        // 将响应内容写回原始响应
        httpServletResponse.getOutputStream().write(responseWrapper.getContent().getBytes(StandardCharsets.UTF_8));

    }

    @Override
    public void destroy() {

    }

    private String getIPAddress(HttpServletRequest request) {
        String ip = null;

        // X-Forwarded-For：Squid 服务代理
        String ipAddresses = request.getHeader("X-Forwarded-For");

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            // Proxy-Client-IP：apache 服务代理
            ipAddresses = request.getHeader("Proxy-Client-IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            // WL-Proxy-Client-IP：weblogic 服务代理
            ipAddresses = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            // HTTP_CLIENT_IP：有些代理服务器
            ipAddresses = request.getHeader("HTTP_CLIENT_IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            // X-Real-IP：nginx服务代理
            ipAddresses = request.getHeader("X-Real-IP");
        }

        // 有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (ipAddresses != null && ipAddresses.length() != 0) {
            ip = ipAddresses.split(",")[0];
        }

        // 还是不能获取到，最后再通过request.getRemoteAddr();获取
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
