package com.csu.sms.service.impl;

import com.csu.sms.model.note.Note;
import com.csu.sms.persistence.NoteMapper;
import com.csu.sms.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class NoteServiceImpl implements NoteService {

    private final NoteMapper noteMapper;

    @Autowired
    public NoteServiceImpl(NoteMapper noteMapper) {
        this.noteMapper = noteMapper;
    }

    @Override
    @Transactional
    public Note createNote(Note note) {
        noteMapper.insert(note);
        return note;
    }

    @Override
    @Transactional
    public Note updateNote(Note note) {
        noteMapper.update(note);
        return note;
    }

    @Override
    public List<Note> getUserNotes(Long userId) {
        return noteMapper.findByUserId(userId);
    }
}
