package com.csu.sms.service;

import com.csu.sms.common.PageResult;
import com.csu.sms.model.course.Course;
import com.csu.sms.vo.CourseVO;
import org.springframework.web.multipart.MultipartFile; // 导入 MultipartFile

public interface CourseService {
    PageResult<CourseVO> listCourses(Long userId, Integer pageNum, Integer pageSize);
    CourseVO getCourseDetail(Long id, Long userId);

    // 修改方法签名以接收 MultipartFile
    Long createCourse(Course course, MultipartFile coverImageFile);
    boolean updateCourse(Course course, MultipartFile coverImageFile);
    boolean deleteCourse(Long id);
}
