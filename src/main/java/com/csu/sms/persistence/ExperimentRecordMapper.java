package com.csu.sms.persistence;

import com.csu.sms.model.experiment.ExperimentRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExperimentRecordMapper {

    @Select("SELECT * FROM experiment_record WHERE id = #{id}")
    ExperimentRecord selectById(@Param("id") Long id);

    @Select("SELECT * FROM experiment_record WHERE booking_id = #{bookingId}")
    ExperimentRecord selectByBookingId(@Param("bookingId") Long bookingId);

    @Select("SELECT * FROM experiment_record WHERE booking_id IN " +
            "(SELECT id FROM experiment_booking WHERE user_id = #{userId}) " +
            "ORDER BY created_at DESC")
    List<ExperimentRecord> selectByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO experiment_record (booking_id, step_data, parameters, result_data, " +
            "start_time, end_time, created_at) " +
            "VALUES (#{bookingId}, #{stepData}, #{parameters}, #{resultData}, " +
            "#{startTime}, #{endTime}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ExperimentRecord record);

    @Update("UPDATE experiment_record SET step_data=#{stepData}, parameters=#{parameters}, " +
            "result_data=#{resultData}, start_time=#{startTime}, end_time=#{endTime} " +
            "WHERE id=#{id}")
    void update(ExperimentRecord record);

    @Update("UPDATE experiment_record SET end_time=#{endTime} WHERE id=#{id}")
    void updateEndTime(@Param("id") Long id, @Param("endTime") String endTime);
}
