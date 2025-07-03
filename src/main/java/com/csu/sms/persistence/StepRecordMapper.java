package com.csu.sms.persistence;

import com.csu.sms.model.experiment.StepRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StepRecordMapper {

    /**
     * 根据ID查询步骤记录
     */
    StepRecord findById(@Param("id") Long id);

    /**
     * 根据实验记录ID查询步骤记录
     */
    List<StepRecord> findByExperimentRecordId(@Param("experimentRecordId") Long experimentRecordId);

    /**
     * 根据实验记录ID和步骤索引查询
     */
    StepRecord findByExperimentRecordIdAndStepIndex(@Param("experimentRecordId") Long experimentRecordId, @Param("stepIndex") Integer stepIndex);

    /**
     * 插入步骤记录
     */
    int insert(StepRecord stepRecord);

    /**
     * 批量插入步骤记录
     */
    int batchInsert(@Param("list") List<StepRecord> stepRecordList);

    /**
     * 更新步骤记录
     */
    int update(StepRecord stepRecord);

    /**
     * 删除步骤记录
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据实验记录ID删除步骤记录
     */
    int deleteByExperimentRecordId(@Param("experimentRecordId") Long experimentRecordId);

    /**
     * 统计已完成的步骤数量
     */
    int countCompletedByExperimentRecordId(@Param("experimentRecordId") Long experimentRecordId);
}