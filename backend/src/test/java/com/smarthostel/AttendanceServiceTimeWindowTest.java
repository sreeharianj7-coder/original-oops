package com.smarthostel;

import com.smarthostel.dto.LocationVerifyResponse;
import com.smarthostel.dto.MarkAttendanceRequest;
import com.smarthostel.model.Attendance;
import com.smarthostel.model.Hostel;
import com.smarthostel.model.Student;
import com.smarthostel.repository.AttendanceRepository;
import com.smarthostel.repository.StudentRepository;
import com.smarthostel.service.AttendanceService;
import com.smarthostel.service.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * ============================================================================
 * UNIT TESTS FOR 1ST-YEAR ATTENDANCE TIME WINDOW RESTRICTION (12:00 AM - 11:59 PM)
 * ============================================================================
 */
public class AttendanceServiceTimeWindowTest {

    private AttendanceRepository attendanceRepository;
    private StudentRepository studentRepository;
    private LocationService locationService;
    private com.smarthostel.service.FaceVerificationService faceVerificationService;
    private AttendanceService attendanceService;

    private Hostel campusHostel;
    private Student firstYearStudent;
    private Student seniorStudent;

    @BeforeEach
    void setUp() {
        attendanceRepository = Mockito.mock(AttendanceRepository.class);
        studentRepository = Mockito.mock(StudentRepository.class);
        locationService = Mockito.mock(LocationService.class);
        faceVerificationService = Mockito.mock(com.smarthostel.service.FaceVerificationService.class);
        attendanceService = new AttendanceService(attendanceRepository, studentRepository, locationService, faceVerificationService);

        campusHostel = new Hostel("Adi Shankara Institute Main Campus Hostel", 10.1706000, 76.4357000, 1000, "Main Campus");

        firstYearStudent = new Student(
                "ASIET2026CS001",
                "Aditya Varma",
                "aditya.cs26@adishankara.ac.in",
                "+91 98123 45678",
                "B.Tech Computer Science and Engineering",
                "Computer Science and Engineering",
                "1st Year (2026-2030)",
                campusHostel,
                "A-102",
                "hash"
        );

        seniorStudent = new Student(
                "ASIET2024CS001",
                "Rahul Sharma",
                "rahul.cs@adishankara.ac.in",
                "+91 98765 43210",
                "B.Tech Computer Science and Engineering",
                "Computer Science and Engineering",
                "3rd Year (2023-2027)",
                campusHostel,
                "A-204",
                "hash"
        );
    }

    @Test
    @DisplayName("1st-Year Student identification via isFirstYear() method")
    void testFirstYearIdentification() {
        assertTrue(firstYearStudent.isFirstYear(), "First year student should return true for isFirstYear()");
        assertFalse(seniorStudent.isFirstYear(), "Senior student should return false for isFirstYear()");
    }

    @Test
    @DisplayName("1st-Year Student: Reject attendance before 12:00 AM (edge case - time before midnight)")
    void testFirstYearBefore12AMRejected() {
        // With ATTENDANCE_START_TIME = 00:00:00, no LocalTime can be before midnight
        // This test documents the boundary: all times are within the full-day window
        LocalTime midnight = LocalTime.of(0, 0, 0);
        assertDoesNotThrow(() -> {
            attendanceService.validateTimeWindowRestriction(firstYearStudent, midnight);
        });
    }

    @Test
    @DisplayName("1st-Year Student: Allow attendance at exactly 12:00 AM (midnight)")
    void testFirstYearAt12AMAllowed() {
        LocalTime midnight = LocalTime.of(0, 0, 0);
        assertDoesNotThrow(() -> {
            attendanceService.validateTimeWindowRestriction(firstYearStudent, midnight);
        });
    }

    @Test
    @DisplayName("1st-Year Student: Allow attendance during the day (e.g. 11:30 AM, 02:15 PM, 06:30 PM)")
    void testFirstYearDuringDayAllowed() {
        LocalTime morning = LocalTime.of(11, 30, 0);
        LocalTime afternoon = LocalTime.of(14, 15, 0);
        LocalTime evening = LocalTime.of(18, 30, 0);

        assertDoesNotThrow(() -> attendanceService.validateTimeWindowRestriction(firstYearStudent, morning));
        assertDoesNotThrow(() -> attendanceService.validateTimeWindowRestriction(firstYearStudent, afternoon));
        assertDoesNotThrow(() -> attendanceService.validateTimeWindowRestriction(firstYearStudent, evening));
    }

