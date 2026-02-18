package com.sunbox.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;

public class HttpTools {

    /**
     * 获取cooike
     *
     * @param httpServletRequest
     * @param cookieName
     * @return
     */
    public static String getcooike(HttpServletRequest httpServletRequest, String cookieName){
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies == null || cookies.length == 0) {

            return "";

        }
        for (Cookie c : cookies) {
            if (c.getName().equals(cookieName)) {
                String value = "";
                try {
                    value = URLDecoder.decode(c.getValue(), "UTF-8");
                } catch (Exception e) {
                    value = c.getValue();
                }

                return value;
            }
        }
        return "";

    }
}
