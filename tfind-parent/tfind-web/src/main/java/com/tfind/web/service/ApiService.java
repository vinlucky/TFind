package com.tfind.web.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class ApiService {

    private final RestTemplate restTemplate;

    @Value("${user-service.url}")
    private String userServiceUrl;

    @Value("${toilet-service.url}")
    private String toiletServiceUrl;

    public ApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String login(String userId, String password) {
        try {
            String url = userServiceUrl + "/api/user/login";
            Map<String, String> request = new java.util.HashMap<>();
            request.put("userId", userId);
            request.put("password", password);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(request),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            Map<String, Object> result = response.getBody();
            if (result != null && Integer.valueOf(200).equals(result.get("code"))) {
                return (String) result.get("data");
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public List<Map<String, Object>> listUsers(String token) {
        try {
            String url = userServiceUrl + "/api/user/list";
            HttpEntity<Void> entity = createAuthEntity(token);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return extractDataList(response.getBody());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> listDeletedUsers(String token) {
        try {
            String url = userServiceUrl + "/api/user/deleted";
            HttpEntity<Void> entity = createAuthEntity(token);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return extractDataList(response.getBody());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public void deleteUser(String userId, String token) {
        String url = userServiceUrl + "/api/user/" + userId;
        HttpEntity<Void> entity = createAuthEntity(token);
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }

    public void restoreUser(String userId, String token) {
        String url = userServiceUrl + "/api/user/" + userId + "/restore";
        HttpEntity<Void> entity = createAuthEntity(token);
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    }

    public void physicalDeleteUser(String userId, String token) {
        String url = userServiceUrl + "/api/user/" + userId + "/physical";
        HttpEntity<Void> entity = createAuthEntity(token);
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }

    public void addAdmin(String userId, String token) {
        String url = userServiceUrl + "/api/user/" + userId + "/admin";
        HttpEntity<Void> entity = createAuthEntity(token);
        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }

    public void removeAdmin(String userId, String token) {
        String url = userServiceUrl + "/api/user/" + userId + "/admin";
        HttpEntity<Void> entity = createAuthEntity(token);
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }

    public List<Map<String, Object>> listToilets(String token) {
        try {
            String url = toiletServiceUrl + "/api/toilet/list";
            HttpEntity<Void> entity = createAuthEntity(token);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return extractDataList(response.getBody());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> listPendingToilets(String token) {
        try {
            String url = toiletServiceUrl + "/api/toilet/pending";
            HttpEntity<Void> entity = createAuthEntity(token);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return extractDataList(response.getBody());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> listDeletedToilets(String token) {
        try {
            String url = toiletServiceUrl + "/api/toilet/deleted";
            HttpEntity<Void> entity = createAuthEntity(token);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return extractDataList(response.getBody());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public void deleteToilet(String id, String token) {
        String url = toiletServiceUrl + "/api/toilet/" + id;
        HttpEntity<Void> entity = createAuthEntity(token);
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }

    public void restoreToilet(String id, String token) {
        String url = toiletServiceUrl + "/api/toilet/" + id + "/restore";
        HttpEntity<Void> entity = createAuthEntity(token);
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    }

    public void physicalDeleteToilet(String id, String token) {
        String url = toiletServiceUrl + "/api/toilet/" + id + "/physical";
        HttpEntity<Void> entity = createAuthEntity(token);
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }

    public void approveToilet(String id, String token) {
        String url = toiletServiceUrl + "/api/toilet/" + id + "/approve";
        HttpEntity<Void> entity = createAuthEntity(token);
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    }

    public void rejectToilet(String id, String token) {
        String url = toiletServiceUrl + "/api/toilet/" + id + "/reject";
        HttpEntity<Void> entity = createAuthEntity(token);
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    }

    public boolean changePassword(String userId, String oldPassword, String newPassword, String token) {
        String url = userServiceUrl + "/api/user/password";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        Map<String, String> request = new java.util.HashMap<>();
        request.put("oldPassword", oldPassword);
        request.put("newPassword", newPassword);
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.PUT, new HttpEntity<>(request, headers),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            Map<String, Object> result = response.getBody();
            return result != null && Integer.valueOf(200).equals(result.get("code"));
        } catch (Exception e) {
            return false;
        }
    }

    private HttpEntity<Void> createAuthEntity(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return new HttpEntity<>(headers);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractDataList(Map<String, Object> result) {
        if (result != null && Integer.valueOf(200).equals(result.get("code"))) {
            Object data = result.get("data");
            if (data instanceof List) {
                return (List<Map<String, Object>>) data;
            }
        }
        return Collections.emptyList();
    }
}