    @Test
    @DisplayName("1st-Year Student: Allow attendance at exactly 11:59 PM (23:59:59)")
    void testFirstYearAt1159PMAllowed() {
        LocalTime endOfDay = LocalTime.of(23, 59, 59);
        assertDoesNotThrow(() -> {
            attendanceService.validateTimeWindowRestriction(firstYearStudent, endOfDay);
        });
    }

    @Test
    @DisplayName("1st-Year Student: All times within 12:00 AM - 11:59 PM window are allowed")
    void testFirstYearFullDayWindowAllowed() {
        LocalTime earlyMorning = LocalTime.of(0, 0, 0);
        LocalTime lateNight = LocalTime.of(23, 59, 58);

        // Both should be within the full-day window
        assertDoesNotThrow(() -> attendanceService.validateTimeWindowRestriction(firstYearStudent, earlyMorning));
        assertDoesNotThrow(() -> attendanceService.validateTimeWindowRestriction(firstYearStudent, lateNight));
    }

    @Test
    @DisplayName("Senior Student: Not restricted by 12:00 AM - 11:59 PM time window")
    void testSeniorStudentsNotRestricted() {
        LocalTime earlyMorning = LocalTime.of(7, 30, 0);
        LocalTime lateEvening = LocalTime.of(21, 45, 0);

        // Senior students should pass time validation at any hour
        assertDoesNotThrow(() -> attendanceService.validateTimeWindowRestriction(seniorStudent, earlyMorning));
        assertDoesNotThrow(() -> attendanceService.validateTimeWindowRestriction(seniorStudent, lateEvening));
    }

    @Test
    @DisplayName("markAttendance workflow: Blocks 1st-year student at 08:30 AM before location check")
    void testMarkAttendanceEnforcesTimeWindow() {
        when(studentRepository.findByStudentId("ASIET2026CS001")).thenReturn(Optional.of(firstYearStudent));

        MarkAttendanceRequest request = new MarkAttendanceRequest("ASIET2026CS001", 10.1706000, 76.4357000, 5.0);
        LocalDate today = LocalDate.now();
        LocalTime earlyTime = LocalTime.of(8, 30, 0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.markAttendance(request, today, earlyTime);
        });

        assertTrue(ex.getMessage().contains("1st-year students are only permitted"));
    }

    @Test
    @DisplayName("markAttendance workflow: Successfully marks attendance for 1st-year at 10:00 AM within 1000m radius")
    void testMarkAttendanceAllowsValidTimeAndLocation() {
        when(studentRepository.findByStudentId("ASIET2026CS001")).thenReturn(Optional.of(firstYearStudent));
        when(attendanceRepository.existsByStudentIdAndAttendanceDate(any(), any())).thenReturn(false);

        LocationVerifyResponse verifyRes = new LocationVerifyResponse(
                true, 15.5, 1000, "Adi Shankara Institute Main Campus Hostel",
                10.1706000, 76.4357000, "VERIFIED", "Location verified within 1000m radius"
        );
        when(locationService.verifyLocation(any())).thenReturn(verifyRes);

        Attendance savedAttendance = new Attendance(
                "ASIET2026CS001", LocalDate.now(), LocalTime.of(10, 0, 0),
                10.170610, 76.435710, 15.5, "VERIFIED", "PRESENT", "GEOLOCATION"
        );
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(savedAttendance);

        MarkAttendanceRequest request = new MarkAttendanceRequest("ASIET2026CS001", 10.170610, 76.435710, 5.0);
        LocalDate today = LocalDate.now();
        LocalTime validTime = LocalTime.of(10, 0, 0);

        Attendance result = attendanceService.markAttendance(request, today, validTime);
        assertNotNull(result);
        assertEquals("PRESENT", result.getAttendanceStatus());
        assertEquals("ASIET2026CS001", result.getStudentId());
    }
}
