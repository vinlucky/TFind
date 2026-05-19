package com.tfind.toilet.controller;

import com.tfind.toilet.entity.UserProfile;
import com.tfind.toilet.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/{openid}")
    public ResponseEntity<Map<String, Object>> getProfile(@PathVariable String openid) {
        UserProfile profile = userProfileService.getProfileByOpenid(openid);
        Map<String, Object> result = new HashMap<>();
        if (profile != null) {
            result.put("code", 200);
            result.put("data", profile);
            return ResponseEntity.ok(result);
        }
        result.put("code", 404);
        result.put("message", "Profile not found");
        return ResponseEntity.status(404).body(result);
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestBody Map<String, String> body,
            @RequestAttribute("userId") String userId) {
        String nickname = body.get("nickname");
        String avatarUrl = body.get("avatarUrl");
        UserProfile profile = userProfileService.createOrUpdateProfile(userId, nickname, avatarUrl);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", profile);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/avatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestAttribute("userId") String userId) {
        Map<String, Object> result = new HashMap<>();
        if (file.isEmpty()) {
            result.put("code", 400);
            result.put("message", "File is empty");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            String uploadDir = "uploads/avatars";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = userId + "_" + System.currentTimeMillis() + ".jpg";
            String filePath = uploadDir + File.separator + fileName;
            file.transferTo(new File(filePath));

            String avatarUrl = "/uploads/avatars/" + fileName;
            UserProfile profile = userProfileService.createOrUpdateProfile(userId, null, avatarUrl);

            result.put("code", 200);
            result.put("data", profile);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            result.put("code", 500);
            result.put("message", "Upload failed");
            return ResponseEntity.status(500).body(result);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(
            @RequestAttribute("userId") String userId) {
        Map<String, Object> stats = userProfileService.getUserStats(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", stats);
        return ResponseEntity.ok(result);
    }
}
