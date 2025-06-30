package com.csu.sms.model.enums;

import lombok.Getter;

@Getter
public enum ReportStatus {
    PENDING(0,"待处理"),
    PROCESSED(1,"已处理"),
    REJECTED(2,"已驳回");

    private final int code;
    private final String description;

    ReportStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

}
