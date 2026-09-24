package com.ferreteria.v1.controllers;

public class ResetPasswordRequest {
    private String token;
    private String code;
    private String newPassword;
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
