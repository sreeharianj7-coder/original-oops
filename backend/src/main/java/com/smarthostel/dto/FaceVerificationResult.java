package com.smarthostel.dto;

/**
 * ============================================================================
 * FACE VERIFICATION RESULT DTO
 * ============================================================================
 * Represents the authoritative outcome of a live facial biometric comparison
 * against a student's enrolled face templates.
 */
public class FaceVerificationResult {

    private boolean matched;
    private double similarityScore;
    private double threshold;
    private int bestMatchIndex;
    private String message;
    private String studentId;

    public FaceVerificationResult() {
    }

    public FaceVerificationResult(boolean matched, double similarityScore, double threshold, 
                                  int bestMatchIndex, String message, String studentId) {
        this.matched = matched;
        this.similarityScore = similarityScore;
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

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
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
