package com.smarthostel.dto;

import java.util.List;

public class AdminDashboardResponse {

    private Long hostelId;
    private String hostelName;
    private String adminName;
    private String adminId;
    private String selectedDate;
    private long totalStudents;
    private long presentCount;
    private long absentCount;
    private long notMarkedCount;
    private double attendancePercentage;
    private List<StudentAttendanceDetailDTO> studentRecords;

    public AdminDashboardResponse() {
    }

    public AdminDashboardResponse(Long hostelId, String hostelName, String adminName, String adminId, 
                                  String selectedDate, long totalStudents, long presentCount, 
                                  long absentCount, long notMarkedCount, double attendancePercentage, 
                                  List<StudentAttendanceDetailDTO> studentRecords) {
        this.hostelId = hostelId;
        this.hostelName = hostelName;
        this.adminName = adminName;
        this.adminId = adminId;
        this.selectedDate = selectedDate;
        this.totalStudents = totalStudents;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.notMarkedCount = notMarkedCount;
        this.attendancePercentage = attendancePercentage;
        this.studentRecords = studentRecords;
    }

    public Long getHostelId() {
        return hostelId;
    }

    public void setHostelId(Long hostelId) {
        this.hostelId = hostelId;
    }

    public String getHostelName() {
        return hostelName;
    }

    public void setHostelName(String hostelName) {
        this.hostelName = hostelName;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getPresentCount() {
        return presentCount;
    }

    public void setPresentCount(long presentCount) {
        this.presentCount = presentCount;
    }

    public long getAbsentCount() {
        return absentCount;
    }

    public void setAbsentCount(long absentCount) {
        this.absentCount = absentCount;
    }

    public long getNotMarkedCount() {
        return notMarkedCount;
    }

    public void setNotMarkedCount(long notMarkedCount) {
        this.notMarkedCount = notMarkedCount;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public List<StudentAttendanceDetailDTO> getStudentRecords() {
        return studentRecords;
    }

    public void setStudentRecords(List<StudentAttendanceDetailDTO> studentRecords) {
        this.studentRecords = studentRecords;
    }
}
