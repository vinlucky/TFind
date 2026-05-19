package com.tfind.toilet.service;

import com.tfind.toilet.entity.UserProfile;

import java.util.Map;

public interface UserProfileService {

    UserProfile createOrUpdateProfile(String openid, String nickname, String avatarUrl);

    UserProfile getProfileByOpenid(String openid);

    void updateScore(String openid, int score, String action);

    Map<String, Object> getUserStats(String openid);
}
