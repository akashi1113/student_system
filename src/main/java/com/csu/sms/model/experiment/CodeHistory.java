package com.csu.sms.model.experiment;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CodeHistory {
    private Long id;
    private Long experimentRecordId;
    private String code;
    private String language;
    private String actionType; // SAVE, RUN, SUBMIT
    private String executionResult; // JSON
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExperimentRecordId() {
        return experimentRecordId;
    }

    public void setExperimentRecordId(Long experimentRecordId) {
        this.experimentRecordId = experimentRecordId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getExecutionResult() {
        return executionResult;
    }

    public void setExecutionResult(String executionResult) {
        this.executionResult = executionResult;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}