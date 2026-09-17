package com.smarthostel.config;

import com.smarthostel.model.Admin;
import com.smarthostel.model.Attendance;
import com.smarthostel.model.Hostel;
import com.smarthostel.model.Student;
import com.smarthostel.repository.AdminRepository;
import com.smarthostel.repository.AttendanceRepository;
import com.smarthostel.repository.HostelRepository;
import com.smarthostel.repository.StudentRepository;
import com.smarthostel.util.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * ============================================================================
 * DATA INITIALIZER (Automatic Database Seeding on Startup)
 * ============================================================================
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final HostelRepository hostelRepository;
    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository;
    private final AttendanceRepository attendanceRepository;

    @Autowired
    public DataInitializer(HostelRepository hostelRepository, 
                           StudentRepository studentRepository, 
                           AdminRepository adminRepository,
                           AttendanceRepository attendanceRepository) {
        this.hostelRepository = hostelRepository;
        this.studentRepository = studentRepository;
        this.adminRepository = adminRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Override
    public void run(String... args) {
        log.info("Checking database initialization status...");

        // 1. Initialize Hostels if empty (Main campus geofence: 1000m / 1km radius)
        if (hostelRepository.count() == 0) {
            log.info("Seeding default Adi Shankara Institute hostels with 1000m geofence radius...");
            Hostel mainHostel = new Hostel(
                    "Adi Shankara Institute Main Campus Hostel", 
                    10.1706000, 
                    76.4357000, 
                    1000, 
                    "Main Campus Hostel - Block A, Adi Shankara Institute of Science and Technology, Kalady"
            );
            Hostel boysHostel = new Hostel(
                    "ASIET Boys Hostel (Block B)", 
                    10.1708500, 
                    76.4359200, 
                    1000, 
                    "Senior Boys Hostel, Mattoor-Kalady Campus"
            );
            Hostel ladiesHostel = new Hostel(
                    "ASIET Ladies Hostel (Block C)", 
                    10.1714000, 
                    76.4364000, 
                    1000, 
                    "Womens Hostel, North Wing"
            );

            hostelRepository.save(mainHostel);
            hostelRepository.save(boysHostel);
            hostelRepository.save(ladiesHostel);
            log.info("Hostels initialized successfully with 1000m geofence.");
        }

        Hostel defaultHostel = hostelRepository.findAll().get(0);
        String defaultHash = PasswordHasher.hash("Password@123");

        // 2. Initialize Administrators if empty
        if (adminRepository.count() == 0) {
            log.info("Seeding default hostel administrators...");
            Admin admin1 = new Admin(
                    "ADM-HST-001",
                    "Prof. K. Narayanan (Warden)",
                    "warden@adishankara.ac.in",
                    "+91 94471 23456",
                    defaultHostel,
                    defaultHash
            );
            Admin admin2 = new Admin(
                    "ADM-HST-002",
                    "Dr. Meera Radhakrishnan",
                    "meera.warden@adishankara.ac.in",
                    "+91 94472 98765",
                    defaultHostel,
                    defaultHash
            );

            adminRepository.save(admin1);
            adminRepository.save(admin2);
            log.info("Administrators initialized successfully.");
        }

        // 3. Initialize Sample Students if empty
        if (studentRepository.count() == 0) {
            log.info("Seeding demo students...");

            Student student1 = new Student(
                    "ASIET2024CS001",
                    "Rahul Sharma",
                    "rahul.cs@adishankara.ac.in",
                    "+91 98765 43210",
                    "B.Tech Computer Science and Engineering",
                    "Computer Science and Engineering",
                    "3rd Year (2023-2027)",
                    defaultHostel,
                    "A-204",
                    defaultHash
            );

            Student student2 = new Student(
                    "ASIET2024CS042",
                    "Ananya Menon",
                    "ananya.m@adishankara.ac.in",
                    "+91 98451 23456",
                    "B.Tech Computer Science and Engineering",
                    "Computer Science and Engineering",
                    "3rd Year (2023-2027)",
                    defaultHostel,
                    "C-108",
                    defaultHash
            );

            Student student3 = new Student(
                    "ASIET2026CS001",
                    "Aditya Varma",
                    "aditya.cs26@adishankara.ac.in",
                    "+91 98123 45678",
                    "B.Tech Computer Science and Engineering",
                    "Computer Science and Engineering",
                    "1st Year (2026-2030)",
                    defaultHostel,
                    "A-102",
                    defaultHash
            );

            Student student4 = new Student(
                    "ASIET2024CS101",
                    "Arun Kumar",
                    "arun.k@adishankara.ac.in",
                    "+91 97451 11223",
                    "B.Tech Computer Science and Engineering",
                    "Computer Science and Engineering",
                    "3rd Year (2023-2027)",
                    defaultHostel,
                    "A-101",
                    defaultHash
            );

            Student student5 = new Student(
                    "ASIET2024CS102",
                    "Devika Nair",
                    "devika.nair@adishankara.ac.in",
                    "+91 97452 33445",
                    "B.Tech Computer Science and Engineering",
                    "Computer Science and Engineering",
                    "3rd Year (2023-2027)",
                    defaultHostel,
                    "A-105",
                    defaultHash
            );

            studentRepository.save(student1);
            studentRepository.save(student2);
            studentRepository.save(student3);
            studentRepository.save(student4);
            studentRepository.save(student5);

            // Seed sample past attendance records for September 2026
            LocalDate today = LocalDate.now();
            int currentYear = today.getYear();
            int currentMonth = today.getMonthValue();

            // Seed records for past 10 days
            for (int day = 1; day <= Math.min(today.getDayOfMonth(), 14); day++) {
                LocalDate attDate = LocalDate.of(currentYear, currentMonth, day);
                
                // Student 1 attended most days
                if (day % 4 != 0) {
                    attendanceRepository.save(new Attendance(
                            "ASIET2024CS001",
                            attDate,
                            LocalTime.of(8, 30 + (day % 20), 15),
                            10.170610,
                            76.435710,
                            2.5,
                            "VERIFIED",
                            "PRESENT",
                            "GEOLOCATION"
                    ));
                }

                // Student 2 attended some days
                if (day % 2 == 0) {
                    attendanceRepository.save(new Attendance(
                            "ASIET2024CS042",
                            attDate,
                            LocalTime.of(8, 40 + (day % 15), 20),
                            10.170600,
                            76.435700,
                            1.2,
                            "VERIFIED",
                            "PRESENT",
                            "GEOLOCATION"
                    ));
                }

                // Student 3 (1st year) marked during 9am-5pm window
                if (day % 3 != 0) {
                    attendanceRepository.save(new Attendance(
                            "ASIET2026CS001",
                            attDate,
                            LocalTime.of(9, 15 + (day % 15), 10),
                            10.170605,
                            76.435705,
                            0.8,
                            "VERIFIED",
                            "PRESENT",
                            "GEOLOCATION"
                    ));
                }
            }

            log.info("Sample students, admins, and attendance records seeded successfully.");
        }
    }
}
