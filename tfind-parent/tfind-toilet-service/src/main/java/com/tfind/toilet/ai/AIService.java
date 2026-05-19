package com.tfind.toilet.ai;

import com.tfind.toilet.entity.Toilet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class AIService {

    private final DashScopeClient dashScopeClient;

    public AIService(@Value("${ai.dashscope.api-key}") String apiKey,
                     @Value("${ai.dashscope.model}") String model,
                     @Value("${ai.dashscope.vision-model}") String visionModel) {
        this.dashScopeClient = new DashScopeClient(apiKey, model, visionModel);
    }

    public String analyzeToilet(Toilet toilet) {
        String prompt = String.format(
                "分析以下厕所信息，给出评价和建议：名称：%s，地址：%s，楼层：%s，位置数：%d，" +
                "清洁评分：%.1f，评分：%.1f，是否有母婴室：%s，是否有无障碍设施：%s，" +
                "是否免费：%s，是否24小时：%s，是否排队：%s，排队时间：%d分钟",
                toilet.getName(), toilet.getAddress(), toilet.getFloor(),
                toilet.getPositions() != null ? toilet.getPositions() : 0,
                toilet.getCleanScore() != null ? toilet.getCleanScore() : 0,
                toilet.getScore() != null ? toilet.getScore() : 0,
                boolToStr(toilet.getHasMotherRoom()), boolToStr(toilet.getHasAccessibility()),
                boolToStr(toilet.getIsFree()), boolToStr(toilet.getIs24Hours()),
                boolToStr(toilet.getIsQueuing()),
                toilet.getQueueTime() != null ? toilet.getQueueTime() : 0
        );

        String result = dashScopeClient.chat(prompt);
        if (result != null) {
            return result;
        }

        return fallbackAnalyzeToilet(toilet);
    }

    public String analyzeReview(String content, List<String> tags) {
        String prompt = String.format(
                "分析以下厕所评论内容和标签，判断评论是否有效和合理：内容：%s，标签：%s",
                content, tags != null ? String.join(",", tags) : ""
        );

        String result = dashScopeClient.chat(prompt);
        if (result != null) {
            return result;
        }

        return "评论分析：基于规则分析，评论内容" + (content != null && content.length() > 5 ? "有效" : "过短");
    }

    public List<String> generateTags(String content) {
        String prompt = String.format(
                "根据以下厕所描述，生成3-5个标签（只返回标签，用逗号分隔）：%s", content
        );

        String result = dashScopeClient.chat(prompt);
        if (result != null && !result.isEmpty()) {
            String[] tagArr = result.split("[,，]");
            List<String> tags = new ArrayList<>();
            for (String tag : tagArr) {
                String trimmed = tag.trim();
                if (!trimmed.isEmpty()) {
                    tags.add(trimmed);
                }
            }
            return tags;
        }

        return fallbackGenerateTags(content);
    }

    public boolean validateFloor(String floor) {
        String prompt = String.format(
                "判断以下楼层描述是否合理（只回答是或否）：%s", floor
        );

        String result = dashScopeClient.chat(prompt);
        if (result != null) {
            return result.contains("是") || result.toLowerCase().contains("yes") || result.toLowerCase().contains("true");
        }

        return floor != null && floor.matches(".*\\d.*");
    }

    public boolean validateContent(String content) {
        String prompt = String.format(
                "判断以下内容是否合规（不包含违法、色情、暴力等不良信息，只回答是或否）：%s", content
        );

        String result = dashScopeClient.chat(prompt);
        if (result != null) {
            return result.contains("是") || result.toLowerCase().contains("yes") || result.toLowerCase().contains("true");
        }

        List<String> badWords = Arrays.asList("违法", "色情", "暴力");
        if (content == null) {
            return false;
        }
        for (String word : badWords) {
            if (content.contains(word)) {
                return false;
            }
        }
return true;
    }

    public boolean shouldAutoApprove(Toilet toilet) {
        String prompt = String.format(
                "判断以下厕所信息是否应该自动通过审核（只回答是或否）：名称：%s，地址：%s，楼层：%s，位置数：%d，" +
                "清洁评分：%.1f，评分：%.1f，是否有母婴室：%s，是否有无障碍设施：%s，" +
                "是否免费：%s，是否24小时：%s，是否排队：%s，排队时间：%d分钟",
                toilet.getName(), toilet.getAddress(), toilet.getFloor(),
                toilet.getPositions() != null ? toilet.getPositions() : 0,
                toilet.getCleanScore() != null ? toilet.getCleanScore() : 0,
                toilet.getScore() != null ? toilet.getScore() : 0,
                boolToStr(toilet.getHasMotherRoom()), boolToStr(toilet.getHasAccessibility()),
                boolToStr(toilet.getIsFree()), boolToStr(toilet.getIs24Hours()),
                boolToStr(toilet.getIsQueuing()),
                toilet.getQueueTime() != null ? toilet.getQueueTime() : 0
        );
        String result = dashScopeClient.chat(prompt);
        if (result != null) {
            return result.contains("是") || result.toLowerCase().contains("yes") || result.toLowerCase().contains("true");
        }
        return toilet.getScore() != null && toilet.getScore() >= 4.0;
    }

    public Map<String, Object> reviewToiletPhoto(String photoUrl) {
        Map<String, Object> result = new HashMap<>();
        if (photoUrl == null || photoUrl.isEmpty()) {
            result.put("passed", true);
            result.put("reason", "未上传照片，跳过图片审核");
            return result;
        }

        String dataUri;
        try {
            dataUri = localPathToDataUri(photoUrl);
            if (dataUri == null) {
                result.put("passed", false);
                result.put("reason", "图片文件读取失败");
                return result;
            }
        } catch (Exception e) {
            result.put("passed", false);
            result.put("reason", "图片文件读取失败");
            return result;
        }

        String analysis = dashScopeClient.analyzeImage(
                "请判断这张图片是否为厕所照片（只回答是或否），如果是厕所照片，简要描述厕所的卫生状况和设施情况。如果不是厕所照片，说明原因。",
                dataUri
        );

        if (analysis == null) {
            result.put("passed", true);
            result.put("reason", "审核通过（AI暂不可用，默认放行）");
            return result;
        }

        String lowerResult = analysis.toLowerCase().trim();
        if (lowerResult.startsWith("是") || lowerResult.contains("厕所") && !lowerResult.startsWith("否")) {
            result.put("passed", true);
            result.put("reason", "审核通过：" + analysis);
        } else {
            result.put("passed", false);
            result.put("reason", "审核不通过：" + analysis);
        }
        return result;
    }

    private String localPathToDataUri(String photoUrl) {
        try {
            String relativePath = photoUrl.startsWith("/") ? photoUrl.substring(1) : photoUrl;
            File file = new File(relativePath);
            if (!file.exists()) {
                return null;
            }
            byte[] bytes = Files.readAllBytes(file.toPath());
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String mimeType = "image/jpeg";
            String name = file.getName().toLowerCase();
            if (name.endsWith(".png")) {
                mimeType = "image/png";
            } else if (name.endsWith(".gif")) {
                mimeType = "image/gif";
            } else if (name.endsWith(".webp")) {
                mimeType = "image/webp";
            }
            return "data:" + mimeType + ";base64," + base64;
        } catch (IOException e) {
            return null;
        }
    }

    public double calculateAIScore(Toilet toilet) {
        String prompt = String.format(
                "根据以下厕所信息计算综合评分（0-100分，只返回数字）：" +
                "清洁评分：%.1f，评分：%.1f，是否有母婴室：%s，是否有无障碍设施：%s，" +
                "是否免费：%s，是否24小时：%s，是否排队：%s，排队时间：%d分钟",
                toilet.getCleanScore() != null ? toilet.getCleanScore() : 0,
                toilet.getScore() != null ? toilet.getScore() : 0,
                boolToStr(toilet.getHasMotherRoom()), boolToStr(toilet.getHasAccessibility()),
                boolToStr(toilet.getIsFree()), boolToStr(toilet.getIs24Hours()),
                boolToStr(toilet.getIsQueuing()),
                toilet.getQueueTime() != null ? toilet.getQueueTime() : 0
        );

        String result = dashScopeClient.chat(prompt);
        if (result != null) {
            try {
                String numStr = result.replaceAll("[^0-9.]", "").trim();
                if (!numStr.isEmpty()) {
                    double score = Double.parseDouble(numStr);
                    return Math.min(100, Math.max(0, score));
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return fallbackCalculateScore(toilet);
    }

    private String fallbackAnalyzeToilet(Toilet toilet) {
        StringBuilder sb = new StringBuilder();
        sb.append("基于规则分析：");
        if (toilet.getCleanScore() != null && toilet.getCleanScore() >= 4) {
            sb.append("清洁度良好；");
        } else {
            sb.append("清洁度待提升；");
        }
        if (Boolean.TRUE.equals(toilet.getHasMotherRoom())) {
            sb.append("配有母婴室；");
        }
        if (Boolean.TRUE.equals(toilet.getHasAccessibility())) {
            sb.append("配有无障碍设施；");
        }
        if (Boolean.TRUE.equals(toilet.getIsFree())) {
            sb.append("免费使用；");
        }
        if (Boolean.TRUE.equals(toilet.getIsQueuing())) {
            sb.append("可能需要排队；");
        }
        return sb.toString();
    }

    private List<String> fallbackGenerateTags(String content) {
        List<String> tags = new ArrayList<>();
        if (content == null) {
            return tags;
        }
        if (content.contains("免费")) tags.add("免费");
        if (content.contains("母婴")) tags.add("母婴室");
        if (content.contains("无障碍")) tags.add("无障碍");
        if (content.contains("24小时")) tags.add("24小时");
        if (content.contains("排队")) tags.add("排队");
        if (content.contains("干净")) tags.add("干净");
        if (tags.isEmpty()) tags.add("普通");
        return tags;
    }

    private double fallbackCalculateScore(Toilet toilet) {
        double score = 50;
        if (toilet.getCleanScore() != null) score += toilet.getCleanScore() * 3;
        if (toilet.getScore() != null) score += toilet.getScore() * 2;
        if (Boolean.TRUE.equals(toilet.getHasMotherRoom())) score += 5;
        if (Boolean.TRUE.equals(toilet.getHasAccessibility())) score += 5;
        if (Boolean.TRUE.equals(toilet.getIsFree())) score += 5;
        if (Boolean.TRUE.equals(toilet.getIs24Hours())) score += 5;
        if (Boolean.TRUE.equals(toilet.getIsQueuing())) score -= 10;
        if (toilet.getQueueTime() != null && toilet.getQueueTime() > 0) {
            score -= Math.min(toilet.getQueueTime(), 20);
        }
        return Math.min(100, Math.max(0, score));
    }

    private String boolToStr(Boolean b) {
        return b != null && b ? "是" : "否";
    }
}
