package com.csu.sms.service.impl;

import com.csu.sms.common.PageResult;
import com.csu.sms.dto.ForumCommentDTO;
import com.csu.sms.dto.ForumPostDTO;
import com.csu.sms.model.post.ForumComment;
import com.csu.sms.model.post.ForumPost;
import com.csu.sms.model.post.PostReport;
import com.csu.sms.model.user.User;
import com.csu.sms.model.enums.PostStatus;
import com.csu.sms.model.enums.ReportStatus;
import com.csu.sms.model.enums.UserRole;
import com.csu.sms.persistence.*;
import com.csu.sms.service.ForumPostService;
import com.csu.sms.service.NotificationService;
import com.csu.sms.vo.CommentVO;
import com.csu.sms.vo.PostVO;
import com.csu.sms.vo.ReportVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
// import org.springframework.cache.annotation.CacheEvict; // 移除此导入
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
// import java.util.concurrent.TimeUnit; // 移除此导入
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForumPostServiceImpl implements ForumPostService {
    private final ForumPostDao forumPostDao;
    private final UserDao userDao;
    private final PostLikeDao postLikeDao;
    private final PostReportDao postReportDao;
    private final ForumCommentDao forumCommentDao;
    private final CommentLikeDao commentLikeDao;
    private final NotificationService notificationService;

    // 定义通知类型常量
    private static final int NOTIFICATION_TYPE_POST = 2; // 帖子相关通知
    private static final int NOTIFICATION_TYPE_COMMENT = 3; // 评论相关通知

    @Override
    public List<PostVO> getHotPosts(int count){
        // 查询总记录数
        int total = forumPostDao.countPosts(null, null, PostStatus.PUBLISHED);

        // 如果没有记录，返回空结果
        if (total == 0) {
            return new ArrayList<>();
        }

        // 查询帖子列表
        List<ForumPost> posts = forumPostDao.findHotPages(count);

        if (posts.isEmpty()) {
            throw new RuntimeException("No posts found.");
        }

        // 获取所有用户ID
        List<Long> userIds = posts.stream()
                .map(ForumPost::getUserId)
                .distinct()
                .toList();


        // 批量查询用户信息
        List<User> users = userDao.findUsersByIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 转换为VO
        List<PostVO> voList = posts.stream().map(post -> {
            PostVO vo = new PostVO();
            BeanUtils.copyProperties(post, vo);

            // 设置状态描述
            vo.setStatus(post.getStatus().getCode());
            vo.setStatusDesc(post.getStatus().getDescription());

            // 设置用户信息
            User user = userMap.get(post.getUserId());
            if (user != null) {
                vo.setUserId(user.getId());
                vo.setUserName(user.getUsername());
                vo.setUserAvatar(user.getAvatar());
            }

            vo.setViewCount(post.getViewCount()); // 直接从数据库ForumPost对象获取
            vo.setLikeCount(post.getLikeCount()); // 直接从数据库ForumPost对象获取

            // 默认未点赞
            vo.setIsLiked(false); // 后面通过fillUserLikedInfo填充实际点赞状态

            return vo;
        }).collect(Collectors.toList());
        return voList;
    }

    @Override
    public PageResult<PostVO> listPosts(String category, String keyword, Integer page, Integer size) {
        // 计算偏移量
        int offset = (page - 1) * size;

        // 查询总记录数
        int total = forumPostDao.countPosts(category, keyword, PostStatus.PUBLISHED);

        // 如果没有记录，返回空结果
        if (total == 0) {
            return PageResult.of(new ArrayList<>(), 0, page, size);
        }

        // 查询帖子列表
        List<ForumPost> posts = forumPostDao.findPostsByPage(category, keyword, PostStatus.PUBLISHED, offset, size);
        if (posts.isEmpty()) {
            return PageResult.of(new ArrayList<>(), total, page, size);
        }

        // 获取所有用户ID
        List<Long> userIds = posts.stream()
                .map(ForumPost::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询用户信息
        List<User> users = userDao.findUsersByIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 转换为VO
        List<PostVO> voList = posts.stream().map(post -> {
            PostVO vo = new PostVO();
            BeanUtils.copyProperties(post, vo);

            // 设置状态描述
            vo.setStatus(post.getStatus().getCode());
            vo.setStatusDesc(post.getStatus().getDescription());

            // 设置用户信息
            User user = userMap.get(post.getUserId());
            if (user != null) {
                vo.setUserId(user.getId());
                vo.setUserName(user.getUsername());
                vo.setUserAvatar(user.getAvatar());
            }

            vo.setViewCount(post.getViewCount()); // 直接从数据库ForumPost对象获取
            vo.setLikeCount(post.getLikeCount()); // 直接从数据库ForumPost对象获取

            // 默认未点赞
            vo.setIsLiked(false); // 后面通过fillUserLikedInfo填充实际点赞状态

            return vo;
        }).collect(Collectors.toList());

        // 创建分页结果
        PageResult<PostVO> result = PageResult.of(voList, total, page, size);

        return result;
    }

    @Override
    public PostVO getPostDetailAndIncreaseView(Long id) {

        ForumPost post = forumPostDao.findById(id);
        if (post == null || post.getStatus() != PostStatus.PUBLISHED) {
            return null;
        }

        // 获取用户信息
        User user = userDao.findById(post.getUserId());

        // 转换为VO
        PostVO detailPostVO = new PostVO();
        BeanUtils.copyProperties(post, detailPostVO);

        // 设置状态描述
        detailPostVO.setStatus(post.getStatus().getCode());
        detailPostVO.setStatusDesc(post.getStatus().getDescription());

        // 设置用户信息
        if (user != null) {
            detailPostVO.setUserId(user.getId());
            detailPostVO.setUserName(user.getUsername());
            detailPostVO.setUserAvatar(user.getAvatar());
        }

        // 默认未点赞
        detailPostVO.setIsLiked(false);

        // 增加浏览量
        // String viewCountKey = String.format(POST_VIEW_COUNT_KEY, id); // 移除

        // 从数据库获取当前浏览量并递增
        Integer viewCount = post.getViewCount() + 1; // 直接从post对象获取并递增

        // 异步更新数据库中的浏览量
        asyncUpdateViewCount(id, viewCount);

        // 更新VO中的浏览量
        detailPostVO.setViewCount(viewCount);

        // 移除更新缓存
        // redisTemplate.opsForValue().set(cacheKey, cachedPost, CACHE_TTL_HOURS, TimeUnit.HOURS);

        return detailPostVO;
    }

    @Override
    public PostVO getPostDetailForAdmin(Long id) {
        ForumPost post = forumPostDao.findById(id);
        if (post == null) {
            return null;
        }

        // 获取用户信息
        User user = userDao.findById(post.getUserId());

        // 转换为VO
        PostVO detailPostVO = new PostVO();
        BeanUtils.copyProperties(post, detailPostVO);

        // 设置状态描述
        detailPostVO.setStatus(post.getStatus().getCode());
        detailPostVO.setStatusDesc(post.getStatus().getDescription());

        // 设置用户信息
        if (user != null) {
            detailPostVO.setUserId(user.getId());
            detailPostVO.setUserName(user.getUsername());
            detailPostVO.setUserAvatar(user.getAvatar());
        }

        // 默认未点赞
        detailPostVO.setIsLiked(false);

        return detailPostVO;
    }

    @Override
    @Transactional
    public Long createPost(ForumPostDTO forumPostDTO) {
        // 创建帖子
        ForumPost post = new ForumPost();
        BeanUtils.copyProperties(forumPostDTO, post);

        // 设置初始值
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setStatus(forumPostDTO.getStatus());
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());

        // 插入数据库
        int result = forumPostDao.insertPost(post);
        if (result <= 0) {
            return null;
        }

        // 移除清除帖子列表缓存
        // clearPostListCache();

        return post.getId();
    }

    @Override
    @Transactional
    // @CacheEvict(value = "postDetail", key = "'id_' + #forumPostDTO.id") // 移除此注解
    public boolean updatePost(ForumPostDTO forumPostDTO) {
        // 查询原帖子
        ForumPost existingPost = forumPostDao.findById(forumPostDTO.getId());
        if (existingPost == null) {
            return false;
        }

        // 检查权限（只有作者或管理员可以修改）
        if (!existingPost.getUserId().equals(forumPostDTO.getUserId())) {
            // 检查是否为管理员
            User user = userDao.findById(existingPost.getUserId());
            if (user == null || user.getRole() != UserRole.ADMIN.getCode()) {
                return false;
            }
        }

        // 更新帖子
        ForumPost post = new ForumPost();
        BeanUtils.copyProperties(forumPostDTO, post);
        post.setUpdateTime(LocalDateTime.now());

        // 保留原有的计数器值
        post.setViewCount(existingPost.getViewCount());
        post.setLikeCount(existingPost.getLikeCount());
        post.setCommentCount(existingPost.getCommentCount());

        int result = forumPostDao.updatePost(post);
        if (result <= 0) {
            return false;
        }

        // 移除清除帖子详情缓存
        // clearPostDetailCache(post.getId());

        // 移除清除帖子列表缓存
        // clearPostListCache();

        return true;
    }

    @Override
    @Transactional
    public boolean deletePost(Long id) {
        // 查询帖子
        ForumPost post = forumPostDao.findById(id);
        if (post == null) {
            return false;
        }

        // 逻辑删除帖子
        post.setStatus(PostStatus.DELETED);
        post.setUpdateTime(LocalDateTime.now());

        int result = forumPostDao.updatePostStatus(id, PostStatus.DELETED);
        if (result <= 0) {
            return false;
        }

        // 移除清除帖子详情缓存
        // clearPostDetailCache(id);

        // 移除清除帖子列表缓存
        // clearPostListCache();

        return true;
    }

    @Override
    @Transactional
    public boolean likePost(Long postId, Long userId) {
        // 查询帖子是否存在
        ForumPost post = forumPostDao.findById(postId);
        if (post == null || post.getStatus() != PostStatus.PUBLISHED) {
            return false;
        }

        // 检查是否已点赞 (直接查询数据库)
        // String userLikedKey = String.format(USER_LIKED_POSTS_KEY, userId); // 移除
        // Boolean isLiked = redisTemplate.opsForSet().isMember(userLikedKey, postId); // 移除
        boolean isLiked = postLikeDao.checkUserLiked(postId, userId) > 0; // 直接从数据库检查

        if (isLiked) { // Boolean.TRUE.equals(isLiked) 变为 isLiked
            return true; // 已点赞，直接返回成功
        }

        // 添加点赞记录
        int result = postLikeDao.insertPostLike(postId, userId);
        if (result <= 0) {
            return false;
        }

        // 移除更新Redis中的点赞记录
        // redisTemplate.opsForSet().add(userLikedKey, postId);

        // 移除更新Redis中的点赞数
        // String likeCountKey = String.format(POST_LIKE_COUNT_KEY, postId);
        // redisTemplate.opsForValue().increment(likeCountKey);

        // 更新帖子点赞数 (直接更新数据库)
        forumPostDao.incrementLikeCount(postId);

        // 移除清除帖子详情缓存
        // clearPostDetailCache(postId);

        return true;
    }

    @Override
    @Transactional
    public boolean unlikePost(Long postId, Long userId) {
        // 查询帖子是否存在
        ForumPost post = forumPostDao.findById(postId);
        if (post == null || post.getStatus() != PostStatus.PUBLISHED) {
            return false;
        }

        // 检查是否已点赞 (直接查询数据库)
        // String userLikedKey = String.format(USER_LIKED_POSTS_KEY, userId); // 移除
        // Boolean isLiked = redisTemplate.opsForSet().isMember(userLikedKey, postId); // 移除
        boolean isLiked = postLikeDao.checkUserLiked(postId, userId) > 0; // 直接从数据库检查

        if (!isLiked) { // Boolean.FALSE.equals(isLiked) 变为 !isLiked
            return true; // 未点赞，直接返回成功
        }

        // 删除点赞记录
        int result = postLikeDao.deletePostLike(postId, userId);
        if (result <= 0) {
            return false;
        }

        // 移除更新Redis中的点赞记录
        // redisTemplate.opsForSet().remove(userLikedKey, postId);

        // 移除更新Redis中的点赞数
        // String likeCountKey = String.format(POST_LIKE_COUNT_KEY, postId);
        // redisTemplate.opsForValue().decrement(likeCountKey);

        // 更新帖子点赞数 (直接更新数据库)
        forumPostDao.decrementLikeCount(postId);

        // 移除清除帖子详情缓存
        // clearPostDetailCache(postId);

        return true;
    }

    @Override
    public PageResult<CommentVO> getComments(Long postId, Integer page, Integer size, Long currentUserId) {
        int total = forumCommentDao.countCommentsByPostId(postId); // 此方法现在只查顶级评论
        if (total == 0) {
            return PageResult.of(new ArrayList<>(), 0, page, size);
        }

        int offset = (page - 1) * size;
        List<ForumComment> comments = forumCommentDao.findCommentsByPostId(postId, offset, size); // 此方法现在只查顶级评论

        if (comments.isEmpty()) {
            return PageResult.of(new ArrayList<>(), total, page, size);
        }

        // 将转换CommentVO的逻辑封装成私有方法，避免重复代码
        return convertCommentsToVOPage(comments, total, page, size, currentUserId);
    }

    // 获取某个评论的回复列表 (楼中楼)
    @Override
    public PageResult<CommentVO> getCommentReplies(Long parentCommentId, Integer page, Integer size, Long currentUserId) {
        // 验证父评论是否存在且有效
        ForumComment parentComment = forumCommentDao.findById(parentCommentId);
        if (parentComment == null || parentComment.getStatus() != 0) { // 假设 0 为正常状态
            log.warn("获取评论回复失败：父评论 {} 不存在或已删除。", parentCommentId);
            return PageResult.of(new ArrayList<>(), 0, page, size);
        }

        int total = forumCommentDao.countRepliesByParentId(parentCommentId);
        if (total == 0) {
            return PageResult.of(new ArrayList<>(), 0, page, size);
        }

        int offset = (page - 1) * size;
        List<ForumComment> replies = forumCommentDao.findRepliesByParentId(parentCommentId, offset, size);

        if (replies.isEmpty()) {
            return PageResult.of(new ArrayList<>(), total, page, size);
        }

        // 复用转换逻辑
        return convertCommentsToVOPage(replies, total, page, size, currentUserId);
    }

    // 辅助方法：将 ForumComment 列表转换为 CommentVO 列表并填充额外信息
    private PageResult<CommentVO> convertCommentsToVOPage(List<ForumComment> comments, int total, Integer page, Integer size, Long currentUserId) {
        List<Long> commentIds = comments.stream().map(ForumComment::getId).collect(Collectors.toList());

        // 获取所有评论者ID
        List<Long> userIds = comments.stream()
                .map(ForumComment::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询用户信息
        Map<Long, User> userMap = userDao.findUsersByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // Set<Object> userLikedCommentIds = Collections.emptySet(); // 移除
        List<Long> userLikedCommentIds = Collections.emptyList(); // 替换为List或Set<Long>

        if (currentUserId != null && !commentIds.isEmpty()) {
            // String userLikedCommentsKey = String.format(USER_LIKED_COMMENTS_KEY, currentUserId); // 移除
            // userLikedCommentIds = redisTemplate.opsForSet().members(userLikedCommentsKey); // 移除
            // if (userLikedCommentIds == null || userLikedCommentIds.isEmpty()) { // 移除
            // 如果Redis没有该用户的评论点赞记录，从数据库加载
            List<Long> dbLikedIds = commentLikeDao.findUserLikedCommentIds(currentUserId, commentIds); // 直接从数据库获取
            // if (!dbLikedIds.isEmpty()) { // 移除
            //     redisTemplate.opsForSet().add(userLikedCommentsKey, dbLikedIds.toArray()); // 移除
            //     redisTemplate.expire(userLikedCommentsKey, CACHE_TTL_HOURS, TimeUnit.HOURS); // 移除
            // }
            userLikedCommentIds = dbLikedIds; // 直接使用数据库结果
            // }
        }
        final Set<Long> finalUserLikedCommentIdsSet = new HashSet<>(userLikedCommentIds); // lambda表达式需要final或effective final变量

        // 转换为 CommentVO
        List<CommentVO> voList = comments.stream().map(comment -> {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(comment, vo);

            User user = userMap.get(comment.getUserId());
            if (user != null) {
                vo.setUserName(user.getUsername());
                vo.setUserAvatar(user.getAvatar());
            }

            // 查询这条评论有多少条子评论，并设置 replyCount
            Integer replyCount = forumCommentDao.countRepliesByParentId(comment.getId());
            vo.setReplyCount(replyCount);

            // 设置评论点赞数 (直接从数据库ForumComment对象的likeCount字段获取)
            // String likeCountKey = String.format(COMMENT_LIKE_COUNT_KEY, comment.getId()); // 移除
            // Integer cachedLikeCount = (Integer) redisTemplate.opsForValue().get(likeCountKey); // 移除
            // if (cachedLikeCount != null) { // 移除
            //     vo.setLikeCount(cachedLikeCount); // 移除
            // } else { // 移除
            // Redis没有，使用DB中的值
            vo.setLikeCount(comment.getLikeCount()); // ForumComment对象自带likeCount
            //     redisTemplate.opsForValue().set(likeCountKey, comment.getLikeCount(), CACHE_TTL_HOURS, TimeUnit.HOURS); // 移除
            // }

            // 设置用户是否点赞
            vo.setIsLiked(currentUserId != null && finalUserLikedCommentIdsSet.contains(comment.getId())); // 之前是 finalUserLikedCommentIds.contains(comment.getId())

            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(voList, total, page, size);
    }

    @Override
    @Transactional
    public Long createComment(Long postId, Long userId, ForumCommentDTO commentDTO) {
        ForumPost post = forumPostDao.findById(postId);
        if (post == null || post.getStatus() != PostStatus.PUBLISHED) {
            log.warn("创建评论失败：帖子 {} 不存在或未发布。", postId);
            return null;
        }

        // 验证父评论是否存在 (如果存在 parentId)
        if (commentDTO.getParentId() != null && commentDTO.getParentId() != 0) {
            ForumComment parentComment = forumCommentDao.findById(commentDTO.getParentId());
            if (parentComment == null || !parentComment.getPostId().equals(postId)) {
                log.warn("创建评论失败：父评论 {} 不存在或不属于帖子 {}.", commentDTO.getParentId(), postId);
                return null;
            }
        }

        ForumComment comment = new ForumComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(commentDTO.getContent());
        comment.setParentId(commentDTO.getParentId()); // 如果是回复，这里会有值
        comment.setLikeCount(0); // 初始点赞数为0
        comment.setStatus(0); // 初始状态，0为正常
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());

        int result = forumCommentDao.insertComment(comment);
        if (result <= 0) {
            log.error("创建评论失败：插入数据库失败，postId: {}, userId: {}", postId, userId);
            return null;
        }

        // 更新帖子评论计数 (直接更新数据库)
        forumPostDao.incrementCommentCount(postId);
        // clearPostDetailCache(postId); // 移除清除帖子详情缓存

        // 发送通知
        // 通知帖子作者
        if (!post.getUserId().equals(userId)) { // 如果评论者不是帖子作者
            String title = "您的帖子有新评论";
            String content = String.format("您的帖子《%s》收到了来自用户 %d 的新评论/回复。",
                    post.getTitle(), userId);
            notificationService.sendNotification(post.getUserId(), title, content, NOTIFICATION_TYPE_POST, postId);
        }
        // 如果是回复，通知父评论作者
        if (comment.getParentId() != null && comment.getParentId() != 0) {
            ForumComment parentComment = forumCommentDao.findById(comment.getParentId());
            if (parentComment != null && !parentComment.getUserId().equals(userId) && !parentComment.getUserId().equals(post.getUserId())) { // 排除给自己回复和帖子作者重复通知
                String title = "您的评论有新回复";
                String content = String.format("您在帖子《%s》中的评论收到了来自用户 %d 的回复。",
                        post.getTitle(), userId);
                notificationService.sendNotification(parentComment.getUserId(), title, content, NOTIFICATION_TYPE_COMMENT, comment.getId());
            }
        }

        log.info("用户 {} 在帖子 {} 创建了评论 {}", userId, postId, comment.getId());
        return comment.getId();
    }

    @Override
    @Transactional
    public boolean deleteComment(Long postId, Long commentId, Long userId) {
        ForumComment comment = forumCommentDao.findById(commentId);
        if (comment == null || !comment.getPostId().equals(postId)) {
            log.warn("删除评论失败：评论 {} 不存在或不属于帖子 {}.", commentId, postId);
            return false;
        }

        ForumPost post = forumPostDao.findById(postId);
        User user = userDao.findById(userId);
        //评论的作者，帖子的作者，管理员才有权限删除评论
        if (!(comment.getUserId().equals(userId) ||
                (post != null && post.getUserId().equals(userId)) ||
                (user != null && user.getRole() == UserRole.ADMIN.getCode()))) {
            log.warn("删除评论失败：用户 {} 没有权限删除评论 {}。", userId, commentId);
            return false;
        }

        // 逻辑删除评论 (更新状态)
        int result = forumCommentDao.updateCommentStatus(commentId, 1);
        if (result <= 0) {
            log.error("删除评论失败：更新数据库状态失败，commentId: {}", commentId);
            return false;
        }

        // 更新帖子评论计数 (直接更新数据库)
        forumPostDao.decrementCommentCount(postId);
        // clearPostDetailCache(postId); // 移除清除帖子详情缓存

        // 移除清除评论点赞相关缓存 (如果评论被删除，其点赞数据也应清除)
        // String commentLikeCountKey = String.format(COMMENT_LIKE_COUNT_KEY, commentId);
        // redisTemplate.delete(commentLikeCountKey);

        log.info("用户 {} 删除了评论 {}", userId, commentId);
        return true;
    }

    @Override
    @Transactional
    public boolean updateComment(Long postId, Long commentId, Long userId, ForumCommentDTO commentDTO) {
        ForumComment comment = forumCommentDao.findById(commentId);
        if (comment == null || !comment.getPostId().equals(postId)) {
            log.warn("更新评论失败：评论 {} 不存在或不属于帖子 {}.", commentId, postId);
            return false;
        }

        User user = userDao.findById(userId);
        //只有管理员或者评论的作者才有权限修改评论
        if (!(comment.getUserId().equals(userId) ||
                (user != null && user.getRole() == UserRole.ADMIN.getCode()))) {
            log.warn("更新评论失败：用户 {} 没有权限修改评论 {}。", userId, commentId);
            return false;
        }

        comment.setContent(commentDTO.getContent());
        comment.setUpdateTime(LocalDateTime.now());
        int result = forumCommentDao.updateComment(comment);
        if (result <= 0) {
            log.error("更新评论失败：更新数据库失败，commentId: {}", commentId);
            return false;
        }
        log.info("用户 {} 更新了评论 {}", userId, commentId);
        return true;
    }

    @Override
    @Transactional
    public boolean likeComment(Long commentId, Long userId) {
        ForumComment comment = forumCommentDao.findById(commentId);
        if (comment == null || comment.getStatus() != 0) {
            log.warn("点赞评论失败：评论 {} 不存在或不可点赞。", commentId);
            return false;
        }

        // 直接查询数据库
        // String userLikedKey = String.format(USER_LIKED_COMMENTS_KEY, userId); // 移除
        // Boolean isLiked = redisTemplate.opsForSet().isMember(userLikedKey, commentId); // 移除
        boolean isLiked = commentLikeDao.checkUserLiked(commentId, userId) > 0; // 假设有此方法检查用户是否已点赞

        if (isLiked) { // Boolean.TRUE.equals(isLiked) 变为 isLiked
            log.debug("用户 {} 已点赞评论 {}。", userId, commentId);
            return true; // 已点赞，直接返回成功
        }

        int result = commentLikeDao.insertCommentLike(commentId, userId);
        if (result <= 0) {
            log.error("点赞评论失败：插入点赞记录失败，commentId: {}, userId: {}", commentId, userId);
            return false;
        }

        // 移除更新Redis中的点赞记录
        // redisTemplate.opsForSet().add(userLikedKey, commentId);
        // redisTemplate.expire(userLikedKey, CACHE_TTL_HOURS, TimeUnit.HOURS);

        // 移除更新Redis中的点赞数
        // String likeCountKey = String.format(COMMENT_LIKE_COUNT_KEY, commentId);
        // redisTemplate.opsForValue().increment(likeCountKey);
        // redisTemplate.expire(likeCountKey, CACHE_TTL_HOURS, TimeUnit.HOURS);

        // 更新评论点赞数 (直接更新数据库)
        forumCommentDao.incrementLikeCount(commentId);

        log.info("用户 {} 点赞了评论 {}。", userId, commentId);
        return true;
    }

    @Override
    @Transactional
    public boolean unlikeComment(Long commentId, Long userId) {
        ForumComment comment = forumCommentDao.findById(commentId);
        if (comment == null || comment.getStatus() != 0) {
            log.warn("取消点赞评论失败：评论 {} 不存在或不可操作。", commentId);
            return false;
        }

        // 直接查询数据库
        // String userLikedKey = String.format(USER_LIKED_COMMENTS_KEY, userId); // 移除
        // Boolean isLiked = redisTemplate.opsForSet().isMember(userLikedKey, commentId); // 移除
        boolean isLiked = commentLikeDao.checkUserLiked(commentId, userId) > 0; // 假设有此方法检查用户是否已点赞

        if (!isLiked) { // Boolean.FALSE.equals(isLiked) 变为 !isLiked
            log.debug("用户 {} 未点赞评论 {}。", userId, commentId);
            return true; // 未点赞，直接返回成功
        }

        int result = commentLikeDao.deleteCommentLike(commentId, userId);
        if (result <= 0) {
            log.error("取消点赞评论失败：删除点赞记录失败，commentId: {}, userId: {}", commentId, userId);
            return false;
        }

        // 移除更新Redis中的点赞记录
        // redisTemplate.opsForSet().remove(userLikedKey, commentId);

        // 移除更新Redis中的点赞数
        // String likeCountKey = String.format(COMMENT_LIKE_COUNT_KEY, commentId);
        // redisTemplate.opsForValue().decrement(likeCountKey);

        // 更新评论点赞数 (直接更新数据库)
        forumCommentDao.decrementLikeCount(commentId);

        log.info("用户 {} 取消点赞了评论 {}。", userId, commentId);
        return true;
    }

    @Override
    @Transactional
    public boolean reportPost(Long postId, Long userId, String reason) {
        // 查询帖子是否存在
        ForumPost post = forumPostDao.findById(postId);
        if (post == null || post.getStatus() != PostStatus.PUBLISHED) {
            log.warn("举报失败：帖子 {} 不存在或未发布。", postId);
            return false;
        }

        // 插入举报记录
        int result = postReportDao.insertPostReport(postId, userId, reason);
        if (result <= 0) {
            log.error("举报失败：插入举报记录失败，postId: {}, userId: {}", postId, userId);
            return false;
        }

        // 举报成功，立即发送通知给管理员
        String title = "新的帖子举报通知";
        String content = String.format("用户ID：%d举报了帖子《%s》(ID：%d)，原因：%s。请尽快处理。",
                userId, post.getTitle(), postId, reason);
        List<User> admins = userDao.findUsersByRole(UserRole.ADMIN.getCode());
        for (User admin : admins) {
            notificationService.sendNotification(admin.getId(), title, content, 2, postId);
        }
        log.info("帖子 {} 已被用户 {} 举报，原因：{}。通知已发送给管理员。", postId, userId, reason);
        return true;
    }

    @Override
    @Transactional
    public boolean approvePost(Long postId, Long adminId) {
        // 验证管理员身份
        User admin = userDao.findById(adminId);
        if (admin == null || admin.getRole() != UserRole.ADMIN.getCode()) {
            return false;
        }

        // 查询帖子
        ForumPost post = forumPostDao.findById(postId);
        if (post == null || post.getStatus() != PostStatus.PENDING_REVIEW) {
            return false;
        }

        // 更新帖子状态
        int result = forumPostDao.updatePostStatus(postId, PostStatus.PUBLISHED);
        if (result <= 0) {
            return false;
        }

        // 移除清除帖子详情缓存
        // clearPostDetailCache(postId);

        // 移除清除帖子列表缓存
        // clearPostListCache();

        return true;
    }

    @Override
    @Transactional
    public boolean rejectPost(Long postId, Long adminId, String reason) {
        // 验证管理员身份
        User admin = userDao.findById(adminId);
        if (admin == null || admin.getRole() != UserRole.ADMIN.getCode()) {
            return false;
        }

        // 查询帖子
        ForumPost post = forumPostDao.findById(postId);
        if (post == null || post.getStatus() != PostStatus.PENDING_REVIEW) {
            return false;
        }

        // 更新帖子状态
        int result = forumPostDao.updatePostStatus(postId, PostStatus.DELETED);
        if (result <= 0) {
            return false;
        }

        // 发送通知给用户，说明拒绝原因
        String title = "您的帖子未通过审核";
        String content = String.format("您的帖子《%s》未通过审核，原因：%s", post.getTitle(), reason);
        notificationService.sendNotification(post.getUserId(), title, content, 2, postId);

        // 移除清除帖子详情缓存
        // clearPostDetailCache(postId);

        // 移除清除帖子列表缓存
        // clearPostListCache();

        return true;
    }


    @Override
    public PageResult<PostVO> getPendingPosts(Integer page, Integer size) {
        // 计算偏移量
        int offset = (page - 1) * size;

        // 查询总记录数
        int total = forumPostDao.countPosts(null, null, PostStatus.PENDING_REVIEW);

        // 如果没有记录，返回空结果
        if (total == 0) {
            return PageResult.of(new ArrayList<>(), 0, page, size);
        }

        // 查询帖子列表
        List<ForumPost> posts = forumPostDao.findPostsByPage(null, null, PostStatus.PENDING_REVIEW, offset, size);
        if (posts.isEmpty()) {
            return PageResult.of(new ArrayList<>(), total, page, size);
        }

        // 获取所有用户ID
        List<Long> userIds = posts.stream()
                .map(ForumPost::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询用户信息
        List<User> users = userDao.findUsersByIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 转换为VO
        List<PostVO> voList = posts.stream().map(post -> {
            PostVO vo = new PostVO();
            BeanUtils.copyProperties(post, vo);

            // 设置状态描述
            vo.setStatus(post.getStatus().getCode());
            vo.setStatusDesc(post.getStatus().getDescription());

            // 设置用户信息
            User user = userMap.get(post.getUserId());
            if (user != null) {
                vo.setUserId(user.getId());
                vo.setUserName(user.getUsername());
                vo.setUserAvatar(user.getAvatar());
            }

            // 默认未点赞
            vo.setIsLiked(false);

            return vo;
        }).collect(Collectors.toList());

        // 创建分页结果
        return PageResult.of(voList, total, page, size);
    }

    // 异步更新浏览量
    private void asyncUpdateViewCount(Long postId, Integer viewCount) {
        // 这里可以使用线程池或消息队列来异步更新
        // 为了简单，这里使用一个新线程
        new Thread(() -> {
            try {
                forumPostDao.updateViewCount(postId, viewCount);
            } catch (Exception e) {
                log.error("Failed to update view count for post {}: {}", postId, e.getMessage());
            }
        }).start();
    }

    // 移除清除帖子详情缓存
    // private void clearPostDetailCache(Long postId) {
    //     String cacheKey = String.format(POST_DETAIL_CACHE_KEY, postId);
    //     redisTemplate.delete(cacheKey);
    // }

    // 移除清除帖子列表缓存
    // private void clearPostListCache() {
    //     Set<String> keys = redisTemplate.keys("post:list:*");
    //     if (keys != null && !keys.isEmpty()) {
    //         redisTemplate.delete(keys);
    //     }
    // }
    @Override
    @Transactional
    public PageResult<ReportVO> getPendingReports(int page, int size){
        // 计算偏移量
        int offset = (page - 1) * size;

        // 查询总记录数
        int total = postReportDao.countReports(ReportStatus.PENDING);

        // 如果没有记录，返回空结果
        if (total == 0) {
            return PageResult.of(new ArrayList<>(), 0, page, size);
        }

        // 查询举报列表
        List<PostReport> reports = postReportDao.findReportsByPage(ReportStatus.PENDING, offset, size);
        if (reports.isEmpty())
            return PageResult.of(new ArrayList<>(), total, page, size);
        // 获取所有用户ID
        List<Long> userIds = reports.stream()
               .map(PostReport::getReporterId)
               .distinct()
               .collect(Collectors.toList());
        // 批量查询用户信息
        List<User> users = userDao.findUsersByIds(userIds);
        Map<Long, User> userMap = users.stream()
              .collect(Collectors.toMap(User::getId, user -> user));
        // 获取所有帖子ID
        List<Long> postIds = reports.stream()
              .map(PostReport::getPostId)
              .distinct()
              .collect(Collectors.toList());
        // 批量查询帖子信息
        List<ForumPost> posts = forumPostDao.findPostsByIds(postIds);
        Map<Long, ForumPost> postMap = posts.stream()
             .collect(Collectors.toMap(ForumPost::getId, post -> post));
        // 转换为VO
        List<ReportVO> voList = reports.stream().map(report -> {
            ReportVO vo = new ReportVO();
            BeanUtils.copyProperties(report, vo);
            vo.setReportTime(report.getCreateTime());
            // 设置用户信息
            User user = userMap.get(report.getReporterId());
            if (user!= null) {
                vo.setReporterName(user.getUsername());
            }
            // 设置帖子信息
            ForumPost post = postMap.get(report.getPostId());
            if (post != null) {
                vo.setPostTitle(post.getTitle());
            }
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(voList, total, page, size);
    }


    @Override
    @Transactional
    public boolean processReportAndDeletePost(Long reportId, Long adminId) {
        // 1. 验证管理员身份
        User admin = userDao.findById(adminId);
        if (admin == null || admin.getRole() != UserRole.ADMIN.getCode()) {
            log.warn("处理举报失败：用户 {} 不是管理员。", adminId);
            return false;
        }

        // 2. 查询举报记录
        PostReport report = postReportDao.findReportByReportId(reportId);
        if (report == null || report.getStatus() != ReportStatus.PENDING) {
            log.warn("处理举报失败：举报记录 {} 不存在或状态不是待处理。", reportId);
            return false;
        }

        // 3. 查询被举报的帖子
        Long postId = report.getPostId();
        ForumPost post = forumPostDao.findById(postId);
        if (post == null) {
            log.warn("处理举报失败：关联帖子 {} 不存在。", postId);
            // 即使帖子不存在，举报记录也需要更新为已处理
            postReportDao.updateReportStatus(reportId, ReportStatus.PROCESSED, adminId, LocalDateTime.now());
            return true; // 认为处理成功，因为原帖子已不存在
        }

        // 4. 更新帖子状态为删除
        int postUpdateResult = forumPostDao.updatePostStatus(postId, PostStatus.DELETED);
        if (postUpdateResult <= 0) {
            log.error("处理举报失败：更新帖子 {} 状态为删除失败。", postId);
            return false;
        }
        log.info("帖子《{}》(ID:{}) 已被管理员 {} 删除，原因由举报产生。", post.getTitle(), postId, adminId);

        // 5. 更新举报记录状态
        int reportUpdateResult = postReportDao.updateReportStatus(reportId, ReportStatus.PROCESSED, adminId, LocalDateTime.now());
        if (reportUpdateResult <= 0) {
            log.error("处理举报失败：更新举报记录 {} 状态为已处理失败。", reportId);
            return false;
        }
        log.info("举报记录 {} 已被管理员 {} 处理为已处理。", reportId, adminId);

        // 6. 发送通知给原帖子作者
        String title = "您的帖子已被删除通知";
        String content = String.format("您的帖子《%s》因被举报且审核通过，已被管理员删除。举报原因为：%s",
                post.getTitle(), report.getReason());
        notificationService.sendNotification(post.getUserId(), title, content, 2, postId);
        log.info("已通知帖子作者 {} 帖子《{}》已被删除。", post.getUserId(), post.getTitle());

        // 移除清除相关缓存
        // clearPostDetailCache(postId);
        // clearPostListCache();

        return true;
    }

    @Override
    @Transactional
    public boolean processReportAndKeepPost(Long reportId, Long adminId, String reasonForKeeping) {
        // 1. 验证管理员身份
        User admin = userDao.findById(adminId);
        if (admin == null || admin.getRole() != UserRole.ADMIN.getCode()) {
            log.warn("处理举报失败：用户 {} 不是管理员。", adminId);
            return false;
        }

        // 2. 查询举报记录
        PostReport report = postReportDao.findReportByReportId(reportId);
        if (report == null || report.getStatus() != ReportStatus.PENDING) {
            log.warn("处理举报失败：举报记录 {} 不存在或状态不是待处理。", reportId);
            return false;
        }

        // 3. 更新举报记录状态为已处理
        int reportUpdateResult = postReportDao.updateReportStatus(reportId, ReportStatus.REJECTED, adminId, LocalDateTime.now());
        if (reportUpdateResult <= 0) {
            log.error("处理举报失败：更新举报记录 {} 状态为已处理失败。", reportId);
            return false;
        }
        log.info("举报记录 {} 已被管理员 {} 处理为已处理（帖子保留）。原因：{}", reportId, adminId, reasonForKeeping);

        return true;
    }
    // 移除所有注释掉的 fillUserLikedInfo 等辅助方法，因为它们与Redis缓存紧密关联
    /*
    // 检查用户是否点赞了帖子
    private boolean checkUserLiked(Long postId, Long userId) {
        return postLikeDao.checkUserLiked(postId, userId) > 0;
    }

    // 填充用户是否点赞信息
    public void fillUserLikedInfo(List<PostVO> posts, Long userId) {
        if (userId == null || posts.isEmpty()) {
            return;
        }

        // 从Redis获取用户点赞的帖子集合
        String userLikedKey = String.format(USER_LIKED_POSTS_KEY, userId);
        Set<Object> likedPostIds = redisTemplate.opsForSet().members(userLikedKey);

        if (likedPostIds == null || likedPostIds.isEmpty()) {
            // 如果Redis中没有，从数据库查询
            List<Long> postIds = posts.stream()
                    .map(PostVO::getId)
                    .collect(Collectors.toList());

            List<Long> userLikedPostIds = postLikeDao.findUserLikedPostIds(userId, postIds);

            // 更新Redis缓存
            if (!userLikedPostIds.isEmpty()) {
                for (Long postId : userLikedPostIds) {
                    redisTemplate.opsForSet().add(userLikedKey, postId);
                }
                redisTemplate.expire(userLikedKey, CACHE_TTL_HOURS, TimeUnit.HOURS);
            }

            // 设置点赞状态
            for (PostVO post : posts) {
                post.setIsLiked(userLikedPostIds.contains(post.getId()));
            }
        } else {
            // 使用Redis中的数据
            for (PostVO post : posts) {
                post.setIsLiked(likedPostIds.contains(post.getId()));
            }
        }
    }
    */
}
