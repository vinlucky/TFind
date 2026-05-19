package com.tfind.toilet.service.impl;

import com.tfind.toilet.entity.ReviewUpdate;
import com.tfind.toilet.entity.Toilet;
import com.tfind.toilet.service.BerkeleyDbService;
import com.tfind.toilet.service.ReviewUpdateService;
import com.tfind.toilet.service.ToiletService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewUpdateServiceImpl implements ReviewUpdateService {

    private static final String COLLECTION = "review_updates";
    private static final String TOILET_COLLECTION = "toilets";

    private final BerkeleyDbService berkeleyDbService;
    private final ToiletService toiletService;

    public ReviewUpdateServiceImpl(BerkeleyDbService berkeleyDbService, ToiletService toiletService) {
        this.berkeleyDbService = berkeleyDbService;
        this.toiletService = toiletService;
    }

    @Override
    public ReviewUpdate addReviewUpdate(ReviewUpdate update) {
        update.setId(UUID.randomUUID().toString());
        update.setStatus("pending");
        update.setCreateTime(System.currentTimeMillis());

        String generatedId = berkeleyDbService.save(COLLECTION, update);
        update.setId(generatedId);

        return update;
    }

    @Override
    public List<ReviewUpdate> getUpdatesByToiletId(String toiletId) {
        return berkeleyDbService.query(COLLECTION, "/toiletId = :1", ReviewUpdate.class);
    }

    @Override
    public void checkAndUpdateFacility(String toiletId) {
        List<ReviewUpdate> updates = getUpdatesByToiletId(toiletId);
        if (updates.size() >= 3) {
            Toilet toilet = toiletService.getToiletById(toiletId);
            if (toilet == null) {
                return;
            }

            int freeCount = 0;
            int motherRoomCount = 0;
            int accessibilityCount = 0;
            int hours24Count = 0;

            for (ReviewUpdate update : updates) {
                if (Boolean.TRUE.equals(update.getIsFree())) freeCount++;
                if (Boolean.TRUE.equals(update.getHasMotherRoom())) motherRoomCount++;
                if (Boolean.TRUE.equals(update.getHasAccessibility())) accessibilityCount++;
                if (Boolean.TRUE.equals(update.getIs24Hours())) hours24Count++;
            }

            int total = updates.size();
            if (freeCount > total / 2) toilet.setIsFree(true);
            if (motherRoomCount > total / 2) toilet.setHasMotherRoom(true);
            if (accessibilityCount > total / 2) toilet.setHasAccessibility(true);
            if (hours24Count > total / 2) toilet.setIs24Hours(true);

            toiletService.updateToilet(toiletId, toilet);
        }
    }
}
