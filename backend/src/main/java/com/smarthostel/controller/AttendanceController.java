package com.smarthostel.controller;

import com.smarthostel.dto.ApiResponse;
import com.smarthostel.dto.LocationVerifyRequest;
import com.smarthostel.dto.LocationVerifyResponse;
import com.smarthostel.dto.MarkAttendanceRequest;
import com.smarthostel.model.Attendance;
import com.smarthostel.service.AttendanceService;
import com.smarthostel.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * ATTENDANCE CONTROLLER (REST API for Geolocation & Attendance Recording)
 * ============================================================================
 */
import com.smarthostel.dto.FaceVerificationResult;
import com.smarthostel.dto.MonthlyAttendanceResponse;
import com.smarthostel.service.FaceVerificationService;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    private final LocationService locationService;
    private final AttendanceService attendanceService;
    private final FaceVerificationService faceVerificationService;

    @Autowired
    public AttendanceController(LocationService locationService, 
                                AttendanceService attendanceService,
                                FaceVerificationService faceVerificationService) {
        this.locationService = locationService;
        this.attendanceService = attendanceService;
        this.faceVerificationService = faceVerificationService;
    }

    /**
     * Verifies live face photo against student enrolled templates.
     * POST /api/attendance/verify-face
     */
    @PostMapping("/verify-face")
    public ResponseEntity<ApiResponse<FaceVerificationResult>> verifyFace(@RequestBody Map<String, String> payload) {
        try {
            String studentId = payload.get("studentId");
            String faceImage = payload.get("faceImage");
            if (studentId == null || faceImage == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Missing required parameters: studentId and faceImage"));
            }

            FaceVerificationResult result = faceVerificationService.verifyStudentFace(studentId, faceImage);
            if (result.isMatched()) {
                return ResponseEntity.ok(ApiResponse.ok("Face verified successfully", result));
            } else {
                return ResponseEntity.ok(new ApiResponse<>(false, result.getMessage(), result));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Face verification failed: " + ex.getMessage()));
        }
    }

    /**
     * Verifies student GPS coordinates against hostel allowed radius.
     * POST /api/attendance/verify-location
     */
    @PostMapping("/verify-location")
    public ResponseEntity<ApiResponse<LocationVerifyResponse>> verifyLocation(@Valid @RequestBody LocationVerifyRequest request) {
        try {
            LocationVerifyResponse response = locationService.verifyLocation(request);
            if (response.isVerified()) {
                return ResponseEntity.ok(ApiResponse.ok("Location verified successfully", response));
            } else {
                return ResponseEntity.ok(new ApiResponse<>(false, response.getMessage(), response));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Location verification error: " + ex.getMessage()));
        }
    }

    /**
     * Marks student attendance after server-side location verification.
     * POST /api/attendance/mark
     */
    @PostMapping("/mark")
    public ResponseEntity<ApiResponse<Attendance>> markAttendance(@Valid @RequestBody MarkAttendanceRequest request) {
        try {
            Attendance attendance = attendanceService.markAttendance(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Attendance Marked Successfully", attendance));
        } catch (IllegalStateException ex) {
            // Duplicate attendance on same day
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            // Outside hostel radius or invalid student
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Attendance processing failed: " + ex.getMessage()));
        }
    }

    /**
     * Retrieves attendance history for the requesting student.
     * GET /api/attendance/history?studentId=ASIET2024CS001
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<Attendance>>> getHistory(@RequestParam String studentId) {
        try {
            List<Attendance> history = attendanceService.getAttendanceHistory(studentId);
            return ResponseEntity.ok(ApiResponse.ok("Attendance history loaded", history));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to load history: " + ex.getMessage()));
        }
    }

    /**
     * Retrieves attendance statistics for dashboard cards.
     * GET /api/attendance/stats?studentId=ASIET2024CS001
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats(@RequestParam String studentId) {
        try {
            Map<String, Object> stats = attendanceService.getStudentAttendanceStats(studentId);
            return ResponseEntity.ok(ApiResponse.ok("Attendance statistics loaded", stats));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to load stats: " + ex.getMessage()));
        }
    }

    /**
     * Retrieves attendance history for a student by path variable.
     * GET /api/attendance/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<Attendance>>> getStudentAttendance(@PathVariable String studentId) {
        try {
            List<Attendance> history = attendanceService.getAttendanceHistory(studentId);
            return ResponseEntity.ok(ApiResponse.ok("Attendance history loaded", history));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to load history: " + ex.getMessage()));
        }
    }

    /**
     * Retrieves monthly attendance calendar data with status dots.
     * GET /api/attendance/student/{studentId}/month?year=2026&month=9
     */
    @GetMapping("/student/{studentId}/month")
    public ResponseEntity<ApiResponse<MonthlyAttendanceResponse>> getStudentMonthlyAttendance(
            @PathVariable String studentId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        try {
            LocalDate now = LocalDate.now();
            int reqYear = (year != null && year > 0) ? year : now.getYear();
            int reqMonth = (month != null && month >= 1 && month <= 12) ? month : now.getMonthValue();
            MonthlyAttendanceResponse response = attendanceService.getMonthlyAttendance(studentId, reqYear, reqMonth);
            return ResponseEntity.ok(ApiResponse.ok("Monthly attendance records loaded", response));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to load monthly attendance: " + ex.getMessage()));
        }
    }

    /**
     * Alias for monthly attendance using query params.
     * GET /api/attendance/month?studentId=...&year=...&month=...
     */
    @GetMapping("/month")
    public ResponseEntity<ApiResponse<MonthlyAttendanceResponse>> getMonthlyAttendanceQuery(
            @RequestParam String studentId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return getStudentMonthlyAttendance(studentId, year, month);
    }
}
