package com.csu.sms.model;

public class MonitorAnalysisResult {
    private boolean abnormal = false;
    private String abnormalType;
    private double score;
    private String message;
    private String detectedUserId;
    private double yawAngle;
    private double pitchAngle;
    private double rollAngle;
    private long analysisTime;

    public MonitorAnalysisResult() {
        this.analysisTime = System.currentTimeMillis();
    }

    // getter/setter
    public boolean hasAbnormal() {
        return abnormal;
    }

    public boolean isAbnormal() {
        return abnormal;
    }

    public void setAbnormal(boolean abnormal) {
        this.abnormal = abnormal;
    }

    public String getAbnormalType() {
        return abnormalType;
    }

    public void setAbnormalType(String abnormalType) {
        this.abnormalType = abnormalType;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetectedUserId() {
        return detectedUserId;
    }

    public void setDetectedUserId(String detectedUserId) {
        this.detectedUserId = detectedUserId;
    }

    public double getYawAngle() {
        return yawAngle;
    }

    public void setYawAngle(double yawAngle) {
        this.yawAngle = yawAngle;
    }

    public double getPitchAngle() {
        return pitchAngle;
    }

    public void setPitchAngle(double pitchAngle) {
        this.pitchAngle = pitchAngle;
    }

    public double getRollAngle() {
        return rollAngle;
    }

    public void setRollAngle(double rollAngle) {
        this.rollAngle = rollAngle;
    }

    public long getAnalysisTime() {
        return analysisTime;
    }

    public void setAnalysisTime(long analysisTime) {
        this.analysisTime = analysisTime;
    }
}