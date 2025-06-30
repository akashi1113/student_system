package com.csu.sms.service;

import com.csu.sms.model.course.CourseVideo;
import com.csu.sms.model.course.StudyRecord;
import com.csu.sms.vo.VideoVO;
import org.springframework.web.multipart.MultipartFile; // 导入 MultipartFile

import java.util.List;

public interface VideoService {
    VideoVO getVideoDetail(Long id, Long userId);
    List<VideoVO> getVideosByCourseId(Long courseId, Long userId);

    // 新增/修改：视频上传需要关联课程ID，并接收文件
    Long addVideo(CourseVideo video, MultipartFile videoFile);
    boolean updateVideo(CourseVideo video, MultipartFile videoFile);
    boolean deleteVideo(Long id);

    void clearVideoDetailCacheForUser(Long videoId, Long userId);
    void onStudyRecordUpdated(StudyRecord record); // 完整的包名
}
