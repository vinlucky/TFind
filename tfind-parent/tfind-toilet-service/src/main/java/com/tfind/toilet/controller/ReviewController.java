package com.tfind.toilet.controller;

import com.tfind.toilet.entity.Review;
import com.tfind.toilet.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addReview(
            @RequestBody Review review,
            @RequestAttribute("userId") String userId) {
        review.setOpenid(userId);
        Review created = reviewService.addReview(review);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", created);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/toilet/{toiletId}")
    public ResponseEntity<Map<String, Object>> getReviewsByToiletId(@PathVariable String toiletId) {
        List<Review> reviews = reviewService.getReviewsByToiletId(toiletId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", reviews);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Map<String, Object>> deleteReview(
            @PathVariable String reviewId,
            @RequestAttribute("userId") String userId,
            @RequestAttribute("role") String role) {
        reviewService.deleteReview(reviewId, userId, role);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "Deleted");
        return ResponseEntity.ok(result);
    }
}
