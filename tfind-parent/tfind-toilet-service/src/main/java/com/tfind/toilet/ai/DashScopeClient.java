package com.tfind.toilet.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashScopeClient {

    private static final String BASE_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private static final String MULTIMODAL_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";

    private final String apiKey;
    private final String model;
    private final String visionModel;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public DashScopeClient(String apiKey, String model, String visionModel) {
        this.apiKey = apiKey;
        this.model = model;
        this.visionModel = visionModel;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String chat(String prompt) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(message);

            Map<String, Object> input = new HashMap<>();
            input.put("messages", messages);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("result_format", "message");

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("input", input);
            body.put("parameters", parameters);

            String jsonBody = objectMapper.writeValueAsString(body);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");

            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    BASE_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCodeValue() == 200) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode choices = root.path("output").path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    return choices.get(0).path("message").path("content").asText();
                }
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public String analyzeImage(String prompt, String imageUrl) {
        try {
            List<Map<String, Object>> content = new ArrayList<>();
            Map<String, Object> imagePart = new HashMap<>();
            imagePart.put("image", imageUrl);
            content.add(imagePart);
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", prompt);
            content.add(textPart);

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", content);

            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(message);

            Map<String, Object> input = new HashMap<>();
            input.put("messages", messages);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("result_format", "message");

            Map<String, Object> body = new HashMap<>();
            body.put("model", visionModel);
            body.put("input", input);
            body.put("parameters", parameters);

            String jsonBody = objectMapper.writeValueAsString(body);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");

            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    MULTIMODAL_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCodeValue() == 200) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode choices = root.path("output").path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    return choices.get(0).path("message").path("content").asText();
                }
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
