package com.csu.sms.service;

import com.csu.sms.common.ApiResponse;
import com.csu.sms.dto.exam.*;
import com.csu.sms.model.question.AnswerRecord;
import com.csu.sms.model.question.Question;
import com.csu.sms.model.question.QuestionOption;
import com.csu.sms.persistence.QuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Arrays;

@Service
public class QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    public AIGradingService aiGradingService;

    // 根据考试ID获取所有题目（用于考试）
    // 不包含正确答案信息
    public List<QuestionResponseDTO> getQuestionsByExamId(Long examId) {
        List<Question> questions = questionMapper.findByExamId(examId);
        return questions.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    // 根据考试ID获取所有题目（用于管理）
    // 包含完整信息
    public List<Question> getQuestionsWithAnswersByExamId(Long examId) {
        List<Question> questions = questionMapper.findByExamId(examId);
        for (Question question : questions) {
            List<QuestionOption> options = questionMapper.findOptionsByQuestionId(question.getId());
            question.setOptions(options);
        }
        return questions;
    }

    // 根据题目ID获取题目详情
    public Question getQuestionById(Long id) {
        Question question = questionMapper.findById(id);
        if (question != null) {
            List<QuestionOption> options = questionMapper.findOptionsByQuestionId(id);
            question.setOptions(options);
        }
        return question;
    }

    // 创建题目
    @Transactional
    public Question createQuestion(QuestionCreateDTO questionDTO) {
        // 验证题目类型和选项
        validateQuestionAndOptions(questionDTO);

        // 创建题目
        Question question = convertToEntity(questionDTO);

        // 如果没有指定排序号，自动生成
        if (question.getOrderNum() == null) {
            int count = questionMapper.countByExamId(question.getExamId());
            question.setOrderNum(count + 1);
        }

        questionMapper.insert(question);

        // 创建选项
        if (questionDTO.getOptions() != null && !questionDTO.getOptions().isEmpty()) {
            List<QuestionOption> options = new ArrayList<>();
            for (int i = 0; i < questionDTO.getOptions().size(); i++) {
                QuestionOptionDTO optionDTO = questionDTO.getOptions().get(i);
                QuestionOption option = new QuestionOption();
                option.setQuestionId(question.getId());
                option.setContent(optionDTO.getContent());
                option.setIsCorrect(optionDTO.getIsCorrect());
                option.setOptionLabel(optionDTO.getOptionLabel());
                option.setOrderNum(i + 1);
                options.add(option);
            }
            questionMapper.batchInsertOptions(options);
            question.setOptions(options);
        }

        return question;
    }

    // 更新题目
    @Transactional
    public Question updateQuestion(Long id, QuestionCreateDTO questionDTO) {
        Question existingQuestion = questionMapper.findById(id);
        if (existingQuestion == null) {
            throw new RuntimeException("题目不存在");
        }

        // 验证题目类型和选项
        validateQuestionAndOptions(questionDTO);

        // 更新题目信息
        Question question = convertToEntity(questionDTO);
        question.setId(id);
        questionMapper.update(question);

        // 删除原有选项
        questionMapper.deleteOptionsByQuestionId(id);

        // 创建新选项
        if (questionDTO.getOptions() != null && !questionDTO.getOptions().isEmpty()) {
            List<QuestionOption> options = new ArrayList<>();
            for (int i = 0; i < questionDTO.getOptions().size(); i++) {
                QuestionOptionDTO optionDTO = questionDTO.getOptions().get(i);
                QuestionOption option = new QuestionOption();
                option.setQuestionId(id);
                option.setContent(optionDTO.getContent());
                option.setIsCorrect(optionDTO.getIsCorrect());
                option.setOptionLabel(optionDTO.getOptionLabel());
                option.setOrderNum(i + 1);
                options.add(option);
            }
            questionMapper.batchInsertOptions(options);
            question.setOptions(options);
        }

        return question;
    }

    // 删除题目
    @Transactional
    public void deleteQuestion(Long id) {
        // 删除选项
        questionMapper.deleteOptionsByQuestionId(id);
        // 删除题目
        questionMapper.deleteById(id);
    }

    // 批量删除考试的所有题目
    @Transactional
    public void deleteQuestionsByExamId(Long examId) {
        List<Question> questions = questionMapper.findByExamId(examId);
        for (Question question : questions) {
            questionMapper.deleteOptionsByQuestionId(question.getId());
        }
        questionMapper.deleteByExamId(examId);
    }

    // 评分单个题目
    public int scoreQuestion(Question question, String studentAnswer) {
        if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
            return 0;
        }

        switch (question.getType()) {
            case "SINGLE":
            case "JUDGE":
            case "MULTIPLE":
                return scoreObjectiveQuestion(question, studentAnswer);

            case "TEXT":
                ApiResponse<AIGradingResponse> textResult = aiGradingService.gradeTextAnswer(question, studentAnswer);
                return textResult.isSuccess() ? textResult.getData().getScore() : 0;

            case "FILL":
                ApiResponse<AIGradingResponse> fillResult = aiGradingService.gradeFillAnswer(question, studentAnswer);
                return fillResult.isSuccess() ? fillResult.getData().getScore() : 0;

            case "PROGRAMMING":
                ApiResponse<AIGradingResponse> progResult = aiGradingService.gradeProgrammingAnswer(question, studentAnswer);
                return progResult.isSuccess() ? progResult.getData().getScore() : 0;

            default:
                return 0;
        }
    }

    // 批量评分
    @Transactional
    public ApiResponse<Void> scoreAnswersWithAI(Long examRecordId, List<AnswerDTO> answers) {
        try {
            List<AnswerRecord> answerRecords = new ArrayList<>();

            for (AnswerDTO answerDTO : answers) {
                Question question = questionMapper.findById(answerDTO.getQuestionId());
                if (question == null) continue;

                AnswerRecord record = new AnswerRecord();
                record.setExamRecordId(examRecordId);
                record.setQuestionId(answerDTO.getQuestionId());
                record.setAnswer(answerDTO.getAnswer());

                if (isSubjectiveQuestion(question.getType())) {
                    // 主观题使用AI评分
                    ApiResponse<AIGradingResponse> aiResult = gradeSubjectiveQuestion(question, answerDTO.getAnswer());
                    if (aiResult.isSuccess()) {
                        AIGradingResponse gradingResult = aiResult.getData();
                        record.setScore(gradingResult.getScore());
                        record.setIsCorrect(gradingResult.getScore() > 0);
                        record.setAiFeedback(gradingResult.getFeedback());
                        record.setAiScoreRatio(gradingResult.getScoreRatio());
                        record.setGradingMethod("AI");
                    } else {
                        record.setScore(0);
                        record.setIsCorrect(false);
                        record.setAiFeedback("AI评分失败: " + aiResult.getMessage());
                        record.setGradingMethod("AUTO");
                    }
                    record.setCorrectAnswer(question.getAnalysis());
                } else {
                    // 客观题使用传统评分
                    String correctAnswer = questionMapper.getCorrectAnswersByQuestionId(question.getId());
                    int score = scoreObjectiveQuestion(question, answerDTO.getAnswer());
                    record.setScore(score);
                    record.setIsCorrect(score > 0);
                    record.setCorrectAnswer(correctAnswer);
                    record.setGradingMethod("AUTO");
                }

                answerRecords.add(record);
            }

            if (!answerRecords.isEmpty()) {
                questionMapper.batchInsertAnswerRecords(answerRecords);
            }

            return ApiResponse.success("评分完成",null);

        } catch (Exception e) {
            return ApiResponse.error("评分过程中发生错误: " + e.getMessage());
        }
    }

    // 客观题评分
    private int scoreObjectiveQuestion(Question question, String studentAnswer) {
        String correctAnswer = questionMapper.getCorrectAnswersByQuestionId(question.getId());
        if (correctAnswer == null) {
            return 0;
        }

        boolean isCorrect = false;

        switch (question.getType()) {
            case "SINGLE":
            case "JUDGE":
                isCorrect = studentAnswer.equals(correctAnswer);
                break;

            case "MULTIPLE":
                List<String> studentOptions = Arrays.asList(studentAnswer.split(","));
                List<String> correctOptions = Arrays.asList(correctAnswer.split(","));
                studentOptions.sort(String::compareTo);
                correctOptions.sort(String::compareTo);
                isCorrect = studentOptions.equals(correctOptions);
                break;
        }

        return isCorrect ? question.getScore() : 0;
    }

    // 判断是否为主观题
    private boolean isSubjectiveQuestion(String type) {
        return "TEXT".equals(type) || "FILL".equals(type) || "PROGRAMMING".equals(type);
    }

    // 主观题AI评分
    private ApiResponse<AIGradingResponse> gradeSubjectiveQuestion(Question question, String studentAnswer) {
        switch (question.getType()) {
            case "TEXT":
                return aiGradingService.gradeTextAnswer(question, studentAnswer);
            case "FILL":
                return aiGradingService.gradeFillAnswer(question, studentAnswer);
            case "PROGRAMMING":
                return aiGradingService.gradeProgrammingAnswer(question, studentAnswer);
            default:
                return ApiResponse.error("不支持的题目类型");
        }
    }

    // 获取答题分析结果
    public List<QuestionAnalysisDTO> getQuestionAnalysis(Long examRecordId) {
        List<AnswerRecord> answerRecords = questionMapper.findAnswerRecordsByExamRecordId(examRecordId);
        List<QuestionAnalysisDTO> analysisList = new ArrayList<>();

        for (AnswerRecord record : answerRecords) {
            Question question = questionMapper.findById(record.getQuestionId());
            if (question == null) continue;

            QuestionAnalysisDTO analysis = new QuestionAnalysisDTO();
            analysis.setQuestionId(question.getId());
            analysis.setContent(question.getContent());
            analysis.setType(question.getType());
            analysis.setScore(question.getScore());
            analysis.setEarnedScore(record.getScore());
            analysis.setStudentAnswer(formatStudentAnswer(record.getAnswer(), question));
            analysis.setCorrectAnswer(formatCorrectAnswer(record.getCorrectAnswer(), question));
            analysis.setAnalysis(question.getAnalysis());
            analysis.setIsCorrect(record.getIsCorrect());

            analysisList.add(analysis);
        }

        return analysisList;
    }

    // 获取考试总分
    public Integer getTotalScore(Long examId) {
        return questionMapper.getTotalScoreByExamId(examId);
    }

    // ========== 私有辅助方法 ==========

    // 验证题目和选项的有效性
    private void validateQuestionAndOptions(QuestionCreateDTO questionDTO) {
        String type = questionDTO.getType();
        List<QuestionOptionDTO> options = questionDTO.getOptions();

        switch (type) {
            case "SINGLE":
                if (options == null || options.size() < 2) {
                    throw new RuntimeException("单选题至少需要2个选项");
                }
                long correctCount = options.stream().mapToLong(o -> o.getIsCorrect() ? 1 : 0).sum();
                if (correctCount != 1) {
                    throw new RuntimeException("单选题必须有且只有一个正确答案");
                }
                break;

            case "MULTIPLE":
                if (options == null || options.size() < 2) {
                    throw new RuntimeException("多选题至少需要2个选项");
                }
                long multiCorrectCount = options.stream().mapToLong(o -> o.getIsCorrect() ? 1 : 0).sum();
                if (multiCorrectCount < 2) {
                    throw new RuntimeException("多选题至少需要2个正确答案");
                }
                break;

            case "JUDGE":
                if (options == null || options.size() != 2) {
                    throw new RuntimeException("判断题必须有2个选项");
                }
                long judgeCorrectCount = options.stream().mapToLong(o -> o.getIsCorrect() ? 1 : 0).sum();
                if (judgeCorrectCount != 1) {
                    throw new RuntimeException("判断题必须有且只有一个正确答案");
                }
                break;

            case "TEXT":
                break;
            case "FILL":
                break;
            case "PROGRAMMING":
                break;

            default:
                throw new RuntimeException("不支持的题目类型: " + type);
        }
    }

    // 转换 DTO 到实体
    private Question convertToEntity(QuestionCreateDTO dto) {
        Question question = new Question();
        question.setExamId(dto.getExamId());
        question.setContent(dto.getContent());
        question.setType(dto.getType());
        question.setScore(dto.getScore());
        question.setOrderNum(dto.getOrderNum());
        question.setAnalysis(dto.getAnalysis());
        question.setDifficulty(dto.getDifficulty());
        return question;
    }

    // 转换实体到响应 DTO（不包含正确答案）
    private QuestionResponseDTO convertToResponseDTO(Question question) {
        QuestionResponseDTO dto = new QuestionResponseDTO();
        dto.setId(question.getId());
        dto.setContent(question.getContent());
        dto.setType(question.getType());
        dto.setScore(question.getScore());
        dto.setOrderNum(question.getOrderNum());
        dto.setDifficulty(question.getDifficulty());

        // 获取选项但不包含正确答案信息
        List<QuestionOption> options = questionMapper.findOptionsByQuestionId(question.getId());
        List<QuestionOptionResponseDTO> optionDTOs = options.stream()
                .map(option -> new QuestionOptionResponseDTO(option.getId(), option.getContent(), option.getOptionLabel()))
                .collect(Collectors.toList());
        dto.setOptions(optionDTOs);

        return dto;
    }

    // 评估简答题答案
    private boolean evaluateTextAnswer(String studentAnswer, String correctAnswer) {
        // 简化的文本匹配逻辑，实际项目中可能需要更复杂的NLP处理
        if (correctAnswer == null || correctAnswer.trim().isEmpty()) {
            return false;
        }

        String[] keywords = correctAnswer.split("[,，;；]");
        int matchCount = 0;

        for (String keyword : keywords) {
            if (studentAnswer.toLowerCase().contains(keyword.trim().toLowerCase())) {
                matchCount++;
            }
        }

        // 如果匹配了一半以上的关键词，认为正确
        return matchCount >= keywords.length / 2.0;
    }

    // 格式化学生答案用于显示
    private String formatStudentAnswer(String answer, Question question) {
        if (answer == null) return "未作答";

        switch (question.getType()) {
            case "SINGLE":
            case "MULTIPLE":
                // 将选项ID转换为选项内容
                return formatOptionAnswer(answer, question.getId());
            case "JUDGE":
                return "true".equals(answer) ? "正确" : "错误";
            case "TEXT":
                return answer;
            default:
                return answer;
        }
    }

    // 格式化正确答案用于显示
    private String formatCorrectAnswer(String answer, Question question) {
        if (answer == null) return "";

        switch (question.getType()) {
            case "SINGLE":
            case "MULTIPLE":
                return formatOptionAnswer(answer, question.getId());
            case "JUDGE":
                return "true".equals(answer) ? "正确" : "错误";
            case "TEXT":
                return answer;
            case "FILL":
                return answer;
            case "PROGRAMMING":
                return null;
            default:
                return answer;
        }
    }

    // 将选项ID转换为选项内容
    private String formatOptionAnswer(String optionIds, Long questionId) {
        if (optionIds == null || optionIds.trim().isEmpty()) {
            return "未选择";
        }

        List<QuestionOption> options = questionMapper.findOptionsByQuestionId(questionId);
        List<String> selectedOptions = new ArrayList<>();

        String[] ids = optionIds.split(",");
        for (String id : ids) {
            for (QuestionOption option : options) {
                if (option.getId().toString().equals(id.trim())) {
                    selectedOptions.add(option.getOptionLabel() + ". " + option.getContent());
                    break;
                }
            }
        }

        return String.join("; ", selectedOptions);
    }
}