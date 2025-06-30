package com.csu.sms.service.impl;

import com.csu.sms.common.FileStorageUtil;
import com.csu.sms.dto.*;
import com.csu.sms.model.experiment.Experiment;
import com.csu.sms.model.experiment.ExperimentBooking;
import com.csu.sms.model.experiment.ExperimentRecord;
import com.csu.sms.model.experiment.ExperimentReport;
import com.csu.sms.persistence.*;
import com.csu.sms.model.*;
import com.csu.sms.service.ExperimentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExperimentServiceImpl implements ExperimentService {

    @Autowired
    private ExperimentMapper experimentMapper;

    @Autowired
    private ExperimentBookingMapper bookingMapper;

    @Autowired
    private ExperimentRecordMapper recordMapper;

    @Autowired
    private ExperimentReportMapper reportMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FileStorageUtil fileStorageUtil;

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



    @Override
    public ExperimentRecordDTO getRecord(Long recordId) {
        ExperimentRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("实验记录不存在");
        }

        ExperimentRecordDTO dto = convertToDTO(record);
        if (record.getBookingId() != null) {
            ExperimentBooking booking = bookingMapper.selectById(record.getBookingId());
            dto.setBooking(convertToDTO(booking));
        }

        return dto;
    }

    @Override
    @Transactional
    public ExperimentRecordDTO startExperiment(Long bookingId) {
        ExperimentBooking booking = bookingMapper.selectById(bookingId);
        if (booking == null || booking.getStatus() != 0) {
            throw new RuntimeException("无效的预约记录");
        }

        // 更新预约状态为进行中
        booking.setStatus(1);
        bookingMapper.update(booking);

        // 创建实验记录
        ExperimentRecord record = new ExperimentRecord();
        record.setBookingId(bookingId);
        record.setStartTime(LocalDateTime.now());
        record.setCreatedAt(LocalDateTime.now());

        // 初始化步骤和参数数据
        try {
            record.setStepData(objectMapper.writeValueAsString(new ExperimentStepData()));
            record.setParameters(objectMapper.writeValueAsString(new ExperimentParameters()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("初始化实验数据失败", e);
        }

        recordMapper.insert(record);
        return convertToDTO(record);
    }

    @Override
    @Transactional
    public ExperimentRecordDTO saveExperimentRecord(ExperimentRecordDTO recordDTO) {
        ExperimentRecord record = convertToEntity(recordDTO);
        recordMapper.update(record);
        return recordDTO;
    }

    @Override
    @Transactional
    public ExperimentRecordDTO endExperiment(Long recordId) {
        ExperimentRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("实验记录不存在");
        }

        record.setEndTime(LocalDateTime.now());
        recordMapper.update(record);

        // 更新预约状态为已完成
        ExperimentBooking booking = bookingMapper.selectById(record.getBookingId());
        if (booking != null) {
            booking.setStatus(2); // 已完成
            bookingMapper.update(booking);
        }

        return convertToDTO(record);
    }

    @Override
    @Transactional
    public ExperimentReportDTO generateReport(ExperimentReportDTO reportDTO) {
        ExperimentRecord record = recordMapper.selectById(reportDTO.getRecordId());
        if (record == null) {
            throw new RuntimeException("实验记录不存在");
        }

        ExperimentReport report = new ExperimentReport();
        report.setRecordId(reportDTO.getRecordId());
        report.setTemplateId(reportDTO.getTemplateId());
        report.setContent(reportDTO.getContent());
        report.setStatus(1); // 已生成
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        reportMapper.insert(report);
        return convertToDTO(report);
    }

    @Override
    public String exportReport(Long reportId, String format) {
        ExperimentReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new RuntimeException("报告不存在");
        }

        // 这里简化为返回文件路径，实际应生成文件
        String fileName = "report_" + reportId + "." + format.toLowerCase();
        return "/reports/" + fileName;
    }

    @Override
    @Transactional
    public ExperimentRecordDTO importExperimentData(Long recordId, MultipartFile file) {
        ExperimentRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("实验记录不存在");
        }

        try {
            // 保存上传的文件
            String filePath = fileStorageUtil.storeFile(file);

            // 更新实验记录
            record.setResultData("{\"importedData\": \"" + filePath + "\"}");
            recordMapper.update(record);

            return convertToDTO(record);
        } catch (IOException e) {
            throw new RuntimeException("文件处理失败", e);
        }
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

    private ExperimentReportDTO convertToDTO(ExperimentReport report) {
        ExperimentReportDTO dto = new ExperimentReportDTO();
        BeanUtils.copyProperties(report, dto);
        return dto;
    }

    private ExperimentRecord convertToEntity(ExperimentRecordDTO dto) {
        ExperimentRecord record = new ExperimentRecord();
        BeanUtils.copyProperties(dto, record);
        return record;
    }

    // 内部类用于初始化实验数据
    private static class ExperimentStepData {
        private int currentStep = 0;
        private List<String> steps;
        // getters and setters
    }

    private static class ExperimentParameters {
        private List<Parameter> parameters;
        // getters and setters
    }

    private static class Parameter {
        private String name;
        private String type;
        private String value;
        // getters and setters
    }
}
