package com.smarthostel.dto;

import java.util.List;

/**
 * Response payload returned after face registration attempt.
 */
public class FaceEnrollResponse {

    private boolean success;
    private String studentId;
    private int registeredPhotos;
    private int totalSubmitted;
    private boolean allPhotosValid;
    private String message;
    private List<PhotoValidationResult> results;

    public static class PhotoValidationResult {
        private int photoIndex;
        private boolean valid;
        private String message;

        public PhotoValidationResult() {
        }

        public PhotoValidationResult(int photoIndex, boolean valid, String message) {
            this.photoIndex = photoIndex;
            this.valid = valid;
            this.message = message;
        }

        public int getPhotoIndex() {
            return photoIndex;
        }

        public void setPhotoIndex(int photoIndex) {
            this.photoIndex = photoIndex;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public FaceEnrollResponse() {
    }

    public FaceEnrollResponse(boolean success, String studentId, int registeredPhotos, int totalSubmitted, boolean allPhotosValid, String message, List<PhotoValidationResult> results) {
        this.success = success;
        this.studentId = studentId;
        this.registeredPhotos = registeredPhotos;
        this.totalSubmitted = totalSubmitted;
        this.allPhotosValid = allPhotosValid;
        this.message = message;
        this.results = results;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public int getRegisteredPhotos() {
        return registeredPhotos;
    }

    public void setRegisteredPhotos(int registeredPhotos) {
        this.registeredPhotos = registeredPhotos;
    }

    public int getTotalSubmitted() {
        return totalSubmitted;
    }

    public void setTotalSubmitted(int totalSubmitted) {
        this.totalSubmitted = totalSubmitted;
    }

    public boolean isAllPhotosValid() {
        return allPhotosValid;
    }

    public void setAllPhotosValid(boolean allPhotosValid) {
        this.allPhotosValid = allPhotosValid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<PhotoValidationResult> getResults() {
        return results;
    }

    public void setResults(List<PhotoValidationResult> results) {
        this.results = results;
    }
}
