package com.csu.sms.service;

import com.csu.sms.common.PageResult;
import com.csu.sms.dto.ExperimentBookingDTO;
import com.csu.sms.dto.ExperimentTemplateDTO;
import com.csu.sms.dto.TimeSlotDTO;
import com.csu.sms.dto.BookingApprovalDTO;
import com.csu.sms.dto.TeacherExperimentDTO;
import com.csu.sms.dto.TeacherExperimentQueryDTO;

import java.util.List;

public interface TeacherExperimentService {

    Long createExperiment(TeacherExperimentDTO dto, Long teacherId);
    void updateExperiment(Long id, TeacherExperimentDTO dto);
    void deleteExperiment(Long id);
    TeacherExperimentDTO getExperimentById(Long id);
    PageResult<TeacherExperimentDTO> queryExperiments(TeacherExperimentQueryDTO queryDTO);
    void togglePublishStatus(Long id, Boolean isPublished);
    // 实验模板管理
    void saveExperimentTemplate(ExperimentTemplateDTO templateDTO);
    void deleteExperimentTemplate(Long templateId);
    ExperimentTemplateDTO getTemplateByExperimentId(Long experimentId);

    // 时间段管理
    Long createTimeSlot(TimeSlotDTO timeSlotDTO);
    void updateTimeSlot(Long id, TimeSlotDTO timeSlotDTO);
    void deleteTimeSlot(Long slotId);
    List<TimeSlotDTO> getTimeSlotsByExperimentId(Long experimentId);

    // 预约审批
    void approveBooking(BookingApprovalDTO approvalDTO);
    List<ExperimentBookingDTO> getPendingBookings();

    PageResult<TeacherExperimentDTO> getExperiments(Integer page, Integer size);
}
