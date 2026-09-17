package com.smarthostel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ============================================================================
 * STUDENT ENTITY (Java OOP Model Layer)
 * ============================================================================
 * 
 * Represents student personal, academic, hostel allocation, and credential data.
 * Demonstrates OOP Encapsulation and Association with Hostel entity.
 */
@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false, unique = true, length = 50)
    private String studentId;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "course", nullable = false, length = 100)
    private String course = "B.Tech Computer Science and Engineering";

    @Column(name = "department", nullable = false, length = 100)
    private String department = "Computer Science and Engineering";

    @Column(name = "academic_year", nullable = false, length = 50)
    private String academicYear = "2023 - 2027";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hostel_id", nullable = false)
    private Hostel hostel;

    @Column(name = "room_number", nullable = false, length = 20)
    private String roomNumber;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "role", nullable = false, length = 30)
    private String role = "STUDENT";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Default Constructor (Required by JPA)
    public Student() {
    }

    // Parameterized Constructor
    public Student(String studentId, String name, String email, String phone, 
                   String course, String department, String academicYear, 
                   Hostel hostel, String roomNumber, String passwordHash) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.course = (course != null && !course.trim().isEmpty()) ? course : "B.Tech Computer Science and Engineering";
        this.department = (department != null && !department.trim().isEmpty()) ? department : "Computer Science and Engineering";
        this.academicYear = (academicYear != null && !academicYear.trim().isEmpty()) ? academicYear : "2023 - 2027";
        this.hostel = hostel;
        this.roomNumber = roomNumber;
        this.passwordHash = passwordHash;
        this.role = "STUDENT";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getters and Setters (Encapsulation) ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public Hostel getHostel() {
        return hostel;
    }

    public void setHostel(Hostel hostel) {
        this.hostel = hostel;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Checks if the student is in their 1st year of study.
     * Evaluates academicYear field for variations such as "1st Year", "1st Year (2026-2030)", "First Year", "Year 1", etc.
     */
    public boolean isFirstYear() {
        if (this.academicYear == null || this.academicYear.trim().isEmpty()) {
            return false;
        }
        String normalized = this.academicYear.trim().toLowerCase();
        return normalized.contains("1st") ||
               normalized.contains("first") ||
               normalized.startsWith("1") ||
               normalized.contains("year 1") ||
               normalized.contains("year-1");
    }
}
