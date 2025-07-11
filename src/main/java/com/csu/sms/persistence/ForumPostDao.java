package com.csu.sms.persistence;

import com.csu.sms.model.post.ForumPost;
import com.csu.sms.model.enums.PostStatus;
import com.csu.sms.vo.PostVO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ForumPostDao {
    int countPosts(@Param("category") String category,
                   @Param("keyword") String keyword,
                   @Param("postStatus") PostStatus postStatus);

    List<ForumPost> findPostsByPage(@Param("category") String category,
                                    @Param("keyword") String keyword,
                                    @Param("postStatus") PostStatus postStatus,
                                    @Param("offset") int offset,
                                    @Param("size") Integer size);

    ForumPost findById(@Param("id") Long id);

    int insertPost(ForumPost post);

    int updatePost(ForumPost post);

    int updatePostStatus(@Param("id") Long id,
                         @Param("postStatus") PostStatus postStatus);

    void incrementLikeCount(@Param("postId") Long postId);

    void decrementLikeCount(@Param("postId") Long postId);

    void updateViewCount(@Param("postId") Long postId,
                         @Param("viewCount") Integer viewCount);

    void incrementCommentCount(@Param("postId") Long postId);

    void decrementCommentCount(@Param("postId") Long postId);

    List<ForumPost> findPostsByIds(@Param("postIds") List<Long> postIds);

    List<ForumPost> findHotPages(@Param("count") Integer count);

    List<Map<String, Object>> getUserPostCategories(@Param("userId") Long userId);

    List<ForumPost> getTopPostsByCategory(@Param("category") String category,@Param("i") int i);
}
