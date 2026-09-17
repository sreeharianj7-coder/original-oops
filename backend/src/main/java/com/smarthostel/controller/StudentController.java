package com.smarthostel.controller;

import com.smarthostel.dto.ApiResponse;
import com.smarthostel.dto.MonthlyAttendanceResponse;
import com.smarthostel.model.Attendance;
import com.smarthostel.model.Student;
import com.smarthostel.service.AttendanceService;
import com.smarthostel.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ============================================================================
 * STUDENT CONTROLLER (REST API for Student Profile & Monthly Attendance)
 * ============================================================================
 */
@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentService studentService;
    private final AttendanceService attendanceService;

    @Value("${app.college.name:Adi Shankara Institute of Science and Technology}")
    private String collegeName;

    @Value("${app.college.program:B.Tech Computer Science and Engineering}")
    private String programName;

    @Value("${app.college.department:Computer Science and Engineering}")
    private String departmentName;

    @Autowired
    public StudentController(StudentService studentService, AttendanceService attendanceService) {
        this.studentService = studentService;
        this.attendanceService = attendanceService;
    }

    /**
     * Retrieves student profile by studentId.
     * GET /api/student/profile?studentId=ASIET2024CS001
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile(@RequestParam String studentId) {
        Optional<Student> studentOpt = studentService.findByStudentId(studentId.trim());
        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Student not found with ID: " + studentId));
        }

        Student student = studentOpt.get();
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("student", student);
        responseData.put("college", Map.of(
                "collegeName", collegeName,
                "program", programName,
                "department", departmentName
        ));

        return ResponseEntity.ok(ApiResponse.ok("Student profile loaded successfully", responseData));
    }

    /**
     * Retrieves monthly attendance calendar data with status dots and summary statistics.
     * GET /api/student/attendance/monthly?studentId=ASIET2024CS001&year=2026&month=9
     */
    @GetMapping("/attendance/monthly")
    public ResponseEntity<ApiResponse<MonthlyAttendanceResponse>> getMonthlyAttendance(
            @RequestParam String studentId,
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
     * Retrieves full attendance history for a student.
     * GET /api/student/attendance/history?studentId=ASIET2024CS001
     */
    @GetMapping("/attendance/history")
    public ResponseEntity<ApiResponse<List<Attendance>>> getAttendanceHistory(@RequestParam String studentId) {
        try {
            List<Attendance> history = attendanceService.getAttendanceHistory(studentId);
            return ResponseEntity.ok(ApiResponse.ok("Attendance history loaded", history));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to load history: " + ex.getMessage()));
        }
    }

    /**
     * Retrieves static institution details.
     * GET /api/student/college-details
     */
    @GetMapping("/college-details")
    public ResponseEntity<ApiResponse<Map<String, String>>> getCollegeDetails() {
        Map<String, String> details = new HashMap<>();
        details.put("collegeName", collegeName);
        details.put("program", programName);
        details.put("department", departmentName);
        details.put("accreditation", "NAAC Accredited, Affiliated to APJ Abdul Kalam Technological University");
        details.put("location", "Mattoor, Kalady, Ernakulam, Kerala - 683574");

        return ResponseEntity.ok(ApiResponse.ok("College details retrieved", details));
    }
}
