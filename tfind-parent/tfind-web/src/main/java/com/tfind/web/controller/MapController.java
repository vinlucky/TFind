package com.tfind.web.controller;

import com.tfind.web.service.ApiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
public class MapController {

    private final ApiService apiService;

    public MapController(ApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping("/map")
    public String mapPage(Model model, HttpServletRequest request) {
        String token = getToken(request);
        List<Map<String, Object>> toilets = apiService.listToilets(token);
        model.addAttribute("toilets", toilets);

        Boolean isMobile = (Boolean) request.getAttribute("isMobile");
        if (Boolean.TRUE.equals(isMobile)) {
            return "mobile/map";
        }
        return "map/map";
    }

    private String getToken(HttpServletRequest request) {
        return (String) request.getSession().getAttribute("token");
    }
}
