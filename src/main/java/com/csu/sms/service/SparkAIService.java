package com.csu.sms.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.csu.sms.config.SparkConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SparkAIService {
    private final SparkConfig sparkConfig;

    public String generateKeywords(String query) {
        try {
            // 构建请求URL
            URL url = new URL("https://spark-api-open.xf-yun.com/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + sparkConfig.getApiPassword());
            conn.setDoOutput(true);

            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", sparkConfig.getModel()); // "lite"
            requestBody.put("user", "forum_search");

            JSONArray messages = new JSONArray();
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", "你是一个专业的关键词提取器，请根据用户问题生成3-5个最适合的关键词，用空格分隔");
            messages.add(systemMsg);

            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", query);
            messages.add(userMsg);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.1);
            requestBody.put("max_tokens", 100);

            // 发送请求
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toJSONString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 读取响应
            if (conn.getResponseCode() != 200) {
                log.error("Spark API request failed: {}", conn.getResponseCode());
                return query; // 失败时返回原始查询
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                // 解析响应
                JSONObject jsonResponse = JSON.parseObject(response.toString());
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject firstChoice = choices.getJSONObject(0);
                    JSONObject message = firstChoice.getJSONObject("message");
                    return message.getString("content");
                }
            }
        } catch (Exception e) {
            log.error("Error calling Spark API", e);
        }
        return query;
    }

    public List<Long> findRelatedPosts(Long postId, String postContent, int count) {
        List<Long> relatedPostIds = new ArrayList<>();
        try {
            // 构建请求URL
            URL url = new URL("https://spark-api-open.xf-yun.com/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + sparkConfig.getApiPassword());
            conn.setDoOutput(true);

            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", sparkConfig.getModel()); // "lite"
            requestBody.put("user", "forum_related");

            JSONArray messages = new JSONArray();
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", "你是一个内容匹配专家，请根据提供的帖子内容，推荐" + count + "个最相关的帖子ID。"
                    + "只返回ID列表，格式如：[123, 456, 789]");
            messages.add(systemMsg);

            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", "帖子内容：" + postContent);
            messages.add(userMsg);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.2);
            requestBody.put("max_tokens", 100);

            // 发送请求
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toJSONString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 读取响应
            if (conn.getResponseCode() != 200) {
                log.error("Spark API request failed: {}", conn.getResponseCode());
                return relatedPostIds;
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                // 解析响应
                JSONObject jsonResponse = JSON.parseObject(response.toString());
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject firstChoice = choices.getJSONObject(0);
                    JSONObject message = firstChoice.getJSONObject("message");
                    String content = message.getString("content");

                    // 解析ID列表
                    if (content.startsWith("[") && content.endsWith("]")) {
                        JSONArray idArray = JSON.parseArray(content);
                        for (int i = 0; i < idArray.size(); i++) {
                            relatedPostIds.add(idArray.getLong(i));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error finding related posts", e);
        }
        return relatedPostIds;
    }

    public String chatWithAI(String userMessage) {
        try {
            URL url = new URL("https://spark-api-open.xf-yun.com/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + sparkConfig.getApiPassword());
            conn.setDoOutput(true);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", sparkConfig.getModel()); // "lite"

            JSONArray messages = new JSONArray();

            // 系统角色设定
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", "你是视频学习助手小光球，专注解答计算机网络课程相关问题");
            messages.add(systemMsg);

            // 用户消息
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1024);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toJSONString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            if (conn.getResponseCode() != 200) {
                log.error("Spark API request failed: {}", conn.getResponseCode());
                return "抱歉，我暂时无法回答这个问题";
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                JSONObject jsonResponse = JSON.parseObject(response.toString());
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject firstChoice = choices.getJSONObject(0);
                    JSONObject message = firstChoice.getJSONObject("message");
                    return message.getString("content");
                }
            }
        } catch (Exception e) {
            log.error("Error calling Spark API", e);
        }
        return "网络请求异常，请稍后再试";
    }

    public String summarizeContent(String content) {
        try {
            // 简化内容（保留前3000字）
            String simplifiedContent = content.length() > 3000
                    ? content.substring(0, 3000) + "..."
                    : content;

            URL url = new URL("https://spark-api-open.xf-yun.com/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + sparkConfig.getApiPassword());
            conn.setDoOutput(true);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", sparkConfig.getModel());
            requestBody.put("user", "forum_summary");

            JSONArray messages = new JSONArray();

            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", "你是一个内容总结助手，请用简洁的语言总结以下帖子内容，控制在100字以内");
            messages.add(systemMsg);

            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", "帖子内容：" + simplifiedContent);
            messages.add(userMsg);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.2);
            requestBody.put("max_tokens", 150);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toJSONString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            if (conn.getResponseCode() != 200) {
                log.error("总结请求失败: {}", conn.getResponseCode());
                return "总结生成失败，请稍后再试";
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                JSONObject jsonResponse = JSON.parseObject(response.toString());
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    return choices.getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");
                }
            }
        } catch (Exception e) {
            log.error("内容总结异常", e);
        }
        return "总结生成失败";
    }

    public String reviewPostContent(String content) {
        try {
            // 简化内容（保留前3000字）
            String simplifiedContent = content.length() > 3000
                    ? content.substring(0, 3000) + "..."
                    : content;

            // 构建请求URL
            URL url = new URL("https://spark-api-open.xf-yun.com/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + sparkConfig.getApiPassword());
            conn.setDoOutput(true);

            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", sparkConfig.getModel());
            requestBody.put("user", "forum_review");

            JSONArray messages = new JSONArray();

            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", "你是一个论坛内容审核助手。请仔细阅读以下帖子内容并判断："
                    + "1. 是否包含违法违规内容（暴力、色情、赌博、毒品等）\n"
                    + "2. 是否包含恶意攻击、侮辱、诽谤他人内容\n"
                    + "3. 是否包含广告、垃圾信息\n"
                    + "4. 是否与论坛主题（学习交流）相关\n\n"
                    + "请用以下格式回答：\n"
                    + "【审核结果】通过/不通过/建议人工审核\n"
                    + "【原因】简要说明原因（1-2句话）");
            messages.add(systemMsg);

            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", "帖子内容：" + simplifiedContent);
            messages.add(userMsg);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.1);
            requestBody.put("max_tokens", 200);

            // 发送请求
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toJSONString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 读取响应
            if (conn.getResponseCode() != 200) {
                log.error("总结请求失败: {}", conn.getResponseCode());
                return "总结生成失败，请稍后再试";
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                // 解析响应
                JSONObject jsonResponse = JSON.parseObject(response.toString());
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject firstChoice = choices.getJSONObject(0);
                    JSONObject message = firstChoice.getJSONObject("message");
                    return message.getString("content");
                }
            }
        } catch (Exception e) {
            log.error("AI审核异常", e);
            return "审核失败：" + e.getMessage();
        }
        return "审核失败，未知错误";
    }
}
