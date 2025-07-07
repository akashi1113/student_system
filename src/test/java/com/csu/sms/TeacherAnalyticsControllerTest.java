package com.csu.sms;

import com.csu.sms.dto.analytics.ExamAnalysisRequestDTO;
import com.csu.sms.dto.analytics.ExamAnalysisResponseDTO;
import com.csu.sms.service.TeacherAnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TeacherAnalyticsControllerTest {

    @Autowired
    private TeacherAnalyticsService teacherAnalyticsService;

    @Test
    void testExamComparison() {
        // 创建测试请求
        ExamAnalysisRequestDTO request = new ExamAnalysisRequestDTO();
        request.setStartTime(LocalDateTime.now().minusDays(30));
        request.setEndTime(LocalDateTime.now());
        
        // 调用考试对比服务
        List<ExamAnalysisResponseDTO.ExamComparison> result = teacherAnalyticsService.getExamComparisons(request);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result instanceof List);
        
        // 如果有数据，验证数据结构
        if (!result.isEmpty()) {
            ExamAnalysisResponseDTO.ExamComparison firstExam = result.get(0);
            assertNotNull(firstExam.getExamName());
            assertNotNull(firstExam.getAverageScore());
            assertTrue(firstExam.getAverageScore() >= 0);
        }
        
        System.out.println("考试对比测试通过，返回数据条数: " + result.size());
        if (!result.isEmpty()) {
            System.out.println("第一个考试: " + result.get(0).getExamName() + ", 平均分: " + result.get(0).getAverageScore());
        }
    }
} 