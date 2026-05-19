package com.tfind.user.config;

import org.noear.folkmq.FolkMQ;
import org.noear.folkmq.client.MqClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.IOException;

@Configuration
public class FolkMqConfig {

    @Value("${folkmq.server-url}")
    private String serverUrl;

    @Value("${folkmq.client-name:user-service}")
    private String clientName;

    private MqClient client;

    @Bean
    public MqClient mqClient() {
        try {
            client = FolkMQ.createClient(serverUrl)
                    .nameAs(clientName)
                    .connect();
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to FolkMQ server", e);
        }
        return client;
    }

    @PreDestroy
    public void close() {
        if (client != null) {
            try {
                client.disconnect();
            } catch (IOException e) {
                // Ignore disconnect errors
            }
        }
    }
}
