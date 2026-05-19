package com.tfind.toilet.service.impl;

import com.tfind.toilet.entity.Review;
import com.tfind.toilet.service.BerkeleyDbService;
import com.tfind.toilet.service.ReviewService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewServiceImpl implements ReviewService {

    private static final String COLLECTION = "reviews";

    private final BerkeleyDbService berkeleyDbService;

    public ReviewServiceImpl(BerkeleyDbService berkeleyDbService) {
        this.berkeleyDbService = berkeleyDbService;
    }

    @Override
    public Review addReview(Review review) {
        review.setId(UUID.randomUUID().toString());
        review.setStatus("pending");
        review.setCreateTime(System.currentTimeMillis());

        String generatedId = berkeleyDbService.save(COLLECTION, review);
        review.setId(generatedId);

        return review;
    }

    @Override
    public List<Review> getReviewsByToiletId(String toiletId) {
        return berkeleyDbService.query(COLLECTION, "/toiletId = :1", Review.class);
    }

    @Override
    public void deleteReview(String reviewId, String openid, String role) {
        Review review = berkeleyDbService.getById(COLLECTION, reviewId, Review.class);
        if (review == null) {
            return;
        }

        if ("admin".equals(role) || openid.equals(review.getOpenid())) {
            berkeleyDbService.delete(COLLECTION, reviewId);
        }
    }

    @Override
    public void approveReview(String reviewId) {
        Review review = berkeleyDbService.getById(COLLECTION, reviewId, Review.class);
        if (review == null) {
            return;
        }

        review.setStatus("approved");
        berkeleyDbService.update(COLLECTION, reviewId, review);
    }
}
