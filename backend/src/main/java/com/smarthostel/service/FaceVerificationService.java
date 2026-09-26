package com.smarthostel.service;

import com.smarthostel.dto.FaceEnrollRequest;
import com.smarthostel.dto.FaceEnrollResponse;
import com.smarthostel.dto.FaceStatusResponse;
import com.smarthostel.dto.FaceVerifyResponse;

/**
 * ============================================================================
 * FACE RECOGNITION SERVICE INTERFACE
 * ============================================================================
 * Service contract for student facial biometric enrollment, live verification,
 * and registration status queries.
 */
public interface FaceVerificationService {

    /**
     * Enrolls up to 10 facial photographs for a student.
     * Communicates with Python FaceNet microservice to extract 512-d embeddings
     * and securely persists them in MySQL (student_face_profiles table).
     * 
     * @param request FaceEnrollRequest containing studentId and list of base64 images
     * @return FaceEnrollResponse with per-photo validation results
     */
    FaceEnrollResponse enrollFaces(FaceEnrollRequest request);

    /**
     * Verifies a captured live camera face against a student's enrolled embeddings.
     * 
     * @param studentId Student identifier
     * @param faceImageBase64 Captured live image base64 data
     * @return FaceVerifyResponse containing matched boolean and similarity score
     */
    FaceVerifyResponse verifyFace(String studentId, String faceImageBase64);

    /**
     * Verifies live face and returns structured FaceVerificationResult.
     */
    com.smarthostel.dto.FaceVerificationResult verifyStudentFace(String studentId, String faceImageBase64);

    /**
     * Checks registration status and count of enrolled photos for a student.
     * 
     * @param studentId Student identifier
     * @return FaceStatusResponse
     */
    FaceStatusResponse getFaceStatus(String studentId);

    /**
     * Resets all registered facial templates for a student.
     * 
     * @param studentId Student identifier
     */
    void resetFaceProfiles(String studentId);

    /**
     * Returns true if the student has at least 1 registered face template.
     * 
     * @param studentId Student identifier
     * @return boolean
     */
    boolean isFaceRegistered(String studentId);

    /**
     * Checks if the biometric microservice is enabled and reachable.
     * 
     * @return boolean
     */
    boolean isBiometricEnabled();
}
