package com.csu.sms.dto;

public class FaceRegisterRequest {
    private String username;
    private String password;
    private String email;
    private String faceImage; // base64字符串

    // getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFaceImage() { return faceImage; }
    public void setFaceImage(String faceImage) { this.faceImage = faceImage; }
} 