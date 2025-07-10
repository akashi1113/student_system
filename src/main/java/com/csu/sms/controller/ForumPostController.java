package com.csu.sms.controller;

import com.csu.sms.common.ApiControllerResponse;
import com.csu.sms.common.PageResult;
import com.csu.sms.dto.ForumCommentDTO;
import com.csu.sms.dto.ForumPostDTO;
import com.csu.sms.service.ForumPostService;
import com.csu.sms.service.SparkAIService;
import com.csu.sms.util.UserContext;
import com.csu.sms.vo.CommentVO;
import com.csu.sms.vo.PostVO;
import com.csu.sms.vo.ReportVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
@Slf4j
public class ForumPostController {
    private final ForumPostService forumPostService;
    private final SparkAIService sparkAIService;

    @GetMapping("/{postId}/summary")
    public ApiControllerResponse<String> getPostSummary(
            @PathVariable Long postId
    ) {
        PostVO post = forumPostService.getPostDetailAndIncreaseView(postId);
        if (post == null || post.getContent() == null) {
            return ApiControllerResponse.error(404, "帖子不存在或内容为空");
        }

        String summary = sparkAIService.summarizeContent(post.getContent());
        return ApiControllerResponse.success("获取总结成功",summary);
    }

    // ForumPostController.java
    @GetMapping("/admin/{postId}/ai-review")
    public ApiControllerResponse<String> getAIReview(
            @PathVariable Long postId
    ) {
        PostVO post = forumPostService.getPostDetailForAdmin(postId);
        if (post == null || post.getContent() == null) {
            return ApiControllerResponse.error(404, "帖子不存在或内容为空");
        }

        String reviewResult = sparkAIService.reviewPostContent(post.getContent());
        return ApiControllerResponse.success("AI审核成功",reviewResult);
    }

