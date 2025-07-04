package com.csu.sms.service;

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
}
