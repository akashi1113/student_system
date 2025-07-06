package com.csu.sms.persistence;

import com.csu.sms.model.post.ForumComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ForumCommentDao {
    int countCommentsByPostId(@Param("postId") Long postId);

    List<ForumComment> findCommentsByPostId(@Param("postId") Long postId,@Param("offset") int offset,@Param("size") Integer size);

    ForumComment findById(@Param("id") Long id);

    int insertComment(ForumComment comment);

    int updateCommentStatus(@Param("commentId") Long commentId,@Param("status") int status);

    int updateComment(ForumComment comment);

    void incrementLikeCount(@Param("commentId") Long commentId);

    void decrementLikeCount(@Param("commentId") Long commentId);

    // 查询某个父评论下的回复总数
    int countRepliesByParentId(@Param("parentId") Long parentId);

    // 查询某个父评论下的回复列表
    List<ForumComment> findRepliesByParentId(@Param("parentId") Long parentId, @Param("offset") int offset, @Param("size") int size);
}
