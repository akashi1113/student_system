package com.csu.sms.persistence;

import com.csu.sms.model.experiment.ExperimentBooking;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ExperimentBookingMapper {

    @Select("SELECT id, experiment_id, time_slot_id, experiment_name, user_id, " +
            "start_time, end_time, status, approval_status, created_at " +
            "FROM experiment_booking WHERE id = #{id}")
    ExperimentBooking selectById(@Param("id") Long id);

    @Select("SELECT * FROM experiment_booking WHERE user_id = #{userId} ORDER BY start_time DESC")
    List<ExperimentBooking> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM experiment_booking WHERE experiment_id = #{experimentId} AND status = 1")
    List<ExperimentBooking> selectActiveByExperimentId(@Param("experimentId") Long experimentId);

    @Select("SELECT * FROM experiment_booking WHERE user_id = #{userId} " +
            "AND ((start_time BETWEEN #{startTime} AND #{endTime}) " +
            "OR (end_time BETWEEN #{startTime} AND #{endTime}) " +
            "OR (start_time <= #{startTime} AND end_time >= #{endTime}))")
    List<ExperimentBooking> findConflicts(@Param("userId") Long userId,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    @Insert("INSERT INTO experiment_booking (experiment_id, time_slot_id, experiment_name, user_id, start_time, end_time, status, created_at) " +
            "VALUES (#{experimentId}, #{timeSlotId}, #{experimentName}, #{userId}, #{startTime}, #{endTime}, #{status}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ExperimentBooking booking);

    @Update("UPDATE experiment_booking SET status=#{status} WHERE id=#{id}")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE experiment_booking SET approval_status=#{approvalStatus}, status=#{status} WHERE id=#{id}")
    void updateApproveStatus(@Param("id") Long id,  @Param("approvalStatus") Integer approvalStatus, @Param("status") Integer status);

    @Update("UPDATE experiment_booking SET experiment_id=#{experimentId}, experiment_name=#{experimentName}, start_time=#{startTime}, " +
            "end_time=#{endTime}, status=#{status} WHERE id=#{id}")
    void update(ExperimentBooking booking);

    @Delete("DELETE FROM experiment_booking WHERE id = #{id}")
    void delete(@Param("id") Long id);

    @Update("UPDATE experiment_booking SET approval_status=#{status} WHERE id=#{id}")
    void updateApprovalStatus(@Param("id") Long id, @Param("status") Integer status);

    @Select("SELECT * FROM experiment_booking WHERE time_slot_id = #{timeSlotId}")
    List<ExperimentBooking> findByTimeSlotId(@Param("timeSlotId") Long timeSlotId);

    @Select("SELECT * FROM experiment_booking")
    List<ExperimentBooking> findPendingApprovals();

    @Select("SELECT id, experiment_id, time_slot_id, experiment_name, user_id, " +
            "start_time, end_time, status, approval_status, created_at " +
            "FROM experiment_booking WHERE experiment_id = #{experimentId} ORDER BY created_at DESC")
    List<ExperimentBooking> selectByExperimentId(@Param("experimentId") Long experimentId);

    @Select("SELECT id, experiment_id, time_slot_id, experiment_name, user_id, " +
            "start_time, end_time, status, approval_status, created_at " +
            "FROM experiment_booking WHERE experiment_id = #{experimentId} AND user_id = #{userId} ORDER BY created_at DESC")
    List<ExperimentBooking> selectByUserAndExperiment(@Param("userId") Long userId,@Param("experimentId") Long experimentId);
}
