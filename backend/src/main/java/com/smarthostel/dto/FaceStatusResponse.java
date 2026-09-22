package com.smarthostel.dto;

/**
 * Status of student's face enrollment for dashboard cards and verification checks.
 */
public class FaceStatusResponse {

    private String studentId;
    private boolean registered;
    private int photoCount;
    private int maxAllowed;
    private String statusText;
    private String message;

    public FaceStatusResponse() {
    }

    public FaceStatusResponse(String studentId, boolean registered, int photoCount, int maxAllowed, String statusText, String message) {
        this.studentId = studentId;
        this.registered = registered;
        this.photoCount = photoCount;
        this.maxAllowed = maxAllowed;
        this.statusText = statusText;
        this.message = message;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public int getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(int photoCount) {
        this.photoCount = photoCount;
    }

    public int getMaxAllowed() {
        return maxAllowed;
    }

    public void setMaxAllowed(int maxAllowed) {
        this.maxAllowed = maxAllowed;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
