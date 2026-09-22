package com.smarthostel.controller;

import com.smarthostel.dto.*;
import com.smarthostel.service.FaceVerificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ============================================================================
 * FACE BIOMETRICS CONTROLLER (REST API for Face Registration & Verification)
 * ============================================================================
 */
@RestController
@RequestMapping("/api/face")
@CrossOrigin(origins = "*")
public class FaceController {

    private final FaceVerificationService faceVerificationService;

    @Autowired
    public FaceController(FaceVerificationService faceVerificationService) {
        this.faceVerificationService = faceVerificationService;
    }

    /**
     * Enrolls up to 10 student facial photographs.
     * POST /api/face/enroll
     */
    @PostMapping("/enroll")
    public ResponseEntity<ApiResponse<FaceEnrollResponse>> enrollFaces(@Valid @RequestBody FaceEnrollRequest request) {
        try {
            FaceEnrollResponse response = faceVerificationService.enrollFaces(request);
            if (response.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.ok(response.getMessage(), response));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, response.getMessage(), response));
            }
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Facial enrollment failed: " + ex.getMessage()));
        }
    }

    /**
     * Checks student facial registration status.
     * GET /api/face/status?studentId=ASIET2024CS001
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<FaceStatusResponse>> getFaceStatus(@RequestParam String studentId) {
        try {
            FaceStatusResponse status = faceVerificationService.getFaceStatus(studentId);
            return ResponseEntity.ok(ApiResponse.ok("Face registration status loaded", status));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to load face status: " + ex.getMessage()));
        }
    }

    /**
     * Verifies live facial capture against enrolled student templates.
     * POST /api/face/verify
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<FaceVerifyResponse>> verifyLiveFace(@RequestBody Map<String, String> payload) {
        try {
            String studentId = payload.get("studentId");
            String faceImage = payload.get("faceImage");

            if (studentId == null || faceImage == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Missing required fields: studentId and faceImage"));
            }

            FaceVerifyResponse response = faceVerificationService.verifyFace(studentId, faceImage);
            if (response.isMatched()) {
                return ResponseEntity.ok(ApiResponse.ok("Face verified successfully", response));
            } else {
                return ResponseEntity.ok(new ApiResponse<>(false, response.getMessage(), response));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Face verification error: " + ex.getMessage()));
        }
    }

    /**
     * Resets registered facial profile for a student.
     * DELETE /api/face/reset?studentId=ASIET2024CS001
     */
    @DeleteMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> resetFaceProfiles(@RequestParam String studentId) {
        try {
            faceVerificationService.resetFaceProfiles(studentId);
            return ResponseEntity.ok(ApiResponse.ok("Face profiles reset successfully", null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to reset face profiles: " + ex.getMessage()));
        }
    }
}
