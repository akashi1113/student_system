package com.csu.sms.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.csu.sms.config.AIConfig;
import com.csu.sms.dto.AILearningSuggestionDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AIService {
    private static final Logger logger = LoggerFactory.getLogger(AIService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private AIConfig aiConfig;

    public List<AILearningSuggestionDTO.Suggestion> getLearningSuggestions(AILearningSuggestionDTO.Request request) {
        try {
            logger.info("开始获取AI学习建议，请求参数：{}", request);
            String prompt = buildPrompt(request);
            logger.info("构建的AI提示词：{}", prompt);
            String aiResponse = callQwenAPI(prompt);
            logger.info("AI API返回原始响应：{}", aiResponse);
            List<AILearningSuggestionDTO.Suggestion> suggestions = parseAIResponse(aiResponse);
            if (suggestions == null || suggestions.isEmpty()) {
                logger.warn("AI响应解析失败，使用备用建议");
                return getFallbackSuggestions(request);
            }
            logger.info("成功解析AI响应，返回{}条建议", suggestions.size());
            return suggestions;
        } catch (Exception e) {
            logger.error("获取AI学习建议失败，使用备用建议", e);
            return getFallbackSuggestions(request);
        }
    }

    private String buildPrompt(AILearningSuggestionDTO.Request request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的学习顾问，请根据以下学习数据分析，为用户提供个性化的学习建议：\n\n");
        prompt.append("学习数据概况：\n");
        prompt.append("- 平均分：").append(request.getAverageScore() != null ? request.getAverageScore() : 0).append("分\n");
        prompt.append("- 学习时长：").append(formatStudyTime(request.getTotalStudyTime())).append("\n");
        prompt.append("- 考试通过率：").append(request.getExamPassRate() != null ? request.getExamPassRate() : 0).append("%\n");
        prompt.append("- 最活跃学习时段：").append(request.getMostActivePeriod() != null ? request.getMostActivePeriod() : "未知").append("\n\n");
        if (request.getTimeDistribution() != null && !request.getTimeDistribution().isEmpty()) {
            prompt.append("学习时间分布：\n");
            for (java.util.Map.Entry<String, Double> entry : request.getTimeDistribution().entrySet()) {
                prompt.append("- ").append(entry.getKey()).append("：").append(entry.getValue()).append("小时\n");
            }
            prompt.append("\n");
        }
        if (request.getExamRecords() != null && !request.getExamRecords().isEmpty()) {
            prompt.append("成绩记录：\n");
            for (AILearningSuggestionDTO.ExamRecordInfo exam : request.getExamRecords()) {
                prompt.append("- ").append(exam.getExamTitle()).append("：")
                      .append(exam.getScore()).append("分 (第").append(exam.getAttemptNumber()).append("次)\n");
            }
            prompt.append("\n");
        }
        if (request.getStudyRecords() != null && !request.getStudyRecords().isEmpty()) {
            prompt.append("学习记录：\n");
            for (AILearningSuggestionDTO.StudyRecordInfo study : request.getStudyRecords()) {
                prompt.append("- ").append(study.getCourseTitle()).append(" - ").append(study.getVideoTitle())
                      .append("：进度").append(study.getProgressPercentage()).append("%\n");
            }
            prompt.append("\n");
        }
        prompt.append("请提供3-5条具体的、可操作的学习建议，每条建议包含：\n");
        prompt.append("1. 建议类型（improve/optimize/maintain）\n");
        prompt.append("2. 建议标题\n");
        prompt.append("3. 具体内容\n");
        prompt.append("4. 优先级（high/medium/low）\n\n");
        prompt.append("请以JSON格式返回，格式如下：\n");
        prompt.append("[{\"type\":\"improve\",\"title\":\"建议标题\",\"content\":\"具体内容\",\"priority\":\"high\"}]\n");
        prompt.append("注意：只返回JSON格式数据，不要包含其他文字。");
        return prompt.toString();
    }

    private String callQwenAPI(String prompt) throws Exception {
        logger.info("开始调用通义千问API");
        logger.info("使用的模型：{}", aiConfig.getModel());
        logger.info("API Key长度：{}", aiConfig.getApiKey() != null ? aiConfig.getApiKey().length() : 0);
        GenerationParam param = GenerationParam.builder()
                .model(aiConfig.getModel())
                .prompt(prompt)
                .apiKey(aiConfig.getApiKey())
                .topP(Double.valueOf(0.8f))
                .temperature(Float.valueOf(0.7f))
                .maxTokens(Integer.valueOf(2000))
                .build();
        logger.info("构建的请求参数：model={}, topP={}, temperature={}, maxTokens={}", 
                   param.getModel(), param.getTopP(), param.getTemperature(), param.getMaxTokens());
        Generation generation = new Generation();
        logger.info("开始调用Generation.call()");
        GenerationResult result = generation.call(param);
        logger.info("API调用完成，结果：{}", result);
        if (result != null) {
            logger.info("结果输出：{}", result.getOutput());
            if (result.getOutput() != null) {
                if (result.getOutput().getText() != null && !result.getOutput().getText().isEmpty()) {
                    String content = result.getOutput().getText();
                    logger.info("从text字段获取到内容：{}", content);
                    return content;
                }
                logger.info("输出选择：{}", result.getOutput().getChoices());
                if (result.getOutput().getChoices() != null && !result.getOutput().getChoices().isEmpty()) {
                    String content = result.getOutput().getChoices().get(0).getMessage().getContent();
                    logger.info("从choices字段获取到内容：{}", content);
                    return content;
                }
            }
        }
        logger.error("AI API返回结果为空，完整结果对象：{}", result);
        throw new RuntimeException("AI API返回结果为空");
    }

    private List<AILearningSuggestionDTO.Suggestion> parseAIResponse(String aiResponse) {
        try {
            String jsonStr = extractJsonFromResponse(aiResponse);
            List<AILearningSuggestionDTO.Suggestion> suggestions = objectMapper.readValue(
                jsonStr, new TypeReference<List<AILearningSuggestionDTO.Suggestion>>() {}
            );
            return suggestions;
        } catch (JsonProcessingException e) {
            logger.error("解析AI响应失败", e);
            return null;
        }
    }

    private String extractJsonFromResponse(String response) {
        int start = response.indexOf('[');
        int end = response.lastIndexOf(']');
        if (start != -1 && end != -1 && end > start) {
            return response.substring(start, end + 1);
        }
        start = response.indexOf('{');
        end = response.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return "[" + response.substring(start, end + 1) + "]";
        }
        throw new RuntimeException("无法从AI响应中提取JSON");
    }

    private List<AILearningSuggestionDTO.Suggestion> getFallbackSuggestions(AILearningSuggestionDTO.Request request) {
        List<AILearningSuggestionDTO.Suggestion> suggestions = new ArrayList<>();
        if (request.getAverageScore() != null && request.getAverageScore() < 60) {
            suggestions.add(new AILearningSuggestionDTO.Suggestion(
                "improve", "提高基础成绩", 
                "建议加强基础知识复习，多做练习题，争取平均分提升到及格线以上。", 
                "high"
            ));
        } else if (request.getAverageScore() != null && request.getAverageScore() < 80) {
            suggestions.add(new AILearningSuggestionDTO.Suggestion(
                "improve", "提升学习效果", 
                "建议多做历年真题，总结错题，争取平均分提升到80分以上。", 
                "medium"
            ));
        } else {
            suggestions.add(new AILearningSuggestionDTO.Suggestion(
                "maintain", "保持学习节奏", 
                "建议保持现有的学习节奏，定期复习已学内容。", 
                "medium"
            ));
        }
        if (request.getTotalStudyTime() != null && request.getTotalStudyTime() < 3600) {
            suggestions.add(new AILearningSuggestionDTO.Suggestion(
                "optimize", "增加学习时间", 
                "建议增加学习投入时间，每天至少保证1小时的学习时间。", 
                "high"
            ));
        }
        if (request.getExamPassRate() != null && request.getExamPassRate() < 80) {
            suggestions.add(new AILearningSuggestionDTO.Suggestion(
                "improve", "提高考试通过率", 
                "考试通过率偏低，建议在充分准备后再参加考试，多做模拟题。", 
                "medium"
            ));
        }
        if (suggestions.isEmpty()) {
            suggestions.add(new AILearningSuggestionDTO.Suggestion(
                "maintain", "保持学习节奏", 
                "建议保持现有的学习节奏，定期复习已学内容。", 
                "medium"
            ));
        }
        return suggestions;
    }

    private String formatStudyTime(Long totalSeconds) {
        if (totalSeconds == null) return "0小时";
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        if (hours > 0) {
            return hours + "小时" + (minutes > 0 ? minutes + "分钟" : "");
        } else {
            return minutes + "分钟";
        }
    }
} 