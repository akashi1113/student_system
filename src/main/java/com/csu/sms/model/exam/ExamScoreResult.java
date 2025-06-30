package com.csu.sms.model.exam;

public class ExamScoreResult {
    private Long examRecordId;
    private Integer totalScore;     // 总得分
    private Integer maxScore;       // 总分
    private Integer passingScore;   // 及格分数
    private Boolean passed;         // 是否及格
    private Double percentage;      // 得分百分比

    public ExamScoreResult() {}

    public Long getExamRecordId() {
        return examRecordId;
    }

    public void setExamRecordId(Long examRecordId) {
        this.examRecordId = examRecordId;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
        calculatePercentage();
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
        calculatePercentage();
    }

    public Integer getPassingScore() {
        return passingScore;
    }

    public void setPassingScore(Integer passingScore) {
        this.passingScore = passingScore;
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    private void calculatePercentage() {
        if (totalScore != null && maxScore != null && maxScore > 0) {
            this.percentage = (double) totalScore / maxScore * 100;
        }
    }
}