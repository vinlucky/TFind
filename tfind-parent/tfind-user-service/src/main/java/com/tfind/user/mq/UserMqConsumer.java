package com.tfind.user.mq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfind.user.entity.User;
import com.tfind.user.service.UserService;
import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqMessageReceived;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMqConsumer implements InitializingBean {

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MqClient mqClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        mqClient.subscribe("user.query", this::handleUserMessage);
    }

    public void handleUserMessage(MqMessageReceived messageReceived) {
        try {
            String body = messageReceived.getBodyAsString();
            JsonNode node = objectMapper.readTree(body);
            String action = node.has("action") ? node.get("action").asText() : "";

            switch (action) {
                case "query_user":
                    handleQueryUser(node);
                    break;
                case "query_user_list":
                    handleQueryUserList(node);
                    break;
                case "verify_user":
                    handleVerifyUser(node);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleQueryUser(JsonNode node) {
        String userId = node.has("userId") ? node.get("userId").asText() : "";
        if (!userId.isEmpty()) {
            User user = userService.getUserByUserId(userId);
        }
    }

    private void handleQueryUserList(JsonNode node) {
        userService.getAllUsers();
    }

    private void handleVerifyUser(JsonNode node) {
        String userId = node.has("userId") ? node.get("userId").asText() : "";
        String password = node.has("password") ? node.get("password").asText() : "";
        if (!userId.isEmpty() && !password.isEmpty()) {
            try {
                userService.login(userId, password);
            } catch (Exception e) {
            }
        }
    }
}
