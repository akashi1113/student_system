package com.csu.sms.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NoteDTO {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String drawingData;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
