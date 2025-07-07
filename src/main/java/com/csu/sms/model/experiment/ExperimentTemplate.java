package com.csu.sms.model.experiment;

import java.time.LocalDateTime;

public class ExperimentTemplate {
    private Long id;
    private Long experimentId;
    private String purpose;
    private String content;
    private String method;
    private String steps; // JSON字符串
    private String conclusionGuide;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getExperimentId() { return experimentId; }
    public void setExperimentId(Long experimentId) { this.experimentId = experimentId; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getSteps() { return steps; }
    public void setSteps(String steps) { this.steps = steps; }
    public String getConclusionGuide() { return conclusionGuide; }
    public void setConclusionGuide(String conclusionGuide) { this.conclusionGuide = conclusionGuide; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
