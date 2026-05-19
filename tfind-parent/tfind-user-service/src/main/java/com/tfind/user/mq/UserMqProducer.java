package com.tfind.user.mq;

import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UserMqProducer {

    @Autowired
    private MqClient mqClient;

    public void sendUserRegisterMessage(String userId, String openid) {
        try {
            String message = "{\"type\":\"USER_REGISTER\",\"userId\":\"" + userId + "\",\"openid\":\"" + openid + "\",\"timestamp\":" + System.currentTimeMillis() + "}";
            mqClient.publish("user.register", new MqMessage(message));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendUserDeleteMessage(String userId) {
        try {
            String message = "{\"type\":\"USER_DELETE\",\"userId\":\"" + userId + "\",\"timestamp\":" + System.currentTimeMillis() + "}";
            mqClient.publish("user.delete", new MqMessage(message));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendUserRestoreMessage(String userId) {
        try {
            String message = "{\"type\":\"USER_RESTORE\",\"userId\":\"" + userId + "\",\"timestamp\":" + System.currentTimeMillis() + "}";
            mqClient.publish("user.restore", new MqMessage(message));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendUserRoleChangeMessage(String userId, String role) {
        try {
            String message = "{\"type\":\"USER_ROLE_CHANGE\",\"userId\":\"" + userId + "\",\"role\":\"" + role + "\",\"timestamp\":" + System.currentTimeMillis() + "}";
            mqClient.publish("user.role.change", new MqMessage(message));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendUserIdChangeMessage(String oldUserId, String newUserId) {
        try {
            String message = "{\"type\":\"USER_ID_CHANGE\",\"oldUserId\":\"" + oldUserId + "\",\"newUserId\":\"" + newUserId + "\",\"timestamp\":" + System.currentTimeMillis() + "}";
            mqClient.publish("user.id.change", new MqMessage(message));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
