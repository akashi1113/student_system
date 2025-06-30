package com.csu.sms.dto.exam;

public class ViolationRequest {
    private String type; // 违规类型：WINDOW_BLUR, TAB_SWITCH, DEVTOOLS_OPEN 等
    private String description; // 违规描述

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
