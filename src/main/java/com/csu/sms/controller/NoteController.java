package com.csu.sms.controller;

import com.csu.sms.dto.ApiResponse;
import com.csu.sms.dto.NoteDTO;
import com.csu.sms.model.note.Note;
import com.csu.sms.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notes")
public class NoteController {

    private final NoteService noteService;

    @Autowired
    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NoteDTO>> createNote(@RequestBody Note note) {
        Note createdNote = noteService.createNote(note);
        return ResponseEntity.ok(ApiResponse.success(convertToDTO(createdNote)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NoteDTO>> updateNote(@PathVariable Long id, @RequestBody Note note) {
        note.setId(id);
        Note updatedNote = noteService.updateNote(note);
        return ResponseEntity.ok(ApiResponse.success(convertToDTO(updatedNote)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<NoteDTO>>> getUserNotes(@PathVariable Long userId) {
        List<Note> notes = noteService.getUserNotes(userId);
        List<NoteDTO> noteDTOs = notes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(noteDTOs));
    }

    private NoteDTO convertToDTO(Note note) {
        NoteDTO dto = new NoteDTO();
        dto.setId(note.getId());
        dto.setUserId(note.getUserId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setDrawingData(note.getDrawingData());
        dto.setCreateTime(note.getCreateTime());
        dto.setUpdateTime(note.getUpdateTime()); // 添加这一行
        return dto;
    }
}
