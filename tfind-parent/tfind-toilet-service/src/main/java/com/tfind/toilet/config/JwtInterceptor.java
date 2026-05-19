package com.tfind.toilet.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Value("${jwt.secret:tfind-secret-key}")
    private String secret;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                request.setAttribute("userId", claims.getSubject());
                request.setAttribute("role", claims.get("role", String.class));
                return true;
            } catch (Exception e) {
                response.setStatus(401);
                response.getWriter().write("{\"code\":401,\"message\":\"Invalid token\"}");
                return false;
            }
        }

        response.setStatus(401);
        response.getWriter().write("{\"code\":401,\"message\":\"Missing token\"}");
        return false;
    }
}
