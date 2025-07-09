package com.csu.sms.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@Component
@Slf4j
public class BaiduFaceApiUtil {
    @Value("${baidu.face.api-key}")
    private String apiKey;
    @Value("${baidu.face.secret-key}")
    private String secretKey;
    @Value("${baidu.face.group-id}")
    private String groupId;

    // API URLs
    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String FACE_ADD_URL = "https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/add";
    private static final String FACE_SEARCH_URL = "https://aip.baidubce.com/rest/2.0/face/v3/search";
    private static final String FACE_DETECT_URL = "https://aip.baidubce.com/rest/2.0/face/v3/detect";
    private static final String FACE_MATCH_URL = "https://aip.baidubce.com/rest/2.0/face/v3/match";

    private String accessToken;
    private long tokenExpireTime = 0;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        log.info("初始化百度人脸API工具类，group_id: {}", groupId);
        getAccessToken();
    }

    /**
     * 获取access_token，自动缓存和刷新
     */
    public synchronized String getAccessToken() {
        long now = System.currentTimeMillis();
        if (StringUtils.hasText(accessToken) && now < tokenExpireTime) {
            return accessToken;
        }

        try {
            log.info("正在获取百度API access_token...");
            String url = TOKEN_URL + "?grant_type=client_credentials&client_id=" + apiKey + "&client_secret=" + secretKey;
            String result = httpGet(url);

            JsonNode node = objectMapper.readTree(result);
            accessToken = node.get("access_token").asText();
            int expiresIn = node.get("expires_in").asInt();
            tokenExpireTime = now + (expiresIn - 60) * 1000L; // 提前1分钟刷新
            return accessToken;
        } catch (Exception e) {
            log.error("获取百度access_token失败", e);
            throw new RuntimeException("获取百度access_token失败", e);
        }
    }

    /**
     * 注册人脸
     * @param userId 用户id
     * @param imageBase64 base64图片
     * @return 百度API返回结果
     */
    public JsonNode faceRegister(String userId, String imageBase64) throws IOException {
        log.info("开始注册人脸，用户ID: {}", userId);

        Map<String, Object> params = new HashMap<>();
        params.put("image", imageBase64);
        params.put("image_type", "BASE64");
        params.put("group_id", groupId);
        params.put("user_id", userId);
        params.put("quality_control", "NORMAL");
        params.put("liveness_control", "NORMAL");

        String result = httpPost(FACE_ADD_URL + "?access_token=" + getAccessToken(), objectMapper.writeValueAsString(params));
        return objectMapper.readTree(result);
    }

    /**
     * 搜索人脸
     * @param imageBase64 base64图片
     * @return 百度API返回结果
     */
    public JsonNode faceSearch(String imageBase64) throws IOException {
        log.info("开始搜索人脸匹配");

        Map<String, Object> params = new HashMap<>();
        params.put("image", imageBase64);
        params.put("image_type", "BASE64");
        params.put("group_id_list", groupId);
        params.put("quality_control", "NORMAL");
        params.put("liveness_control", "NORMAL");

        String result = httpPost(FACE_SEARCH_URL + "?access_token=" + getAccessToken(), objectMapper.writeValueAsString(params));
        return objectMapper.readTree(result);
    }

    /**
     * 人脸检测 - 检测图片中的人脸并返回详细信息
     * @param imageBase64 base64图片
     * @param faceField 需要返回的人脸字段，如 "age,beauty,expression,face_shape,gender,glasses,landmark,race,quality,face_type,angle"
     * @return 百度API返回结果
     */
    public JsonNode faceDetect(String imageBase64, String faceField) throws IOException {
        log.info("开始人脸检测，返回字段: {}", faceField);

        Map<String, Object> params = new HashMap<>();
        params.put("image", imageBase64);
        params.put("image_type", "BASE64");
        params.put("face_field", faceField);
        params.put("max_face_num", 10); // 最多检测10张人脸

        String result = httpPost(FACE_DETECT_URL + "?access_token=" + getAccessToken(), objectMapper.writeValueAsString(params));
        JsonNode response = objectMapper.readTree(result);

        log.info("人脸检测结果: {}", response);
        return response;
    }

    /**
     * 人脸检测 - 使用默认字段
     * @param imageBase64 base64图片
     * @return 百度API返回结果
     */
    public JsonNode faceDetect(String imageBase64) throws IOException {
        // 默认返回角度、质量、表情等关键信息
        String defaultFields = "age,beauty,expression,face_shape,gender,glasses,quality,face_type,angle";
        return faceDetect(imageBase64, defaultFields);
    }

    /**
     * 人脸对比 - 比较两张图片中的人脸相似度
     * @param imageBase64_1 第一张图片base64
     * @param imageBase64_2 第二张图片base64
     * @return 百度API返回结果
     */
    public JsonNode faceMatch(String imageBase64_1, String imageBase64_2) throws IOException {
        log.info("开始人脸对比");

        Map<String, Object> params = new HashMap<>();
        params.put("image", imageBase64_1);
        params.put("image_type", "BASE64");
        params.put("face_field", "quality");

        // 构建对比图片数组
        Map<String, Object> compareImage = new HashMap<>();
        compareImage.put("image", imageBase64_2);
        compareImage.put("image_type", "BASE64");
        compareImage.put("face_field", "quality");

        params.put("face_field", "quality");

        // 实际API需要的是两个独立的参数
        String jsonStr = String.format(
                "[{\"image\":\"%s\",\"image_type\":\"BASE64\",\"face_field\":\"quality\"}," +
                        "{\"image\":\"%s\",\"image_type\":\"BASE64\",\"face_field\":\"quality\"}]",
                imageBase64_1, imageBase64_2
        );

        String result = httpPost(FACE_MATCH_URL + "?access_token=" + getAccessToken(), jsonStr);
        JsonNode response = objectMapper.readTree(result);

        log.info("人脸对比结果: {}", response);
        return response;
    }

    /**
     * 综合人脸分析 - 检测人脸并进行身份验证
     * @param imageBase64 base64图片
     * @param expectedUserId 期望的用户ID（可选）
     * @return 综合分析结果
     */
    public FaceAnalysisResult comprehensiveFaceAnalysis(String imageBase64, String expectedUserId) throws IOException {
        log.info("开始综合人脸分析，期望用户ID: {}", expectedUserId);

        FaceAnalysisResult result = new FaceAnalysisResult();

        try {
            // 1. 先进行人脸检测
            JsonNode detectResult = faceDetect(imageBase64);
            result.setDetectResult(detectResult);

            if (detectResult.get("error_code").asInt() != 0) {
                result.setSuccess(false);
                result.setErrorMessage("人脸检测失败: " + detectResult.get("error_msg").asText());
                return result;
            }

            JsonNode faceList = detectResult.get("result").get("face_list");
            result.setFaceCount(faceList.size());

            if (faceList.size() == 0) {
                result.setSuccess(false);
                result.setErrorMessage("未检测到人脸");
                return result;
            }

            if (faceList.size() > 1) {
                result.setSuccess(false);
                result.setErrorMessage("检测到多张人脸");
                return result;
            }

            // 2. 获取人脸信息
            JsonNode face = faceList.get(0);
            if (face.has("angle")) {
                JsonNode angle = face.get("angle");
                result.setYaw(angle.get("yaw").asDouble());
                result.setPitch(angle.get("pitch").asDouble());
                result.setRoll(angle.get("roll").asDouble());
            }

            if (face.has("quality")) {
                result.setQuality(face.get("quality").get("completeness").asDouble());
            }

            // 3. 如果需要身份验证，进行人脸搜索
            if (expectedUserId != null) {
                JsonNode searchResult = faceSearch(imageBase64);
                result.setSearchResult(searchResult);

                if (searchResult.get("error_code").asInt() == 0) {
                    JsonNode userList = searchResult.get("result").get("user_list");
                    if (userList.size() > 0) {
                        JsonNode topUser = userList.get(0);
                        result.setDetectedUserId(topUser.get("user_id").asText());
                        result.setMatchScore(topUser.get("score").asDouble());
                        result.setIdentityVerified(
                                topUser.get("user_id").asText().equals(expectedUserId) &&
                                        topUser.get("score").asDouble() >= 80
                        );
                    }
                }
            }

            result.setSuccess(true);
            log.info("综合人脸分析完成: {}", result);

        } catch (Exception e) {
            log.error("综合人脸分析失败", e);
            result.setSuccess(false);
            result.setErrorMessage("分析过程中发生错误: " + e.getMessage());
        }

        return result;
    }

    /**
     * 检查API连接状态
     * @return 连接状态信息
     */
    public Map<String, Object> checkApiStatus() {
        Map<String, Object> status = new HashMap<>();

        try {
            String token = getAccessToken();
            status.put("token_available", token != null);
            status.put("token_expires_at", tokenExpireTime);
            status.put("group_id", groupId);
            status.put("api_key_configured", apiKey != null && !apiKey.isEmpty());
            status.put("secret_key_configured", secretKey != null && !secretKey.isEmpty());

            // 简单测试API连接
            if (token != null) {
                // 可以调用一个简单的API来测试连接
                status.put("api_accessible", true);
            }

        } catch (Exception e) {
            log.error("检查API状态失败", e);
            status.put("error", e.getMessage());
            status.put("api_accessible", false);
        }

        return status;
    }

    // HTTP 请求方法
    private String httpGet(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000); // 10秒连接超时
        conn.setReadTimeout(30000); // 30秒读取超时
        conn.connect();

        try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    private String httpPost(String urlStr, String json) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000); // 10秒连接超时
        conn.setReadTimeout(30000); // 30秒读取超时
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");

        conn.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));

        try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    // Getter方法
    public String getGroupId() {
        return groupId;
    }

    /**
     * 人脸分析结果类
     */
    public static class FaceAnalysisResult {
        private boolean success;
        private String errorMessage;
        private int faceCount;
        private double yaw;
        private double pitch;
        private double roll;
        private double quality;
        private String detectedUserId;
        private double matchScore;
        private boolean identityVerified;
        private JsonNode detectResult;
        private JsonNode searchResult;

        // Getter和Setter方法
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public int getFaceCount() { return faceCount; }
        public void setFaceCount(int faceCount) { this.faceCount = faceCount; }

        public double getYaw() { return yaw; }
        public void setYaw(double yaw) { this.yaw = yaw; }

        public double getPitch() { return pitch; }
        public void setPitch(double pitch) { this.pitch = pitch; }

        public double getRoll() { return roll; }
        public void setRoll(double roll) { this.roll = roll; }

        public double getQuality() { return quality; }
        public void setQuality(double quality) { this.quality = quality; }

        public String getDetectedUserId() { return detectedUserId; }
        public void setDetectedUserId(String detectedUserId) { this.detectedUserId = detectedUserId; }

        public double getMatchScore() { return matchScore; }
        public void setMatchScore(double matchScore) { this.matchScore = matchScore; }

        public boolean isIdentityVerified() { return identityVerified; }
        public void setIdentityVerified(boolean identityVerified) { this.identityVerified = identityVerified; }

        public JsonNode getDetectResult() { return detectResult; }
        public void setDetectResult(JsonNode detectResult) { this.detectResult = detectResult; }

        public JsonNode getSearchResult() { return searchResult; }
        public void setSearchResult(JsonNode searchResult) { this.searchResult = searchResult; }

        @Override
        public String toString() {
            return "FaceAnalysisResult{" +
                    "success=" + success +
                    ", errorMessage='" + errorMessage + '\'' +
                    ", faceCount=" + faceCount +
                    ", yaw=" + yaw +
                    ", pitch=" + pitch +
                    ", roll=" + roll +
                    ", quality=" + quality +
                    ", detectedUserId='" + detectedUserId + '\'' +
                    ", matchScore=" + matchScore +
                    ", identityVerified=" + identityVerified +
                    '}';
        }
    }
}