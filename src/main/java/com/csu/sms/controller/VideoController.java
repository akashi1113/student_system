package com.csu.sms.controller;

import com.csu.sms.common.ApiControllerResponse;
import com.csu.sms.common.PageResult;
import com.csu.sms.common.ServiceException; // 引入ServiceException
import com.csu.sms.model.course.CourseVideo;
import com.csu.sms.service.VideoService;
import com.csu.sms.vo.VideoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus; // 引入HttpStatus
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // 引入MultipartFile

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class VideoController {
    private final VideoService videoService;

    // 获取视频详情
    @GetMapping("/video/{id}")
    public ApiControllerResponse<VideoVO> getVideoDetail(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Long userId
    ) {
        try {
            VideoVO videoVO = videoService.getVideoDetail(id, userId);
            if(videoVO == null) {
                return ApiControllerResponse.error(HttpStatus.NOT_FOUND.value(), "视频不存在。");
            }
            return ApiControllerResponse.success(videoVO);
        } catch (ServiceException e) {
            log.warn("Failed to get video detail for id {}: {}", id, e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred while getting video detail for id {}: {}", id, e.getMessage(), e);
            return ApiControllerResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误，获取视频详情失败。");
        }
    }

    // 获取某个课程下的所有视频
    @GetMapping("/courses/{courseId}/videos")
    public ApiControllerResponse<List<VideoVO>> getVideosByCourseId(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "1") Long userId
    ) {
        try {
            return ApiControllerResponse.success(videoService.getVideosByCourseId(courseId, userId));
        } catch (ServiceException e) {
            log.warn("Failed to get videos for course {}: {}", courseId, e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred while getting videos for course {}: {}", courseId, e.getMessage(), e);
            return ApiControllerResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误，获取课程视频列表失败。");
        }
    }

    // 添加视频 (管理员接口，支持文件上传)
    // 前端请求 Content-Type 应为 multipart/form-data
    @PostMapping("/admin/videos")
    public ApiControllerResponse<Long> addVideo(
            @RequestParam Long courseId,
            @RequestParam String title,
            @RequestParam(value = "duration", defaultValue = "0") Integer duration,
            @RequestParam(value = "sort", defaultValue = "0") Integer sort,       // 排序
            @RequestParam("videoFile") MultipartFile videoFile // 接收视频文件，这里是必须的
    ) {
        try {
            if (videoFile.isEmpty()) {
                return ApiControllerResponse.error(401, "视频文件不能为空。");
            }
            CourseVideo video = new CourseVideo();
            video.setCourseId(courseId);
            video.setTitle(title);
            video.setDuration(duration);
            video.setSort(sort);

            Long videoId = videoService.addVideo(video, videoFile); // 调用 service 层方法
            return ApiControllerResponse.success("视频添加成功！", videoId);
        } catch (ServiceException e) {
            log.warn("Failed to add video for course {}: {}", courseId, e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred during video creation for course {}: {}", courseId, e.getMessage(), e);
            return ApiControllerResponse.error(500, "服务器内部错误，视频添加失败，请稍后再试。");
        }
    }

    //视频列表（管理员接口）
    @GetMapping("/admin/videos")
    public ApiControllerResponse<PageResult<VideoVO>> listAdminVideos(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ){
        try {
            return ApiControllerResponse.success(videoService.listVideos(pageNum, pageSize));
        }
        catch (ServiceException e) {
            log.warn("Failed to list videos: {}", e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        }
    }

    // 更新视频 (管理员接口，支持文件上传)
    @PutMapping("/admin/videos/{id}")
    public ApiControllerResponse<Boolean> updateVideo(
            @PathVariable Long id,
            @RequestParam(value = "courseId", required = false) Long courseId, // 视频可以更换所属课程，可选
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "duration", required = false) Integer duration,
            @RequestParam(value = "sort", required = false) Integer sort,
            @RequestParam(value = "videoFile", required = false) MultipartFile videoFile // 可选的视频文件
    ) {
        try {
            CourseVideo video = new CourseVideo();
            video.setId(id); // 必须设置ID
            video.setCourseId(courseId);
            video.setTitle(title);
            video.setDuration(duration);
            video.setSort(sort);

            boolean success = videoService.updateVideo(video, videoFile);
            return ApiControllerResponse.success(success);
        } catch (ServiceException e) {
            log.warn("Failed to update video {}: {}", id, e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred during video update for video {}: {}", id, e.getMessage(), e);
            return ApiControllerResponse.error(500, "服务器内部错误，视频更新失败，请稍后再试。");
        }
    }

    // 删除视频 (管理员接口)
    @DeleteMapping("/admin/videos/{id}")
    public ApiControllerResponse<Boolean> deleteVideo(@PathVariable Long id) {
        try {
            boolean success = videoService.deleteVideo(id);
            return ApiControllerResponse.success(success);
        } catch (ServiceException e) {
            log.warn("Failed to delete video {}: {}", id, e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred during video deletion for video {}: {}", id, e.getMessage(), e);
            return ApiControllerResponse.error(500, "服务器内部错误，视频删除失败，请稍后再试。");
        }
    }
}
