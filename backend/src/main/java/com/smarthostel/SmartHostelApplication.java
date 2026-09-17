package com.smarthostel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * SMART HOSTEL ATTENDANCE MANAGEMENT SYSTEM
 * Main Spring Boot Application Entry Point
 * 
 * Project Name: Smart Hostel Attendance Management System Using Face Recognition and Geolocation
 * Institution: Adi Shankara Institute of Science and Technology
 * Department: Computer Science and Engineering
 * Current Phase: Login + Authoritative Geolocation Distance Verification
 * ============================================================================
 */
@SpringBootApplication
public class SmartHostelApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartHostelApplication.class, args);
        System.out.println("==================================================================");
        System.out.println(" SMART HOSTEL ATTENDANCE SYSTEM BACKEND STARTED SUCCESSFULLY");
        System.out.println(" Server URL: http://localhost:8080");
        System.out.println(" H2 Console: http://localhost:8080/h2-console");
        System.out.println("==================================================================");
    }
}
