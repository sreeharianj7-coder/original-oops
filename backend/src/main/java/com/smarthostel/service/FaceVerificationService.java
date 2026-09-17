package com.smarthostel.service;

/**
 * ============================================================================
 * FACE RECOGNITION MODULE — FUTURE IMPLEMENTATION INTERFACE
 * ============================================================================
 * 
 * DESIGN NOTE:
 * This interface establishes the architectural contract for facial biometrics
 * verification planned for future phases of the Smart Hostel Attendance System.
 * 
 * IMPORTANT SCOPE RULES:
 * 1. The current production phase operates strictly on Secure Student Login
 *    + Real-time Geolocation Verification (Haversine distance validation).
 * 2. Future biometric implementation will evaluate secure, modern facial
 *    biometric microservices in Phase 2.
 * 
 * OOP Concept:
 *   - Abstraction & Interface Segregation: Defines what a future verification
 *     module must implement without coupling the system to a specific biometric library.
 */
public interface FaceVerificationService {

    /**
     * Future biometric verification signature.
     * 
     * @param studentId Unique student identifier
     * @param imagePayload Base64 encoded or byte payload of facial frame
     * @return true if face matches enrolled student template, false otherwise
     */
    boolean verifyFace(String studentId, Object imagePayload);

    /**
     * Checks if the biometric service is currently enabled in system configuration.
     * 
     * @return false (Current development phase uses Geolocation Verification)
     */
    default boolean isBiometricEnabled() {
        return false;
    }

    /**
     * Descriptive status of the Face Recognition module.
     * 
     * @return Status message indicating future roadmap
     */
    default String getModuleStatus() {
        return "FACE RECOGNITION MODULE — FUTURE IMPLEMENTATION (Current Phase: Login + Geolocation)";
    }
}
