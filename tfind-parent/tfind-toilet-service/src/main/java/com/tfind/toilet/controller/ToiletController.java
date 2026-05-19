package com.tfind.toilet.controller;

import com.tfind.toilet.entity.Toilet;
import com.tfind.toilet.service.ToiletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/toilet")
public class ToiletController {

    private final ToiletService toiletService;

    public ToiletController(ToiletService toiletService) {
        this.toiletService = toiletService;
    }

    @GetMapping("/nearby")
    public ResponseEntity<Map<String, Object>> getNearbyToilets(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "speed") String mode,
            @RequestParam(defaultValue = "5.0") double distance) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Toilet> toilets = toiletService.getNearbyToilets(lat, lng, mode, distance);
            result.put("code", 200);
            result.put("data", toilets);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败");
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestAttribute("userId") String userId) {
        Map<String, Object> result = new HashMap<>();
        if (file.isEmpty()) {
            result.put("code", 400);
            result.put("message", "文件为空");
            return ResponseEntity.badRequest().body(result);
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            result.put("code", 400);
            result.put("message", "只能上传图片文件");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            String uploadDir = "uploads/toilets";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String suffix = ".jpg";
            if (contentType.contains("png")) {
                suffix = ".png";
            } else if (contentType.contains("gif")) {
                suffix = ".gif";
            } else if (contentType.contains("webp")) {
                suffix = ".webp";
            }

            String fileName = UUID.randomUUID().toString().substring(0, 8) + "_" + System.currentTimeMillis() + suffix;
            String filePath = uploadDir + File.separator + fileName;
            file.transferTo(new File(filePath));

            String photoUrl = "/uploads/toilets/" + fileName;

            result.put("code", 200);
            result.put("data", photoUrl);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            result.put("code", 500);
            result.put("message", "上传失败");
            return ResponseEntity.status(500).body(result);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getToiletById(@PathVariable String id) {
        Toilet toilet = toiletService.getToiletById(id);
        Map<String, Object> result = new HashMap<>();
        if (toilet != null) {
            result.put("code", 200);
            result.put("data", toilet);
            return ResponseEntity.ok(result);
        }
        result.put("code", 404);
        result.put("message", "Toilet not found");
        return ResponseEntity.status(404).body(result);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createToilet(
            @RequestBody Toilet toilet,
            @RequestAttribute("userId") String userId) {
        toilet.setOpenid(userId);
        Toilet created = toiletService.createToilet(toilet);
        Map<String, Object> result = new HashMap<>();
        if ("rejected".equals(created.getStatus())) {
            result.put("code", 400);
            result.put("message", created.getAiAnalysis());
            return ResponseEntity.badRequest().body(result);
        }
        result.put("code", 200);
        result.put("data", created);
        if ("approved".equals(created.getStatus())) {
            result.put("message", "审核通过，厕所已发布");
        } else {
            result.put("message", "已提交，等待管理员审核");
        }
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateToilet(
            @PathVariable String id,
            @RequestBody Toilet toilet,
            @RequestAttribute("userId") String userId) {
        Toilet updated = toiletService.updateToilet(id, toilet);
        Map<String, Object> result = new HashMap<>();
        if (updated != null) {
            result.put("code", 200);
            result.put("data", updated);
            return ResponseEntity.ok(result);
        }
        result.put("code", 404);
        result.put("message", "Toilet not found");
        return ResponseEntity.status(404).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteToilet(
            @PathVariable String id,
            @RequestAttribute("role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 403);
            result.put("message", "Permission denied");
            return ResponseEntity.status(403).body(result);
        }
        toiletService.deleteToiletByAdmin(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "Deleted");
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<Map<String, Object>> restoreToilet(
            @PathVariable String id,
            @RequestAttribute("role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 403);
            result.put("message", "Permission denied");
            return ResponseEntity.status(403).body(result);
        }
        toiletService.restoreToilet(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "Restored");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/deleted")
    public ResponseEntity<Map<String, Object>> getAllDeletedToilets(
            @RequestAttribute("role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 403);
            result.put("message", "Permission denied");
            return ResponseEntity.status(403).body(result);
        }
        List<Toilet> deleted = toiletService.getAllDeletedToilets();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", deleted);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}/physical")
    public ResponseEntity<Map<String, Object>> physicalDeleteToilet(
            @PathVariable String id,
            @RequestAttribute("role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 403);
            result.put("message", "Permission denied");
            return ResponseEntity.status(403).body(result);
        }
        toiletService.physicalDeleteSingleToilet(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "Physically deleted");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAllToilets() {
        List<Toilet> toilets = toiletService.getAllToilets();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", toilets);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingToilets() {
        List<Toilet> toilets = toiletService.getPendingToilets();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", toilets);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveToilet(
            @PathVariable String id,
            @RequestAttribute("role") String role,
            @RequestAttribute("userId") String userId) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 403);
            result.put("message", "Permission denied");
            return ResponseEntity.status(403).body(result);
        }
        toiletService.approveToilet(id, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "Approved");
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectToilet(
            @PathVariable String id,
            @RequestAttribute("role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 403);
            result.put("message", "Permission denied");
            return ResponseEntity.status(403).body(result);
        }
        toiletService.rejectToilet(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "Rejected");
        return ResponseEntity.ok(result);
    }
}
