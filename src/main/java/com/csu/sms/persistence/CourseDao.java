package com.csu.sms.persistence;

import com.csu.sms.model.course.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseDao {
    Course findById(@Param("id") Long id);

    List<Course> findCoursesByPage(@Param("offset") Integer offset,
                                   @Param("limit") Integer limit);

    int updateCourse(Course course);

    int insertCourse(Course course);

    int deleteCourse(@Param("id") Long id);

    int countCourses();

    List<Course> findByIds(@Param("list") List<Long> distinctCourseIds);

    List<Course> findCoursesByPageForAdmin(@Param("offset") int offset,@Param("limit") Integer limit);

    int countCoursesForAdmin();
}
