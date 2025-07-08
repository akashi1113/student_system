package com.csu.sms.persistence;

import com.csu.sms.model.note.Note;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NoteMapper {
    /**
     * 插入新笔记
     * @param note 笔记对象
     * 注意：数据库字段名与Java属性名的对应关系：
     * - user_id → userId
     * - course_id → courseId
     * - drawing_data → drawingData
     * - create_time → createTime
     * - update_time → updateTime
     */
    @Insert("INSERT INTO note (user_id, course_id, title, content, drawing_data, create_time, update_time) " +
            "VALUES (#{userId}, #{courseId}, #{title}, #{content}, #{drawingData}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Note note);

    /**
     * 更新笔记
     */
    @Update({
            "UPDATE note SET",
            "course_id = #{courseId},",
            "title = #{title},",
            "content = #{content},",
            "drawing_data = #{drawingData},",
//            "create_time = #{creareTime}",
            "update_time = NOW()", // 使用数据库函数确保时间一致
            "WHERE id = #{id}"
    })
    void update(Note note);

    @Select("SELECT * FROM note WHERE user_id = #{userId}")
    List<Note> findByUserId(Long userId);

    // 可选：添加其他基础CRUD方法
    @Select("SELECT * FROM note WHERE id = #{id}")
    Note findById(Long id);

    @Delete("DELETE FROM note WHERE id = #{id}")
    void delete(Long id);
}
