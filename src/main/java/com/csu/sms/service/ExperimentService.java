package com.csu.sms.service;

import com.csu.sms.dto.*;
import org.springframework.web.multipart.MultipartFile;

import com.csu.sms.controller.ExperimentController;
import com.csu.sms.dto.ExperimentBookingDTO;
import com.csu.sms.dto.ExperimentDTO;
import com.csu.sms.model.experiment.CodeHistory;
import com.csu.sms.model.experiment.Experiment;
import com.csu.sms.model.experiment.ExperimentRecord;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    // ExperimentRecordDTO saveExperimentRecord(ExperimentRecordDTO recordDTO);
    // ExperimentRecordDTO endExperiment(Long recordId);
    //ExperimentRecordDTO getRecord(Long recordId);

    // 实验报告管理
    // ExperimentReportDTO generateReport(ExperimentReportDTO reportDTO);
    //String exportReport(Long reportId, String format);

    // 数据导入
    //ExperimentRecordDTO importExperimentData(Long recordId, MultipartFile file);


    // 新增方法：发布/取消发布实验
    void togglePublishStatus(Long experimentId, boolean isPublished);

    // 新增方法：获取已发布的实验列表
    List<ExperimentDTO> getPublishedExperiments();

    List<TimeSlotDTO> getTimeSlotsByExperimentId(Long experimentId);

    void updateExperimentStatus(Long experimentId, Integer status);
    List<ExperimentBookingDTO> getBookingsByExperimentId(Long experimentId);

    ExperimentDTO getExperimentById(Long id);

    /**
     * 开始实验
     */
    ExperimentRecord startExperiment(Long experimentId, Long userId);

    /**
     * 保存代码历史
     */
    void saveCodeHistory(Long experimentRecordId, String code, String language, String actionType, Object executionResult);

    /**
     * 完成实验
     */
    void completeExperiment(Long experimentRecordId, String finalCode, String finalLanguage, Object executionResult);

    /**
     * 获取代码历史
     */
    List<CodeHistory> getCodeHistory(Long experimentRecordId);

    /**
     * 获取实验记录详情
     */
    Map<String, Object> getExperimentRecordDetail(Long experimentRecordId);

    /**
     * 更新步骤记录
     */
    void updateStepRecord(ExperimentController.StepRecordRequest request);

    /**
     * 生成报告数据
     */
    Map<String, Object> generateReportData(Long experimentRecordId);

    /**
     * 获取用户实验记录
     */
    List<ExperimentRecord> getUserExperimentRecords(Long userId);


    /**
     * 获取实验记录
     */
    ExperimentRecord getExperimentRecordById(Long experimentRecordId);

    //获取最后一次提交
    Map<String, Object> getStudentFinalReport(Long experimentId, Long studentId);

    /**
     * 获取指定实验的所有记录
     * @param experimentId 实验ID
     * @return 实验记录列表
     */
    List<ExperimentRecord> getExperimentRecords(Long experimentId);

    /**
     * 获取用户对特定实验的预约记录
     * @param userId 用户ID
     * @param experimentId 实验ID
     * @return 预约记录列表
     */
    List<ExperimentBookingDTO> getBookingsByUserAndExperiment(Long userId, Long experimentId);
}
