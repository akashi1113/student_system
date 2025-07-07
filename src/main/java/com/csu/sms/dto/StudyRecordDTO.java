package com.csu.sms.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class StudyRecordDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "视频ID不能为空")
    private Long videoId;

    //前端播放器当前时间点（秒）
    private Integer currentPlaybackPosition;;

    //本次上报周期内的观看时长（秒）
    private Integer watchDurationSinceLastSave;
}
