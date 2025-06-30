package com.csu.sms.persistence;

import com.csu.sms.model.user.UserNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface NotificationDao {
    int insertNotification(UserNotification notification);

    List<UserNotification> findByUserId(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    int countUnreadByUserId(@Param("userId") Long userId);

    int markAsRead(@Param("id") Long id);

    int markAllAsRead(@Param("userId") Long userId);
}
