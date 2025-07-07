package com.csu.sms.persistence;

import com.csu.sms.model.experiment.ExperimentTemplate;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ExperimentTemplateMapper {

    @Insert("INSERT INTO experiment_template (experiment_id, purpose, content, method, steps, conclusion_guide, created_by, created_at, updated_at) " +
            "VALUES (#{experimentId}, #{purpose}, #{content}, #{method}, #{steps}, #{conclusionGuide}, #{createdBy}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ExperimentTemplate template);

    @Update("UPDATE experiment_template SET purpose=#{purpose}, content=#{content}, method=#{method}, " +
            "steps=#{steps}, conclusion_guide=#{conclusionGuide}, updated_at=#{updatedAt} " +
            "WHERE id=#{id}")
    void update(ExperimentTemplate template);

    @Select("SELECT * FROM experiment_template WHERE experiment_id = #{experimentId}")
    ExperimentTemplate findByExperimentId(@Param("experimentId") Long experimentId);

    @Delete("DELETE FROM experiment_template WHERE id = #{id}")
    void delete(@Param("id") Long id);
}
