package com.smarthostel.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthostel.dto.*;
import com.smarthostel.face.FaceEmbeddingEngine;
import com.smarthostel.model.StudentFaceProfile;
import com.smarthostel.repository.StudentFaceProfileRepository;
import com.smarthostel.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * ============================================================================
 * FACENET VERIFICATION SERVICE (OOP IMPLEMENTATION)
 * ============================================================================
 * Implements the FaceVerificationService interface using FaceNet-style 512-d
 * feature vector embeddings and cosine similarity comparison across multi-angle
 * registered facial templates.
 * 
 * Strict Constraint: ZERO OpenCV. Uses pure Java ImageIO and mathematical
 * spatial-gradient L2-normalized vector analysis.
 */
@Service
@Primary
public class FaceNetVerificationService implements FaceVerificationService {

    private static final Logger log = LoggerFactory.getLogger(FaceNetVerificationService.class);
    private static final int MAX_PHOTOS_ALLOWED = 10;

    private final StudentFaceProfileRepository faceProfileRepository;
    private final StudentRepository studentRepository;
    private final FaceEmbeddingEngine embeddingEngine;
    private final ObjectMapper objectMapper;

    @Value("${face.match.threshold:0.70}")
    private double matchThreshold;

    @Autowired
    public FaceNetVerificationService(StudentFaceProfileRepository faceProfileRepository,
                                      StudentRepository studentRepository,
                                      FaceEmbeddingEngine embeddingEngine,
                                      ObjectMapper objectMapper) {
        this.faceProfileRepository = faceProfileRepository;
        this.studentRepository = studentRepository;
        this.embeddingEngine = embeddingEngine;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public FaceEnrollResponse enrollFaces(FaceEnrollRequest request) {
        String studentId = request.getStudentId().trim().toUpperCase();
        List<String> images = request.getImages();

        log.info("Enrolling facial biometric profiles for Student: {} (Count: {})", 
                studentId, (images != null ? images.size() : 0));

        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("At least 3 facial photographs from different angles are recommended.");
        }

        if (images.size() > MAX_PHOTOS_ALLOWED) {
            throw new IllegalArgumentException("Maximum of " + MAX_PHOTOS_ALLOWED + " photos allowed.");
        }

        if (!studentRepository.existsByStudentId(studentId)) {
            throw new IllegalArgumentException("Student not found with ID: " + studentId);
        }

        // Clean out prior enrollments to store fresh representations
        faceProfileRepository.deleteByStudentId(studentId);

        List<FaceEnrollResponse.PhotoValidationResult> results = new ArrayList<>();
        int validCount = 0;

        for (int i = 0; i < images.size(); i++) {
            int photoIndex = i + 1;
            String base64Img = images.get(i);
            try {
                // Extract 512-d normalized embedding vector
                List<Double> embedding = embeddingEngine.extractEmbedding(base64Img);
                String embeddingJson = objectMapper.writeValueAsString(embedding);

                StudentFaceProfile profile = new StudentFaceProfile(studentId, photoIndex, embeddingJson);
                faceProfileRepository.save(profile);
                validCount++;

                results.add(new FaceEnrollResponse.PhotoValidationResult(
                        photoIndex, true, "FaceNet 512-d embedding extracted and saved successfully."
                ));
            } catch (Exception ex) {
                log.warn("Photo #{} rejected for student {}: {}", photoIndex, studentId, ex.getMessage());
                results.add(new FaceEnrollResponse.PhotoValidationResult(
                        photoIndex, false, "Image rejected: " + ex.getMessage()
                ));
            }
        }

        boolean success = validCount > 0;
        boolean allValid = validCount == images.size();
        String summary = success 
                ? "Successfully enrolled " + validCount + " of " + images.size() + " face photos."
                : "Biometric enrollment failed. Please upload clear face photos.";

        return new FaceEnrollResponse(success, studentId, validCount, images.size(), allValid, summary, results);
    }

