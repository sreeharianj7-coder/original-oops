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
 * UNIT TESTS FOR 1ST-YEAR ATTENDANCE TIME WINDOW RESTRICTION (9:00 AM - 5:00 PM)
 * ============================================================================
 */
public class AttendanceServiceTimeWindowTest {

    private AttendanceRepository attendanceRepository;
    private StudentRepository studentRepository;
    private LocationService locationService;
    private AttendanceService attendanceService;

    private Hostel campusHostel;
    private Student firstYearStudent;
    private Student seniorStudent;

    @BeforeEach
    void setUp() {
        attendanceRepository = Mockito.mock(AttendanceRepository.class);
        studentRepository = Mockito.mock(StudentRepository.class);
        locationService = Mockito.mock(LocationService.class);
        attendanceService = new AttendanceService(attendanceRepository, studentRepository, locationService);

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
    @DisplayName("1st-Year Student: Reject attendance before 9:00 AM (e.g. 08:59:59 AM)")
    void testFirstYearBefore9AMRejected() {
        LocalTime earlyMorning = LocalTime.of(8, 59, 59);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.validateTimeWindowRestriction(firstYearStudent, earlyMorning);
        });

        assertTrue(ex.getMessage().contains("09:00 AM and 05:00 PM"), "Exception should mention allowed window");
    }

    @Test
    @DisplayName("1st-Year Student: Allow attendance at exactly 9:00 AM")
    void testFirstYearAt9AMAllowed() {
        LocalTime exactlyNineAM = LocalTime.of(9, 0, 0);
        assertDoesNotThrow(() -> {
            attendanceService.validateTimeWindowRestriction(firstYearStudent, exactlyNineAM);
        });
    }

    @Test
    @DisplayName("1st-Year Student: Allow attendance during the day (e.g. 11:30 AM, 02:15 PM)")
    void testFirstYearDuringDayAllowed() {
        LocalTime morning = LocalTime.of(11, 30, 0);
        LocalTime afternoon = LocalTime.of(14, 15, 0);

        assertDoesNotThrow(() -> attendanceService.validateTimeWindowRestriction(firstYearStudent, morning));
        assertDoesNotThrow(() -> attendanceService.validateTimeWindowRestriction(firstYearStudent, afternoon));
    }

    @Test
    @DisplayName("1st-Year Student: Allow attendance at exactly 5:00 PM (17:00:00)")
    void testFirstYearAt5PMAllowed() {
        LocalTime exactlyFivePM = LocalTime.of(17, 0, 0);
        assertDoesNotThrow(() -> {
            attendanceService.validateTimeWindowRestriction(firstYearStudent, exactlyFivePM);
        });
    }

    @Test
    @DisplayName("1st-Year Student: Reject attendance after 5:00 PM (e.g. 05:00:01 PM, 06:30 PM)")
    void testFirstYearAfter5PMRejected() {
        LocalTime justPastFive = LocalTime.of(17, 0, 1);
        LocalTime evening = LocalTime.of(18, 30, 0);

        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.validateTimeWindowRestriction(firstYearStudent, justPastFive);
        });
        assertTrue(ex1.getMessage().contains("09:00 AM and 05:00 PM"));

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.validateTimeWindowRestriction(firstYearStudent, evening);
        });
        assertTrue(ex2.getMessage().contains("09:00 AM and 05:00 PM"));
    }

    @Test
    @DisplayName("Senior Student: Not restricted by 9:00 AM - 5:00 PM time window")
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
