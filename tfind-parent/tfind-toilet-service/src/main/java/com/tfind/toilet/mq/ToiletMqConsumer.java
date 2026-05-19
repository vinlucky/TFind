package com.tfind.toilet.mq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tfind.toilet.entity.UserProfile;
import com.tfind.toilet.service.BerkeleyDbService;
import com.tfind.toilet.service.UserProfileService;
import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqMessageReceived;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ToiletMqConsumer implements InitializingBean {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private BerkeleyDbService berkeleyDbService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MqClient mqClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        mqClient.subscribe("toilet.topic", this::receiveToiletMessage);
        mqClient.subscribe("user.topic", this::receiveUserMessage);
    }

    public void receiveToiletMessage(MqMessageReceived messageReceived) {
        try {
            String body = messageReceived.getBodyAsString();
            JsonNode node = objectMapper.readTree(body);
            String action = node.has("action") ? node.get("action").asText() : "";
            switch (action) {
                case "toilet_created":
                    handleToiletCreated(node);
                    break;
                case "toilet_updated":
                    handleToiletUpdated(node);
                    break;
                case "toilet_soft_deleted":
                    handleToiletDeleted(node);
                    break;
                case "toilet_restored":
                    handleToiletRestored(node);
                    break;
                case "toilet_approved":
                    handleToiletApproved(node);
                    break;
                case "toilet_rejected":
                    handleToiletRejected(node);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receiveUserMessage(MqMessageReceived messageReceived) {
        try {
            String body = messageReceived.getBodyAsString();
            JsonNode node = objectMapper.readTree(body);
            String action = node.has("action") ? node.get("action").asText() : "";
            switch (action) {
                case "user_register":
                    handleUserRegister(node);
                    break;
                case "user_delete":
                    handleUserDelete(node);
                    break;
                case "user_restore":
                    handleUserRestore(node);
                    break;
                case "user_role_change":
                    handleUserRoleChange(node);
                    break;
                case "user_id_change":
                    handleUserIdChange(node);
                    break;
                case "query_user_profile":
                    handleQueryUserProfile(node);
                    break;
                case "query_toilet_data":
                    handleQueryToiletData(node);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleToiletCreated(JsonNode node) {
        String toiletId = node.has("data") ? node.get("data").asText() : "";
    }

    private void handleToiletUpdated(JsonNode node) {
        String toiletId = node.has("data") ? node.get("data").asText() : "";
    }

    private void handleToiletDeleted(JsonNode node) {
        String toiletId = node.has("data") ? node.get("data").asText() : "";
    }

    private void handleToiletRestored(JsonNode node) {
        String toiletId = node.has("data") ? node.get("data").asText() : "";
    }

    private void handleToiletApproved(JsonNode node) {
        String toiletId = node.has("data") ? node.get("data").asText() : "";
    }

    private void handleToiletRejected(JsonNode node) {
        String toiletId = node.has("data") ? node.get("data").asText() : "";
    }

    private void handleUserRegister(JsonNode node) {
        String userId = node.has("userId") ? node.get("userId").asText() : "";
        String openid = node.has("openid") ? node.get("openid").asText() : "";
        if (!userId.isEmpty() && !openid.isEmpty()) {
            UserProfile profile = new UserProfile();
            profile.setOpenid(openid);
            profile.setNickname(userId);
            profile.setAvatarUrl("");
            profile.setScore(0);
            profile.setUploadCount(0);
            profile.setApprovedCount(0);
            userProfileService.createOrUpdateProfile(openid, userId, "");
        }
    }

    private void handleUserDelete(JsonNode node) {
        String userId = node.has("userId") ? node.get("userId").asText() : "";
    }

    private void handleUserRestore(JsonNode node) {
        String userId = node.has("userId") ? node.get("userId").asText() : "";
    }

    private void handleUserRoleChange(JsonNode node) {
        String userId = node.has("userId") ? node.get("userId").asText() : "";
        String role = node.has("role") ? node.get("role").asText() : "";
    }

    private void handleUserIdChange(JsonNode node) {
        String oldUserId = node.has("oldUserId") ? node.get("oldUserId").asText() : "";
        String newUserId = node.has("newUserId") ? node.get("newUserId").asText() : "";
    }

    private void handleQueryUserProfile(JsonNode node) {
        String openid = node.has("openid") ? node.get("openid").asText() : "";
        if (!openid.isEmpty()) {
            UserProfile profile = userProfileService.getProfileByOpenid(openid);
        }
    }

    private void handleQueryToiletData(JsonNode node) {
        String toiletId = node.has("toiletId") ? node.get("toiletId").asText() : "";
        if (!toiletId.isEmpty()) {
            berkeleyDbService.getById("toilets", toiletId, ObjectNode.class);
        }
    }
}
