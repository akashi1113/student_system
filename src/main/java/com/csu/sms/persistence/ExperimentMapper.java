package com.csu.sms.persistence;

import com.csu.sms.model.experiment.Experiment;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ExperimentMapper {


    // @Select("SELECT * FROM experiment WHERE status = 1 ORDER BY created_at DESC")
    @Select("SELECT id, name, subject, " +
            "CAST(description AS CHAR) AS description, " +  // 显式转换BLOB为字符串
            "CAST(steps AS CHAR) AS steps, " +
            "duration, status, created_at, updated_at " +
            "FROM experiment WHERE status = 1 ORDER BY created_at DESC")
    List<Experiment> selectAll();

    @Select("SELECT * FROM experiment WHERE id = #{id}")
    Experiment selectById(@Param("id") Long id);

    @Select("SELECT * FROM experiment WHERE subject = #{subject} AND status = 1")
    List<Experiment> selectBySubject(@Param("subject") String subject);

    @Insert("INSERT INTO experiment (name, subject, description, duration, status, created_at, updated_at) " +
            "VALUES (#{name}, #{subject}, #{description}, #{duration}, #{status}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Experiment experiment);

    @Update("UPDATE experiment SET name=#{name}, subject=#{subject}, description=#{description}, " +
            "duration=#{duration}, status=#{status}, updated_at=#{updatedAt} WHERE id=#{id}")
    void update(Experiment experiment);

    @Update("UPDATE experiment SET status=0 WHERE id=#{id}")
    void disable(@Param("id") Long id);

    @Update("UPDATE experiment SET steps=#{steps} WHERE id=#{id}")
    void updateSteps(@Param("id") Long id, @Param("steps") String steps);

    // 添加发布状态更新方法
    @Update("UPDATE experiment SET is_published=#{isPublished}, publish_time=#{publishTime} WHERE id=#{id}")
    void updatePublishStatus(@Param("id") Long id,
                             @Param("isPublished") Boolean isPublished,
                             @Param("publishTime") LocalDateTime publishTime);

    // 添加获取已发布实验的方法
    @Select("SELECT * FROM experiment WHERE is_published = 1 AND status = 1") // 只获取已发布且可预约的实验
    List<Experiment> selectPublishedExperiments();

    @Update("UPDATE experiment SET status=#{status} WHERE id=#{id}")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Select("SELECT * FROM experiment WHERE is_published = true AND status = #{status} ORDER BY created_at DESC")
    List<Experiment> selectPublishedByStatus(@Param("status") Integer status);


}
