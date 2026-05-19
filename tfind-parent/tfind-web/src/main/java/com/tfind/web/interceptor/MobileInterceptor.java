package com.tfind.web.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MobileInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return true;
        }
        String ua = userAgent.toLowerCase();
        boolean isMobile = ua.contains("mobile")
                || ua.contains("android")
                || ua.contains("iphone")
                || ua.contains("ipad")
                || ua.contains("ipod")
                || ua.contains("blackberry")
                || ua.contains("windows phone")
                || ua.contains("mobi");

        if (isMobile) {
            request.setAttribute("isMobile", true);
        } else {
            request.setAttribute("isMobile", false);
        }
        return true;
    }
}
