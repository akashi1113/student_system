package com.csu.sms.persistence;

import com.csu.sms.model.experiment.TimeSlot;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TimeSlotMapper {

    @Insert("INSERT INTO experiment_time_slot (experiment_id, start_time, end_time, max_capacity, current_capacity, status, created_at, updated_at) " +
            "VALUES (#{experimentId}, #{startTime}, #{endTime}, #{maxCapacity}, #{currentCapacity}, #{status}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(TimeSlot timeSlot);

    @Update("UPDATE experiment_time_slot SET start_time=#{startTime}, end_time=#{endTime}, " +
            "max_capacity=#{maxCapacity}, current_capacity=#{currentCapacity}, status=#{status}, updated_at=#{updatedAt} " +
            "WHERE id=#{id}")
    void update(TimeSlot timeSlot);

    @Update("UPDATE experiment_time_slot SET current_capacity=#{currentCapacity} WHERE id=#{id}")
    void updateCapacity(TimeSlot timeSlot);

    @Select("SELECT * FROM experiment_time_slot WHERE experiment_id = #{experimentId}")
    List<TimeSlot> findByExperimentId(@Param("experimentId") Long experimentId);

    @Select("SELECT * FROM experiment_time_slot WHERE id = #{id}")
    TimeSlot findById(@Param("id") Long id);

    @Update("UPDATE experiment_time_slot SET current_capacity = current_capacity + 1 WHERE id = #{id}")
    void incrementCapacity(@Param("id") Long id);

    @Update("UPDATE experiment_time_slot SET current_capacity = current_capacity - 1 WHERE id = #{id}")
    void decrementCapacity(@Param("id") Long id);

    @Delete("DELETE FROM experiment_time_slot WHERE id = #{id}")
    void delete(@Param("id") Long id);
}
