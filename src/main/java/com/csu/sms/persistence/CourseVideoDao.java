package com.csu.sms.persistence;

import com.csu.sms.model.course.CourseVideo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseVideoDao {
    CourseVideo getVideoById(@Param("id") Long id);
    List<CourseVideo> findVideosByCourseId(@Param("courseId") Long courseId);
    List<CourseVideo> findVideosByCourseIds(@Param("courseIds") List<Long> courseIds);
    int updateVideo(CourseVideo video);
    int insertVideo(CourseVideo video);
    int deleteVideo(@Param("id") Long id);
    int deleteVideosByCourseId(@Param("courseId") Long courseId);

    int countAll();

    List<CourseVideo> findVideosByPage(@Param("offset") int offset,@Param("pageSize") Integer pageSize);
}
