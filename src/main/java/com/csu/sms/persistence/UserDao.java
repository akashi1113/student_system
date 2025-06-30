package com.csu.sms.persistence;

import com.csu.sms.model.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserDao {
    User findById(@Param("id") Long id);

    User findByUsername(@Param("username") String username);

    User findByEmail(@Param("email") String email);

    int insertUser(User user);

    int updateUser(User user);

    int updateUserStatus(@Param("id") Long id, @Param("status") Integer status);

    int updateUserRole(@Param("id") Long id, @Param("role") Integer role);

    int updatePassword(@Param("id") Long id, @Param("password") String password);

    List<User> findUsersByPage(
            @Param("offset") Integer offset,
            @Param("limit") Integer limit
    );

    int countUsers();

    List<User> findUsersByIds(@Param("userIds") List<Long> userIds);

    List<User> findUsersByRole(@Param("code") int code);
}
