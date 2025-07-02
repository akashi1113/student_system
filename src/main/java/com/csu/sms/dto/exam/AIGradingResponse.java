package com.csu.sms.dto.exam;

import java.util.List;

public class AIGradingResponse {
    private Integer score;           // 实际得分
    private Double scoreRatio;       // 得分率 (0-1)
    private String feedback;         // 评分反馈
    private List<String> keyPoints;  // 关键得分点

    public AIGradingResponse() {}

    public AIGradingResponse(Integer score, Double scoreRatio, String feedback, List<String> keyPoints) {
        this.score = score;
        this.scoreRatio = scoreRatio;
        this.feedback = feedback;
        this.keyPoints = keyPoints;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Double getScoreRatio() {
        return scoreRatio;
    }

    public void setScoreRatio(Double scoreRatio) {
        this.scoreRatio = scoreRatio;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public List<String> getKeyPoints() {
        return keyPoints;
    }

    public void setKeyPoints(List<String> keyPoints) {
        this.keyPoints = keyPoints;
    }
}