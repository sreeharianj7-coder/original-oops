package com.smarthostel.dto;

import java.util.List;

public class StudentAttendanceDetailDTO {

    private String studentId;
    private String studentName;
    private String roomNumber;
    private String course;
    private String department;
    private String academicYear;
    private String phone;
    private String email;
    private String hostelName;
    private String attendanceStatus; // PRESENT | ABSENT | NOT_MARKED
    private String locationStatus;
    private String time;
    private String distance;
    private double overallPercentage;
    private boolean faceRegistered;
    private String faceStatus; // "Registered" | "Not Registered"
    private List<DayAttendanceDTO> recentHistory;

    public StudentAttendanceDetailDTO() {
    }

    public StudentAttendanceDetailDTO(String studentId, String studentName, String roomNumber, 
                                      String course, String department, String academicYear, 
                                      String phone, String email, String hostelName, 
                                      String attendanceStatus, String locationStatus, 
                                      String time, String distance, double overallPercentage) {
        this(studentId, studentName, roomNumber, course, department, academicYear, phone, email, hostelName,
             attendanceStatus, locationStatus, time, distance, overallPercentage, false, "Not Registered");
    }

    public StudentAttendanceDetailDTO(String studentId, String studentName, String roomNumber, 
                                      String course, String department, String academicYear, 
                                      String phone, String email, String hostelName, 
                                      String attendanceStatus, String locationStatus, 
                                      String time, String distance, double overallPercentage,
                                      boolean faceRegistered, String faceStatus) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.roomNumber = roomNumber;
        this.course = course;
        this.department = department;
        this.academicYear = academicYear;
        this.phone = phone;
        this.email = email;
        this.hostelName = hostelName;
        this.attendanceStatus = attendanceStatus;
        this.locationStatus = locationStatus;
        this.time = time;
        this.distance = distance;
        this.overallPercentage = overallPercentage;
        this.faceRegistered = faceRegistered;
        this.faceStatus = faceStatus;
    }


    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHostelName() {
        return hostelName;
    }

    public void setHostelName(String hostelName) {
        this.hostelName = hostelName;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public String getLocationStatus() {
        return locationStatus;
    }

    public void setLocationStatus(String locationStatus) {
        this.locationStatus = locationStatus;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public double getOverallPercentage() {
        return overallPercentage;
    }

    public void setOverallPercentage(double overallPercentage) {
        this.overallPercentage = overallPercentage;
    }

    public boolean isFaceRegistered() {
        return faceRegistered;
    }

    public void setFaceRegistered(boolean faceRegistered) {
        this.faceRegistered = faceRegistered;
    }

    public String getFaceStatus() {
        return faceStatus;
    }

    public void setFaceStatus(String faceStatus) {
        this.faceStatus = faceStatus;
    }

    public List<DayAttendanceDTO> getRecentHistory() {
        return recentHistory;
    }


    public void setRecentHistory(List<DayAttendanceDTO> recentHistory) {
        this.recentHistory = recentHistory;
    }
}
