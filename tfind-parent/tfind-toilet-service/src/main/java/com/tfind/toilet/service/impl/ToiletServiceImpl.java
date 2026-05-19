package com.tfind.toilet.service.impl;

import com.tfind.toilet.ai.AIService;
import com.tfind.toilet.entity.Toilet;
import com.tfind.toilet.mq.ToiletMqProducer;
import com.tfind.toilet.service.BerkeleyDbService;
import com.tfind.toilet.service.ToiletService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ToiletServiceImpl implements ToiletService {

    private static final String COLLECTION = "toilets";
    private static final String REDIS_KEY_PREFIX = "toilet:";
    private static final double EARTH_RADIUS = 6371.0;

    private final BerkeleyDbService berkeleyDbService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ToiletMqProducer mqProducer;
    private final AIService aiService;

    public ToiletServiceImpl(BerkeleyDbService berkeleyDbService, RedisTemplate<String, Object> redisTemplate, ToiletMqProducer mqProducer, AIService aiService) {
        this.berkeleyDbService = berkeleyDbService;
        this.redisTemplate = redisTemplate;
        this.mqProducer = mqProducer;
        this.aiService = aiService;
    }

    @Override
    public List<Toilet> getNearbyToilets(double lat, double lng, String mode, double distance) {
        String cacheKey = "nearby:" + lat + ":" + lng + ":" + mode + ":" + distance;
        @SuppressWarnings("unchecked")
        List<Toilet> cached = (List<Toilet>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<Toilet> allToilets = berkeleyDbService.query(COLLECTION, "/deltoilet = false and /status = approved", Toilet.class);
        List<Toilet> nearby = new ArrayList<>();

        for (Toilet toilet : allToilets) {
            if (toilet.getLatitude() != null && toilet.getLongitude() != null) {
                double d = haversine(lat, lng, toilet.getLatitude(), toilet.getLongitude());
                if (d <= distance) {
                    nearby.add(toilet);
                }
            }
        }

        if ("smart".equals(mode)) {
            for (Toilet toilet : nearby) {
                if (toilet.getAiScore() == null) {
                    double aiScore = aiService.calculateAIScore(toilet);
                    toilet.setAiScore(aiScore);
                }
            }
            nearby.sort(Comparator.comparingDouble(Toilet::getAiScore).reversed());
        } else if ("comfort".equals(mode)) {
            nearby.sort(Comparator.comparingDouble(t -> t.getCleanScore() != null ? -t.getCleanScore() : 0));
        } else {
            nearby.sort(Comparator.comparingDouble(t -> haversine(lat, lng, t.getLatitude(), t.getLongitude())));
        }

        redisTemplate.opsForValue().set(cacheKey, nearby, 5, TimeUnit.MINUTES);
        mqProducer.sendToiletMessage("nearby_query", "lat=" + lat + ",lng=" + lng + ",mode=" + mode);

        return nearby;
    }

    @Override
    public Toilet getToiletById(String id) {
        String cacheKey = REDIS_KEY_PREFIX + id;
        @SuppressWarnings("unchecked")
        Toilet cached = (Toilet) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Toilet toilet = berkeleyDbService.getById(COLLECTION, id, Toilet.class);
        if (toilet != null) {
            redisTemplate.opsForValue().set(cacheKey, toilet, 10, TimeUnit.MINUTES);
        }
        return toilet;
    }

    @Override
    public Toilet createToilet(Toilet toilet) {
        toilet.setId(UUID.randomUUID().toString());
        toilet.setStatus("pending");
        toilet.setDeltoilet(false);
        toilet.setCreateTime(System.currentTimeMillis());
        toilet.setUpdateTime(System.currentTimeMillis());

        Map<String, Object> photoReview = aiService.reviewToiletPhoto(toilet.getPhotoUrl());
        boolean photoPassed = (boolean) photoReview.get("passed");
        String photoReason = (String) photoReview.get("reason");

        if (!photoPassed) {
            toilet.setStatus("rejected");
            toilet.setAiAnalysis("图片审核不通过：" + photoReason);
            toilet.setReviewer("AI");
            return toilet;
        }

        try {
            String aiAnalysis = aiService.analyzeToilet(toilet);
            boolean hasPhoto = toilet.getPhotoUrl() != null && !toilet.getPhotoUrl().isEmpty();
            if (hasPhoto) {
                toilet.setAiAnalysis("图片审核通过：" + photoReason + " | " + aiAnalysis);
            } else {
                toilet.setAiAnalysis(aiAnalysis);
            }
            boolean autoPass = aiService.shouldAutoApprove(toilet);
            if (autoPass) {
                toilet.setStatus("approved");
                toilet.setReviewer("AI");
            }
        } catch (Exception e) {
            boolean hasPhoto = toilet.getPhotoUrl() != null && !toilet.getPhotoUrl().isEmpty();
            if (hasPhoto) {
                toilet.setAiAnalysis("图片审核通过：" + photoReason + " | AI信息分析暂不可用");
            } else {
                toilet.setAiAnalysis("AI信息分析暂不可用");
            }
        }

        String generatedId = berkeleyDbService.save(COLLECTION, toilet);
        toilet.setId(generatedId);

        String cacheKey = REDIS_KEY_PREFIX + generatedId;
        redisTemplate.opsForValue().set(cacheKey, toilet, 10, TimeUnit.MINUTES);

        mqProducer.sendToiletMessage("toilet_created", generatedId);

        return toilet;
    }

    @Override
    public Toilet updateToilet(String id, Toilet toilet) {
        Toilet existing = berkeleyDbService.getById(COLLECTION, id, Toilet.class);
        if (existing == null) {
            return null;
        }

        toilet.setId(id);
        toilet.setUpdateTime(System.currentTimeMillis());
        toilet.setCreateTime(existing.getCreateTime());
        toilet.setDeltoilet(existing.getDeltoilet());

        berkeleyDbService.update(COLLECTION, id, toilet);

        String cacheKey = REDIS_KEY_PREFIX + id;
        redisTemplate.opsForValue().set(cacheKey, toilet, 10, TimeUnit.MINUTES);

        mqProducer.sendToiletMessage("toilet_updated", id);

        return toilet;
    }

    @Override
    public void softDeleteToilet(String id) {
        Toilet toilet = berkeleyDbService.getById(COLLECTION, id, Toilet.class);
        if (toilet == null) {
            return;
        }

        toilet.setDeltoilet(true);
        toilet.setDeltime(System.currentTimeMillis());
        toilet.setUpdateTime(System.currentTimeMillis());

        berkeleyDbService.update(COLLECTION, id, toilet);

        String cacheKey = REDIS_KEY_PREFIX + id;
        redisTemplate.delete(cacheKey);

        mqProducer.sendToiletMessage("toilet_soft_deleted", id);
    }

    @Override
    public void restoreToilet(String id) {
        Toilet toilet = berkeleyDbService.getById(COLLECTION, id, Toilet.class);
        if (toilet == null) {
            return;
        }

        toilet.setDeltoilet(false);
        toilet.setDeltime(null);
        toilet.setUpdateTime(System.currentTimeMillis());

        berkeleyDbService.update(COLLECTION, id, toilet);

        String cacheKey = REDIS_KEY_PREFIX + id;
        redisTemplate.opsForValue().set(cacheKey, toilet, 10, TimeUnit.MINUTES);

        mqProducer.sendToiletMessage("toilet_restored", id);
    }

    @Override
    public int physicalDeleteToilets() {
        List<Toilet> deletedToilets = getAllDeletedToilets();
        long threshold = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000;
        int count = 0;

        for (Toilet toilet : deletedToilets) {
            if (toilet.getDeltime() != null && toilet.getDeltime() < threshold) {
                berkeleyDbService.delete(COLLECTION, toilet.getId());
                redisTemplate.delete(REDIS_KEY_PREFIX + toilet.getId());
                count++;
            }
        }

        return count;
    }

    @Override
    public void deleteToiletByAdmin(String id) {
        softDeleteToilet(id);
    }

    @Override
    public List<Toilet> getAllDeletedToilets() {
        return berkeleyDbService.query(COLLECTION, "/deltoilet = true", Toilet.class);
    }

    @Override
    public void approveToilet(String id, String userId) {
        Toilet toilet = berkeleyDbService.getById(COLLECTION, id, Toilet.class);
        if (toilet == null) {
            return;
        }
        toilet.setStatus("approved");
        toilet.setReviewer(userId);
        toilet.setUpdateTime(System.currentTimeMillis());
        berkeleyDbService.update(COLLECTION, id, toilet);
        String cacheKey = REDIS_KEY_PREFIX + id;
        redisTemplate.opsForValue().set(cacheKey, toilet, 10, TimeUnit.MINUTES);
        mqProducer.sendToiletMessage("toilet_approved", id);
    }

    @Override
    public void rejectToilet(String id) {
        updateToiletStatus(id, "rejected");
        mqProducer.sendToiletMessage("toilet_rejected", id);
    }

    @Override
    public List<Toilet> getAllToilets() {
        return berkeleyDbService.query(COLLECTION, "/deltoilet = false", Toilet.class);
    }

    @Override
    public List<Toilet> getPendingToilets() {
        return berkeleyDbService.query(COLLECTION, "/deltoilet = false and /status = pending", Toilet.class);
    }

    @Override
    public void physicalDeleteSingleToilet(String id) {
        berkeleyDbService.delete(COLLECTION, id);
        redisTemplate.delete(REDIS_KEY_PREFIX + id);
    }

    @Override
    public void updateToiletStatus(String id, String status) {
        Toilet toilet = berkeleyDbService.getById(COLLECTION, id, Toilet.class);
        if (toilet == null) {
            return;
        }

        toilet.setStatus(status);
        toilet.setUpdateTime(System.currentTimeMillis());

        berkeleyDbService.update(COLLECTION, id, toilet);

        String cacheKey = REDIS_KEY_PREFIX + id;
        redisTemplate.opsForValue().set(cacheKey, toilet, 10, TimeUnit.MINUTES);
    }

    private double haversine(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }
}
