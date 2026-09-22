package com.smarthostel.dto;

/**
 * Result of live face verification.
 */
public class FaceVerifyResponse {

    private boolean matched;
    private double similarity;
    private double threshold;
    private int bestMatchIndex;
    private String message;
    private String studentId;

    public FaceVerifyResponse() {
    }

    public FaceVerifyResponse(boolean matched, double similarity, double threshold, int bestMatchIndex, String message, String studentId) {
        this.matched = matched;
        this.similarity = similarity;
        this.threshold = threshold;
        this.bestMatchIndex = bestMatchIndex;
        this.message = message;
        this.studentId = studentId;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public int getBestMatchIndex() {
        return bestMatchIndex;
    }

    public void setBestMatchIndex(int bestMatchIndex) {
        this.bestMatchIndex = bestMatchIndex;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
}
