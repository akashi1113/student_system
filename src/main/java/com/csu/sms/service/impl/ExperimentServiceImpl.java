package com.csu.sms.service.impl;

import com.csu.sms.common.FileStorageUtil;
import com.csu.sms.controller.ExperimentController;
import com.csu.sms.dto.*;
import com.csu.sms.model.experiment.*;
import com.csu.sms.persistence.*;
import com.csu.sms.service.ExperimentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExperimentServiceImpl implements ExperimentService {

    @Autowired
    private ExperimentMapper experimentMapper;

    @Autowired
    private ExperimentBookingMapper bookingMapper;

    @Autowired
    private ExperimentRecordMapper experimentRecordMapper;

    @Autowired
    private CodeHistoryMapper codeHistoryMapper;

    @Autowired
    private StepRecordMapper stepRecordMapper;

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public List<ExperimentDTO> getAllExperiments() {
        List<Experiment> experiments = experimentMapper.selectAll();
//        System.out.println("从数据库获取的实验数量: " + experiments.size()); // 调试输出
        return experiments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ExperimentDTO getExperimentById(Long experimentId) {
        Experiment experiment = experimentMapper.selectById(experimentId);
        if (experiment == null) {
            throw new RuntimeException("Experiment not found");
        }
        return convertToDTO(experiment);
    }

    @Override
    public ExperimentBookingDTO getBooking(Long bookingId) {
        ExperimentBooking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new RuntimeException("预约记录不存在");
        }

        ExperimentBookingDTO dto = convertToDTO(booking);
        if (booking.getExperimentId() != null) {
            Experiment experiment = experimentMapper.selectById(booking.getExperimentId());
            dto.setExperiment(convertToDTO(experiment));
        }
//        Experiment experiment = experimentMapper.selectById(bookingId);

        return dto;
    }

    @Override
    @Transactional
    public ExperimentBookingDTO bookExperiment(Long experimentId, Long userId,
                                               LocalDateTime startTime, LocalDateTime endTime) {
        // 获取实验信息
        Experiment experiment = experimentMapper.selectById(experimentId);
        if (experiment == null) {
            throw new RuntimeException("实验不存在");
        }
        // 检查时间冲突
//        List<ExperimentBooking> conflicts = bookingMapper.findConflicts(userId, startTime, endTime);
//        if (!conflicts.isEmpty()) {
//            throw new RuntimeException("该时间段已有其他预约");
//        }

        ExperimentBooking booking = new ExperimentBooking();
        booking.setExperimentId(experimentId);
        booking.setExperimentName(experiment.getName());
        booking.setUserId(userId);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setStatus(1); // 待进行
        booking.setCreatedAt(LocalDateTime.now());

        bookingMapper.insert(booking);
        // 更新实验状态为已预约(0)

        experiment.setStatus(0);
        experimentMapper.update(experiment);

        return convertToDTO(booking);
    }

    // 转换方法
    private ExperimentDTO convertToDTO(Experiment experiment) {
        ExperimentDTO dto = new ExperimentDTO();
        BeanUtils.copyProperties(experiment, dto);
        return dto;
    }

    private ExperimentBookingDTO convertToDTO(ExperimentBooking booking) {
        ExperimentBookingDTO dto = new ExperimentBookingDTO();
        BeanUtils.copyProperties(booking, dto);
        return dto;
    }

    private ExperimentRecordDTO convertToDTO(ExperimentRecord record) {
        ExperimentRecordDTO dto = new ExperimentRecordDTO();
        BeanUtils.copyProperties(record, dto);
        return dto;
    }


    private ExperimentRecord convertToEntity(ExperimentRecordDTO dto) {
        ExperimentRecord record = new ExperimentRecord();
        BeanUtils.copyProperties(dto, record);
        return record;
    }

    @Override
    @Transactional
    public ExperimentRecord startExperiment(Long experimentId, Long userId) {
        // 检查是否有未完成的实验
        ExperimentRecord existingRecord = experimentRecordMapper.findRunningByUserAndExperiment(userId, experimentId);
        if (existingRecord != null) {
            return existingRecord;
        }

        // 创建新的实验记录
        ExperimentRecord record = new ExperimentRecord();
        record.setExperimentId(experimentId);
        record.setUserId(userId);
        record.setStartTime(LocalDateTime.now());
        record.setStatus("RUNNING");

        experimentRecordMapper.insert(record);

        // 初始化实验步骤记录
        Experiment experiment = experimentMapper.selectById(experimentId);
        if (experiment != null && experiment.getSteps() != null) {
            initializeStepRecords(record.getId(), experiment.getSteps());
        }

        return record;
    }

    @Override
    public void saveCodeHistory(Long experimentRecordId, String code, String language, String actionType, Object executionResult) {
        try {
            CodeHistory history = new CodeHistory();
            history.setExperimentRecordId(experimentRecordId);
            history.setCode(code);
            history.setLanguage(language);
            history.setActionType(actionType);

            if (executionResult != null) {
                history.setExecutionResult(objectMapper.writeValueAsString(executionResult));
            }

            history.setCreatedAt(LocalDateTime.now());
            codeHistoryMapper.insert(history);
        } catch (Exception e) {
            throw new RuntimeException("保存代码历史失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void completeExperiment(Long experimentRecordId, String finalCode, String finalLanguage, Object executionResult) {
        try {
            ExperimentRecord record = experimentRecordMapper.findById(experimentRecordId);
            if (record == null) {
                throw new RuntimeException("实验记录不存在");
            }

            record.setEndTime(LocalDateTime.now());
            record.setStatus("COMPLETED");
            record.setFinalCode(finalCode);
            record.setFinalLanguage(finalLanguage);

            if (executionResult != null) {
                record.setExecutionResult(objectMapper.writeValueAsString(executionResult));
            }

            // 生成报告数据
            Map<String, Object> reportData = generateReportData(experimentRecordId);
            record.setReportData(objectMapper.writeValueAsString(reportData));

            experimentRecordMapper.update(record);

            // 保存最终提交的代码历史
            saveCodeHistory(experimentRecordId, finalCode, finalLanguage, "SUBMIT", executionResult);
        } catch (Exception e) {
            throw new RuntimeException("完成实验失败: " + e.getMessage());
        }
    }

    @Override
    public List<CodeHistory> getCodeHistory(Long experimentRecordId) {
        return codeHistoryMapper.findByExperimentRecordId(experimentRecordId);
    }

    @Override
    public Map<String, Object> getExperimentRecordDetail(Long experimentRecordId) {
        ExperimentRecord record = experimentRecordMapper.findById(experimentRecordId);
        if (record == null) {
            return null;
        }

        Map<String, Object> detail = new HashMap<>();
        detail.put("record", record);

        // 获取实验信息
        Experiment experiment = experimentMapper.selectById(record.getExperimentId());
        detail.put("experiment", experiment);

        // 获取步骤记录
        List<StepRecord> stepRecords = stepRecordMapper.findByExperimentRecordId(experimentRecordId);
        detail.put("stepRecords", stepRecords);

        // 获取代码历史
        List<CodeHistory> codeHistory = getCodeHistory(experimentRecordId);
        detail.put("codeHistory", codeHistory);

        return detail;
    }

    @Override
    @Transactional
    public void updateStepRecord(ExperimentController.StepRecordRequest request) {
        try {
            StepRecord stepRecord = stepRecordMapper.findByExperimentRecordIdAndStepIndex(
                    request.getExperimentRecordId(), request.getStepIndex());

            if (stepRecord == null) {
                throw new RuntimeException("步骤记录不存在");
            }

            stepRecord.setCompleted(request.getCompleted());
            stepRecord.setNotes(request.getNotes());

            if (request.getCompleted() != null && request.getCompleted()) {
                stepRecord.setCompletionTime(LocalDateTime.now());
            }

            stepRecordMapper.update(stepRecord);
        } catch (Exception e) {
            throw new RuntimeException("更新步骤记录失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> generateReportData(Long experimentRecordId) {
        Map<String, Object> reportData = new HashMap<>();

        ExperimentRecord record = experimentRecordMapper.findById(experimentRecordId);
        if (record == null) {
            throw new RuntimeException("实验记录不存在");
        }

        Experiment experiment = experimentMapper.selectById(record.getExperimentId());
        List<StepRecord> stepRecords = stepRecordMapper.findByExperimentRecordId(experimentRecordId);
        List<CodeHistory> codeHistory = getCodeHistory(experimentRecordId);

        reportData.put("experimentInfo", experiment);
        reportData.put("duration", calculateDuration(record.getStartTime(), record.getEndTime()));
        reportData.put("completedSteps", stepRecords.stream().mapToInt(s -> Boolean.TRUE.equals(s.getCompleted()) ? 1 : 0).sum());
        reportData.put("totalSteps", stepRecords.size());
        reportData.put("codeVersions", codeHistory.size());
        reportData.put("runCount", (int) codeHistory.stream().filter(h -> "RUN".equals(h.getActionType())).count());
        reportData.put("saveCount", (int) codeHistory.stream().filter(h -> "SAVE".equals(h.getActionType())).count());

        return reportData;
    }

    @Override
    public List<ExperimentRecord> getUserExperimentRecords(Long userId) {
        return experimentRecordMapper.findByUserId(userId);
    }

    @Override
    public ExperimentRecord getExperimentRecordById(Long experimentRecordId) {
        return experimentRecordMapper.findById(experimentRecordId);
    }

    private long calculateDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return java.time.Duration.between(start, end).toMinutes();
    }

    private void initializeStepRecords(Long experimentRecordId, String stepsJson) {
        try {
            // 使用明确的类型定义
            List<Map<String, Object>> steps = objectMapper.readValue(
                    stepsJson,
                    new TypeReference<List<Map<String, Object>>>(){}
            );

            List<StepRecord> stepRecords = new ArrayList<>();
            for (Map<String, Object> step : steps) {
                StepRecord stepRecord = new StepRecord();
                stepRecord.setExperimentRecordId(experimentRecordId);
                stepRecord.setStepIndex(((Number)step.get("step")).intValue() - 1); // 转为0-based
                stepRecord.setStepName(step.get("title").toString());
                stepRecord.setCompleted(false);
                stepRecords.add(stepRecord);
            }

            if (!stepRecords.isEmpty()) {
                stepRecordMapper.batchInsert(stepRecords);
            }
        } catch (Exception e) {
            throw new RuntimeException("初始化步骤记录失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getStudentFinalReport(Long experimentId, Long studentId) {
        // 获取最后一次完成的实验记录
        ExperimentRecord record = experimentRecordMapper.findLastCompletedByExperimentAndUser(experimentId, studentId);
        if (record == null) {
            throw new RuntimeException("该学生尚未完成此实验");
        }

        // 获取详细数据
        Map<String, Object> reportData = new HashMap<>();

        try {
            Experiment experiment = experimentMapper.selectById(experimentId);
            reportData.put("experiment", experiment);

            reportData.put("record", record);

            List<StepRecord> stepRecords = stepRecordMapper.findByExperimentRecordId(record.getId());
            reportData.put("stepRecords", stepRecords);

            List<CodeHistory> codeHistory = codeHistoryMapper.findByExperimentRecordId(record.getId());
            reportData.put("codeHistory", codeHistory);

            if (record.getExecutionResult() != null) {
                Object executionResult = objectMapper.readValue(record.getExecutionResult(), Object.class);
                reportData.put("executionResult", executionResult);
            }

            if (record.getReportData() != null) {
                Object report = objectMapper.readValue(record.getReportData(), Object.class);
                reportData.put("report", report);
            }

            return reportData;
        } catch (Exception e) {
            throw new RuntimeException("生成报告数据失败: " + e.getMessage());
        }
    }

    @Override
    public List<ExperimentRecord> getExperimentRecords(Long experimentId) {
        // 参数校验
        if (experimentId == null || experimentId <= 0) {
            throw new IllegalArgumentException("实验ID不能为空且必须大于0");
        }

        // 查询实验是否存在
        Experiment experiment = experimentMapper.selectById(experimentId);
        if (experiment == null) {
            throw new RuntimeException("实验不存在");
        }

        // 获取该实验的所有记录
        List<ExperimentRecord> records = experimentRecordMapper.findByExperimentId(experimentId);

        // 按完成时间降序排序（已完成实验优先，未完成的按创建时间排序）
        records.sort((r1, r2) -> {
            if ("COMPLETED".equals(r1.getStatus()) && !"COMPLETED".equals(r2.getStatus())) {
                return -1;
            } else if (!"COMPLETED".equals(r1.getStatus()) && "COMPLETED".equals(r2.getStatus())) {
                return 1;
            } else if ("COMPLETED".equals(r1.getStatus()) && "COMPLETED".equals(r2.getStatus())) {
                return r2.getEndTime().compareTo(r1.getEndTime()); // 完成时间降序
            } else {
                return r2.getCreatedAt().compareTo(r1.getCreatedAt()); // 创建时间降序
            }
        });

        return records;
    }
}
