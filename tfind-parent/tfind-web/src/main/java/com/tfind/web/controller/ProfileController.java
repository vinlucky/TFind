package com.tfind.web.controller;

import com.tfind.web.service.ApiService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

@Controller
public class ProfileController {

    private final ApiService apiService;

    public ProfileController(ApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping("/change-password")
    public String changePasswordPage() {
        return "profile/change-password";
    }

    @PostMapping("/change-password")
    @ResponseBody
    public Map<String, Object> changePassword(@RequestParam String oldPassword,
                                               @RequestParam String newPassword,
                                               HttpServletRequest request) {
        String token = (String) request.getSession().getAttribute("token");
        String userId = (String) request.getSession().getAttribute("userId");
        boolean success = apiService.changePassword(userId, oldPassword, newPassword, token);
        if (success) {
            return Collections.singletonMap("success", true);
        }
        return Collections.singletonMap("success", false);
    }
}
