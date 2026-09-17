package com.smarthostel.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * ============================================================================
 * ATTENDANCE ENTITY (Java OOP Model Layer)
 * ============================================================================
 * 
 * Represents an attendance record marked by a student after server-side
 * geolocation verification.
 */
@Entity
@Table(name = "attendance", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "attendance_date"}, name = "uq_student_daily_attendance")
})
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false, length = 50)
    private String studentId;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "attendance_time", nullable = false)
    private LocalTime attendanceTime;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "distance_from_hostel", nullable = false)
    private Double distanceFromHostel; // in meters

    @Column(name = "location_status", nullable = false, length = 30)
    private String locationStatus = "VERIFIED"; // VERIFIED | OUTSIDE_HOSTEL

    @Column(name = "attendance_status", nullable = false, length = 30)
    private String attendanceStatus = "PRESENT"; // PRESENT | REJECTED | ABSENT

    @Column(name = "verification_mode", nullable = false, length = 50)
    private String verificationMode = "GEOLOCATION";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Default Constructor (Required by JPA)
    public Attendance() {
    }

    // Parameterized Constructor
    public Attendance(String studentId, LocalDate attendanceDate, LocalTime attendanceTime,
                      Double latitude, Double longitude, Double distanceFromHostel,
                      String locationStatus, String attendanceStatus, String verificationMode) {
        this.studentId = studentId;
        this.attendanceDate = attendanceDate != null ? attendanceDate : LocalDate.now();
        this.attendanceTime = attendanceTime != null ? attendanceTime : LocalTime.now();
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceFromHostel = distanceFromHostel;
        this.locationStatus = locationStatus;
        this.attendanceStatus = attendanceStatus;
        this.verificationMode = (verificationMode != null) ? verificationMode : "GEOLOCATION";
        this.createdAt = LocalDateTime.now();
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

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public LocalTime getAttendanceTime() {
        return attendanceTime;
    }

    public void setAttendanceTime(LocalTime attendanceTime) {
        this.attendanceTime = attendanceTime;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getDistanceFromHostel() {
        return distanceFromHostel;
    }

    public void setDistanceFromHostel(Double distanceFromHostel) {
        this.distanceFromHostel = distanceFromHostel;
    }

    public String getLocationStatus() {
        return locationStatus;
    }

    public void setLocationStatus(String locationStatus) {
        this.locationStatus = locationStatus;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public String getVerificationMode() {
        return verificationMode;
    }

    public void setVerificationMode(String verificationMode) {
        this.verificationMode = verificationMode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
