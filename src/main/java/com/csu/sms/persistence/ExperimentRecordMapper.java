package com.csu.sms.persistence;

import com.csu.sms.model.experiment.ExperimentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExperimentRecordMapper {

    /**
     * 根据ID查询实验记录
     */
    ExperimentRecord findById(@Param("id") Long id);

    /**
     * 查询用户正在进行的实验
     */
    ExperimentRecord findRunningByUserAndExperiment(@Param("userId") Long userId, @Param("experimentId") Long experimentId);

    /**
     * 查询用户实验记录列表
     */
    List<ExperimentRecord> findByUserId(@Param("userId") Long userId);

    /**
     * 查询实验的所有记录
     */
    List<ExperimentRecord> findByExperimentId(@Param("experimentId") Long experimentId);

    /**
     * 插入实验记录
     */
    int insert(ExperimentRecord record);

    /**
     * 更新实验记录
     */
    int update(ExperimentRecord record);

    /**
     * 删除实验记录
     */
    int deleteById(@Param("id") Long id);

    /**
     * 统计用户完成的实验数量
     */
    int countCompletedByUserId(@Param("userId") Long userId);

    // 根据实验ID和学生ID获取最后一次完成的实验记录
    ExperimentRecord findLastCompletedByExperimentAndUser(@Param("experimentId") Long experimentId,
                                                          @Param("userId") Long userId);

}