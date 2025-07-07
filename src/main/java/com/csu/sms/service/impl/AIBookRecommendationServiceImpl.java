package com.csu.sms.service.impl;

import com.csu.sms.model.AIBookRecommendation;
import com.csu.sms.model.course.Course;
import com.csu.sms.model.exam.ExamRecord;
import com.csu.sms.persistence.AIBookRecommendationMapper;
import com.csu.sms.persistence.CourseDao;
import com.csu.sms.persistence.ExamMapper;
import com.csu.sms.service.AIBookRecommendationService;
import com.csu.sms.service.AIService;
import com.csu.sms.vo.AIBookRecommendationVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIBookRecommendationServiceImpl implements AIBookRecommendationService {

    private final AIBookRecommendationMapper recommendationMapper;
    private final ExamMapper examMapper;
    private final CourseDao courseDao;
    private final AIService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<AIBookRecommendationVO> generateRecommendationsForStudent(Long userId) {
        log.info("开始为用户{}生成书籍推荐", userId);

        List<AIBookRecommendationVO> recommendations = new ArrayList<>();

        // 1. 查询用户的所有考试记录
        List<ExamRecord> allExams = examMapper.findExamRecordsByUserId(userId);
        
        if (allExams == null || allExams.isEmpty()) {
            log.info("用户{}没有任何考试记录，无法生成推荐", userId);
            return new ArrayList<>();
        }

        log.info("用户{}有{}个考试记录，开始生成综合推荐", userId, allExams.size());

        // 2. 查询学生成绩较低的考试记录（低于70%）
        List<ExamRecord> lowScoreExams = examMapper.findLowScoreExamsByUserId(userId, 0.7);
        
        // 3. 生成综合推荐：结合低分课程推荐和智能推荐
        if (lowScoreExams != null && !lowScoreExams.isEmpty()) {
            log.info("用户{}有{}个低分考试记录，生成低分课程推荐", userId, lowScoreExams.size());
            // 所有低分课程一共推荐3本书
            recommendations.addAll(generateRecommendationsFromExamsWithLimit(lowScoreExams, userId, "低分课程", 3));
        }
        
        // 4. 从所有考试记录中选择一些进行智能推荐（避免与低分考试重复）
        List<ExamRecord> smartRecommendationExams = new ArrayList<>();
        if (lowScoreExams != null && !lowScoreExams.isEmpty()) {
            // 如果有低分考试，选择非低分考试进行智能推荐
            Set<Long> lowScoreExamIds = lowScoreExams.stream()
                    .map(ExamRecord::getExamId)
                    .collect(Collectors.toSet());
            
            smartRecommendationExams = allExams.stream()
                    .filter(exam -> !lowScoreExamIds.contains(exam.getExamId()))
                    .limit(3) // 限制智能推荐数量，避免推荐过多
                    .collect(Collectors.toList());
            } else {
            // 如果没有低分考试，直接使用所有考试记录进行智能推荐
            smartRecommendationExams = allExams.stream()
                    .limit(5) // 限制推荐数量
                    .collect(Collectors.toList());
        }
        
        if (!smartRecommendationExams.isEmpty()) {
            log.info("用户{}有{}个考试记录用于智能推荐", userId, smartRecommendationExams.size());
            // 智能推荐最多2本书
            recommendations.addAll(generateRecommendationsFromExamsWithLimit(smartRecommendationExams, userId, "智能推荐", 2));
        }

        return recommendations;
    }

    /**
     * 根据考试记录生成书籍推荐
     * @param examRecords 考试记录列表
     * @param userId 用户ID
     * @param recommendationType 推荐类型（"低分课程"或"智能推荐"）
     * @return 推荐列表
     */
    private List<AIBookRecommendationVO> generateRecommendationsFromExams(List<ExamRecord> examRecords, Long userId, String recommendationType) {
        List<AIBookRecommendationVO> recommendations = new ArrayList<>();
        
        // 按考试分组，避免重复推荐同一考试
        Map<Long, ExamRecord> examMap = new HashMap<>();
        
        for (ExamRecord examRecord : examRecords) {
            try {
                // 获取考试信息
                com.csu.sms.model.exam.Exam exam = examMapper.findById(examRecord.getExamId());
                if (exam == null) {
                    log.warn("找不到考试{}", examRecord.getExamId());
                    continue;
                }

                // 如果该考试已经有推荐，跳过（避免重复）
                if (examMap.containsKey(exam.getId())) {
                    continue;
                }

                examMap.put(exam.getId(), examRecord);

                // 计算百分比成绩
                double scorePercentage = 0;
                if (exam.getTotalScore() > 0) {
                    scorePercentage = (double) examRecord.getScore() / exam.getTotalScore() * 100;
                }

                // 根据推荐类型调整AI提示词
                String aiResponse;
                if ("低分课程".equals(recommendationType)) {
                    aiResponse = aiService.generateBookRecommendations(exam.getTitle(), scorePercentage);
                } else {
                    // 智能推荐：基于考试和成绩，但更注重提升和拓展
                    aiResponse = aiService.generateSmartBookRecommendations(exam.getTitle(), scorePercentage);
                }

                // 解析AI响应
                List<Map<String, String>> bookList = parseAIResponse(aiResponse);

                // 保存并构建推荐VO
                for (Map<String, String> book : bookList) {
                    AIBookRecommendation recommendation = new AIBookRecommendation();
                    recommendation.setUserId(userId);
                    recommendation.setCourseId(exam.getCourseId()); // 可能为null，但不影响推荐
                    recommendation.setCourseName(exam.getTitle()); // 使用考试名称作为课程名称
                    recommendation.setScore(scorePercentage);
                    recommendation.setBookTitle(book.get("title"));
                    recommendation.setBookAuthor(book.get("author"));
                    recommendation.setDoubanUrl(null); // 不再设置豆瓣链接
                    recommendation.setRecommendationReason(book.get("reason"));
                    recommendation.setIsRead(0);
                    recommendation.setCreateTime(LocalDateTime.now());

                    // 保存到数据库
                    recommendationMapper.insert(recommendation);

                    // 添加到返回列表
                    AIBookRecommendationVO vo = new AIBookRecommendationVO();
                    BeanUtils.copyProperties(recommendation, vo);
                    recommendations.add(vo);
                }
            } catch (Exception e) {
                log.error("为考试记录{}生成书籍推荐失败", examRecord.getId(), e);
            }
        }

        return recommendations;
    }

    /**
     * 根据考试记录生成书籍推荐（限制总推荐数量）
     * @param examRecords 考试记录列表
     * @param userId 用户ID
     * @param recommendationType 推荐类型（"低分课程"或"智能推荐"）
     * @param maxRecommendations 最大推荐数量
     * @return 推荐列表
     */
    private List<AIBookRecommendationVO> generateRecommendationsFromExamsWithLimit(List<ExamRecord> examRecords, Long userId, String recommendationType, int maxRecommendations) {
        List<AIBookRecommendationVO> allRecommendations = new ArrayList<>();
        
        // 按考试分组，避免重复推荐同一考试
        Map<Long, ExamRecord> examMap = new HashMap<>();
        
        for (ExamRecord examRecord : examRecords) {
            try {
                // 如果已经达到最大推荐数量，停止生成
                if (allRecommendations.size() >= maxRecommendations) {
                    break;
                }

                // 获取考试信息
                com.csu.sms.model.exam.Exam exam = examMapper.findById(examRecord.getExamId());
                if (exam == null) {
                    log.warn("找不到考试{}", examRecord.getExamId());
                    continue;
                }

                // 如果该考试已经有推荐，跳过（避免重复）
                if (examMap.containsKey(exam.getId())) {
                    continue;
                }

                examMap.put(exam.getId(), examRecord);

                // 计算百分比成绩
                double scorePercentage = 0;
                if (exam.getTotalScore() > 0) {
                    scorePercentage = (double) examRecord.getScore() / exam.getTotalScore() * 100;
                }

                // 根据推荐类型调整AI提示词
                String aiResponse;
                if ("低分课程".equals(recommendationType)) {
                    aiResponse = aiService.generateBookRecommendations(exam.getTitle(), scorePercentage);
                } else {
                    // 智能推荐：基于考试和成绩，但更注重提升和拓展
                    aiResponse = aiService.generateSmartBookRecommendations(exam.getTitle(), scorePercentage);
                }

                // 解析AI响应
                List<Map<String, String>> bookList = parseAIResponse(aiResponse);

                // 保存并构建推荐VO（限制每个考试只推荐一本书）
                if (!bookList.isEmpty()) {
                    Map<String, String> book = bookList.get(0); // 只取第一本书
                    
                    AIBookRecommendation recommendation = new AIBookRecommendation();
                    recommendation.setUserId(userId);
                    recommendation.setCourseId(exam.getCourseId()); // 可能为null，但不影响推荐
                    recommendation.setCourseName(exam.getTitle()); // 使用考试名称作为课程名称
                    recommendation.setScore(scorePercentage);
                    recommendation.setBookTitle(book.get("title"));
                    recommendation.setBookAuthor(book.get("author"));
                    recommendation.setDoubanUrl(null); // 不再设置豆瓣链接
                    recommendation.setRecommendationReason(book.get("reason"));
                    recommendation.setIsRead(0);
                    recommendation.setCreateTime(LocalDateTime.now());

                    // 保存到数据库
                    recommendationMapper.insert(recommendation);

                    // 添加到返回列表
                    AIBookRecommendationVO vo = new AIBookRecommendationVO();
                    BeanUtils.copyProperties(recommendation, vo);
                    allRecommendations.add(vo);
                }
            } catch (Exception e) {
                log.error("为考试记录{}生成书籍推荐失败", examRecord.getId(), e);
            }
        }

        return allRecommendations;
    }

    @Override
    public List<AIBookRecommendationVO> getRecommendationsForStudent(Long userId) {
        List<AIBookRecommendation> recommendations = recommendationMapper.findByUserId(userId);
        return recommendations.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AIBookRecommendationVO> getRecommendationsByCourse(Long courseId, Long userId) {
        List<AIBookRecommendation> recommendations = recommendationMapper.findByCourseIdAndUserId(courseId, userId);
        return recommendations.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean markAsRead(Long recommendationId, Long userId) {
        return recommendationMapper.updateIsRead(recommendationId, userId) > 0;
    }

    @Override
    public boolean deleteRecommendation(Long recommendationId, Long userId) {
        log.info("用户{}请求删除推荐记录{}", userId, recommendationId);
        
        // 先检查推荐记录是否存在且属于该用户
        AIBookRecommendation recommendation = recommendationMapper.findById(recommendationId);
        if (recommendation == null) {
            log.warn("推荐记录{}不存在", recommendationId);
            return false;
        }
        
        if (!recommendation.getUserId().equals(userId)) {
            log.warn("用户{}尝试删除不属于自己的推荐记录{}", userId, recommendationId);
            return false;
        }
        
        // 执行删除操作
        int result = recommendationMapper.deleteById(recommendationId, userId);
        if (result > 0) {
            log.info("用户{}成功删除推荐记录{}", userId, recommendationId);
            return true;
        } else {
            log.warn("用户{}删除推荐记录{}失败", userId, recommendationId);
            return false;
        }
    }

    private AIBookRecommendationVO convertToVO(AIBookRecommendation recommendation) {
        AIBookRecommendationVO vo = new AIBookRecommendationVO();
        BeanUtils.copyProperties(recommendation, vo);
        return vo;
    }

    private List<Map<String, String>> parseAIResponse(String aiResponse) {
        try {
            return objectMapper.readValue(aiResponse, new TypeReference<List<Map<String, String>>>(){});
        } catch (JsonProcessingException e) {
            log.error("解析AI响应失败: {}", aiResponse, e);

            // 返回一个默认值，避免空指针
            List<Map<String, String>> fallback = new ArrayList<>();
            Map<String, String> defaultBook = new HashMap<>();
            defaultBook.put("title", "如何高效学习");
            defaultBook.put("author", "斯科特·扬");
            defaultBook.put("douban_url", "https://book.douban.com/subject/25783654/");
            defaultBook.put("reason", "提供实用学习技巧，帮助提高学习效率。");
            fallback.add(defaultBook);

            return fallback;
        }
    }
}