package com.sunbox.sdpadmin.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Created by Administrator on 2016/7/26.
 */
public class ServletUtils {
    private static ThreadLocal<HttpServletRequest> currentRequest = new ThreadLocal<HttpServletRequest>();
    private static ThreadLocal<HttpServletResponse> currentResponse = new ThreadLocal<HttpServletResponse>();

    public static void setRequest(HttpServletRequest request)
    {
        currentRequest.set(request);
    }

    public static HttpServletRequest getRequest()
    {
        return  currentRequest.get();
    }

    public static void removeRequest()
    {
        currentRequest.remove();
    }

    public static void setResponse(HttpServletResponse response)
    {
        currentResponse.set(response);
    }

    public static HttpServletResponse getResponse()
    {
        return currentResponse.get();
    }

    public static void removeResponse()
    {
        currentResponse.remove();
    }
}