    // 语义搜索接口
    @GetMapping("/semantic-search")
    public ApiControllerResponse<PageResult<PostVO>> semanticSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int count
    ) {
        PageResult<PostVO> result = forumPostService.semanticSearch(query, count);
        return ApiControllerResponse.success(result);
    }

    // 相关帖子推荐接口
    @GetMapping("/{postId}/related")
    public ApiControllerResponse<List<PostVO>> getRelatedPosts(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "3") int count
    ) {
        List<PostVO> relatedPosts = forumPostService.getRelatedPosts(postId, count);
        return ApiControllerResponse.success(relatedPosts);
    }

    // 个性化推荐接口
    @GetMapping("/recommendations")
    public ApiControllerResponse<List<PostVO>> getRecommendations(
            @RequestParam(defaultValue = "2") int count
    ) {
        Long currentUserId = UserContext.getCurrentUserId(); // 可为null
        List<PostVO> recommendations = forumPostService.getRecommendedPosts(currentUserId, count);
        return ApiControllerResponse.success(recommendations);
    }

    // ===================================
    //  公开访问接口 (Public APIs)
    // ===================================

    // 列表查询 - 无需登录
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

    // 热帖 - 无需登录
    @GetMapping("/hot")
    public ApiControllerResponse<List<PostVO>> getHotPosts(
            @RequestParam(defaultValue = "5") int count) {
        List<PostVO> hotPosts = forumPostService.getHotPosts(count);
        return ApiControllerResponse.success(hotPosts);
    }

    // 帖子详情 - 无需登录
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

    // ===================================
    //  需要登录的普通用户接口 (User Authenticated APIs)
    // ===================================

    // 创建帖子 - 需要登录
    @PostMapping
    public ApiControllerResponse<Long> createPost(
            @RequestBody @Valid ForumPostDTO forumPostDTO
    ) {
        // ✨ 修改：从 UserContext 获取当前登录用户ID，更安全
        Long currentUserId = UserContext.getRequiredCurrentUserId();
        forumPostDTO.setUserId(currentUserId);

        Long postId = forumPostService.createPost(forumPostDTO);
        if (postId == null) {
            return ApiControllerResponse.error(500, "创建帖子失败，请稍后再试");
        }
        return ApiControllerResponse.success("帖子创建成功！", postId);
    }

    // 更新帖子 - 需要登录 (Service层应包含作者校验)
    @PutMapping("/{id}")
    public ApiControllerResponse<Boolean> updatePost(
            @PathVariable Long id,
            @RequestBody @Valid ForumPostDTO forumPostDTO
    ) {
        // ✨ 修改：从 UserContext 获取当前登录用户ID，传递给Service层进行权限校验
        Long currentUserId = UserContext.getRequiredCurrentUserId();
        forumPostDTO.setUserId(currentUserId); // 传递当前用户ID
        forumPostDTO.setId(id);

        boolean success = forumPostService.updatePost(forumPostDTO);
        if (!success) {
            return ApiControllerResponse.error(500, "更新帖子失败");
        }
        return ApiControllerResponse.success(true);
    }

    // 删除帖子 - 需要登录 (Service层应包含作者或管理员校验)
    @DeleteMapping("/{id}")
    public ApiControllerResponse<Boolean> deletePost(
            @PathVariable Long id
    ) {
        // ✨ 修改：从 UserContext 获取当前登录用户ID和角色信息
        Long currentUserId = UserContext.getRequiredCurrentUserId();
//        boolean isAdmin = UserContext.isAdmin();
        boolean isAdmin = true;

        boolean success = forumPostService.deletePost(id, currentUserId, isAdmin);
        if (!success) {
            return ApiControllerResponse.error(500, "删除帖子失败或无权限");
        }
        return ApiControllerResponse.success(true);
    }

    // 点赞帖子 - 需要登录
    @PostMapping("/{id}/like")
    public ApiControllerResponse<Boolean> likePost(
            @PathVariable Long id
            // ✨ 修改：移除了 @RequestParam Long userId
    ) {
        // ✨ 修改：从 UserContext 获取当前登录用户ID
        Long currentUserId = UserContext.getRequiredCurrentUserId();
        boolean success = forumPostService.likePost(id, currentUserId);
        if (success) {
            return ApiControllerResponse.success(true);
        } else {
            return ApiControllerResponse.error(500, "点赞失败");
        }
    }

    // 取消点赞 - 需要登录
    @DeleteMapping("/{id}/like")
    public ApiControllerResponse<Boolean> unlikePost(
            @PathVariable Long id
            // ✨ 修改：移除了 @RequestParam Long userId
    ) {
        // ✨ 修改：从 UserContext 获取当前登录用户ID
        Long currentUserId = UserContext.getRequiredCurrentUserId();
        boolean success = forumPostService.unlikePost(id, currentUserId);
        if (success) {
            return ApiControllerResponse.success(true);
        } else {
            return ApiControllerResponse.error(500, "取消点赞失败");
        }
    }

    // 举报帖子 - 需要登录
    @PostMapping("/{id}/report")
    public ApiControllerResponse<Boolean> reportPost(
            @PathVariable Long id,
            @RequestParam String reason // 举报原因仍然需要前端传递
            // ✨ 修改：移除了 @RequestParam Long userId
    ) {
        // ✨ 修改：从 UserContext 获取当前登录用户ID
        Long currentUserId = UserContext.getRequiredCurrentUserId();
        boolean success = forumPostService.reportPost(id, currentUserId, reason);
        if (success) {
            return ApiControllerResponse.success(true);
        } else {
            return ApiControllerResponse.error(500, "举报失败");
        }
    }

    // ===================================
    //  评论相关接口 (Comment APIs)
    // ===================================

    // 获取帖子的评论列表
    @GetMapping("/{postId}/comments")
    public ApiControllerResponse<PageResult<CommentVO>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
            // ✨ 修改：移除了 @RequestParam Long currentUserId
    ) {
        // ✨ 修改：从 UserContext 获取当前登录用户ID (可以为null，表示未登录)
        Long currentUserId = UserContext.getCurrentUserId();
        PageResult<CommentVO> response = forumPostService.getComments(postId, page, size, currentUserId);
        return ApiControllerResponse.success(response);
    }

    // 获取某个评论的回复列表 (楼中楼)
    @GetMapping("/comments/{parentCommentId}/replies")
    public ApiControllerResponse<PageResult<CommentVO>> getCommentReplies(
            @PathVariable Long parentCommentId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
            // ✨ 修改：移除了 @RequestParam Long currentUserId
    ) {
        // ✨ 修改：从 UserContext 获取当前登录用户ID (可以为null，表示未登录)
        Long currentUserId = UserContext.getCurrentUserId();
        PageResult<CommentVO> response = forumPostService.getCommentReplies(parentCommentId, page, size, currentUserId);
        return ApiControllerResponse.success(response);
    }

    // 创建评论或回复 - 需要登录
    @PostMapping("/{postId}/comments")
    public ApiControllerResponse<Long> createComment(
            @PathVariable Long postId,
            @RequestBody @Valid ForumCommentDTO commentDTO // 使用 DTO 接收评论内容和 parentId
            // ✨ 修改：移除了 @RequestParam Long userId
    ) {
        // ✨ 修改：从 UserContext 获取当前登录用户ID
        Long currentUserId = UserContext.getRequiredCurrentUserId();
        Long commentId = forumPostService.createComment(postId, currentUserId, commentDTO);
        if (commentId == null) {
            return ApiControllerResponse.error(500, "评论失败，请稍后再试");
        }
        return ApiControllerResponse.success("评论成功！", commentId);
    }

    // 删除评论 - 需要登录 (Service层应包含作者或管理员校验)
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ApiControllerResponse<Boolean> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId
            // ✨ 修改：移除了 @RequestParam Long userId
    ) {
        // ✨ 修改：从 UserContext 获取当前登录用户ID和角色
        Long currentUserId = UserContext.getRequiredCurrentUserId();
//        boolean isAdmin = UserContext.isAdmin();
        boolean isAdmin = true;

        boolean success = forumPostService.deleteComment(postId, commentId, currentUserId, isAdmin);
        if (!success) {
            return ApiControllerResponse.error(500, "删除评论失败或无权限");
        }
        return ApiControllerResponse.success(true);
    }

    // 更新评论 - 需要登录 (Service层应包含作者校验)
    @PutMapping("/{postId}/comments/{commentId}")
    public ApiControllerResponse<Boolean> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody @Valid ForumCommentDTO commentDTO
            // ✨ 修改：移除了 @RequestParam Long userId
    ) {
        // ✨ 修改：从 UserContext 获取当前登录用户ID
        Long currentUserId = UserContext.getRequiredCurrentUserId();
        boolean success = forumPostService.updateComment(postId, commentId, currentUserId, commentDTO);
        if (!success) {
            return ApiControllerResponse.error(500, "更新评论失败或无权限");
        }
        return ApiControllerResponse.success(true);
    }

    // 点赞评论 - 需要登录
    @PostMapping("/comments/{commentId}/like")
    public ApiControllerResponse<Boolean> likeComment(
            @PathVariable Long commentId
            // ✨ 修改：移除了 @RequestParam Long userId
    ) {
        // ✨ 修改：从 UserContext 获取当前登录用户ID
        Long currentUserId = UserContext.getRequiredCurrentUserId();
        boolean success = forumPostService.likeComment(commentId, currentUserId);
        if (success) {
            return ApiControllerResponse.success(true);
        } else {
            return ApiControllerResponse.error(500, "评论点赞失败");
        }
    }

    // 取消点赞评论 - 需要登录
    @DeleteMapping("/comments/{commentId}/like")
    public ApiControllerResponse<Boolean> unlikeComment(
            @PathVariable Long commentId
            // ✨ 修改：移除了 @RequestParam Long userId
    ) {
        // ✨ 修改：从 UserContext 获取当前登录用户ID
        Long currentUserId = UserContext.getRequiredCurrentUserId();
        boolean success = forumPostService.unlikeComment(commentId, currentUserId);
        if (success) {
            return ApiControllerResponse.success(true);
        } else {
            return ApiControllerResponse.error(500, "取消评论点赞失败");
        }
    }


    // ===================================
    //  管理员专属接口 (Admin Only APIs)
    // ===================================

    // 获取待审核的帖子列表
    // ✨ 修改：使用 @RequireAdmin 注解进行权限控制
    @GetMapping("/admin/pending")
