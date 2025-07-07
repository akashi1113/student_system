package com.csu.sms.service.impl;

import com.csu.sms.common.PageResult;
import com.csu.sms.dto.*;
import com.csu.sms.model.experiment.*;
import com.csu.sms.persistence.*;
import com.csu.sms.service.TeacherExperimentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TeacherExperimentServiceImpl implements TeacherExperimentService {

    @Autowired
    private ExperimentTemplateMapper templateMapper;

    @Autowired
    private TimeSlotMapper timeSlotMapper;

    @Autowired
    private ExperimentBookingMapper bookingMapper;

    @Autowired
    private TeacherExperimentMapper teacherExperimentMapper;

    @Autowired
    private ExperimentMapper experimentMapper;

    public void saveExperimentTemplate(ExperimentTemplateDTO dto) {
        // 检查是否已存在该实验的模板
        ExperimentTemplate existing = templateMapper.findByExperimentId(dto.getExperimentId());

        if (existing != null) {
            // 更新现有模板
            existing.setPurpose(dto.getPurpose());
            existing.setContent(dto.getContent());
            existing.setMethod(dto.getMethod());
            existing.setSteps(dto.getSteps());
            existing.setConclusionGuide(dto.getConclusionGuide());
            existing.setUpdatedAt(LocalDateTime.now());
            templateMapper.update(existing);
        } else {
            // 创建新模板
            ExperimentTemplate template = new ExperimentTemplate();
            // 设置所有属性...
            templateMapper.insert(template);
        }
    }

    @Override
    @Transactional
    public void deleteExperimentTemplate(Long templateId) {
        templateMapper.delete(templateId);
    }

    @Override
    public ExperimentTemplateDTO getTemplateByExperimentId(Long experimentId) {
        ExperimentTemplate template = templateMapper.findByExperimentId(experimentId);
        if (template == null) return null;

        ExperimentTemplateDTO dto = new ExperimentTemplateDTO();
        BeanUtils.copyProperties(template, dto);
        return dto;
    }

    @Override
    @Transactional
    public Long createTimeSlot(TimeSlotDTO timeSlotDTO) {
        TimeSlot timeSlot = new TimeSlot();
        BeanUtils.copyProperties(timeSlotDTO, timeSlot);
        timeSlot.setCurrentCapacity(0);
        timeSlot.setCreatedAt(LocalDateTime.now());
        timeSlot.setUpdatedAt(LocalDateTime.now());
        timeSlotMapper.insert(timeSlot);
        return timeSlot.getId();  // 返回新生成的 ID
    }

    @Override
    @Transactional
    public void updateTimeSlot(Long id, TimeSlotDTO timeSlotDTO) {  // 添加 id 参数
        TimeSlot timeSlot = timeSlotMapper.findById(id);  // 使用传入的 id
        if (timeSlot == null) throw new RuntimeException("时间段不存在");

        // 仅更新允许修改的字段（避免覆盖不该修改的字段）
        timeSlot.setStartTime(timeSlotDTO.getStartTime());
        timeSlot.setEndTime(timeSlotDTO.getEndTime());
        timeSlot.setMaxCapacity(timeSlotDTO.getMaxCapacity());
        timeSlot.setUpdatedAt(LocalDateTime.now());

        timeSlotMapper.update(timeSlot);
    }

    @Override
    @Transactional
    public void deleteTimeSlot(Long slotId) {
        timeSlotMapper.delete(slotId);
    }

    @Override
    public List<TimeSlotDTO> getTimeSlotsByExperimentId(Long experimentId) {
        List<TimeSlot> slots = timeSlotMapper.findByExperimentId(experimentId);
        return slots.stream().map(slot -> {
            TimeSlotDTO dto = new TimeSlotDTO();
            BeanUtils.copyProperties(slot, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void approveBooking(BookingApprovalDTO approvalDTO) {
        ExperimentBooking booking = bookingMapper.selectById(approvalDTO.getBookingId());
        if (booking == null) throw new RuntimeException("预约记录不存在");
        TimeSlot timeSlot = timeSlotMapper.findById(booking.getTimeSlotId());
        if (timeSlot == null) {
            throw new RuntimeException("时间段不存在");
        }

        // 1：通过  2：拒绝
        if (approvalDTO.getStatus() == 1) { // 审批通过
            if (Objects.equals(timeSlot.getCurrentCapacity(),timeSlot.getMaxCapacity())){
                throw new RuntimeException("预约人数已满");
            }

            bookingMapper.updateApproveStatus(booking.getId(), 1,2);
        } else {
            bookingMapper.updateApproveStatus(booking.getId(), 2,1);

            TimeSlot updateTimeSlot = new TimeSlot();
            updateTimeSlot.setCurrentCapacity(timeSlot.getCurrentCapacity() - 1);
            timeSlotMapper.update(timeSlot);
        }
    }

    @Override
    public List<ExperimentBookingDTO> getPendingBookings() {
        List<ExperimentBooking> bookings = bookingMapper.findPendingApprovals();
        return bookings.stream().map(booking -> {
            ExperimentBookingDTO dto = new ExperimentBookingDTO();
            BeanUtils.copyProperties(booking, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Long createExperiment(TeacherExperimentDTO dto, Long teacherId) {
        // 创建教师端实验
        TeacherExperiment experiment = convertToModel(dto);
        experiment.setCreatedBy(teacherId);
        teacherExperimentMapper.insert(experiment);  // 使用teacherExperimentMapper

        // 同步到学生端实验表
        Experiment studentExperiment = new Experiment();
        studentExperiment.setName(dto.getName());
        studentExperiment.setSubject(dto.getSubject());
        studentExperiment.setDescription(dto.getDescription());
        studentExperiment.setDuration(dto.getDuration());
        studentExperiment.setStatus(0); // 默认禁用，需要教师发布
        studentExperiment.setIsPublished(false);
        studentExperiment.setCreatedAt(LocalDateTime.now());
        studentExperiment.setUpdatedAt(LocalDateTime.now());

        experimentMapper.insert(studentExperiment); // 使用experimentMapper

        // 更新教师端实验的关联ID
        experiment.setStudentExperimentId(studentExperiment.getId());
        teacherExperimentMapper.update(experiment);  // 使用teacherExperimentMapper

        return experiment.getId();
    }

    @Override
    @Transactional
    public void updateExperiment(Long id, TeacherExperimentDTO dto) {
        TeacherExperiment experiment = convertToModel(dto);
        experiment.setId(id);
        teacherExperimentMapper.update(experiment);
    }

    @Override
    @Transactional
    public void togglePublishStatus(Long id, Boolean isPublished) {
        // 1. 更新教师端发布状态
        teacherExperimentMapper.updatePublishStatus(id, isPublished);

        // 2. 获取教师实验
        TeacherExperiment teacherExperiment = teacherExperimentMapper.selectById(id);
        if (teacherExperiment == null) {
            throw new RuntimeException("实验不存在");
        }

        // 3. 如果首次发布且未关联学生实验，则创建学生实验
        if (isPublished && teacherExperiment.getStudentExperimentId() == null) {
            Experiment studentExperiment = new Experiment();
            studentExperiment.setName(teacherExperiment.getName());
            studentExperiment.setSubject(teacherExperiment.getSubject());
            studentExperiment.setDescription(teacherExperiment.getDescription());
            studentExperiment.setDuration(teacherExperiment.getDuration());
            studentExperiment.setSteps(teacherExperiment.getSteps());
            studentExperiment.setStatus(Experiment.STATUS_AVAILABLE); // 可预约状态
            studentExperiment.setIsPublished(true);
            studentExperiment.setPublishTime(LocalDateTime.now());
            studentExperiment.setCreatedAt(LocalDateTime.now());
            studentExperiment.setUpdatedAt(LocalDateTime.now());

            experimentMapper.insert(studentExperiment);

            // 更新关联ID
            teacherExperiment.setStudentExperimentId(studentExperiment.getId());
            teacherExperimentMapper.update(teacherExperiment);
        }
        // 4. 如果已关联学生实验，则更新状态
        else if (teacherExperiment.getStudentExperimentId() != null) {
            int status = isPublished ? Experiment.STATUS_AVAILABLE : Experiment.STATUS_DISABLED;
            experimentMapper.updatePublishStatus(
                    teacherExperiment.getStudentExperimentId(),
                    isPublished,
                    isPublished ? LocalDateTime.now() : null
            );
            experimentMapper.updateStatus(teacherExperiment.getStudentExperimentId(), status);
        }
    }

    @Override
    public TeacherExperimentDTO getExperimentById(Long id) {
        TeacherExperiment experiment = teacherExperimentMapper.selectById(id);
        return convertToDTO(experiment);
    }

    @Override
    @Transactional
    public void deleteExperiment(Long id) {
        teacherExperimentMapper.delete(id);
    }

    @Override
    public PageResult<TeacherExperimentDTO> getExperiments(Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<TeacherExperiment> experiments = teacherExperimentMapper.selectByPage(offset, size);
        long total = teacherExperimentMapper.countAll();

        List<TeacherExperimentDTO> dtos = experiments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.of(dtos, total, page, size);
    }

    private TeacherExperiment convertToModel(TeacherExperimentDTO dto) {
        TeacherExperiment experiment = new TeacherExperiment();
        experiment.setName(dto.getName());
        experiment.setSubject(dto.getSubject());
        experiment.setDescription(dto.getDescription());
        experiment.setDuration(dto.getDuration());
        experiment.setLocation(dto.getLocation());
        experiment.setStatus(dto.getStatus());
        experiment.setIsPublished(dto.getIsPublished());
        experiment.setSteps(dto.getSteps());
        return experiment;
    }

    private TeacherExperimentDTO convertToDTO(TeacherExperiment experiment) {
        TeacherExperimentDTO dto = new TeacherExperimentDTO();
        dto.setId(experiment.getId());
        dto.setName(experiment.getName());
        dto.setSubject(experiment.getSubject());
        dto.setDescription(experiment.getDescription());
        dto.setDuration(experiment.getDuration());
        dto.setLocation(experiment.getLocation());
        dto.setStatus(experiment.getStatus());
        dto.setIsPublished(experiment.getIsPublished());
        dto.setSteps(experiment.getSteps());
        dto.setCreatedAt(experiment.getCreatedAt());
        return dto;
    }

    @Override
    public PageResult<TeacherExperimentDTO> queryExperiments(TeacherExperimentQueryDTO queryDTO) {
        int offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        List<TeacherExperiment> experiments = teacherExperimentMapper.selectByCondition(
                queryDTO.getTeacherId(),
                queryDTO.getName(),
                queryDTO.getSubject(),
                queryDTO.getStatus(),
                queryDTO.getIsPublished(),
                queryDTO.getPageSize(),
                offset
        );

        long total = teacherExperimentMapper.countByCondition(
                queryDTO.getTeacherId(),
                queryDTO.getName(),
                queryDTO.getSubject(),
                queryDTO.getStatus(),
                queryDTO.getIsPublished()
        );

        List<TeacherExperimentDTO> dtos = experiments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.of(dtos, total, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

}
