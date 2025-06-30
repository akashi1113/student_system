package com.csu.sms.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostLikeDao {
    int insertPostLike(@Param("postId") Long postId,
                       @Param("userId") Long userId);

    int deletePostLike(@Param("postId") Long postId,
                       @Param("userId") Long userId);
}
