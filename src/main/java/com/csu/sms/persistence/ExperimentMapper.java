package com.csu.sms.persistence;

import com.csu.sms.model.experiment.Experiment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExperimentMapper {


    @Select("SELECT * FROM experiment WHERE status = 1 ORDER BY created_at DESC")
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
}
