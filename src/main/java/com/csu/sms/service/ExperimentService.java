package com.csu.sms.service;

import com.csu.sms.dto.ExperimentBookingDTO;
import com.csu.sms.dto.ExperimentDTO;
import com.csu.sms.dto.ExperimentRecordDTO;
import com.csu.sms.dto.ExperimentReportDTO;
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
}
