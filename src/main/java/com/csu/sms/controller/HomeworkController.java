package com.csu.sms.controller;

import com.csu.sms.dto.homework.HomeworkCreateRequest;
import com.csu.sms.dto.homework.HomeworkGradeRequest;
import com.csu.sms.dto.homework.HomeworkSubmitRequest;
import com.csu.sms.dto.homework.HomeworkUpdateRequest;
import com.csu.sms.model.homework.Homework;
import com.csu.sms.model.homework.HomeworkSubmission;
import com.csu.sms.model.homework.HomeworkAnswer;
import com.csu.sms.service.HomeworkService;
import com.csu.sms.annotation.LogOperation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/homework")
@CrossOrigin(origins = "http://localhost:5173")
public class HomeworkController {

    @Autowired
    private HomeworkService homeworkService;

    // ================ 作业管理接口 ================

    /**
     * 创建作业（基于课程）
     */
    @PostMapping
    @LogOperation(module = "作业管理", operation = "创建作业", description = "教师创建作业")
    public ResponseEntity<Map<String, Object>> createHomework(
            @Valid @RequestBody HomeworkCreateRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long homeworkId = homeworkService.createHomework(request.getHomework(), request.getQuestions());
            response.put("success", true);
            response.put("message", "作业创建成功");
            response.put("data", homeworkId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "作业创建失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新作业
     */
    @PutMapping("/{id}")
    @LogOperation(module = "作业管理", operation = "修改作业", description = "教师修改作业")
    public ResponseEntity<Map<String, Object>> updateHomework(
            @PathVariable Long id,
            @Valid @RequestBody HomeworkUpdateRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            request.getHomework().setId(id);
            homeworkService.updateHomework(request.getHomework(), request.getQuestions());
            response.put("success", true);
            response.put("message", "作业更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "作业更新失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除作业
     */
    @DeleteMapping("/{id}")
    @LogOperation(module = "作业管理", operation = "删除作业", description = "教师删除作业")
    public ResponseEntity<Map<String, Object>> deleteHomework(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            homeworkService.deleteHomework(id);
            response.put("success", true);
            response.put("message", "作业删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "作业删除失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取作业详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getHomework(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Homework homework = homeworkService.getHomeworkById(id);
            if (homework != null) {
                response.put("success", true);
                response.put("data", homework);
            } else {
                response.put("success", false);
                response.put("message", "作业不存在");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取作业失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取教师发布的作业列表
     */
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<Map<String, Object>> getHomeworkByTeacher(@PathVariable Long teacherId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Homework> homeworkList = homeworkService.getHomeworkByTeacher(teacherId);
            response.put("success", true);
            response.put("data", homeworkList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取作业列表失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取课程下的所有作业
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<Map<String, Object>> getHomeworkByCourse(@PathVariable Long courseId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Homework> homeworkList = homeworkService.getHomeworkByCourse(courseId);
            response.put("success", true);
            response.put("data", homeworkList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取课程作业失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取学生可用的作业列表（未截止的）
     */
    @GetMapping("/student/{studentId}/available")
    public ResponseEntity<Map<String, Object>> getAvailableHomework(@PathVariable Long studentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Homework> homeworkList = homeworkService.getAvailableHomeworkByStudent(studentId);
            response.put("success", true);
            response.put("data", homeworkList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取可用作业失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取学生的所有作业列表
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getHomeworkByStudent(@PathVariable Long studentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Homework> homeworkList = homeworkService.getHomeworkByStudent(studentId);
            response.put("success", true);
            response.put("data", homeworkList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取学生作业失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 发布作业
     */
    @PutMapping("/{id}/publish")
    @LogOperation(module = "作业管理", operation = "发布作业", description = "教师发布作业")
    public ResponseEntity<Map<String, Object>> publishHomework(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            homeworkService.publishHomework(id);
            response.put("success", true);
            response.put("message", "作业发布成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "作业发布失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 关闭作业
     */
    @PutMapping("/{id}/close")
    @LogOperation(module = "作业管理", operation = "关闭作业", description = "教师关闭作业")
    public ResponseEntity<Map<String, Object>> closeHomework(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            homeworkService.closeHomework(id);
            response.put("success", true);
            response.put("message", "作业关闭成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "作业关闭失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ================ 课程相关接口 ================

    /**
     * 获取课程内的学生列表
     */
    @GetMapping("/course/{courseId}/students")
    public ResponseEntity<Map<String, Object>> getStudentsByCourse(@PathVariable Long courseId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Long> studentIds = homeworkService.getStudentsByCourse(courseId);
            response.put("success", true);
            response.put("data", studentIds);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取课程学生失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取教师的课程列表
     */
    @GetMapping("/teacher/{teacherId}/courses")
    public ResponseEntity<Map<String, Object>> getCoursesByTeacher(@PathVariable Long teacherId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> courses = homeworkService.getCoursesByTeacher(teacherId);
            response.put("success", true);
            response.put("data", courses);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取教师课程失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取学生的课程列表
     */
    @GetMapping("/student/{studentId}/courses")
    public ResponseEntity<Map<String, Object>> getCoursesByStudent(@PathVariable Long studentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> courses = homeworkService.getCoursesByStudent(studentId);
            response.put("success", true);
            response.put("data", courses);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取学生课程失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ================ 作业提交接口 ================

    /**
     * 提交作业
     */
    @PostMapping("/{homeworkId}/submit")
    @LogOperation(module = "作业管理", operation = "提交作业", description = "学生提交作业")
    public ResponseEntity<Map<String, Object>> submitHomework(
            @PathVariable Long homeworkId,
            @RequestBody HomeworkSubmitRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long submissionId = homeworkService.submitHomework(homeworkId, request.getStudentId(), request.getAnswers());
            response.put("success", true);
            response.put("message", "作业提交成功");
            response.put("data", submissionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "作业提交失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取学生作业提交情况
     */
    @GetMapping("/{homeworkId}/submission/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentSubmissions(
            @PathVariable Long homeworkId,
            @PathVariable Long studentId) {
        List<HomeworkSubmission> submissions = homeworkService.getStudentSubmissions(homeworkId, studentId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", submissions);

        return ResponseEntity.ok(result);
    }

    /**
     * 获取单个提交记录详情
     */
    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<Map<String, Object>> getSubmissionById(@PathVariable Long submissionId) {
        Map<String, Object> response = new HashMap<>();
        try {
            HomeworkSubmission submission = homeworkService.getSubmissionById(submissionId);
            response.put("success", true);
            response.put("data", submission);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取提交记录失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取作业的所有提交记录
     */
    @GetMapping("/{homeworkId}/submissions")
    public ResponseEntity<Map<String, Object>> getHomeworkSubmissions(@PathVariable Long homeworkId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<HomeworkSubmission> submissions = homeworkService.getHomeworkSubmissions(homeworkId);
            response.put("success", true);
            response.put("data", submissions);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取提交记录失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取学生的所有作业提交记录
     */
    @GetMapping("/student/{studentId}/submissions")
    public ResponseEntity<Map<String, Object>> getStudentSubmissions(@PathVariable Long studentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<HomeworkSubmission> submissions = homeworkService.getStudentSubmissions(studentId);
            response.put("success", true);
            response.put("data", submissions);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取提交记录失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取课程的所有作业提交记录
     */
    @GetMapping("/course/{courseId}/submissions")
    public ResponseEntity<Map<String, Object>> getCourseSubmissions(@PathVariable Long courseId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<HomeworkSubmission> submissions = homeworkService.getCourseSubmissions(courseId);
            response.put("success", true);
            response.put("data", submissions);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取课程提交记录失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 检查是否可以重新提交
     */
    @GetMapping("/{homeworkId}/student/{studentId}/can-resubmit")
    public ResponseEntity<Map<String, Object>> canResubmit(
            @PathVariable Long homeworkId,
            @PathVariable Long studentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean canResubmit = homeworkService.canResubmit(homeworkId, studentId);
            response.put("success", true);
            response.put("data", canResubmit);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "检查失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ================ 作业批改接口 ================

    /**
     * 批改作业
     */
    @PutMapping("/submission/{submissionId}/grade")
    @LogOperation(module = "作业管理", operation = "批改作业", description = "教师批改作业")
    public ResponseEntity<Map<String, Object>> gradeHomework(
            @PathVariable Long submissionId,
            @RequestBody HomeworkGradeRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            homeworkService.gradeHomework(submissionId, request.getAnswers(),
                    request.getFeedback(), request.getTeacherId());
            response.put("success", true);
            response.put("message", "作业批改成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "作业批改失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取提交的答案详情
     */
    @GetMapping("/submission/{submissionId}/answers")
    public ResponseEntity<Map<String, Object>> getSubmissionAnswers(@PathVariable Long submissionId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<HomeworkAnswer> answers = homeworkService.getSubmissionAnswers(submissionId);
            response.put("success", true);
            response.put("data", answers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取答案失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 批量返回已批改作业
     */
    @PutMapping("/submissions/return")
    public ResponseEntity<Map<String, Object>> returnGradedHomework(@RequestBody List<Long> submissionIds) {
        Map<String, Object> response = new HashMap<>();
        try {
            homeworkService.returnGradedHomework(submissionIds);
            response.put("success", true);
            response.put("message", "作业返回成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "作业返回失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ================ 统计分析接口 ================

    /**
     * 获取作业统计信息
     */
    @GetMapping("/{homeworkId}/statistics")
    public ResponseEntity<Map<String, Object>> getHomeworkStatistics(@PathVariable Long homeworkId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> statistics = homeworkService.getHomeworkStatistics(homeworkId);
            response.put("success", true);
            response.put("data", statistics);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取统计信息失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取学生作业进度
     */
    @GetMapping("/student/{studentId}/progress")
    public ResponseEntity<Map<String, Object>> getStudentProgress(@PathVariable Long studentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> progress = homeworkService.getStudentProgress(studentId);
            response.put("success", true);
            response.put("data", progress);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取学习进度失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取课程作业统计
     */
    @GetMapping("/course/{courseId}/statistics")
    public ResponseEntity<Map<String, Object>> getCourseHomeworkStatistics(@PathVariable Long courseId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> statistics = homeworkService.getCourseHomeworkStatistics(courseId);
            response.put("success", true);
            response.put("data", statistics);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取课程统计失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 检查作业是否已截止
     */
    @GetMapping("/{id}/expired")
    public ResponseEntity<Map<String, Object>> checkHomeworkExpired(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean expired = homeworkService.isHomeworkExpired(id);
            response.put("success", true);
            response.put("data", expired);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "检查失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 重新计算提交记录得分
     */
    @PutMapping("/submission/{submissionId}/recalculate")
    public ResponseEntity<Map<String, Object>> recalculateScore(@PathVariable Long submissionId) {
        Map<String, Object> response = new HashMap<>();
        try {
            homeworkService.recalculateSubmissionScore(submissionId);
            response.put("success", true);
            response.put("message", "得分重新计算成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "重新计算失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ================ 权限检查接口 ================

    /**
     * 检查学生是否有权限访问作业
     */
    @GetMapping("/{homeworkId}/student/{studentId}/access")
    public ResponseEntity<Map<String, Object>> checkHomeworkAccess(
            @PathVariable Long homeworkId,
            @PathVariable Long studentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean hasAccess = homeworkService.hasHomeworkAccess(homeworkId, studentId);
            response.put("success", true);
            response.put("data", hasAccess);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "检查权限失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 检查教师是否有权限管理作业
     */
    @GetMapping("/{homeworkId}/teacher/{teacherId}/manage-access")
    public ResponseEntity<Map<String, Object>> checkHomeworkManageAccess(
            @PathVariable Long homeworkId,
            @PathVariable Long teacherId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean hasAccess = homeworkService.hasHomeworkManageAccess(homeworkId, teacherId);
            response.put("success", true);
            response.put("data", hasAccess);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "检查管理权限失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

}