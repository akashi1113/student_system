package com.csu.sms.service;

import com.csu.sms.model.chat.ChatMessage;
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
import java.util.List;

@Service
public class ChatAIService {

    private static final Logger logger = LoggerFactory.getLogger(ChatAIService.class);

    @Autowired
    private DeepSeekChatModel chatModel;

    @Value("${ai.chat.enabled:true}")
    private boolean aiChatEnabled;

    @Value("${ai.chat.retry-count:3}")
    private int retryCount;

    @Value("${ai.chat.max-history:10}")
    private int maxHistoryCount;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 生成AI回复
     */
    public String generateResponse(String userMessage, List<ChatMessage> conversationHistory) {
        try {
            if (!aiChatEnabled) {
                return "AI聊天功能未启用";
            }

            if (userMessage == null || userMessage.trim().isEmpty()) {
                return "请输入有效的问题";
            }

            String response = callAIServiceWithRetry(userMessage, conversationHistory);
            return response;

        } catch (Exception e) {
            logger.error("生成AI回复失败", e);
            return "抱歉，我现在无法回答您的问题，请稍后再试。";
        }
    }

    /**
     * 生成对话标题
     */
    public String generateTitle(String firstMessage) {
        try {
            if (!aiChatEnabled) {
                return "新对话";
            }

            String prompt = buildTitlePrompt(firstMessage);
            String title = callAIServiceWithRetry(prompt, new ArrayList<>());

            // 清理标题，移除引号和多余字符
            title = title.replaceAll("^\"|\"$", "").trim();
            if (title.length() > 30) {
                title = title.substring(0, 30) + "...";
            }

            return title.isEmpty() ? "新对话" : title;

        } catch (Exception e) {
            logger.error("生成对话标题失败", e);
            return "新对话";
        }
    }

    /**
     * 带重试的AI服务调用
     */
    private String callAIServiceWithRetry(String userMessage, List<ChatMessage> history) {
        Exception lastException = null;

        for (int i = 0; i < retryCount; i++) {
            try {
                return callAIService(userMessage, history);
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

    /**
     * 调用AI服务
     */
    private String callAIService(String userMessage, List<ChatMessage> history) {
        try {
            // 系统提示词
            SystemMessage systemMessage = new SystemMessage(buildSystemPrompt());

            // 构建消息列表
            List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();
            messages.add(systemMessage);

            // 添加历史对话（限制数量）
            List<ChatMessage> limitedHistory = history.size() > maxHistoryCount ?
                    history.subList(history.size() - maxHistoryCount, history.size()) : history;

            for (ChatMessage msg : limitedHistory) {
                if ("USER".equals(msg.getMessageType())) {
                    messages.add(new UserMessage(msg.getContent()));
                } else {
                    messages.add(new org.springframework.ai.chat.messages.AssistantMessage(msg.getContent()));
                }
            }

            // 添加当前用户消息
            messages.add(new UserMessage(userMessage));

            // 创建提示
            Prompt prompt = new Prompt(messages);

            logger.debug("调用DeepSeek AI服务");

            // 调用AI模型
            ChatResponse response = chatModel.call(prompt);

            if (response == null || response.getResult() == null ||
                    response.getResult().getOutput() == null) {
                throw new RuntimeException("AI服务返回空响应");
            }

            String content = String.valueOf(response.getResult().getOutput());
            logger.debug("AI服务响应内容: {}", content);

            return content.trim();

        } catch (Exception e) {
            logger.error("AI服务调用异常", e);
            throw new RuntimeException("AI服务调用异常: " + e.getMessage(), e);
        }
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt() {
        return "你是一个专业的学习助手，名叫「智学助手」。你的主要职责是帮助学生解决学习问题，提供准确、详细的学习指导。\n\n" +
                "请遵循以下原则：\n" +
                "1. 对学习问题给出准确、详细的解答\n" +
                "2. 解释概念时要通俗易懂，适合学生理解\n" +
                "3. 提供具体的学习建议和方法\n" +
                "4. 鼓励学生独立思考，引导而非直接给出答案\n" +
                "5. 保持友好、耐心的语气\n" +
                "6. 如果不确定答案，请诚实说明\n" +
                "7. 可以适当使用例子来说明概念\n\n" +
                "你可以回答各种学科的问题，包括但不限于：数学、物理、化学、生物、历史、语文、英语、计算机科学等。";
    }

    /**
     * 构建标题生成提示词
     */
    private String buildTitlePrompt(String firstMessage) {
        return "请为以下对话生成一个简洁的标题（不超过15个字）：\n\n" +
                "用户问题：" + firstMessage + "\n\n" +
                "要求：\n" +
                "1. 标题要简洁明了，体现问题的核心内容\n" +
                "2. 不要包含多余的标点符号或引号\n" +
                "3. 直接返回标题，不要其他解释\n" +
                "4. 如果是学科问题，可以包含学科名称";
    }
}