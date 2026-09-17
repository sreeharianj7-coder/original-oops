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
@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    private final LocationService locationService;
    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(LocationService locationService, AttendanceService attendanceService) {
        this.locationService = locationService;
        this.attendanceService = attendanceService;
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
     * Informational endpoint describing future Face Recognition module.
     * GET /api/attendance/face-module-info
     */
    @GetMapping("/face-module-info")
    public ResponseEntity<ApiResponse<Map<String, String>>> getFaceModuleInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("moduleName", "Face Recognition Biometric Verification");
        info.put("phase", "FUTURE DEVELOPMENT (Phase 2 Roadmap)");
        info.put("currentAuthentication", "Secure Login + GPS Geolocation Distance Verification");
        info.put("notice", "Future biometrics will integrate via dedicated secure microservices in Phase 2.");
        return ResponseEntity.ok(ApiResponse.ok("Future module specifications", info));
    }
}
