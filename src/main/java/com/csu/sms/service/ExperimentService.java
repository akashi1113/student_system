package com.csu.sms.service;

import com.csu.sms.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public interface ExperimentService {
    // 实验项目管理
    List<ExperimentDTO> getAllExperiments();


    // 实验预约管理
    ExperimentBookingDTO bookExperiment(Long experimentId, Long userId,
                                        LocalDateTime startTime, LocalDateTime endTime);
    ExperimentBookingDTO getBooking(Long bookingId);

    // 新增基于时间段的预约方法
    ExperimentBookingDTO bookExperimentWithTimeSlot(Long experimentId, Long userId, Long timeSlotId);

    // 实验记录管理
    ExperimentRecordDTO startExperiment(Long bookingId);
    ExperimentRecordDTO saveExperimentRecord(ExperimentRecordDTO recordDTO);
    ExperimentRecordDTO endExperiment(Long recordId);
    ExperimentRecordDTO getRecord(Long recordId);

    // 实验报告管理
    ExperimentReportDTO generateReport(ExperimentReportDTO reportDTO);
    String exportReport(Long reportId, String format);

    // 数据导入
    ExperimentRecordDTO importExperimentData(Long recordId, MultipartFile file);

    ExperimentDTO getExperimentById(Long id);

    // 新增方法：发布/取消发布实验
    void togglePublishStatus(Long experimentId, boolean isPublished);

    // 新增方法：获取已发布的实验列表
    List<ExperimentDTO> getPublishedExperiments();

    List<TimeSlotDTO> getTimeSlotsByExperimentId(Long experimentId);

    void updateExperimentStatus(Long experimentId, Integer status);
    List<ExperimentBookingDTO> getBookingsByExperimentId(Long experimentId);
}
