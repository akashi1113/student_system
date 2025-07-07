package com.csu.sms.persistence;


import com.csu.sms.model.experiment.TeacherExperiment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TeacherExperimentMapper {
    @Insert("INSERT INTO teacher_experiment(name, subject, description, duration, location, status, is_published, steps, created_by, student_experiment_id) " +
            "VALUES(#{name}, #{subject}, #{description}, #{duration}, #{location}, #{status}, #{isPublished}, #{steps}, #{createdBy}, #{studentExperimentId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TeacherExperiment experiment);

    @Update("UPDATE teacher_experiment SET name=#{name}, subject=#{subject}, description=#{description}, " +
            "duration=#{duration}, location=#{location}, status=#{status}, is_published=#{isPublished}, " +
            "steps=#{steps}, student_experiment_id=#{studentExperimentId} " +
            "WHERE id=#{id}")
    int update(TeacherExperiment experiment);

    @Select("SELECT * FROM teacher_experiment WHERE id=#{id}")
    TeacherExperiment selectById(Long id);

    @Delete("DELETE FROM teacher_experiment WHERE id=#{id}")
    int delete(Long id);

    @Update("UPDATE teacher_experiment SET is_published=#{isPublished} WHERE id=#{id}")
    int updatePublishStatus(@Param("id") Long id, @Param("isPublished") Boolean isPublished);


    @Select("SELECT * FROM teacher_experiment ORDER BY id DESC LIMIT #{size} OFFSET #{offset}")
    List<TeacherExperiment> selectByPage(@Param("offset") Integer offset,
                                         @Param("size") Integer size);

    @Select("SELECT COUNT(*) FROM teacher_experiment")
    long countAll();

    @Select("<script>" +
            "SELECT COUNT(*) FROM teacher_experiment " +
            "WHERE created_by=#{teacherId} " +
            "<if test='name!=null'>AND name LIKE CONCAT('%',#{name},'%')</if>" +
            "<if test='subject!=null'>AND subject=#{subject}</if>" +
            "<if test='status!=null'>AND status=#{status}</if>" +
            "<if test='isPublished!=null'>AND is_published=#{isPublished}</if>" +
            "</script>")
    long countByCondition(@Param("teacherId") Long teacherId,
                          @Param("name") String name,
                          @Param("subject") String subject,
                          @Param("status") Integer status,
                          @Param("isPublished") Boolean isPublished);

    @Select("<script>" +
            "SELECT * FROM teacher_experiment " +
            "WHERE created_by=#{teacherId} " +
            "<if test='name!=null'>AND name LIKE CONCAT('%',#{name},'%')</if>" +
            "<if test='subject!=null'>AND subject=#{subject}</if>" +
            "<if test='status!=null'>AND status=#{status}</if>" +
            "<if test='isPublished!=null'>AND is_published=#{isPublished}</if>" +
            "ORDER BY updated_at DESC " +
            "LIMIT #{pageSize} OFFSET #{offset}" +
            "</script>")
    List<TeacherExperiment> selectByCondition(@Param("teacherId") Long teacherId,
                                              @Param("name") String name,
                                              @Param("subject") String subject,
                                              @Param("status") Integer status,
                                              @Param("isPublished") Boolean isPublished,
                                              @Param("pageSize") Integer pageSize,
                                              @Param("offset") Integer offset);
}
