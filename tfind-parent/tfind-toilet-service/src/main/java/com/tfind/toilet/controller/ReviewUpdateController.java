package com.tfind.toilet.controller;

import com.tfind.toilet.entity.ReviewUpdate;
import com.tfind.toilet.service.ReviewUpdateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/review-update")
public class ReviewUpdateController {

    private final ReviewUpdateService reviewUpdateService;

    public ReviewUpdateController(ReviewUpdateService reviewUpdateService) {
        this.reviewUpdateService = reviewUpdateService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addReviewUpdate(
            @RequestBody ReviewUpdate update,
            @RequestAttribute("userId") String userId) {
        update.setOpenid(userId);
        ReviewUpdate created = reviewUpdateService.addReviewUpdate(update);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", created);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/toilet/{toiletId}")
    public ResponseEntity<Map<String, Object>> getUpdatesByToiletId(@PathVariable String toiletId) {
        List<ReviewUpdate> updates = reviewUpdateService.getUpdatesByToiletId(toiletId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", updates);
        return ResponseEntity.ok(result);
    }
}
