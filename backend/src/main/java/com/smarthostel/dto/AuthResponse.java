package com.smarthostel.dto;

import com.smarthostel.model.Student;

public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private Student student;
    private String message;

    public AuthResponse() {
    }

    public AuthResponse(String token, Student student, String message) {
        this.token = token;
        this.student = student;
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
