package com.csu.sms.persistence;

import com.csu.sms.model.experiment.CodeHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CodeHistoryMapper {

    /**
     * 根据ID查询代码历史
     */
    CodeHistory findById(@Param("id") Long id);

    /**
     * 根据实验记录ID查询代码历史
     */
    List<CodeHistory> findByExperimentRecordId(@Param("experimentRecordId") Long experimentRecordId);

    /**
     * 查询最新的代码历史
     */
    CodeHistory findLatestByExperimentRecordId(@Param("experimentRecordId") Long experimentRecordId);

    /**
     * 插入代码历史
     */
    int insert(CodeHistory codeHistory);

    /**
     * 批量插入代码历史
     */
    int batchInsert(@Param("list") List<CodeHistory> codeHistoryList);

    /**
     * 删除代码历史
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据实验记录ID删除代码历史
     */
    int deleteByExperimentRecordId(@Param("experimentRecordId") Long experimentRecordId);

    /**
     * 统计代码历史数量
     */
    int countByExperimentRecordId(@Param("experimentRecordId") Long experimentRecordId);
}