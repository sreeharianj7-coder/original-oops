package com.smarthostel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MarkAttendanceRequest {

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private Double accuracy;

    private String faceImage; // Optional Base64 captured frame for Face Verification

    public MarkAttendanceRequest() {
    }

    public MarkAttendanceRequest(String studentId, Double latitude, Double longitude, Double accuracy) {
        this.studentId = studentId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
    }

    public MarkAttendanceRequest(String studentId, Double latitude, Double longitude, Double accuracy, String faceImage) {
        this.studentId = studentId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.faceImage = faceImage;
    }


    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public String getFaceImage() {
        return faceImage;
    }

    public void setFaceImage(String faceImage) {
        this.faceImage = faceImage;
    }
}

