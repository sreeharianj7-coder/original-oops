package com.smarthostel.controller;

import com.smarthostel.dto.ApiResponse;
import com.smarthostel.dto.AuthResponse;
import com.smarthostel.dto.LoginRequest;
import com.smarthostel.dto.RegisterAdminRequest;
import com.smarthostel.dto.RegisterRequest;
import com.smarthostel.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ============================================================================
 * AUTH CONTROLLER (REST API for Student & Admin Registration & Login)
 * ============================================================================
 */
@RestController
@RequestMapping({"/api/auth", "/api"})
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new student account.
     * POST /api/auth/student/register OR POST /api/students/register
     */
    @PostMapping({"/student/register", "/students/register"})
    public ResponseEntity<ApiResponse<AuthResponse>> registerStudent(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.registerStudent(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Student registration successful", response));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Registration failed: " + ex.getMessage()));
        }
    }

    /**
     * Authenticates a student.
     * POST /api/auth/student/login OR POST /api/students/login
     */
    @PostMapping({"/student/login", "/students/login"})
    public ResponseEntity<ApiResponse<AuthResponse>> loginStudent(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.loginStudent(request);
            return ResponseEntity.ok(ApiResponse.ok("Student login successful", response));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Authentication error: " + ex.getMessage()));
        }
    }

    /**
     * Registers a new hostel administrator account.
     * POST /api/auth/admin/register OR POST /api/admin/register
     */
    @PostMapping("/admin/register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerAdmin(@Valid @RequestBody RegisterAdminRequest request) {
        try {
            AuthResponse response = authService.registerAdmin(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Administrator registration successful", response));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Admin registration failed: " + ex.getMessage()));
        }
    }

    /**
     * Authenticates a hostel administrator.
     * POST /api/auth/admin/login OR POST /api/admin/login
     */
    @PostMapping("/admin/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginAdmin(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.loginAdmin(request);
            return ResponseEntity.ok(ApiResponse.ok("Administrator login successful", response));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Admin authentication error: " + ex.getMessage()));
        }
    }

    // --- Backward Compatible Endpoints ---

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerLegacy(@Valid @RequestBody RegisterRequest request) {
        return registerStudent(request);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginLegacy(@Valid @RequestBody LoginRequest request) {
        return loginStudent(request);
    }
}
