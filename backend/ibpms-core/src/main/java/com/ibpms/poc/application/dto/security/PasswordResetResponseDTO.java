package com.ibpms.poc.application.dto.security;

public class PasswordResetResponseDTO {
    
    private String tempPassword;
    private String message;

    public PasswordResetResponseDTO(String tempPassword, String message) {
        this.tempPassword = tempPassword;
        this.message = message;
    }

    public String getTempPassword() { return tempPassword; }
    public void setTempPassword(String tempPassword) { this.tempPassword = tempPassword; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
