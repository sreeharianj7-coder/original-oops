package com.smarthostel.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request payload for student facial profile enrollment (up to 10 photos).
 */
public class FaceEnrollRequest {

    @NotNull(message = "Student ID cannot be null")
    private String studentId;

    @NotEmpty(message = "At least 1 face image is required")
    @Size(max = 10, message = "Maximum of 10 face photos allowed per student")
    private List<String> images;

    public FaceEnrollRequest() {
    }

    public FaceEnrollRequest(String studentId, List<String> images) {
        this.studentId = studentId;
        this.images = images;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}
