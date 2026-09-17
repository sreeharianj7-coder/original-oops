package com.smarthostel.service;

import com.smarthostel.dto.LocationVerifyRequest;
import com.smarthostel.dto.LocationVerifyResponse;
import com.smarthostel.model.Hostel;
import com.smarthostel.model.Student;
import com.smarthostel.repository.HostelRepository;
import com.smarthostel.repository.StudentRepository;
import com.smarthostel.util.DistanceCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * ============================================================================
 * LOCATION SERVICE (Backend Geolocation & Distance Validation Layer)
 * ============================================================================
 * 
 * Performs authoritative server-side location verification.
 * Client GPS coordinates are compared against the student's allocated hostel
 * coordinates using the Haversine formula.
 */
@Service
public class LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

    private final StudentRepository studentRepository;
    private final HostelRepository hostelRepository;

    @Autowired
    public LocationService(StudentRepository studentRepository, HostelRepository hostelRepository) {
        this.studentRepository = studentRepository;
        this.hostelRepository = hostelRepository;
    }

    /**
     * Authoritative backend validation of student GPS coordinates.
     * 
     * @param request Geolocation request containing student ID and GPS coords
     * @return LocationVerifyResponse with verification flag, distance, and allowed radius
     */
    public LocationVerifyResponse verifyLocation(LocationVerifyRequest request) {
        log.info("Verifying location for student: {} at Lat: {}, Lon: {}", 
                request.getStudentId(), request.getLatitude(), request.getLongitude());

        // 1. Fetch Student from database
        Optional<Student> studentOpt = studentRepository.findByStudentId(request.getStudentId());
        if (studentOpt.isEmpty()) {
            return new LocationVerifyResponse(
                    false, -1.0, 0, "Unknown", 0.0, 0.0, 
                    "INVALID_STUDENT", "Student record not found in system."
            );
        }

        Student student = studentOpt.get();
        Hostel hostel = student.getHostel();

        // If student does not have an assigned hostel, fallback to first configured hostel
        if (hostel == null) {
            hostel = hostelRepository.findAll().stream().findFirst().orElse(null);
        }

        if (hostel == null) {
            return new LocationVerifyResponse(
                    false, -1.0, 0, "Not Configured", 0.0, 0.0,
                    "HOSTEL_NOT_FOUND", "No hostel geolocation configuration available in database."
            );
        }

        // 2. Compute Distance using Haversine formula (DistanceCalculator)
        double distanceMeters = DistanceCalculator.calculateDistance(
                request.getLatitude(),
                request.getLongitude(),
                hostel.getLatitude(),
                hostel.getLongitude()
        );

        int allowedRadius = hostel.getAllowedRadius();
        boolean isWithinRadius = DistanceCalculator.isWithinRadius(distanceMeters, allowedRadius);

        String status = isWithinRadius ? "VERIFIED" : "OUTSIDE_HOSTEL";
        String message = isWithinRadius
                ? String.format("Location verified successfully. You are %.1fm from %s (Allowed: %dm).", 
                        distanceMeters, hostel.getHostelName(), allowedRadius)
                : String.format("Location verification failed. You are %.1fm away, which exceeds the permitted %dm radius of %s.", 
                        distanceMeters, allowedRadius, hostel.getHostelName());

        log.info("Result for student {}: Verified={}, Distance={}m, Radius={}m", 
                request.getStudentId(), isWithinRadius, distanceMeters, allowedRadius);

        return new LocationVerifyResponse(
                isWithinRadius,
                distanceMeters,
                allowedRadius,
                hostel.getHostelName(),
                hostel.getLatitude(),
                hostel.getLongitude(),
                status,
                message
        );
    }
}
