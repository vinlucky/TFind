package com.tfind.web.controller;

import com.tfind.common.util.JwtUtil;
import com.tfind.web.service.ApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class LoginController {

    private final ApiService apiService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public LoginController(ApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping("/login")
    public String loginPage(HttpServletRequest request) {
        Boolean isMobile = (Boolean) request.getAttribute("isMobile");
        return Boolean.TRUE.equals(isMobile) ? "mobile/login" : "login/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String userId,
                        @RequestParam String password,
                        HttpServletRequest request,
                        RedirectAttributes redirectAttributes) {
        String token = apiService.login(userId, password);
        if (token != null) {
            String role = JwtUtil.getRoleFromToken(token, jwtSecret);
            if (!"ADMIN".equals(role)) {
                redirectAttributes.addFlashAttribute("error", "仅管理员账号可登录管理端");
                return "redirect:/login?error=true";
            }
            HttpSession session = request.getSession(true);
            session.setAttribute("token", token);
            session.setAttribute("userId", userId);
            return "redirect:/admin";
        }
        redirectAttributes.addFlashAttribute("error", "用户名或密码错误");
        return "redirect:/login?error=true";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }
}
