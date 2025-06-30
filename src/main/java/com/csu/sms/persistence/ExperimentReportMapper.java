package com.csu.sms.persistence;

import com.csu.sms.model.experiment.ExperimentReport;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExperimentReportMapper {

    @Select("SELECT * FROM experiment_report WHERE id = #{id}")
    ExperimentReport selectById(@Param("id") Long id);

    @Select("SELECT * FROM experiment_report WHERE record_id = #{recordId}")
    ExperimentReport selectByRecordId(@Param("recordId") Long recordId);

    @Select("SELECT * FROM experiment_report WHERE record_id IN " +
            "(SELECT id FROM experiment_record WHERE booking_id IN " +
            "(SELECT id FROM experiment_booking WHERE user_id = #{userId})) " +
            "ORDER BY created_at DESC")
    List<ExperimentReport> selectByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO experiment_report (record_id, template_id, content, file_path, status, created_at, updated_at) " +
            "VALUES (#{recordId}, #{templateId}, #{content}, #{filePath}, #{status}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ExperimentReport report);

    @Update("UPDATE experiment_report SET template_id=#{templateId}, content=#{content}, " +
            "file_path=#{filePath}, status=#{status}, updated_at=#{updatedAt} " +
            "WHERE id=#{id}")
    void update(ExperimentReport report);

    @Update("UPDATE experiment_report SET file_path=#{filePath} WHERE id=#{id}")
    void updateFilePath(@Param("id") Long id, @Param("filePath") String filePath);
}
