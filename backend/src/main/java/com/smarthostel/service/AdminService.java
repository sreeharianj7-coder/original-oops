package com.smarthostel.service;

import com.smarthostel.dto.AdminDashboardResponse;
import com.smarthostel.dto.AuthResponse;
import com.smarthostel.dto.DayAttendanceDTO;
import com.smarthostel.dto.LoginRequest;
import com.smarthostel.dto.RegisterAdminRequest;
import com.smarthostel.dto.StudentAttendanceDetailDTO;
import com.smarthostel.model.Admin;
import com.smarthostel.model.Attendance;
import com.smarthostel.model.Hostel;
import com.smarthostel.model.Student;
import com.smarthostel.model.StudentFaceProfile;
import com.smarthostel.repository.AdminRepository;
import com.smarthostel.repository.AttendanceRepository;
import com.smarthostel.repository.HostelRepository;
import com.smarthostel.repository.StudentFaceProfileRepository;
import com.smarthostel.repository.StudentRepository;
import com.smarthostel.util.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    private final AdminRepository adminRepository;
    private final HostelRepository hostelRepository;
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentFaceProfileRepository faceProfileRepository;

    @Autowired
    public AdminService(AdminRepository adminRepository, 
                        HostelRepository hostelRepository, 
                        StudentRepository studentRepository, 
                        AttendanceRepository attendanceRepository,
                        StudentFaceProfileRepository faceProfileRepository) {
        this.adminRepository = adminRepository;
        this.hostelRepository = hostelRepository;
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.faceProfileRepository = faceProfileRepository;
    }


    /**
     * Registers a new hostel administrator.
     */
    @Transactional
    public AuthResponse register(RegisterAdminRequest request) {
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
     * Authenticates hostel administrator credentials.
     */
    public AuthResponse login(LoginRequest request) {
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

    /**
     * Generates complete Admin Dashboard overview for a given hostel & date.
     */
    public AdminDashboardResponse getDashboard(String adminId, LocalDate date) {
        LocalDate selectedDate = (date != null) ? date : LocalDate.now();

        Admin admin = null;
        Hostel hostel = null;

        if (adminId != null && !adminId.trim().isEmpty()) {
            Optional<Admin> adminOpt = adminRepository.findByAdminId(adminId.trim().toUpperCase());
            if (adminOpt.isPresent()) {
                admin = adminOpt.get();
                hostel = admin.getHostel();
            }
        }

        if (hostel == null) {
            hostel = hostelRepository.findAll().stream().findFirst()
                    .orElse(new Hostel("Main Campus Hostel", 10.1706, 76.4357, 1000, "Campus"));
        }

        List<Student> students = studentRepository.findByHostelId(hostel.getId());
        if (students.isEmpty()) {
            students = studentRepository.findAll();
        }

        List<Attendance> attendanceRecords = attendanceRepository.findByAttendanceDateAndHostelId(selectedDate, hostel.getId());
        Map<String, Attendance> attendanceMap = attendanceRecords.stream()
                .collect(Collectors.toMap(Attendance::getStudentId, a -> a, (existing, replacement) -> existing));

        long totalStudents = students.size();
        long presentCount = 0;
        long absentCount = 0;
        long notMarkedCount = 0;

        List<StudentAttendanceDetailDTO> studentDetails = new ArrayList<>();
        boolean isFuture = selectedDate.isAfter(LocalDate.now());

        for (Student student : students) {
            Attendance att = attendanceMap.get(student.getStudentId());

            String status;
            String locationStatus = "--";
            String timeStr = "--";
            String distanceStr = "--";

            if (att != null) {
                status = att.getAttendanceStatus();
                if ("PRESENT".equalsIgnoreCase(status)) {
                    presentCount++;
                } else if ("ABSENT".equalsIgnoreCase(status)) {
                    absentCount++;
                } else {
                    notMarkedCount++;
                }
                locationStatus = att.getLocationStatus() != null ? att.getLocationStatus() : "VERIFIED";
                timeStr = att.getAttendanceTime() != null ? att.getAttendanceTime().format(TIME_FORMATTER) : "--";
                distanceStr = att.getDistanceFromHostel() != null ? String.format("%.0f m", att.getDistanceFromHostel()) : "--";
            } else {
                if (isFuture) {
                    status = "NO_STATUS";
                } else {
                    status = "NOT_MARKED";
                    notMarkedCount++;
                }
            }

            // Calculate student's overall attendance rate
            long studentTotal = attendanceRepository.countByStudentId(student.getStudentId());
            long studentPresent = attendanceRepository.countByStudentIdAndAttendanceStatus(student.getStudentId(), "PRESENT");
            double studentPct = studentTotal > 0 ? Math.round(((double) studentPresent / studentTotal) * 1000.0) / 10.0 : 100.0;

            boolean isFaceReg = faceProfileRepository.existsByStudentId(student.getStudentId());
            String faceStatus = isFaceReg ? "Registered" : "Not Registered";

            StudentAttendanceDetailDTO detail = new StudentAttendanceDetailDTO(
                    student.getStudentId(),
                    student.getName(),
                    student.getRoomNumber(),
                    student.getCourse(),
                    student.getDepartment(),
                    student.getAcademicYear(),
                    student.getPhone(),
                    student.getEmail(),
                    hostel.getHostelName(),
                    status,
                    locationStatus,
                    timeStr,
                    distanceStr,
                    studentPct,
                    isFaceReg,
                    faceStatus
            );

            studentDetails.add(detail);
        }

        double overallPercentage = totalStudents > 0 ? Math.round(((double) presentCount / totalStudents) * 1000.0) / 10.0 : 0.0;

        return new AdminDashboardResponse(
                hostel.getId(),
                hostel.getHostelName(),
                admin != null ? admin.getFullName() : "Administrator",
                admin != null ? admin.getAdminId() : "ADM-001",
                selectedDate.toString(),
                totalStudents,
                presentCount,
                absentCount,
                notMarkedCount,
                overallPercentage,
                studentDetails
        );
    }

    /**
     * Searches students by name, student ID, or room number.
     */
    public List<StudentAttendanceDetailDTO> searchStudents(Long hostelId, String query) {
        String cleanQuery = (query != null) ? query.trim() : "";
        List<Student> students = studentRepository.searchStudents(hostelId, cleanQuery);

        List<StudentAttendanceDetailDTO> results = new ArrayList<>();
        for (Student s : students) {
            long total = attendanceRepository.countByStudentId(s.getStudentId());
            long present = attendanceRepository.countByStudentIdAndAttendanceStatus(s.getStudentId(), "PRESENT");
            double pct = total > 0 ? Math.round(((double) present / total) * 1000.0) / 10.0 : 100.0;

            Optional<Attendance> todayAtt = attendanceRepository.findByStudentIdAndAttendanceDate(s.getStudentId(), LocalDate.now());
            String todayStatus = todayAtt.map(Attendance::getAttendanceStatus).orElse("NOT_MARKED");
            String todayTime = todayAtt.map(a -> a.getAttendanceTime().format(TIME_FORMATTER)).orElse("--");
            String todayDist = todayAtt.map(a -> String.format("%.0f m", a.getDistanceFromHostel())).orElse("--");

            boolean isFaceReg = faceProfileRepository.existsByStudentId(s.getStudentId());
            String faceStatus = isFaceReg ? "Registered" : "Not Registered";

            StudentAttendanceDetailDTO dto = new StudentAttendanceDetailDTO(
                    s.getStudentId(),
                    s.getName(),
                    s.getRoomNumber(),
                    s.getCourse(),
                    s.getDepartment(),
                    s.getAcademicYear(),
                    s.getPhone(),
                    s.getEmail(),
                    s.getHostel() != null ? s.getHostel().getHostelName() : "Main Hostel",
                    todayStatus,
                    todayAtt.map(Attendance::getLocationStatus).orElse("--"),
                    todayTime,
                    todayDist,
                    pct,
                    isFaceReg,
                    faceStatus
            );
            results.add(dto);
        }
        return results;
    }

    /**
     * Retrieves individual student details with full attendance history.
     */
    public StudentAttendanceDetailDTO getStudentDetails(String studentId) {
        Student student = studentRepository.findByStudentId(studentId.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));

        long total = attendanceRepository.countByStudentId(student.getStudentId());
        long present = attendanceRepository.countByStudentIdAndAttendanceStatus(student.getStudentId(), "PRESENT");
        double pct = total > 0 ? Math.round(((double) present / total) * 1000.0) / 10.0 : 100.0;

        List<Attendance> history = attendanceRepository.findByStudentIdOrderByAttendanceDateDescAttendanceTimeDesc(student.getStudentId());

        List<DayAttendanceDTO> historyDTOs = history.stream().map(a -> new DayAttendanceDTO(
                a.getAttendanceDate().toString(),
                a.getAttendanceDate().getDayOfMonth(),
                a.getAttendanceDate().getDayOfWeek().name(),
                a.getAttendanceStatus(),
                a.getAttendanceTime() != null ? a.getAttendanceTime().format(TIME_FORMATTER) : "--",
                a.getDistanceFromHostel(),
                a.getLocationStatus(),
                a.getAttendanceDate().equals(LocalDate.now()),
                false
        )).collect(Collectors.toList());

        boolean isFaceReg = faceProfileRepository.existsByStudentId(student.getStudentId());
        String faceStatus = isFaceReg ? "Registered" : "Not Registered";

        StudentAttendanceDetailDTO detail = new StudentAttendanceDetailDTO(
                student.getStudentId(),
                student.getName(),
                student.getRoomNumber(),
                student.getCourse(),
                student.getDepartment(),
                student.getAcademicYear(),
                student.getPhone(),
                student.getEmail(),
                student.getHostel() != null ? student.getHostel().getHostelName() : "Main Hostel",
                history.isEmpty() ? "NOT_MARKED" : history.get(0).getAttendanceStatus(),
                history.isEmpty() ? "--" : history.get(0).getLocationStatus(),
                history.isEmpty() ? "--" : (history.get(0).getAttendanceTime() != null ? history.get(0).getAttendanceTime().format(TIME_FORMATTER) : "--"),
                history.isEmpty() ? "--" : String.format("%.0f m", history.get(0).getDistanceFromHostel()),
                pct,
                isFaceReg,
                faceStatus
        );
        detail.setRecentHistory(historyDTOs);

        return detail;
    }

    /**
     * Retrieves admin profile info.
     */
    public Optional<Admin> getAdminProfile(String adminId) {
        return adminRepository.findByAdminId(adminId.trim().toUpperCase());
    }
}
