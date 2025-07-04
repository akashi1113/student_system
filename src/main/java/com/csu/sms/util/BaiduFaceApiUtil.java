package com.csu.sms.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class BaiduFaceApiUtil {
    @Value("${baidu.face.api-key}")
    private String apiKey;
    @Value("${baidu.face.secret-key}")
    private String secretKey;
    @Value("${baidu.face.group-id}")
    private String groupId;

    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String FACE_ADD_URL = "https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/add";
    private static final String FACE_SEARCH_URL = "https://aip.baidubce.com/rest/2.0/face/v3/search";

    private String accessToken;
    private long tokenExpireTime = 0;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
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
            String url = TOKEN_URL + "?grant_type=client_credentials&client_id=" + apiKey + "&client_secret=" + secretKey;
            String result = httpGet(url);
            JsonNode node = objectMapper.readTree(result);
            accessToken = node.get("access_token").asText();
            int expiresIn = node.get("expires_in").asInt();
            tokenExpireTime = now + (expiresIn - 60) * 1000L; // 提前1分钟刷新
            return accessToken;
        } catch (Exception e) {
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
        Map<String, Object> params = new HashMap<>();
        params.put("image", imageBase64);
        params.put("image_type", "BASE64");
        params.put("group_id_list", groupId);
        params.put("quality_control", "NORMAL");
        params.put("liveness_control", "NORMAL");
        String result = httpPost(FACE_SEARCH_URL + "?access_token=" + getAccessToken(), objectMapper.writeValueAsString(params));
        return objectMapper.readTree(result);
    }

    private String httpGet(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
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
        conn.setRequestProperty("Content-Type", "application/json");
        conn.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
        try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    public String getGroupId() {
        return groupId;
    }
} 