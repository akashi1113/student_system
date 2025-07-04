package com.csu.sms.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentLikeDao {
    int countCommentLikes(@Param("commentId") Long commentId);

    List<Long> findUserLikedCommentIds(@Param("currentUserId") Long currentUserId,@Param("commentIds") List<Long> commentIds);

    int insertCommentLike(@Param("commentId") Long commentId,@Param("userId") Long userId);

    int deleteCommentLike(@Param("commentId") Long commentId,@Param("userId") Long userId);

    int checkUserLiked(@Param("commentId") Long commentId,@Param("userId") Long userId);
}