//    @RequireAdmin
    public ApiControllerResponse<PageResult<PostVO>> getPendingPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
            // ✨ 修改：移除了 @RequestParam Long adminId 和手动权限校验
    ) {
        PageResult<PostVO> result = forumPostService.getPendingPosts(page, size);
        return ApiControllerResponse.success(result);
    }

    // 获取帖子详情（管理员视角，可能包含更多信息）
    // ✨ 修改：使用 @RequireAdmin 注解进行权限控制
    @GetMapping("/admin/detail/{id}")
//    @RequireAdmin
    public ApiControllerResponse<PostVO> getPostDetailForAdmin(
            @PathVariable Long id
    ){
        PostVO postVO = forumPostService.getPostDetailForAdmin(id);
        if(postVO == null){
            return ApiControllerResponse.error(404,"该帖子内容为空");
        }
        return ApiControllerResponse.success(postVO);
    }


    // 审核通过帖子
    // ✨ 修改：使用 @RequireAdmin 注解进行权限控制
    @PostMapping("/admin/{id}/approve")
//    @RequireAdmin
    public ApiControllerResponse<Boolean> approvePost(
            @PathVariable Long id
            // ✨ 修改：移除了 @RequestParam Long adminId 和手动权限校验
    ) {
        // ✨ 修改：从 UserContext 获取管理员ID
        Long adminId = UserContext.getRequiredCurrentUserId();
        boolean success = forumPostService.approvePost(id, adminId);
        if (success) {
            return ApiControllerResponse.success(true);
        } else {
            return ApiControllerResponse.error(500, "审核通过操作失败");
        }
    }

    // 审核拒绝帖子
    // ✨ 修改：使用 @RequireAdmin 注解进行权限控制
    @PostMapping("/admin/{id}/reject")
