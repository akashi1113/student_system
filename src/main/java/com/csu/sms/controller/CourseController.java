package com.csu.sms.controller;

import com.csu.sms.annotation.RequireAdmin;
import com.csu.sms.common.ApiControllerResponse;
import com.csu.sms.common.PageResult;
import com.csu.sms.common.ServiceException;
import com.csu.sms.model.course.Course;
import com.csu.sms.service.CourseService;
import com.csu.sms.util.UserContext;
import com.csu.sms.vo.CourseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.csu.sms.annotation.LogOperation;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseController {
    private final CourseService courseService;

    // 获取课程列表
    @GetMapping
    public ApiControllerResponse<PageResult<CourseVO>> listCourses(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        try {
            // 使用UserContext获取当前用户ID
            Long userId = UserContext.getRequiredCurrentUserId();
            return ApiControllerResponse.success(courseService.listCourses(userId, pageNum, pageSize));
        } catch (IllegalStateException e) {
            log.warn("User not logged in: {}", e.getMessage());
            return ApiControllerResponse.error(401, "用户未登录，请先登录");
        } catch (ServiceException e) {
            log.warn("Failed to list courses: {}", e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred while listing courses: {}", e.getMessage(), e);
            return ApiControllerResponse.error(500, "服务器内部错误，获取课程列表失败。");
        }
    }

    // 获取课程详情
    @GetMapping("/{id}")
    public ApiControllerResponse<CourseVO> getCourseDetail(@PathVariable Long id) {
        try {
            // 使用UserContext获取当前用户ID
            Long userId = UserContext.getRequiredCurrentUserId();
            CourseVO courseVO = courseService.getCourseDetail(id, userId);
            if(courseVO == null) {
                return ApiControllerResponse.error(400, "课程不存在。");
            }
            return ApiControllerResponse.success(courseVO);
        } catch (IllegalStateException e) {
            log.warn("User not logged in: {}", e.getMessage());
            return ApiControllerResponse.error(401, "用户未登录，请先登录");
        } catch (ServiceException e) {
            log.warn("Failed to get course detail for id {}: {}", id, e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred while getting course detail for id {}: {}", id, e.getMessage(), e);
            return ApiControllerResponse.error(500, "服务器内部错误，获取课程详情失败。");
        }
    }

    // 添加课程 (管理员接口，支持文件上传)
    @PostMapping("/admin")
    @RequireAdmin // 添加管理员权限注解
    @LogOperation(module = "课程管理", operation = "新增课程", description = "管理员新增课程")
    public ApiControllerResponse<Long> addCourse(
            @RequestParam String title,
            @RequestParam(defaultValue = "") String description,
            @RequestParam String teacherName,
            @RequestParam(value = "status", defaultValue = "0") Integer status,
            @RequestParam(value = "coverImage", required = false) MultipartFile coverImageFile
    ) {
        try {
            // 权限校验 - 确保是管理员
            if (!UserContext.isAdmin()) {
                return ApiControllerResponse.error(403, "权限不足，只有管理员可以访问");
            }

            Course course = new Course();
            course.setTitle(title);
            course.setDescription(description);
            course.setTeacherName(teacherName);
            course.setStatus(status);

            Long courseId = courseService.createCourse(course, coverImageFile);
            return ApiControllerResponse.success("课程添加成功！", courseId);
        } catch (ServiceException e) {
            log.warn("Failed to add course: {}", e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred during course creation: {}", e.getMessage(), e);
            return ApiControllerResponse.error(500, "服务器内部错误，课程添加失败，请稍后再试。");
        }
    }

    // 管理员获取课程列表
    @GetMapping("/admin")
    @RequireAdmin // 添加管理员权限注解
    public ApiControllerResponse<PageResult<CourseVO>> listCoursesForAdmin(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        try {
            // 权限校验 - 确保是管理员
            if (!UserContext.isAdmin()) {
                return ApiControllerResponse.error(403, "权限不足，只有管理员可以访问");
            }
            // 使用UserContext获取当前用户ID
            Long userId = UserContext.getRequiredCurrentUserId();
            // 管理员接口不需要特定用户ID，使用0表示管理员视图
            return ApiControllerResponse.success(courseService.listCoursesForAdmin(userId,pageNum, pageSize));
        } catch (ServiceException e) {
            log.warn("Failed to list courses: {}", e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred while listing courses: {}", e.getMessage(), e);
            return ApiControllerResponse.error(500, "服务器内部错误，获取课程列表失败。");
        }
    }

    // 更新课程 (管理员接口，支持文件上传)
    @PutMapping("/admin/{id}")
    @RequireAdmin // 添加管理员权限注解
    @LogOperation(module = "课程管理", operation = "修改课程", description = "管理员修改课程信息")
    public ApiControllerResponse<Boolean> updateCourse(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String teacherName,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "coverImage", required = false) MultipartFile coverImageFile,
            @RequestParam(value = "clearCoverImage", required = false) Boolean clearCoverImage
    ) {
        try {
            // 权限校验 - 确保是管理员
            if (!UserContext.isAdmin()) {
                return ApiControllerResponse.error(403, "权限不足，只有管理员可以访问");
            }

            Course course = new Course();
            course.setId(id);

            if (StringUtils.hasText(title)) {
                course.setTitle(title);
            } else if (title != null) {
                course.setTitle(null);
            }

            if (StringUtils.hasText(description)) {
                course.setDescription(description);
            } else if (description != null) {
                course.setDescription(null);
            }

            if (StringUtils.hasText(teacherName)) {
                course.setTeacherName(teacherName);
            } else if (teacherName != null) {
                course.setTeacherName(null);
            }

            if (status != null) {
                course.setStatus(status);
            }

            if (Boolean.TRUE.equals(clearCoverImage) && (coverImageFile == null || coverImageFile.isEmpty())) {
                course.setCoverImg("");
            }

            boolean success = courseService.updateCourse(course, coverImageFile);
            return ApiControllerResponse.success(success);
        } catch (ServiceException e) {
            log.warn("Failed to update course {}: {}", id, e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred during course update for id {}: {}", id, e.getMessage(), e);
            return ApiControllerResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误，课程更新失败，请稍后再试。");
        }
    }

    // 删除课程 (管理员接口)
    @DeleteMapping("/admin/{id}")
    @RequireAdmin // 添加管理员权限注解
    @LogOperation(module = "课程管理", operation = "删除课程", description = "管理员删除课程")
    public ApiControllerResponse<Boolean> deleteCourse(@PathVariable Long id) {
        try {
            // 权限校验 - 确保是管理员
            if (!UserContext.isAdmin()) {
                return ApiControllerResponse.error(403, "权限不足，只有管理员可以访问");
            }

            boolean success = courseService.deleteCourse(id);
            return ApiControllerResponse.success(success);
        } catch (ServiceException e) {
            log.warn("Failed to delete course {}: {}", id, e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred during course deletion for id {}: {}", id, e.getMessage(), e);
            return ApiControllerResponse.error(500, "服务器内部错误，课程删除失败，请稍后再试。");
        }
    }

    // 获取课程学习进度
    @GetMapping("/{courseId}/progress")
    public ApiControllerResponse<Double> getCourseProgress(@PathVariable Long courseId) {
        try {
            // 使用UserContext获取当前用户ID
            Long userId = UserContext.getRequiredCurrentUserId();
            double progress = courseService.calculateCourseProgress(courseId, userId);
            return ApiControllerResponse.success(progress);
        } catch (IllegalStateException e) {
            log.warn("User not logged in: {}", e.getMessage());
            return ApiControllerResponse.error(401, "用户未登录，请先登录");
        } catch (ServiceException e) {
            log.warn("Failed to get course progress for courseId {}: {}", courseId, e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred while getting course progress for courseId {}: {}", courseId, e.getMessage(), e);
            return ApiControllerResponse.error(500, "服务器内部错误，获取课程进度失败。");
        }
    }
}



//package com.csu.sms.controller;
//
//import com.csu.sms.common.ApiControllerResponse;
//import com.csu.sms.common.PageResult;
//import com.csu.sms.common.ServiceException; // 引入ServiceException
//import com.csu.sms.model.course.Course;
//import com.csu.sms.service.CourseService;
//import com.csu.sms.vo.CourseVO;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus; // 引入HttpStatus
//import org.springframework.util.StringUtils;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile; // 引入MultipartFile
//import com.csu.sms.annotation.LogOperation;
//
//@CrossOrigin(origins = "http://localhost:5173")
//@RestController
//@RequestMapping("/api/courses")
//@RequiredArgsConstructor
//@Slf4j
//public class CourseController {
//    private final CourseService courseService;
//
//    // 获取课程列表
//    @GetMapping
//    public ApiControllerResponse<PageResult<CourseVO>> listCourses(
//            @RequestParam(defaultValue = "1") Long userId,
//            @RequestParam(defaultValue = "1") Integer pageNum,
//            @RequestParam(defaultValue = "10") Integer pageSize
//    ) {
//        try {
//            return ApiControllerResponse.success(courseService.listCourses(userId, pageNum, pageSize));
//        } catch (ServiceException e) {
//            log.warn("Failed to list courses: {}", e.getMessage());
//            return ApiControllerResponse.error(e.getCode(), e.getMessage());
//        } catch (Exception e) {
//            log.error("An unexpected error occurred while listing courses: {}", e.getMessage(), e);
//            return ApiControllerResponse.error(500, "服务器内部错误，获取课程列表失败。");
//        }
//    }
//
//    // 获取课程详情
//    @GetMapping("/{id}")
//    public ApiControllerResponse<CourseVO> getCourseDetail(
//            @PathVariable Long id,
//            @RequestParam(defaultValue = "1") Long userId
//    ) {
//        try {
//            CourseVO courseVO = courseService.getCourseDetail(id, userId);
//            if(courseVO == null) {
//                return ApiControllerResponse.error(400, "课程不存在。");
//            }
//            return ApiControllerResponse.success(courseVO);
//        } catch (ServiceException e) {
//            log.warn("Failed to get course detail for id {}: {}", id, e.getMessage());
//            return ApiControllerResponse.error(e.getCode(), e.getMessage());
//        } catch (Exception e) {
//            log.error("An unexpected error occurred while getting course detail for id {}: {}", id, e.getMessage(), e);
//            return ApiControllerResponse.error(500, "服务器内部错误，获取课程详情失败。");
//        }
//    }
//
//    // 添加课程 (管理员接口，支持文件上传)
//    @PostMapping("/admin")
//    @LogOperation(module = "课程管理", operation = "新增课程", description = "管理员新增课程")
//    public ApiControllerResponse<Long> addCourse(
//            @RequestParam String title,
//            @RequestParam(defaultValue = "") String description,
//            @RequestParam String teacherName, // 新增：老师姓名
//            @RequestParam(value = "status", defaultValue = "0") Integer status, // 新增：课程状态 0-上架 1-下架
//            @RequestParam(value = "coverImage", required = false) MultipartFile coverImageFile // 课程封面图，可选
//    ) {
//        try {
//            Course course = new Course();
//            course.setTitle(title);
//            course.setDescription(description);
//            course.setTeacherName(teacherName); // 设置老师姓名
//            course.setStatus(status);           // 设置课程状态
//
//            Long courseId = courseService.createCourse(course, coverImageFile); // 调用 service 层方法
//            return ApiControllerResponse.success("课程添加成功！", courseId);
//        } catch (ServiceException e) {
//            log.warn("Failed to add course: {}", e.getMessage());
//            return ApiControllerResponse.error(e.getCode(), e.getMessage());
//        } catch (Exception e) {
//            log.error("An unexpected error occurred during course creation: {}", e.getMessage(), e);
//            return ApiControllerResponse.error(500, "服务器内部错误，课程添加失败，请稍后再试。");
//        }
//    }
//
//    @GetMapping("/admin")
//    public ApiControllerResponse<PageResult<CourseVO>> listCoursesForAdmin(
//            @RequestParam(defaultValue = "1") Long userId,
//            @RequestParam(defaultValue = "1") Integer pageNum,
//            @RequestParam(defaultValue = "10") Integer pageSize
//    ) {
//        try {
//            return ApiControllerResponse.success(courseService.listCoursesForAdmin(userId,pageNum, pageSize));
//        } catch (ServiceException e) {
//            log.warn("Failed to list courses: {}", e.getMessage());
//            return ApiControllerResponse.error(e.getCode(), e.getMessage());
//        } catch (Exception e) {
//            log.error("An unexpected error occurred while listing courses: {}", e.getMessage(), e);
//            return ApiControllerResponse.error(500, "服务器内部错误，获取课程列表失败。");
//        }
//    }
//
//    // 更新课程 (管理员接口，支持文件上传)
//    @PutMapping("/admin/{id}")
//    @LogOperation(module = "课程管理", operation = "修改课程", description = "管理员修改课程信息")
//    public ApiControllerResponse<Boolean> updateCourse(
//            @PathVariable Long id,
//            @RequestParam(required = false) String title,
//            @RequestParam(required = false) String description,
//            @RequestParam(required = false) String teacherName, // 新增：老师姓名
//            @RequestParam(value = "status", required = false) Integer status, // 新增：课程状态
//            @RequestParam(value = "coverImage", required = false) MultipartFile coverImageFile, // 课程封面图，可选
//            @RequestParam(value = "clearCoverImage", required = false) Boolean clearCoverImage
//    ) {
//        try {
//            Course course = new Course();
//            course.setId(id); // 必须设置ID
//
//            // 对于字符串类型，判断是否为null或空字符串，如果是空字符串则转为null
//            // 这样，在Service层通过判断null来决定是否更新字段就更准确
//            if (StringUtils.hasText(title)) { // hasText() 判断是否既不为null也不为空白字符串
//                course.setTitle(title);
//            } else if (title != null) {
//                // 如果是明确传入的空字符串""，则设为null表示不更新 (前端没传 或传了空字符串都会是 "")
//                // 当然，你也可以根据需求决定：如果传了 "" 是不是意味着要清空字段？
//                // 如果传 "" 意味着清空，那就直接 course.setTitle("")
//                // 如果传 "" 意味着不更新，就转为 null
//                // 这里我们选择转为 null，让 Service 层通过 null 判断"不更新"
//                course.setTitle(null); // 或者直接不设置，因为初始course对象的String字段就是null
//            }
//
//            if (StringUtils.hasText(description)) {
//                course.setDescription(description);
//            } else if (description != null) {
//                course.setDescription(null);
//            }
//
//            if (StringUtils.hasText(teacherName)) {
//                course.setTeacherName(teacherName);
//            } else if (teacherName != null) {
//                course.setTeacherName(null);
//            }
//
//            // 对于非String类型，null就是未提供，所以保持原逻辑
//            if (status != null) {
//                course.setStatus(status);
//            }
//
//            // 处理清除图片逻辑：
//            // 如果前端明确发送了 clearCoverImage=true (布尔值) 并且没有上传新文件
//            if (Boolean.TRUE.equals(clearCoverImage) && (coverImageFile == null || coverImageFile.isEmpty())) {
//                // 设置为空字符串，作为清除标记，Service层会识别这个空字符串并删除图片
//                course.setCoverImg("");
//            }
//            // 否则，如果 clearCoverImage 为 false/null，且没有上传文件，
//            // 那么 course.getCoverImg() 保持为 null，Service层会根据 coverImageFile 是否有值
//            // 来判断是上传新图片还是保持旧图片。
//
//            boolean success = courseService.updateCourse(course, coverImageFile);
//            return ApiControllerResponse.success(success);
//        } catch (ServiceException e) {
//            log.warn("Failed to update course {}: {}", id, e.getMessage());
//            return ApiControllerResponse.error(e.getCode(), e.getMessage());
//        } catch (Exception e) {
//            log.error("An unexpected error occurred during course update for id {}: {}", id, e.getMessage(), e);
//            return ApiControllerResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误，课程更新失败，请稍后再试。");
//        }
//    }
//
//    // 删除课程 (管理员接口)
//    @DeleteMapping("/admin/{id}")
//    @LogOperation(module = "课程管理", operation = "删除课程", description = "管理员删除课程")
//    public ApiControllerResponse<Boolean> deleteCourse(@PathVariable Long id) {
//        try {
//            boolean success = courseService.deleteCourse(id);
//            return ApiControllerResponse.success(success);
//        } catch (ServiceException e) {
//            log.warn("Failed to delete course {}: {}", id, e.getMessage());
//            return ApiControllerResponse.error(e.getCode(), e.getMessage());
//        } catch (Exception e) {
//            log.error("An unexpected error occurred during course deletion for id {}: {}", id, e.getMessage(), e);
//            return ApiControllerResponse.error(500, "服务器内部错误，课程删除失败，请稍后再试。");
//        }
//    }
//
//    /**
//     * 获取指定课程的学习总进度
//     * @param courseId 课程ID
//     * @return 包含课程进度的响应，进度值为0-100之间的小数
//     */
//    @GetMapping("/{courseId}/progress")
//    public ApiControllerResponse<Double> getCourseProgress(
//            @PathVariable Long courseId,
//            @RequestParam(defaultValue = "1") Long userId ) {
//        try {
//            double progress = courseService.calculateCourseProgress(courseId, userId);
//            return ApiControllerResponse.success(progress);
//        } catch (ServiceException e) {
//            log.warn("Failed to get course progress for courseId {}: {}", courseId, e.getMessage());
//            return ApiControllerResponse.error(e.getCode(), e.getMessage());
//        } catch (Exception e) {
//            log.error("An unexpected error occurred while getting course progress for courseId {}: {}", courseId, e.getMessage(), e);
//            return ApiControllerResponse.error(500, "服务器内部错误，获取课程进度失败。");
//        }
//    }
//
//}