    @Override
    public FaceVerifyResponse verifyFace(String studentId, String faceImageBase64) {
        FaceVerificationResult result = verifyStudentFace(studentId, faceImageBase64);
        return new FaceVerifyResponse(
                result.isMatched(),
                result.getSimilarityScore(),
                result.getThreshold(),
                result.getBestMatchIndex(),
                result.getMessage(),
                result.getStudentId()
        );
    }

    @Override
    public FaceVerificationResult verifyStudentFace(String studentId, String faceImageBase64) {
        String cleanId = studentId.trim().toUpperCase();
        log.info("Executing biometric face verification for student: {}", cleanId);

        if (faceImageBase64 == null || faceImageBase64.trim().isEmpty()) {
            return new FaceVerificationResult(false, 0.0, matchThreshold, 0,
                    "Live face capture image is missing.", cleanId);
        }

        List<StudentFaceProfile> profiles = faceProfileRepository.findByStudentIdOrderByPhotoIndexAsc(cleanId);
        if (profiles.isEmpty()) {
            return new FaceVerificationResult(false, 0.0, matchThreshold, 0,
                    "No registered face photos found for " + cleanId + ". Please complete face registration.", cleanId);
        }

        // 1. Extract 512-d feature vector from live captured photo
        List<Double> liveEmbedding;
        try {
            liveEmbedding = embeddingEngine.extractEmbedding(faceImageBase64);
        } catch (Exception ex) {
            log.warn("Live face processing failed for student {}: {}", cleanId, ex.getMessage());
            return new FaceVerificationResult(false, 0.0, matchThreshold, 0,
                    "Face not detected: " + ex.getMessage(), cleanId);
        }

        // 2. Compare against all registered photos of the student
        double bestSimilarity = -1.0;
        int bestMatchIndex = 1;

        for (StudentFaceProfile profile : profiles) {
            try {
                List<Double> enrolledVec = objectMapper.readValue(
                        profile.getEmbedding(), 
                        new TypeReference<List<Double>>() {}
                );
                double similarity = embeddingEngine.computeCosineSimilarity(liveEmbedding, enrolledVec);
                if (similarity > bestSimilarity) {
                    bestSimilarity = similarity;
                    bestMatchIndex = profile.getPhotoIndex();
                }
            } catch (Exception ex) {
                log.error("Failed to parse enrolled embedding #{} for {}: {}", 
                        profile.getPhotoIndex(), cleanId, ex.getMessage());
            }
        }

        // 3. Evaluate against threshold (0.70)
        boolean matched = bestSimilarity >= matchThreshold;
        String message = matched 
                ? "Face matches registered profile" 
                : "The captured face does not match the registered profile.";

        log.info("Biometric result for {}: matched={}, similarity={:.4f}, threshold={:.2f}, sample=#{}",
                cleanId, matched, bestSimilarity, matchThreshold, bestMatchIndex);

        return new FaceVerificationResult(matched, bestSimilarity, matchThreshold, bestMatchIndex, message, cleanId);
    }

    @Override
    public FaceStatusResponse getFaceStatus(String studentId) {
        String cleanId = studentId.trim().toUpperCase();
        long count = faceProfileRepository.countByStudentId(cleanId);
        boolean registered = count > 0;
        String statusText = registered ? "Registered" : "Not Registered";
        String message = registered
                ? "Registered (" + count + " photos enrolled)"
                : "Register 3–4 photos from different angles for better face verification.";

        return new FaceStatusResponse(cleanId, registered, (int) count, MAX_PHOTOS_ALLOWED, statusText, message);
    }

    @Override
    @Transactional
    public void resetFaceProfiles(String studentId) {
        String cleanId = studentId.trim().toUpperCase();
        log.info("Resetting face profiles for Student: {}", cleanId);
        faceProfileRepository.deleteByStudentId(cleanId);
    }

    @Override
    public boolean isFaceRegistered(String studentId) {
        return faceProfileRepository.existsByStudentId(studentId.trim().toUpperCase());
    }

    @Override
    public boolean isBiometricEnabled() {
        return true;
    }
}
