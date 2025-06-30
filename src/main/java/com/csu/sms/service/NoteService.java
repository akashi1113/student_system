package com.csu.sms.service;

import com.csu.sms.model.note.Note;

import java.util.List;

public interface NoteService {
    /**
     * 创建笔记
     * @param note 笔记数据
     * @return 保存后的笔记（包含生成的ID）
     */
    Note createNote(Note note);

    /**
     * 更新笔记
     * @param note 笔记数据
     * @return 更新后的笔记
     */
    Note updateNote(Note note);

    /**
     * 获取用户的所有笔记
     * @param userId 用户ID
     * @return 笔记列表
     */
    List<Note> getUserNotes(Long userId);
}


