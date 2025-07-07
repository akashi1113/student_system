package com.csu.sms.dto;

public class FaceLoginRequest {
    private String username;
    private String faceImage; // base64字符串

    // getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFaceImage() { return faceImage; }
    public void setFaceImage(String faceImage) { this.faceImage = faceImage; }
} 