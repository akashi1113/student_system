package com.csu.sms.service;

import com.csu.sms.common.ApiResponse;
import com.csu.sms.dto.exam.AIGradingResponse;
import com.csu.sms.model.question.Question;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class AIGradingService {

    private static final Logger logger = LoggerFactory.getLogger(AIGradingService.class);

    @Autowired
    private DeepSeekChatModel chatModel;

    @Value("${ai.grading.enabled:true}")
    private boolean aiGradingEnabled;

    @Value("${ai.grading.retry-count:3}")
    private int retryCount;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // AI评分简答题
    public ApiResponse<AIGradingResponse> gradeTextAnswer(Question question, String studentAnswer) {
        try {
            if (!aiGradingEnabled) {
                return ApiResponse.error("AI评分功能未启用");
            }

            if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
                return ApiResponse.success("学生未作答",
                        new AIGradingResponse(0, 0.0, "学生未作答", new ArrayList<>()));
            }

            String prompt = buildTextGradingPrompt(question, studentAnswer);
            AIGradingResponse result = callAIServiceWithRetry(prompt, question.getScore());

            return ApiResponse.success("简答题AI评分完成", result);

        } catch (Exception e) {
            logger.error("简答题AI评分失败", e);
            // 降级处理
            AIGradingResponse fallbackResult = fallbackTextGrading(question, studentAnswer);
            return ApiResponse.success("AI服务异常，使用备用评分方案", fallbackResult);
        }
    }

    // AI评分填空题
    public ApiResponse<AIGradingResponse> gradeFillAnswer(Question question, String studentAnswer) {
        try {
            if (!aiGradingEnabled) {
                return ApiResponse.error("AI评分功能未启用");
            }

            if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
                return ApiResponse.success("学生未作答",
                        new AIGradingResponse(0, 0.0, "学生未作答", new ArrayList<>()));
            }

            String prompt = buildFillGradingPrompt(question, studentAnswer);
            AIGradingResponse result = callAIServiceWithRetry(prompt, question.getScore());

            return ApiResponse.success("填空题AI评分完成", result);

        } catch (Exception e) {
            logger.error("填空题AI评分失败", e);
            AIGradingResponse fallbackResult = fallbackFillGrading(question, studentAnswer);
            return ApiResponse.success("AI服务异常，使用备用评分方案", fallbackResult);
        }
    }

    // AI评分编程题
    public ApiResponse<AIGradingResponse> gradeProgrammingAnswer(Question question, String studentAnswer) {
        try {
            if (!aiGradingEnabled) {
                return ApiResponse.error("AI评分功能未启用");
            }

            if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
                return ApiResponse.success("学生未作答",
                        new AIGradingResponse(0, 0.0, "学生未作答", new ArrayList<>()));
            }

            String prompt = buildProgrammingGradingPrompt(question, studentAnswer);
            AIGradingResponse result = callAIServiceWithRetry(prompt, question.getScore());

            return ApiResponse.success("编程题AI评分完成", result);

        } catch (Exception e) {
            logger.error("编程题AI评分失败", e);
            AIGradingResponse fallbackResult = fallbackProgrammingGrading(question, studentAnswer);
            return ApiResponse.success("AI服务异常，使用备用评分方案", fallbackResult);
        }
    }

    // 构建简答题评分提示词
    private String buildTextGradingPrompt(Question question, String studentAnswer) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请作为专业教师评分以下简答题：\n\n");
        prompt.append("题目：").append(question.getContent()).append("\n");
        prompt.append("满分：").append(question.getScore()).append("分\n");

        if (question.getAnalysis() != null && !question.getAnalysis().isEmpty()) {
            prompt.append("参考答案/评分标准：").append(question.getAnalysis()).append("\n");
        }

        prompt.append("学生答案：").append(studentAnswer).append("\n\n");
        prompt.append("评分要求：\n");
        prompt.append("1. 根据答案的准确性、完整性和逻辑性进行评分\n");
        prompt.append("2. 考虑关键知识点的覆盖程度\n");
        prompt.append("3. 允许合理的表达方式差异\n");
        prompt.append("4. 给出具体的评分理由和改进建议\n\n");
        prompt.append("请严格按照以下JSON格式返回评分结果，不要包含任何其他文字：\n");
        prompt.append("{\n");
        prompt.append("  \"score\": 实际得分(整数),\n");
        prompt.append("  \"scoreRatio\": 得分率(0-1的小数),\n");
        prompt.append("  \"feedback\": \"详细的评分反馈和改进建议\",\n");
        prompt.append("  \"keyPoints\": [\"关键得分点1\", \"关键得分点2\", \"关键得分点3\"]\n");
        prompt.append("}");

        return prompt.toString();
    }

    // 构建填空题评分提示词
    private String buildFillGradingPrompt(Question question, String studentAnswer) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请评分以下填空题：\n\n");
        prompt.append("题目：").append(question.getContent()).append("\n");
        prompt.append("满分：").append(question.getScore()).append("分\n");

        if (question.getAnalysis() != null && !question.getAnalysis().isEmpty()) {
            prompt.append("标准答案：").append(question.getAnalysis()).append("\n");
        }

        prompt.append("学生答案：").append(studentAnswer).append("\n\n");
        prompt.append("评分要求：\n");
        prompt.append("1. 考虑答案的准确性和完整性\n");
        prompt.append("2. 允许合理的同义词替换和表达方式\n");
        prompt.append("3. 注意专业术语的准确性\n");
        prompt.append("4. 考虑大小写和格式的合理性\n\n");
        prompt.append("请严格按照以下JSON格式返回评分结果，不要包含任何其他文字：\n");
        prompt.append("{\n");
        prompt.append("  \"score\": 实际得分(整数),\n");
        prompt.append("  \"scoreRatio\": 得分率(0-1的小数),\n");
        prompt.append("  \"feedback\": \"评分说明和建议\",\n");
        prompt.append("  \"keyPoints\": [\"评分要点1\", \"评分要点2\"]\n");
        prompt.append("}");

        return prompt.toString();
    }

    // 构建编程题评分提示词
    private String buildProgrammingGradingPrompt(Question question, String studentAnswer) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请评分以下编程题：\n\n");
        prompt.append("题目：").append(question.getContent()).append("\n");
        prompt.append("满分：").append(question.getScore()).append("分\n");

        if (question.getAnalysis() != null && !question.getAnalysis().isEmpty()) {
            prompt.append("题目要求/测试用例：").append(question.getAnalysis()).append("\n");
        }

        prompt.append("学生代码：\n```\n").append(studentAnswer).append("\n```\n\n");
        prompt.append("评分标准：\n");
        prompt.append("1. 代码正确性 (40%) - 能否解决问题，逻辑是否正确\n");
        prompt.append("2. 算法效率 (30%) - 时间和空间复杂度是否合理\n");
        prompt.append("3. 代码质量 (20%) - 可读性、结构合理性、变量命名\n");
        prompt.append("4. 代码规范 (10%) - 缩进、注释、格式规范\n\n");
        prompt.append("请严格按照以下JSON格式返回评分结果，不要包含任何其他文字：\n");
        prompt.append("{\n");
        prompt.append("  \"score\": 实际得分(整数),\n");
        prompt.append("  \"scoreRatio\": 得分率(0-1的小数),\n");
        prompt.append("  \"feedback\": \"详细评分反馈，包括优点和改进建议\",\n");
        prompt.append("  \"keyPoints\": [\"代码正确性评价\", \"算法效率评价\", \"代码质量评价\", \"代码规范评价\"]\n");
        prompt.append("}");

        return prompt.toString();
    }

    // 带重试的AI服务调用
    private AIGradingResponse callAIServiceWithRetry(String prompt, Integer maxScore) {
        Exception lastException = null;

        for (int i = 0; i < retryCount; i++) {
            try {
                return callAIService(prompt, maxScore);
            } catch (Exception e) {
                lastException = e;
                logger.warn("AI服务调用失败，第{}次重试", i + 1, e);

                if (i < retryCount - 1) {
                    try {
                        Thread.sleep(1000 * (i + 1)); // 递增延迟
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        throw new RuntimeException("AI服务调用失败，已重试" + retryCount + "次", lastException);
    }

    // 调用AI服务
    private AIGradingResponse callAIService(String userPrompt, Integer maxScore) {
        try {
            // 系统消息，定义AI的角色
            SystemMessage systemMessage = new SystemMessage(
                    "你是一个专业的教师，负责客观公正地评分学生答案。" +
                            "你必须严格按照要求的JSON格式返回结果，不要包含任何其他文字或解释。" +
                            "评分要公平、准确，并给出建设性的反馈。"
            );

            // 用户消息，包含具体的评分任务
            UserMessage userMessage = new UserMessage(userPrompt);

            // 创建提示
            Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

            logger.debug("调用DeepSeek AI服务进行评分");

            // 调用AI模型
            ChatResponse response = chatModel.call(prompt);

            if (response == null || response.getResult() == null ||
                    response.getResult().getOutput() == null) {
                throw new RuntimeException("AI服务返回空响应");
            }

            String content = String.valueOf(response.getResult().getOutput());
            logger.debug("AI服务响应内容: {}", content);

            return parseAIResponse(content, maxScore);

        } catch (Exception e) {
            logger.error("AI服务调用异常", e);
            throw new RuntimeException("AI服务调用异常: " + e.getMessage(), e);
        }
    }

    // 解析AI响应
    private AIGradingResponse parseAIResponse(String content, Integer maxScore) {
        try {
            if (content == null || content.trim().isEmpty()) {
                throw new RuntimeException("AI响应内容为空");
            }

            // 清理可能的markdown格式和多余的文字
            content = content.trim();
            if (content.contains("```json")) {
                content = content.substring(content.indexOf("```json") + 7);
                content = content.substring(0, content.indexOf("```"));
            } else if (content.contains("```")) {
                content = content.replaceAll("```[a-zA-Z]*", "").replaceAll("```", "");
            }

            // 尝试提取JSON部分
            int jsonStart = content.indexOf('{');
            int jsonEnd = content.lastIndexOf('}');
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                content = content.substring(jsonStart, jsonEnd + 1);
            }

            // 解析JSON
            var result = objectMapper.readValue(content, java.util.Map.class);

            Integer score = extractIntegerValue(result.get("score"));
            Double scoreRatio = extractDoubleValue(result.get("scoreRatio"));
            String feedback = (String) result.get("feedback");
            List<String> keyPoints = (List<String>) result.get("keyPoints");

            // 数据验证和修正
            if (score == null) score = 0;
            if (score > maxScore) score = maxScore;
            if (score < 0) score = 0;

            if (scoreRatio == null) scoreRatio = (double) score / maxScore;
            if (scoreRatio > 1.0) scoreRatio = 1.0;
            if (scoreRatio < 0.0) scoreRatio = 0.0;

            if (feedback == null) feedback = "AI评分完成";
            if (keyPoints == null) keyPoints = new ArrayList<>();

            return new AIGradingResponse(score, scoreRatio, feedback, keyPoints);

        } catch (Exception e) {
            logger.error("解析AI响应失败，原始内容: {}", content, e);
            throw new RuntimeException("解析AI响应失败: " + e.getMessage(), e);
        }
    }

    // 辅助方法：提取整数值
    private Integer extractIntegerValue(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Double) {
            return ((Double) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    // 辅助方法：提取双精度值
    private Double extractDoubleValue(Object value) {
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    // 降级处理方法（与之前相同）
    private AIGradingResponse fallbackTextGrading(Question question, String studentAnswer) {
        String correctAnswer = question.getAnalysis();
        if (correctAnswer == null || correctAnswer.trim().isEmpty()) {
            return new AIGradingResponse(0, 0.0, "无参考答案，无法评分", new ArrayList<>());
        }

        String[] keywords = correctAnswer.split("[,，;；\\s]+");
        int matchCount = 0;

        for (String keyword : keywords) {
            if (keyword.trim().length() > 0 &&
                    studentAnswer.toLowerCase().contains(keyword.trim().toLowerCase())) {
                matchCount++;
            }
        }

        double ratio = keywords.length > 0 ? (double) matchCount / keywords.length : 0.0;
        int score = (int) (question.getScore() * ratio);

        List<String> keyPoints = new ArrayList<>();
        keyPoints.add("关键词匹配度: " + matchCount + "/" + keywords.length);
        keyPoints.add("使用备用评分算法");

        return new AIGradingResponse(score, ratio, "基于关键词匹配的备用评分", keyPoints);
    }

    private AIGradingResponse fallbackFillGrading(Question question, String studentAnswer) {
        String correctAnswer = question.getAnalysis();
        if (correctAnswer == null || correctAnswer.trim().isEmpty()) {
            return new AIGradingResponse(0, 0.0, "无标准答案", new ArrayList<>());
        }

        String[] possibleAnswers = correctAnswer.split("[;；|]");
        boolean isCorrect = false;

        for (String answer : possibleAnswers) {
            if (studentAnswer.trim().equalsIgnoreCase(answer.trim())) {
                isCorrect = true;
                break;
            }
        }

        int score = isCorrect ? question.getScore() : 0;
        double ratio = isCorrect ? 1.0 : 0.0;

        List<String> keyPoints = new ArrayList<>();
        keyPoints.add("使用备用评分算法");
        keyPoints.add(isCorrect ? "答案匹配" : "答案不匹配");

        return new AIGradingResponse(score, ratio,
                isCorrect ? "答案正确" : "答案不正确，标准答案：" + correctAnswer, keyPoints);
    }

    private AIGradingResponse fallbackProgrammingGrading(Question question, String studentAnswer) {
        List<String> keyPoints = new ArrayList<>();
        int score = 0;

        if (studentAnswer.contains("def ") || studentAnswer.contains("function ") ||
                studentAnswer.contains("public ") || studentAnswer.contains("class ")) {
            score += question.getScore() / 4;
            keyPoints.add("包含基本代码结构");
        }

        if (studentAnswer.contains("return") || studentAnswer.contains("print") ||
                studentAnswer.contains("System.out") || studentAnswer.contains("console.log")) {
            score += question.getScore() / 4;
            keyPoints.add("包含输出语句");
        }

        if (studentAnswer.contains("if") || studentAnswer.contains("for") ||
                studentAnswer.contains("while")) {
            score += question.getScore() / 4;
            keyPoints.add("包含控制结构");
        }

        keyPoints.add("使用备用评分算法");

        double ratio = (double) score / question.getScore();
        return new AIGradingResponse(score, ratio, "基础代码结构检查", keyPoints);
    }
}