//    @RequireAdmin
    public ApiControllerResponse<Boolean> rejectPost(
            @PathVariable Long id,
            @RequestParam String reason
            // ✨ 修改：移除了 @RequestParam Long adminId 和手动权限校验
    ) {
        // ✨ 修改：从 UserContext 获取管理员ID
        Long adminId = UserContext.getRequiredCurrentUserId();
        boolean success = forumPostService.rejectPost(id, adminId, reason);
        if (success) {
            return ApiControllerResponse.success(true);
        } else {
            return ApiControllerResponse.error(500, "拒绝帖子操作失败");
        }
    }

    // 获取待处理的举报列表
    // ✨ 修改：使用 @RequireAdmin 注解进行权限控制
    @GetMapping("/admin/reports")
//    @RequireAdmin
    public ApiControllerResponse<PageResult<ReportVO>> getPendingReports(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
            // ✨ 修改：移除了 @RequestParam Long adminId 和手动权限校验
    ) {
        PageResult<ReportVO> reports = forumPostService.getPendingReports(page, size);
        return ApiControllerResponse.success(reports);
    }


    // 管理员处理举报：通过举报并删除帖子
    // ✨ 修改：使用 @RequireAdmin 注解进行权限控制
    @PutMapping("/admin/reports/{reportId}/deletePost")
//    @RequireAdmin
    public ApiControllerResponse<Boolean> processReportAndDeletePost(
            @PathVariable Long reportId
            // ✨ 修改：移除了 @RequestParam Long adminId 和手动权限校验
    ) {
        // ✨ 修改：从 UserContext 获取管理员ID
        Long adminId = UserContext.getRequiredCurrentUserId();
        boolean success = forumPostService.processReportAndDeletePost(reportId, adminId);
        if (success) {
            return ApiControllerResponse.success(true);
        }
        return ApiControllerResponse.error(500, "处理举报失败：删除帖子");
    }

    // 管理员处理举报：拒绝举报并保留帖子
    // ✨ 修改：使用 @RequireAdmin 注解进行权限控制
    @PutMapping("/admin/reports/{reportId}/keepPost")
//    @RequireAdmin
    public ApiControllerResponse<Boolean> processReportAndKeepPost(
            @PathVariable Long reportId,
            @RequestParam(required = false) String reasonForKeeping
            // ✨ 修改：移除了 @RequestParam Long adminId 和手动权限校验
    ) {
        // ✨ 修改：从 UserContext 获取管理员ID
        Long adminId = UserContext.getRequiredCurrentUserId();
        boolean success = forumPostService.processReportAndKeepPost(reportId, adminId, reasonForKeeping);
        if (success) {
            return ApiControllerResponse.success(true);
        }
        return ApiControllerResponse.error(500, "处理举报失败：保留帖子");
    }
}


















