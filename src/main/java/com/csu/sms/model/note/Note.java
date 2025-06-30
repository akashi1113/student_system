package com.csu.sms.model.note;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Note {
    private Long id;
    private Long userId;
    private Long courseId;
    private String title;
    private String content;
    private String drawingData;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
