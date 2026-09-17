package com.smarthostel.service;

import com.smarthostel.dto.AuthResponse;
import com.smarthostel.dto.LoginRequest;
import com.smarthostel.dto.RegisterAdminRequest;
import com.smarthostel.dto.RegisterRequest;
import com.smarthostel.model.Admin;
import com.smarthostel.model.Hostel;
import com.smarthostel.model.Student;
import com.smarthostel.repository.AdminRepository;
import com.smarthostel.repository.HostelRepository;
import com.smarthostel.repository.StudentRepository;
import com.smarthostel.util.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * ============================================================================
 * AUTHENTICATION SERVICE (Role-Based Registration & Login)
 * ============================================================================
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository;
    private final HostelRepository hostelRepository;

    @Autowired
    public AuthService(StudentRepository studentRepository, 
                       AdminRepository adminRepository, 
                       HostelRepository hostelRepository) {
        this.studentRepository = studentRepository;
        this.adminRepository = adminRepository;
        this.hostelRepository = hostelRepository;
    }

    /**
     * Registers a new student.
     */
    @Transactional
    public AuthResponse registerStudent(RegisterRequest request) {
        log.info("Processing student registration for ID: {}, Email: {}", request.getStudentId(), request.getEmail());

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (studentRepository.existsByStudentId(request.getStudentId().trim())) {
            throw new IllegalArgumentException("Student ID '" + request.getStudentId() + "' is already registered");
        }

        if (studentRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new IllegalArgumentException("Email address '" + request.getEmail() + "' is already in use");
        }

        Hostel hostel = hostelRepository.findById(request.getHostelId())
                .orElseThrow(() -> new IllegalArgumentException("Selected hostel not found (ID: " + request.getHostelId() + ")"));

        String hashedPassword = PasswordHasher.hash(request.getPassword());

        Student student = new Student(
                request.getStudentId().trim().toUpperCase(),
                request.getName().trim(),
                request.getEmail().trim().toLowerCase(),
                request.getPhone().trim(),
                request.getCourse() != null ? request.getCourse().trim() : "B.Tech Computer Science and Engineering",
                request.getDepartment() != null ? request.getDepartment().trim() : "Computer Science and Engineering",
                request.getAcademicYear() != null ? request.getAcademicYear().trim() : "2023 - 2027",
                hostel,
                request.getRoomNumber().trim(),
                hashedPassword
        );

        Student savedStudent = studentRepository.save(student);
        log.info("Student successfully registered with ID: {}", savedStudent.getId());

        String sessionToken = "student_token_" + UUID.randomUUID().toString();
        return new AuthResponse(sessionToken, savedStudent, "Student account created successfully");
    }

    /**
     * Authenticates student credentials.
     */
    public AuthResponse loginStudent(LoginRequest request) {
        String identifier = request.getIdentifier().trim();
        log.info("Authenticating student with identifier: {}", identifier);

        Optional<Student> studentOpt = studentRepository.findByStudentId(identifier.toUpperCase());
        if (studentOpt.isEmpty()) {
            studentOpt = studentRepository.findByEmail(identifier.toLowerCase());
        }

        if (studentOpt.isEmpty()) {
            log.warn("Student login failed: Identifier '{}' not found", identifier);
            throw new IllegalArgumentException("Invalid Student ID/Email or Password.");
        }

        Student student = studentOpt.get();

        if (!PasswordHasher.verify(request.getPassword(), student.getPasswordHash())) {
            log.warn("Student login failed: Password mismatch for student '{}'", student.getStudentId());
            throw new IllegalArgumentException("Invalid Student ID/Email or Password.");
        }

        log.info("Student login successful for: {}", student.getStudentId());
        String sessionToken = "student_token_" + UUID.randomUUID().toString();
        return new AuthResponse(sessionToken, student, "Student login successful");
    }

    /**
     * Registers a new administrator.
     */
    @Transactional
    public AuthResponse registerAdmin(RegisterAdminRequest request) {
        log.info("Processing admin registration for ID: {}, Email: {}", request.getAdminId(), request.getEmail());

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (adminRepository.existsByAdminId(request.getAdminId().trim())) {
            throw new IllegalArgumentException("Admin ID '" + request.getAdminId() + "' is already registered");
        }

        if (adminRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new IllegalArgumentException("Email address '" + request.getEmail() + "' is already in use");
        }

        Hostel hostel = hostelRepository.findById(request.getHostelId())
                .orElseThrow(() -> new IllegalArgumentException("Selected hostel not found (ID: " + request.getHostelId() + ")"));

        String hashedPassword = PasswordHasher.hash(request.getPassword());

        Admin admin = new Admin(
                request.getAdminId().trim().toUpperCase(),
                request.getFullName().trim(),
                request.getEmail().trim().toLowerCase(),
                request.getPhone().trim(),
                hostel,
                hashedPassword
        );

        Admin savedAdmin = adminRepository.save(admin);
        log.info("Admin registered successfully with ID: {}", savedAdmin.getId());

        String sessionToken = "admin_token_" + UUID.randomUUID().toString();
        return new AuthResponse(sessionToken, savedAdmin, "Administrator account created successfully");
    }

    /**
     * Authenticates administrator credentials.
     */
    public AuthResponse loginAdmin(LoginRequest request) {
        String identifier = request.getIdentifier().trim();
        log.info("Authenticating admin with identifier: {}", identifier);

        Optional<Admin> adminOpt = adminRepository.findByAdminId(identifier.toUpperCase());
        if (adminOpt.isEmpty()) {
            adminOpt = adminRepository.findByEmail(identifier.toLowerCase());
        }

        if (adminOpt.isEmpty()) {
            log.warn("Admin login failed: Identifier '{}' not found", identifier);
            throw new IllegalArgumentException("Invalid Admin ID/Email or Password.");
        }

        Admin admin = adminOpt.get();

        if (!PasswordHasher.verify(request.getPassword(), admin.getPasswordHash())) {
            log.warn("Admin login failed: Password mismatch for admin '{}'", admin.getAdminId());
            throw new IllegalArgumentException("Invalid Admin ID/Email or Password.");
        }

        log.info("Admin login successful for: {}", admin.getAdminId());
        String sessionToken = "admin_token_" + UUID.randomUUID().toString();
        return new AuthResponse(sessionToken, admin, "Administrator login successful");
    }

    // --- Backward Compatibility Aliases ---
    public AuthResponse register(RegisterRequest request) {
        return registerStudent(request);
    }

    public AuthResponse login(LoginRequest request) {
        return loginStudent(request);
    }
}