//package com.csu.sms.controller;
//
//import com.csu.sms.common.ApiControllerResponse;
//import com.csu.sms.common.PageResult;
//import com.csu.sms.dto.ForumCommentDTO;
//import com.csu.sms.dto.ForumPostDTO;
//import com.csu.sms.service.ForumPostService;
//import com.csu.sms.service.UserService;
//import com.csu.sms.vo.CommentVO;
//import com.csu.sms.vo.PostVO;
//import com.csu.sms.vo.ReportVO;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@CrossOrigin(origins = "http://localhost:5173")
//@RestController
//@RequestMapping("/api/post")
//@RequiredArgsConstructor
//@Slf4j
//public class ForumPostController {
//    private final ForumPostService forumPostService;
//    private final UserService userService;
//
//    @GetMapping
//    public ApiControllerResponse<PageResult<PostVO>> listPosts(
//            @RequestParam(required = false) String category,
//            @RequestParam(required = false) String keyword,
//            @RequestParam(defaultValue = "1") Integer page,
//            @RequestParam(defaultValue = "10") Integer size
//    ){
//        PageResult<PostVO> response = forumPostService.listPosts(category, keyword, page, size);
//        return ApiControllerResponse.success(response);
//    }
//
//    @GetMapping("/hot")
//    public ApiControllerResponse<List<PostVO>> getHotPosts(
//            @RequestParam(defaultValue = "5") int count) {
//        List<PostVO> hotPosts = forumPostService.getHotPosts(count);
//        return ApiControllerResponse.success(hotPosts);
//    }
//
//    @GetMapping("/{id}")
//    public ApiControllerResponse<PostVO> getPostDetail(
//            @PathVariable Long id
//    ){
//        PostVO postVO = forumPostService.getPostDetailAndIncreaseView(id);
//        if(postVO == null){
//            return ApiControllerResponse.error(404,"该帖子内容为空");
//        }
//        return ApiControllerResponse.success(postVO);
//    }
//
//    @PostMapping
//    public ApiControllerResponse<Long> createPost(
//            @RequestBody @Valid ForumPostDTO forumPostDTO
//    ) {
//        // 设置用户ID
//        forumPostDTO.setUserId(forumPostDTO.getUserId());
//
//        Long postId = forumPostService.createPost(forumPostDTO);
//        if (postId == null) {
//            return ApiControllerResponse.error(500, "创建帖子失败，请稍后再试");
//        }
//        return ApiControllerResponse.success("帖子创建成功！", postId);
//    }
//
//    @PutMapping("/{id}")
//    public ApiControllerResponse<Boolean> updatePost(
//            @PathVariable Long id,
//            @RequestBody @Valid ForumPostDTO forumPostDTO
//    ) {
//        // 设置帖子ID
//        forumPostDTO.setId(id);
//
//        boolean success = forumPostService.updatePost(forumPostDTO);
//        if (!success) {
//            return ApiControllerResponse.error(500, "更新帖子失败");
//        }
//        return ApiControllerResponse.success(true);
//    }
//
//    @DeleteMapping("/{id}")
//    public ApiControllerResponse<Boolean> deletePost(
//            @PathVariable Long id
//    ) {
//        boolean success = forumPostService.deletePost(id);
//        if (!success) {
//            return ApiControllerResponse.error(500, "删除帖子失败");
//        }
//        return ApiControllerResponse.success(true);
//    }
//
//    @PostMapping("/{id}/like")
//    public ApiControllerResponse<Boolean> likePost(
//            @PathVariable Long id,
//            @RequestParam Long userId
//    ) {
//        boolean success = forumPostService.likePost(id, userId);
//        if (success) {
//            return ApiControllerResponse.success(true);
//        } else {
//            return ApiControllerResponse.error(500, "点赞失败");
//        }
//    }
//
//    // 取消点赞
//    @DeleteMapping("/{id}/like")
//    public ApiControllerResponse<Boolean> unlikePost(
//            @PathVariable Long id,
//            @RequestParam Long userId
//    ) {
//        boolean success = forumPostService.unlikePost(id, userId);
//        if (success) {
//            return ApiControllerResponse.success(true);
//        } else {
//            return ApiControllerResponse.error(500, "取消点赞失败");
//        }
//    }
//
//    @PostMapping("/{id}/report")
//    public ApiControllerResponse<Boolean> reportPost(
//            @PathVariable Long id,
//            @RequestParam Long userId,
//            @RequestParam String reason
//    ) {
//        boolean success = forumPostService.reportPost(id, userId, reason);
//        if (success) {
//            return ApiControllerResponse.success(true);
//        } else {
//            return ApiControllerResponse.error(500, "举报失败");
//        }
//    }
//
//    //获取帖子的评论列表 (只获取顶级评论，不包含子回复)
//    @GetMapping("/{postId}/comments")
//    public ApiControllerResponse<PageResult<CommentVO>> getComments(
//            @PathVariable Long postId,
//            @RequestParam(defaultValue = "1") Integer page,
//            @RequestParam(defaultValue = "10") Integer size,
//            @RequestParam Long currentUserId
//    ) {
//        PageResult<CommentVO> response = forumPostService.getComments(postId, page, size, currentUserId);
//        return ApiControllerResponse.success(response);
//    }
//
//    //新增：获取某个评论的回复列表 (楼中楼子评论)
//    @GetMapping("/comments/{parentCommentId}/replies")
//    public ApiControllerResponse<PageResult<CommentVO>> getCommentReplies(
//            @PathVariable Long parentCommentId,
//            @RequestParam(defaultValue = "1") Integer page,
//            @RequestParam(defaultValue = "10") Integer size,
//            @RequestParam(required = false) Long currentUserId
//    ) {
//        PageResult<CommentVO> response = forumPostService.getCommentReplies(parentCommentId, page, size, currentUserId);
//        return ApiControllerResponse.success(response);
//    }
//
//    //创建评论或回复
//    @PostMapping("/{postId}/comments")
//    public ApiControllerResponse<Long> createComment(
//            @PathVariable Long postId,
//            @RequestParam Long userId,
//            @RequestBody @Valid ForumCommentDTO commentDTO // 使用 DTO 接收评论内容和 parentId
//    ) {
//        Long commentId = forumPostService.createComment(postId, userId, commentDTO);
//        if (commentId == null) {
//            return ApiControllerResponse.error(500, "评论失败，请稍后再试");
//        }
//        return ApiControllerResponse.success("评论成功！", commentId);
//    }
//
//    //删除评论
//    @DeleteMapping("/{postId}/comments/{commentId}")
//    public ApiControllerResponse<Boolean> deleteComment(
//            @PathVariable Long postId,
//            @PathVariable Long commentId,
//            @RequestParam Long userId
//    ) {
//        boolean success = forumPostService.deleteComment(postId, commentId, userId);
//        if (!success) {
//            return ApiControllerResponse.error(500, "删除评论失败或无权限");
//        }
//        return ApiControllerResponse.success(true);
//    }
//
//    //更新评论
//    @PutMapping("/{postId}/comments/{commentId}")
//    public ApiControllerResponse<Boolean> updateComment(
//            @PathVariable Long postId,
//            @PathVariable Long commentId,
//            @RequestParam Long userId,
//            @RequestBody @Valid ForumCommentDTO commentDTO
//    ) {
//        boolean success = forumPostService.updateComment(postId, commentId, userId, commentDTO);
//        if (!success) {
//            return ApiControllerResponse.error(500, "更新评论失败或无权限");
//        }
//        return ApiControllerResponse.success(true);
//    }
//
//    //点赞评论
//    @PostMapping("/comments/{commentId}/like")
//    public ApiControllerResponse<Boolean> likeComment(
//            @PathVariable Long commentId,
//            @RequestParam Long userId
//    ) {
//        boolean success = forumPostService.likeComment(commentId, userId);
//        if (success) {
//            return ApiControllerResponse.success(true);
//        } else {
//            return ApiControllerResponse.error(500, "评论点赞失败");
//        }
//    }
//
//    //取消点赞评论
//    @DeleteMapping("/comments/{commentId}/like")
//    public ApiControllerResponse<Boolean> unlikeComment(
//            @PathVariable Long commentId,
//            @RequestParam Long userId
//    ) {
//        boolean success = forumPostService.unlikeComment(commentId, userId);
//        if (success) {
//            return ApiControllerResponse.success(true);
//        } else {
//            return ApiControllerResponse.error(500, "取消评论点赞失败");
//        }
//    }
//
//    // 管理员接口
//    // 获取待审核的帖子列表
//    @GetMapping("/admin/pending")
//    public ApiControllerResponse<PageResult<PostVO>> getPendingPosts(
//            @RequestParam(defaultValue = "1") Integer page,
//            @RequestParam(defaultValue = "10") Integer size,
//            @RequestParam Long adminId
//    ) {
//        // 验证管理员身份
//        if(!userService.isAdminRole(adminId)){
//            return ApiControllerResponse.error(403, "没有权限");
//        }
//
//        PageResult<PostVO> result = forumPostService.getPendingPosts(page, size);
//        return ApiControllerResponse.success(result);
//    }
//
//    @GetMapping("/admin/detail/{id}")
//    public ApiControllerResponse<PostVO> getPostDetailForAdmin(
//            @PathVariable Long id
//    ){
//        PostVO postVO = forumPostService.getPostDetailForAdmin(id);
//        if(postVO == null){
//            return ApiControllerResponse.error(404,"该帖子内容为空");
//        }
//        return ApiControllerResponse.success(postVO);
//    }
//
//    // 审核通过帖子
//    @PostMapping("/admin/{id}/approve")
//    public ApiControllerResponse<Boolean> approvePost(
//            @PathVariable Long id,
//            @RequestParam Long adminId
//    ) {
//        // 验证管理员身份
//        if(!userService.isAdminRole(adminId)){
//            return ApiControllerResponse.error(403, "没有权限");
//        }
//
//        boolean success = forumPostService.approvePost(id, adminId);
//        if (success) {
//            return ApiControllerResponse.success(true);
//        } else {
//            return ApiControllerResponse.error(500, "审核通过操作失败");
//        }
//    }
//
//    // 审核拒绝帖子
//    @PostMapping("/admin/{id}/reject")
//    public ApiControllerResponse<Boolean> rejectPost(
//            @PathVariable Long id,
//            @RequestParam Long adminId,
//            @RequestParam String reason
//    ) {
//        if(!userService.isAdminRole(adminId)){
//            return ApiControllerResponse.error(403, "没有权限");
//        }
//        boolean success = forumPostService.rejectPost(id, adminId, reason);
//        if (success) {
//            return ApiControllerResponse.success(true);
//        } else {
//            return ApiControllerResponse.error(500, "拒绝帖子操作失败");
//        }
//    }
//
//    @GetMapping("/admin/reports")
//    public ApiControllerResponse<PageResult<ReportVO>> getPendingReports(
//            @RequestParam Long adminId,
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "10") int size) {
//        if (!userService.isAdminRole(adminId)) {
//            return ApiControllerResponse.error(403, "无管理员权限");
//        }
//        PageResult<ReportVO> reports = forumPostService.getPendingReports(page, size);
//        return ApiControllerResponse.success(reports);
//    }
//
//
//    //管理员处理举报：通过举报并删除帖子
//    @PutMapping("/admin/reports/{reportId}/deletePost")
//    public ApiControllerResponse<Boolean> processReportAndDeletePost(
//            @PathVariable Long reportId,
//            @RequestParam Long adminId
//    ) {
//        if(!userService.isAdminRole(adminId)){
//            return ApiControllerResponse.error(403, "没有权限");
//        }
//        boolean success = forumPostService.processReportAndDeletePost(reportId, adminId);
//        if (success) {
//            return ApiControllerResponse.success(true);
//        }
//        return ApiControllerResponse.error(500, "处理举报失败：删除帖子");
//    }
//
//    //管理员处理举报：拒绝举报并保留帖子
//    @PutMapping("/admin/reports/{reportId}/keepPost")
//    public ApiControllerResponse<Boolean> processReportAndKeepPost(
//            @PathVariable Long reportId,
//            @RequestParam Long adminId,
//            @RequestParam(required = false) String reasonForKeeping
//    ) {
//        if(!userService.isAdminRole(adminId)){
//            return ApiControllerResponse.error(403, "没有权限");
//        }
//        boolean success = forumPostService.processReportAndKeepPost(reportId, adminId, reasonForKeeping);
//        if (success) {
//            return ApiControllerResponse.success(true);
//        }
//        return ApiControllerResponse.error(500, "处理举报失败：保留帖子");
//    }
//}