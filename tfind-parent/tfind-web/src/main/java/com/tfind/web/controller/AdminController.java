package com.tfind.web.controller;

import com.tfind.web.service.ApiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ApiService apiService;

    public AdminController(ApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping
    public String dashboard(Model model, HttpServletRequest request) {
        String token = getToken(request);
        List<Map<String, Object>> users = apiService.listUsers(token);
        List<Map<String, Object>> toilets = apiService.listToilets(token);
        List<Map<String, Object>> pending = apiService.listPendingToilets(token);
        model.addAttribute("userCount", users != null ? users.size() : 0);
        model.addAttribute("toiletCount", toilets != null ? toilets.size() : 0);
        model.addAttribute("pendingCount", pending != null ? pending.size() : 0);

        Boolean isMobile = (Boolean) request.getAttribute("isMobile");
        if (Boolean.TRUE.equals(isMobile)) {
            model.addAttribute("userId", request.getSession().getAttribute("userId"));
            return "mobile/dashboard";
        }
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(Model model, HttpServletRequest request) {
        String token = getToken(request);
        List<Map<String, Object>> users = apiService.listUsers(token);
        model.addAttribute("users", users);
        return getTemplate(request, "admin/users", "mobile/users");
    }

    @GetMapping("/toilets")
    public String toilets(Model model, HttpServletRequest request) {
        String token = getToken(request);
        List<Map<String, Object>> toilets = apiService.listToilets(token);
        model.addAttribute("toilets", toilets);
        return getTemplate(request, "admin/toilets", "mobile/toilets");
    }

    @GetMapping("/toilets/pending")
    public String pendingToilets(Model model, HttpServletRequest request) {
        String token = getToken(request);
        List<Map<String, Object>> pending = apiService.listPendingToilets(token);
        model.addAttribute("toilets", pending);
        return getTemplate(request, "admin/pending", "mobile/pending");
    }

    @PostMapping("/toilet/{id}/approve")
    @ResponseBody
    public Map<String, Object> approveToilet(@PathVariable String id, HttpServletRequest request) {
        String token = getToken(request);
        apiService.approveToilet(id, token);
        return Collections.singletonMap("success", true);
    }

    @PostMapping("/toilet/{id}/reject")
    @ResponseBody
    public Map<String, Object> rejectToilet(@PathVariable String id, HttpServletRequest request) {
        String token = getToken(request);
        apiService.rejectToilet(id, token);
        return Collections.singletonMap("success", true);
    }

    @GetMapping("/deleted")
    public String deleted(Model model, HttpServletRequest request) {
        String token = getToken(request);
        List<Map<String, Object>> deletedUsers = apiService.listDeletedUsers(token);
        List<Map<String, Object>> deletedToilets = apiService.listDeletedToilets(token);
        model.addAttribute("deletedUsers", deletedUsers);
        model.addAttribute("deletedToilets", deletedToilets);
        return getTemplate(request, "admin/deleted", "mobile/deleted");
    }

    @PostMapping("/user/{userId}/restore")
    @ResponseBody
    public Map<String, Object> restoreUser(@PathVariable String userId, HttpServletRequest request) {
        String token = getToken(request);
        apiService.restoreUser(userId, token);
        return Collections.singletonMap("success", true);
    }

    @PostMapping("/user/{userId}/physical-delete")
    @ResponseBody
    public Map<String, Object> physicalDeleteUser(@PathVariable String userId, HttpServletRequest request) {
        String token = getToken(request);
        apiService.physicalDeleteUser(userId, token);
        return Collections.singletonMap("success", true);
    }

    @PostMapping("/toilet/{id}/restore")
    @ResponseBody
    public Map<String, Object> restoreToilet(@PathVariable String id, HttpServletRequest request) {
        String token = getToken(request);
        apiService.restoreToilet(id, token);
        return Collections.singletonMap("success", true);
    }

    @PostMapping("/toilet/{id}/physical-delete")
    @ResponseBody
    public Map<String, Object> physicalDeleteToilet(@PathVariable String id, HttpServletRequest request) {
        String token = getToken(request);
        apiService.physicalDeleteToilet(id, token);
        return Collections.singletonMap("success", true);
    }

    @PostMapping("/user/{userId}/add-admin")
    @ResponseBody
    public Map<String, Object> addAdmin(@PathVariable String userId, HttpServletRequest request) {
        String token = getToken(request);
        apiService.addAdmin(userId, token);
        return Collections.singletonMap("success", true);
    }

    @PostMapping("/user/{userId}/remove-admin")
    @ResponseBody
    public Map<String, Object> removeAdmin(@PathVariable String userId, HttpServletRequest request) {
        String token = getToken(request);
        apiService.removeAdmin(userId, token);
        return Collections.singletonMap("success", true);
    }

    @DeleteMapping("/user/{userId}")
    @ResponseBody
    public Map<String, Object> deleteUser(@PathVariable String userId, HttpServletRequest request) {
        String token = getToken(request);
        apiService.deleteUser(userId, token);
        return Collections.singletonMap("success", true);
    }

    @DeleteMapping("/toilet/{id}")
    @ResponseBody
    public Map<String, Object> deleteToilet(@PathVariable String id, HttpServletRequest request) {
        String token = getToken(request);
        apiService.deleteToilet(id, token);
        return Collections.singletonMap("success", true);
    }

    private String getToken(HttpServletRequest request) {
        return (String) request.getSession().getAttribute("token");
    }

    private boolean isMobile(HttpServletRequest request) {
        Boolean isMobile = (Boolean) request.getAttribute("isMobile");
        return Boolean.TRUE.equals(isMobile);
    }

    private String getTemplate(HttpServletRequest request, String adminView, String mobileView) {
        return isMobile(request) ? mobileView : adminView;
    }
}
