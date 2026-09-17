package com.smarthostel.controller;

import com.smarthostel.dto.AdminDashboardResponse;
import com.smarthostel.dto.ApiResponse;
import com.smarthostel.dto.StudentAttendanceDetailDTO;
import com.smarthostel.model.Admin;
import com.smarthostel.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * ADMIN CONTROLLER (REST API for Hostel Administrator Dashboard & Monitoring)
 * ============================================================================
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Retrieves Administrator Dashboard overview with real counts, attendance rate, and table.
     * GET /api/admin/dashboard?adminId=ADM-HST-001&date=2026-09-14
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard(
            @RequestParam(required = false) String adminId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            LocalDate selectedDate = (date != null) ? date : LocalDate.now();
            AdminDashboardResponse dashboard = adminService.getDashboard(adminId, selectedDate);
            return ResponseEntity.ok(ApiResponse.ok("Admin dashboard overview retrieved", dashboard));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to load admin dashboard: " + ex.getMessage()));
        }
    }

    /**
     * Retrieves attendance list for a specific hostel and date.
     * GET /api/admin/attendance?hostelId=1&date=2026-09-14
     */
    @GetMapping("/attendance")
    public ResponseEntity<ApiResponse<List<StudentAttendanceDetailDTO>>> getAttendance(
            @RequestParam(required = false) Long hostelId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            LocalDate selectedDate = (date != null) ? date : LocalDate.now();
            AdminDashboardResponse dashboard = adminService.getDashboard(null, selectedDate);
            return ResponseEntity.ok(ApiResponse.ok("Attendance records loaded", dashboard.getStudentRecords()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to load attendance records: " + ex.getMessage()));
        }
    }

    /**
     * Searches students by name, ID, or room number.
     * GET /api/admin/students?hostelId=1&query=Rahul
     */
    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<StudentAttendanceDetailDTO>>> searchStudents(
            @RequestParam(required = false) Long hostelId,
            @RequestParam(required = false) String query) {
        try {
            List<StudentAttendanceDetailDTO> students = adminService.searchStudents(hostelId, query);
            return ResponseEntity.ok(ApiResponse.ok("Student search results", students));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to search students: " + ex.getMessage()));
        }
    }

    /**
     * Retrieves individual student details and attendance history.
     * GET /api/admin/students/{studentId}
     */
    @GetMapping("/students/{studentId}")
    public ResponseEntity<ApiResponse<StudentAttendanceDetailDTO>> getStudentDetail(@PathVariable String studentId) {
        try {
            StudentAttendanceDetailDTO detail = adminService.getStudentDetails(studentId);
            return ResponseEntity.ok(ApiResponse.ok("Student detail loaded", detail));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to load student details: " + ex.getMessage()));
        }
    }

    /**
     * Retrieves Administrator profile details.
     * GET /api/admin/profile?adminId=ADM-HST-001
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Admin>> getAdminProfile(@RequestParam String adminId) {
        Optional<Admin> adminOpt = adminService.getAdminProfile(adminId);
        if (adminOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Admin not found with ID: " + adminId));
        }
        return ResponseEntity.ok(ApiResponse.ok("Admin profile loaded", adminOpt.get()));
    }
}
