package com.csu.sms.service;

import com.csu.sms.common.PageResult;
import com.csu.sms.dto.ForumCommentDTO;
import com.csu.sms.dto.ForumPostDTO;
import com.csu.sms.vo.CommentVO;
import com.csu.sms.vo.PostVO;
import com.csu.sms.vo.ReportVO;

public interface ForumPostService {
    PageResult<ReportVO> getPendingReports(int page, int size);

    PageResult<PostVO> listPosts(String category, String keyword, Integer page, Integer size);
    PostVO getPostDetailAndIncreaseView(Long id);
    Long createPost(ForumPostDTO forumPostDTO);
    boolean updatePost(ForumPostDTO forumPostDTO);
    boolean deletePost(Long id);
    boolean likePost(Long postId, Long userId);
    boolean unlikePost(Long postId, Long userId);

    // 管理员功能
    boolean approvePost(Long postId, Long adminId);
    boolean rejectPost(Long postId, Long adminId, String reason);
    PageResult<PostVO> getPendingPosts(Integer page, Integer size);
    PostVO getPostDetailForAdmin(Long id);

    // 举报功能
    boolean reportPost(Long reportId, Long userId, String reason);

    boolean processReportAndDeletePost(Long reportId, Long adminId);
    boolean processReportAndKeepPost(Long reportId, Long adminId, String reasonForKeeping);

    //评论功能
    PageResult<CommentVO> getComments(Long postId, Integer page, Integer size, Long currentUserId);
    // 获取某个评论的回复列表 (楼中楼)
    PageResult<CommentVO> getCommentReplies(Long parentCommentId, Integer page, Integer size, Long currentUserId);
    Long createComment(Long postId, Long userId, ForumCommentDTO commentDTO);
    boolean deleteComment(Long postId, Long commentId, Long userId);
    boolean updateComment(Long postId, Long commentId, Long userId, ForumCommentDTO commentDTO);

    //评论点赞功能
    boolean likeComment(Long commentId, Long userId);
    boolean unlikeComment(Long commentId, Long userId);
}
