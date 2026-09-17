package com.smarthostel.service;

import com.smarthostel.dto.DayAttendanceDTO;
import com.smarthostel.dto.LocationVerifyRequest;
import com.smarthostel.dto.LocationVerifyResponse;
import com.smarthostel.dto.MarkAttendanceRequest;
import com.smarthostel.dto.MonthlyAttendanceResponse;
import com.smarthostel.model.Attendance;
import com.smarthostel.model.Student;
import com.smarthostel.repository.AttendanceRepository;
import com.smarthostel.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * ATTENDANCE SERVICE (Business Logic & Verification Workflow Layer)
 * ============================================================================
 * 
 * Coordinates 1st-year time restriction validation, location verification,
 * duplicate check, monthly calendar computation, and attendance persistence.
 */
@Service
public class AttendanceService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceService.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    // 1st-Year attendance window policy: 09:00 AM to 05:00 PM (17:00)
    public static final LocalTime FIRST_YEAR_START_TIME = LocalTime.of(9, 0, 0);
    public static final LocalTime FIRST_YEAR_END_TIME = LocalTime.of(17, 0, 0);

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final LocationService locationService;

    @Autowired
    public AttendanceService(AttendanceRepository attendanceRepository, 
                             StudentRepository studentRepository, 
                             LocationService locationService) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.locationService = locationService;
    }

    /**
     * Validates whether the student is allowed to mark attendance at the given time.
     * Enforces institutional policy: 1st-year students are restricted to marking
     * attendance exclusively within the 9:00 AM to 5:00 PM time window.
     */
    public void validateTimeWindowRestriction(Student student, LocalTime attendanceTime) {
        if (student != null && student.isFirstYear()) {
            if (attendanceTime.isBefore(FIRST_YEAR_START_TIME) || attendanceTime.isAfter(FIRST_YEAR_END_TIME)) {
                log.warn("Attendance rejected for 1st-year student {}: Attempted at {} outside allowed window (09:00 AM - 05:00 PM)",
                        student.getStudentId(), attendanceTime);
                throw new IllegalArgumentException(
                        "Attendance restriction: 1st-year students are only permitted to mark attendance between 09:00 AM and 05:00 PM. (Current time: " 
                        + attendanceTime.withNano(0) + ")"
                );
            }
        }
    }

    /**
     * Records verified attendance for a student (using system clock).
     */
    @Transactional
    public Attendance markAttendance(MarkAttendanceRequest request) {
        return markAttendance(request, LocalDate.now(), LocalTime.now());
    }

    /**
     * Overloaded method accepting explicit date and time for testability.
     */
    @Transactional
    public Attendance markAttendance(MarkAttendanceRequest request, LocalDate today, LocalTime now) {
        String studentId = request.getStudentId().trim().toUpperCase();

        log.info("Attempting to mark attendance for Student: {} on Date: {} at Time: {}", studentId, today, now);

        // 1. Verify Student existence
        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));

        // 2. Enforce 1st-Year Time Window Restriction (9:00 AM to 5:00 PM)
        validateTimeWindowRestriction(student, now);

        // 3. Prevent duplicate daily attendance
        if (attendanceRepository.existsByStudentIdAndAttendanceDate(studentId, today)) {
            throw new IllegalStateException("Attendance has already been marked for today (" + today + ")");
        }

        // 4. Perform authoritative server-side location verification
        LocationVerifyRequest verifyReq = new LocationVerifyRequest(
                studentId, request.getLatitude(), request.getLongitude(), request.getAccuracy()
        );
        LocationVerifyResponse verifyRes = locationService.verifyLocation(verifyReq);

        if (!verifyRes.isVerified()) {
            log.warn("Attendance rejected for Student {}: Distance {}m exceeds allowed radius {}m", 
                    studentId, verifyRes.getDistance(), verifyRes.getAllowedRadius());
            throw new IllegalArgumentException("Attendance Failed — You are outside the permitted hostel location (" + 
                    verifyRes.getHostelName() + "). Distance: " + verifyRes.getDistance() + "m, Allowed: " + verifyRes.getAllowedRadius() + "m.");
        }

        // 5. Save Attendance record
        Attendance attendance = new Attendance(
                studentId,
                today,
                now,
                request.getLatitude(),
                request.getLongitude(),
                verifyRes.getDistance(),
                "VERIFIED",
                "PRESENT",
                "GEOLOCATION"
        );

        Attendance savedRecord = attendanceRepository.save(attendance);
        log.info("Attendance marked successfully. ID: {}, Student: {}, Time: {}", 
                savedRecord.getId(), studentId, savedRecord.getAttendanceTime());

        return savedRecord;
    }

    /**
     * Computes real day-by-day attendance data for the Monthly Attendance Calendar.
     * 
     * @param studentId Student identifier
     * @param year Year (e.g. 2026)
     * @param month Month number (1 - 12)
     * @return MonthlyAttendanceResponse with status dots and monthly stats
     */
    public MonthlyAttendanceResponse getMonthlyAttendance(String studentId, int year, int month) {
        String cleanId = studentId.trim().toUpperCase();
        LocalDate now = LocalDate.now();

        if (year <= 0) year = now.getYear();
        if (month < 1 || month > 12) month = now.getMonthValue();

        LocalDate startDate = LocalDate.of(year, month, 1);
        int daysInMonth = startDate.lengthOfMonth();
        LocalDate endDate = LocalDate.of(year, month, daysInMonth);

        List<Attendance> records = attendanceRepository.findByStudentIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
                cleanId, startDate, endDate
        );

        Map<LocalDate, Attendance> recordMap = records.stream()
                .collect(Collectors.toMap(Attendance::getAttendanceDate, a -> a, (e, r) -> e));

        long presentCount = 0;
        long absentCount = 0;
        long notMarkedCount = 0;
        long elapsedDays = 0;

        List<DayAttendanceDTO> dayList = new ArrayList<>();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(year, month, day);
            boolean isToday = date.equals(now);
            boolean isFuture = date.isAfter(now);

            Attendance record = recordMap.get(date);
            String status;
            String timeStr = null;
            Double distance = null;
            String locationStatus = null;

            if (record != null) {
                status = record.getAttendanceStatus(); // "PRESENT" or "ABSENT"
                if ("PRESENT".equalsIgnoreCase(status)) {
                    presentCount++;
                } else if ("ABSENT".equalsIgnoreCase(status)) {
                    absentCount++;
                } else {
                    notMarkedCount++;
                }
                timeStr = record.getAttendanceTime() != null ? record.getAttendanceTime().format(TIME_FORMATTER) : null;
                distance = record.getDistanceFromHostel();
                locationStatus = record.getLocationStatus();
                elapsedDays++;
            } else if (isFuture) {
                status = "FUTURE";
            } else {
                status = "NOT_MARKED";
                notMarkedCount++;
                elapsedDays++;
            }

            DayAttendanceDTO dayDTO = new DayAttendanceDTO(
                    date.toString(),
                    day,
                    date.getDayOfWeek().name(),
                    status,
                    timeStr,
                    distance,
                    locationStatus,
                    isToday,
                    isFuture
            );

            dayList.add(dayDTO);
        }

        double percentage = elapsedDays > 0 
                ? Math.round(((double) presentCount / elapsedDays) * 1000.0) / 10.0 
                : 100.0;

        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        return new MonthlyAttendanceResponse(
                month,
                monthName,
                year,
                presentCount,
                absentCount,
                notMarkedCount,
                percentage,
                elapsedDays,
                dayList
        );
    }

    /**
     * Retrieves full attendance history for the requesting student.
     */
    public List<Attendance> getAttendanceHistory(String studentId) {
        return attendanceRepository.findByStudentIdOrderByAttendanceDateDescAttendanceTimeDesc(studentId.trim().toUpperCase());
    }

    /**
     * Retrieves today's attendance status for a student.
     */
    public Optional<Attendance> getTodayAttendance(String studentId) {
        return attendanceRepository.findByStudentIdAndAttendanceDate(studentId.trim().toUpperCase(), LocalDate.now());
    }

    /**
     * Computes dashboard attendance statistics for a student.
     */
    public Map<String, Object> getStudentAttendanceStats(String studentId) {
        String cleanId = studentId.trim().toUpperCase();
        long totalRecords = attendanceRepository.countByStudentId(cleanId);
        long presentRecords = attendanceRepository.countByStudentIdAndAttendanceStatus(cleanId, "PRESENT");

        double percentage = totalRecords > 0 ? ((double) presentRecords / totalRecords) * 100.0 : 100.0;
        percentage = Math.round(percentage * 10.0) / 10.0;

        Optional<Attendance> todayOpt = getTodayAttendance(cleanId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDays", totalRecords);
        stats.put("presentDays", presentRecords);
        stats.put("absentDays", totalRecords - presentRecords);
        stats.put("attendancePercentage", percentage);
        stats.put("todayMarked", todayOpt.isPresent());
        stats.put("todayAttendance", todayOpt.orElse(null));
        stats.put("todayStatusBadge", todayOpt.isPresent() ? "ATTENDANCE MARKED" : "NOT MARKED");

        return stats;
    }
}
