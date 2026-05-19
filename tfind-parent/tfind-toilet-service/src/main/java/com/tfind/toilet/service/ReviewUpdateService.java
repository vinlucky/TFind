package com.tfind.toilet.service;

import com.tfind.toilet.entity.ReviewUpdate;

import java.util.List;

public interface ReviewUpdateService {

    ReviewUpdate addReviewUpdate(ReviewUpdate update);

    List<ReviewUpdate> getUpdatesByToiletId(String toiletId);

    void checkAndUpdateFacility(String toiletId);
}
