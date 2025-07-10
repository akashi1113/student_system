package com.csu.sms.controller;

import com.csu.sms.annotation.RequireAdmin;
import com.csu.sms.common.ApiControllerResponse;
import com.csu.sms.common.PageResult;
import com.csu.sms.common.ServiceException;
import com.csu.sms.model.course.CourseVideo;
import com.csu.sms.service.SparkAIService;
import com.csu.sms.service.VideoService;
import com.csu.sms.util.UserContext;
import com.csu.sms.util.VideoUtils;
import com.csu.sms.vo.VideoVO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class VideoController {
    private final VideoService videoService;
    private final SparkAIService sparkAIService;

    @PostMapping("/video/ai-chat")
    public ApiControllerResponse<Map<String, String>> chatWithAI(
            @RequestBody AIChatRequest request
    ) {
        try {
            String reply = sparkAIService.chatWithAI(
                    "关于当前视频的问题：" + request.getMessage()
            );
            return ApiControllerResponse.success(Collections.singletonMap("reply", reply));
        } catch (Exception e) {
            log.error("AI聊天失败: {}", e.getMessage());
            return ApiControllerResponse.error("AI暂时无法回答，请稍后再试");
        }
    }

    // 请求体类
    @Data
    static class AIChatRequest {
        private Long videoId;
        private String videoTitle;
        private String message;
    }


    // ===================================
    //  普通用户/游客接口 (Public/User APIs)
    // ===================================

    // 获取视频详情
    @GetMapping("/video/{id}")
    public ApiControllerResponse<VideoVO> getVideoDetail(
            @PathVariable Long id
    ) {
        try {
            Long currentUserId = UserContext.getCurrentUserId();
            VideoVO videoVO = videoService.getVideoDetail(id, currentUserId);
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
            @PathVariable Long courseId
    ) {
        try {
            Long currentUserId = UserContext.getCurrentUserId();
            return ApiControllerResponse.success(videoService.getVideosByCourseId(courseId, currentUserId));
        } catch (ServiceException e) {
            log.warn("Failed to get videos for course {}: {}", courseId, e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred while getting videos for course {}: {}", courseId, e.getMessage(), e);
            return ApiControllerResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误，获取课程视频列表失败。");
        }
    }

    // ===================================
    //  管理员专属接口 (Admin Only APIs)
    // ===================================

    // 添加视频 (管理员接口，支持文件上传)
    @PostMapping("/admin/videos")
//    @RequireAdmin
    public ApiControllerResponse<Long> addVideo(
            @RequestParam Long courseId,
            @RequestParam String title,
            @RequestParam(value = "sort", defaultValue = "0") Integer sort,
            @RequestParam("videoFile") MultipartFile videoFile
    ) {
        try {
            if (videoFile == null || videoFile.isEmpty()) {
                return ApiControllerResponse.error(HttpStatus.BAD_REQUEST.value(), "视频文件不能为空。");
            }
            // 💡 优化：VideoUtils.getVideoDuration 可能抛出IOException
            int durationInSeconds = VideoUtils.getVideoDuration(videoFile);
            if (durationInSeconds <= 0) {
                throw new ServiceException("无法解析视频时长，请检查视频文件是否有效！");
            }

            CourseVideo video = new CourseVideo();
            video.setCourseId(courseId);
            video.setTitle(title);
            video.setDuration(durationInSeconds);
            video.setSort(sort);

            Long videoId = videoService.addVideo(video, videoFile);
            return ApiControllerResponse.success("视频添加成功！", videoId);
        } catch (ServiceException e) {
            log.warn("Failed to add video for course {}: {}", courseId, e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred during video creation for course {}: {}", courseId, e.getMessage(), e);
            return ApiControllerResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误，视频添加失败，请稍后再试。");
        }
    }

    // 视频列表（管理员接口）
    @GetMapping("/admin/videos")
//    @RequireAdmin
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
    @PostMapping("/admin/videos/{id}") // 浏览器 form 表单提交不支持 PATCH, 仍用 POST
//    @RequireAdmin
    public ApiControllerResponse<Boolean> updateVideo(
            @PathVariable Long id,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "sort", required = false) Integer sort,
            @RequestParam(value = "videoFile", required = false) MultipartFile videoFile
    ) {
        try {
            CourseVideo video = new CourseVideo();
            video.setId(id);
            video.setCourseId(courseId);
            video.setTitle(title);
            video.setSort(sort);

            // 只有当上传了新文件时，才去解析和设置时长
            if (videoFile != null && !videoFile.isEmpty()) {
                int durationInSeconds = VideoUtils.getVideoDuration(videoFile);
                if (durationInSeconds <= 0) {
                    throw new ServiceException("无法解析新视频的时长，请检查视频文件是否有效！");
                }
                video.setDuration(durationInSeconds);
            }

            boolean success = videoService.updateVideo(video, videoFile);
            return ApiControllerResponse.success(success);
        } catch (ServiceException e) {
            log.warn("Failed to update video {}: {}", id, e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred during video update for video {}: {}", id, e.getMessage(), e);
            return ApiControllerResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误，视频更新失败，请稍后再试。");
        }
    }

    // 删除视频 (管理员接口)
    @DeleteMapping("/admin/videos/{id}")
//    @RequireAdmin
    public ApiControllerResponse<Boolean> deleteVideo(@PathVariable Long id) {
        try {
            boolean success = videoService.deleteVideo(id);
            return ApiControllerResponse.success(success);
        } catch (ServiceException e) {
            log.warn("Failed to delete video {}: {}", id, e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred during video deletion for video {}: {}", id, e.getMessage(), e);
            return ApiControllerResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误，视频删除失败，请稍后再试。");
        }
    }
}

