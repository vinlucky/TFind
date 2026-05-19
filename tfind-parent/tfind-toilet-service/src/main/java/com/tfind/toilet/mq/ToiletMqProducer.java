package com.tfind.toilet.mq;

import org.noear.folkmq.client.MqClient;
import org.noear.folkmq.client.MqMessage;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ToiletMqProducer {

    private final MqClient mqClient;

    public ToiletMqProducer(MqClient mqClient) {
        this.mqClient = mqClient;
    }

    public void sendToiletMessage(String action, String data) {
        try {
            MqMessage message = new MqMessage("{\"action\":\"" + action + "\",\"data\":\"" + data + "\"}");
            mqClient.publish("toilet.topic", message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendUserMessage(String action, String data) {
        try {
            MqMessage message = new MqMessage("{\"action\":\"" + action + "\",\"data\":\"" + data + "\"}");
            mqClient.publish("user.topic", message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
