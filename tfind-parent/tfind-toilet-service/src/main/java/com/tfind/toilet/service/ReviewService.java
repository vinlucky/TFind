package com.tfind.toilet.service;

import com.tfind.toilet.entity.Review;

import java.util.List;

public interface ReviewService {

    Review addReview(Review review);

    List<Review> getReviewsByToiletId(String toiletId);

    void deleteReview(String reviewId, String openid, String role);

    void approveReview(String reviewId);
}
