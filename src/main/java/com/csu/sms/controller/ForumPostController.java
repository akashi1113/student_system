package com.csu.sms.controller;

import com.csu.sms.common.ApiControllerResponse;
import com.csu.sms.common.PageResult;
import com.csu.sms.dto.ForumCommentDTO;
import com.csu.sms.dto.ForumPostDTO;
import com.csu.sms.service.ForumPostService;
import com.csu.sms.service.UserService;
import com.csu.sms.vo.CommentVO;
import com.csu.sms.vo.PostVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
@Slf4j
public class ForumPostController {
    private final ForumPostService forumPostService;
    private final UserService userService;

    @GetMapping
    public ApiControllerResponse<PageResult<PostVO>> listPosts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ){
        PageResult<PostVO> response = forumPostService.listPosts(category, keyword, page, size);
        return ApiControllerResponse.success(response);
    }

    @GetMapping("/{id}")
    public ApiControllerResponse<PostVO> getPostDetail(
            @PathVariable Long id
    ){
        PostVO postVO = forumPostService.getPostDetailAndIncreaseView(id);
        if(postVO == null){
            return ApiControllerResponse.error(404,"该帖子内容为空");
        }
        return ApiControllerResponse.success(postVO);
    }

    @PostMapping
    public ApiControllerResponse<Long> createPost(
            @RequestBody @Valid ForumPostDTO forumPostDTO
    ) {
        // 设置用户ID
        forumPostDTO.setUserId(forumPostDTO.getUserId());

        Long postId = forumPostService.createPost(forumPostDTO);
        if (postId == null) {
            return ApiControllerResponse.error(500, "创建帖子失败，请稍后再试");
        }
        return ApiControllerResponse.success("帖子创建成功！", postId);
    }

    @PutMapping("/{id}")
    public ApiControllerResponse<Boolean> updatePost(
            @PathVariable Long id,
            @RequestBody @Valid ForumPostDTO forumPostDTO
    ) {
        // 设置帖子ID
        forumPostDTO.setId(id);

        boolean success = forumPostService.updatePost(forumPostDTO);
        if (!success) {
            return ApiControllerResponse.error(500, "更新帖子失败");
        }
        return ApiControllerResponse.success(true);
    }

    @DeleteMapping("/{id}")
    public ApiControllerResponse<Boolean> deletePost(
            @PathVariable Long id
    ) {
        boolean success = forumPostService.deletePost(id);
        if (!success) {
            return ApiControllerResponse.error(500, "删除帖子失败");
        }
        return ApiControllerResponse.success(true);
    }

    @PostMapping("/{id}/like")
    public ApiControllerResponse<Boolean> likePost(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        boolean success = forumPostService.likePost(id, userId);
        if (success) {
            return ApiControllerResponse.success(true);
        } else {
            return ApiControllerResponse.error(500, "点赞失败");
        }
    }

    // 取消点赞
    @DeleteMapping("/{id}/like")
    public ApiControllerResponse<Boolean> unlikePost(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        boolean success = forumPostService.unlikePost(id, userId);
        if (success) {
            return ApiControllerResponse.success(true);
        } else {
            return ApiControllerResponse.error(500, "取消点赞失败");
        }
    }

    @PostMapping("/{id}/report")
    public ApiControllerResponse<Boolean> reportPost(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam String reason
    ) {
        boolean success = forumPostService.reportPost(id, userId, reason);
        if (success) {
            return ApiControllerResponse.success(true);
        } else {
            return ApiControllerResponse.error(500, "举报失败");
        }
    }

    //获取帖子的评论列表 (只获取顶级评论，不包含子回复)
    @GetMapping("/{postId}/comments")
    public ApiControllerResponse<PageResult<CommentVO>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Long currentUserId
    ) {
        PageResult<CommentVO> response = forumPostService.getComments(postId, page, size, currentUserId);
        return ApiControllerResponse.success(response);
    }

    //新增：获取某个评论的回复列表 (楼中楼子评论)
    @GetMapping("/comments/{parentCommentId}/replies")
    public ApiControllerResponse<PageResult<CommentVO>> getCommentReplies(
            @PathVariable Long parentCommentId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long currentUserId
    ) {
        PageResult<CommentVO> response = forumPostService.getCommentReplies(parentCommentId, page, size, currentUserId);
        return ApiControllerResponse.success(response);
    }

    //创建评论或回复
    @PostMapping("/{postId}/comments")
    public ApiControllerResponse<Long> createComment(
            @PathVariable Long postId,
            @RequestParam Long userId,
            @RequestBody @Valid ForumCommentDTO commentDTO // 使用 DTO 接收评论内容和 parentId
    ) {
        Long commentId = forumPostService.createComment(postId, userId, commentDTO);
        if (commentId == null) {
            return ApiControllerResponse.error(500, "评论失败，请稍后再试");
        }
        return ApiControllerResponse.success("评论成功！", commentId);
    }

    //删除评论
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ApiControllerResponse<Boolean> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam Long userId
    ) {
        boolean success = forumPostService.deleteComment(postId, commentId, userId);
        if (!success) {
            return ApiControllerResponse.error(500, "删除评论失败或无权限");
        }
        return ApiControllerResponse.success(true);
    }

    //更新评论
    @PutMapping("/{postId}/comments/{commentId}")
    public ApiControllerResponse<Boolean> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam Long userId,
            @RequestBody @Valid ForumCommentDTO commentDTO
    ) {
        boolean success = forumPostService.updateComment(postId, commentId, userId, commentDTO);
        if (!success) {
            return ApiControllerResponse.error(500, "更新评论失败或无权限");
        }
        return ApiControllerResponse.success(true);
    }

    //点赞评论
    @PostMapping("/comments/{commentId}/like")
    public ApiControllerResponse<Boolean> likeComment(
            @PathVariable Long commentId,
            @RequestParam Long userId
    ) {
        boolean success = forumPostService.likeComment(commentId, userId);
        if (success) {
            return ApiControllerResponse.success(true);
        } else {
            return ApiControllerResponse.error(500, "评论点赞失败");
        }
    }

    //取消点赞评论
    @DeleteMapping("/comments/{commentId}/like")
    public ApiControllerResponse<Boolean> unlikeComment(
            @PathVariable Long commentId,
            @RequestParam Long userId
    ) {
        boolean success = forumPostService.unlikeComment(commentId, userId);
        if (success) {
            return ApiControllerResponse.success(true);
        } else {
            return ApiControllerResponse.error(500, "取消评论点赞失败");
        }
    }

    // 管理员接口
    // 获取待审核的帖子列表
    @GetMapping("/admin/pending")
    public ApiControllerResponse<PageResult<PostVO>> getPendingPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Long adminId
    ) {
        // 验证管理员身份
        if(!userService.isAdminRole(adminId)){
            return ApiControllerResponse.error(403, "没有权限");
        }

        PageResult<PostVO> result = forumPostService.getPendingPosts(page, size);
        return ApiControllerResponse.success(result);
    }

    // 审核通过帖子
    @PostMapping("/admin/{id}/approve")
    public ApiControllerResponse<Boolean> approvePost(
            @PathVariable Long id,
            @RequestParam Long adminId
    ) {
        // 验证管理员身份
        if(!userService.isAdminRole(adminId)){
            return ApiControllerResponse.error(403, "没有权限");
        }

        boolean success = forumPostService.approvePost(id, adminId);
        if (success) {
            return ApiControllerResponse.success(true);
        } else {
            return ApiControllerResponse.error(500, "审核通过操作失败");
        }
    }

    // 审核拒绝帖子
    @PostMapping("/admin/{id}/reject")
    public ApiControllerResponse<Boolean> rejectPost(
            @PathVariable Long id,
            @RequestParam Long adminId,
            @RequestParam String reason
    ) {
        if(!userService.isAdminRole(adminId)){
            return ApiControllerResponse.error(403, "没有权限");
        }
        boolean success = forumPostService.rejectPost(id, adminId, reason);
        if (success) {
            return ApiControllerResponse.success(true);
        } else {
            return ApiControllerResponse.error(500, "拒绝帖子操作失败");
        }
    }

    //管理员处理举报：通过举报并删除帖子
    @PutMapping("/admin/reports/{reportId}/deletePost")
    public ApiControllerResponse<Boolean> processReportAndDeletePost(
            @PathVariable Long reportId,
            @RequestParam Long adminId
    ) {
        if(!userService.isAdminRole(adminId)){
            return ApiControllerResponse.error(403, "没有权限");
        }
        boolean success = forumPostService.processReportAndDeletePost(reportId, adminId);
        if (success) {
            return ApiControllerResponse.success(true);
        }
        return ApiControllerResponse.error(500, "处理举报失败：删除帖子");
    }

    //管理员处理举报：拒绝举报并保留帖子
    @PutMapping("/admin/reports/{reportId}/keepPost")
    public ApiControllerResponse<Boolean> processReportAndKeepPost(
            @PathVariable Long reportId,
            @RequestParam Long adminId,
            @RequestParam(required = false) String reasonForKeeping
    ) {
        if(!userService.isAdminRole(adminId)){
            return ApiControllerResponse.error(403, "没有权限");
        }
        boolean success = forumPostService.processReportAndKeepPost(reportId, adminId, reasonForKeeping);
        if (success) {
            return ApiControllerResponse.success(true);
        }
        return ApiControllerResponse.error(500, "处理举报失败：保留帖子");
    }
}
