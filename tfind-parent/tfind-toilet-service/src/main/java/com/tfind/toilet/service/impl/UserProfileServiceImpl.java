package com.tfind.toilet.service.impl;

import com.tfind.toilet.entity.ScoreRecord;
import com.tfind.toilet.entity.UserProfile;
import com.tfind.toilet.service.BerkeleyDbService;
import com.tfind.toilet.service.UserProfileService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private static final String PROFILE_COLLECTION = "user_profiles";
    private static final String SCORE_COLLECTION = "score_records";
    private static final String REDIS_KEY_PREFIX = "profile:";

    private final BerkeleyDbService berkeleyDbService;
    private final RedisTemplate<String, Object> redisTemplate;

    public UserProfileServiceImpl(BerkeleyDbService berkeleyDbService, RedisTemplate<String, Object> redisTemplate) {
        this.berkeleyDbService = berkeleyDbService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public UserProfile createOrUpdateProfile(String openid, String nickname, String avatarUrl) {
        List<UserProfile> profiles = berkeleyDbService.query(PROFILE_COLLECTION, "/openid = :1", UserProfile.class);
        UserProfile profile;

        if (profiles.isEmpty()) {
            profile = new UserProfile();
            profile.setId(UUID.randomUUID().toString());
            profile.setOpenid(openid);
            profile.setNickname(nickname);
            profile.setAvatarUrl(avatarUrl);
            profile.setScore(0);
            profile.setUploadCount(0);
            profile.setApprovedCount(0);
            profile.setCreateTime(System.currentTimeMillis());
            profile.setLastLoginTime(System.currentTimeMillis());

            String generatedId = berkeleyDbService.save(PROFILE_COLLECTION, profile);
            profile.setId(generatedId);
        } else {
            profile = profiles.get(0);
            profile.setNickname(nickname);
            profile.setAvatarUrl(avatarUrl);
            profile.setLastLoginTime(System.currentTimeMillis());

            berkeleyDbService.update(PROFILE_COLLECTION, profile.getId(), profile);
        }

        String cacheKey = REDIS_KEY_PREFIX + openid;
        redisTemplate.opsForValue().set(cacheKey, profile, 30, TimeUnit.MINUTES);

        return profile;
    }

    @Override
    public UserProfile getProfileByOpenid(String openid) {
        String cacheKey = REDIS_KEY_PREFIX + openid;
        @SuppressWarnings("unchecked")
        UserProfile cached = (UserProfile) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<UserProfile> profiles = berkeleyDbService.query(PROFILE_COLLECTION, "/openid = :1", UserProfile.class);
        if (profiles.isEmpty()) {
            return null;
        }

        UserProfile profile = profiles.get(0);
        redisTemplate.opsForValue().set(cacheKey, profile, 30, TimeUnit.MINUTES);
        return profile;
    }

    @Override
    public void updateScore(String openid, int score, String action) {
        UserProfile profile = getProfileByOpenid(openid);
        if (profile == null) {
            return;
        }

        int newScore = profile.getScore() + score;
        profile.setScore(newScore);

        berkeleyDbService.update(PROFILE_COLLECTION, profile.getId(), profile);

        ScoreRecord record = new ScoreRecord();
        record.setId(UUID.randomUUID().toString());
        record.setOpenid(openid);
        record.setScore(score);
        record.setAction(action);
        record.setBalance(newScore);
        record.setCreateTime(System.currentTimeMillis());
        berkeleyDbService.save(SCORE_COLLECTION, record);

        String cacheKey = REDIS_KEY_PREFIX + openid;
        redisTemplate.opsForValue().set(cacheKey, profile, 30, TimeUnit.MINUTES);
    }

    @Override
    public Map<String, Object> getUserStats(String openid) {
        UserProfile profile = getProfileByOpenid(openid);
        Map<String, Object> stats = new HashMap<>();
        if (profile != null) {
            stats.put("score", profile.getScore());
            stats.put("uploadCount", profile.getUploadCount());
            stats.put("approvedCount", profile.getApprovedCount());
        }
        return stats;
    }
}
