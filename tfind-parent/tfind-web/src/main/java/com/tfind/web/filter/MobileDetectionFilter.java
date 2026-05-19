package com.tfind.web.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class MobileDetectionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String userAgent = request.getHeader("User-Agent");
        boolean isMobile = false;
        if (userAgent != null) {
            String ua = userAgent.toLowerCase();
            isMobile = ua.contains("mobile")
                    || ua.contains("android")
                    || ua.contains("iphone")
                    || ua.contains("ipad")
                    || ua.contains("ipod")
                    || ua.contains("blackberry")
                    || ua.contains("windows phone")
                    || ua.contains("mobi");
        }
        request.setAttribute("isMobile", isMobile);
        filterChain.doFilter(request, response);
    }
}
