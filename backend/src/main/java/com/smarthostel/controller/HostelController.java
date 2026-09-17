package com.smarthostel.controller;

import com.smarthostel.dto.ApiResponse;
import com.smarthostel.model.Hostel;
import com.smarthostel.repository.HostelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ============================================================================
 * HOSTEL CONTROLLER (REST API for Hostel Configuration & Coordinates)
 * ============================================================================
 */
@RestController
@RequestMapping("/api/hostel")
@CrossOrigin(origins = "*")
public class HostelController {

    private final HostelRepository hostelRepository;

    @Autowired
    public HostelController(HostelRepository hostelRepository) {
        this.hostelRepository = hostelRepository;
    }

    /**
     * Retrieves primary hostel configuration.
     * GET /api/hostel/current
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<Hostel>> getCurrentHostel() {
        Hostel hostel = hostelRepository.findAll().stream().findFirst()
                .orElse(new Hostel("ASIET Main College Hostel", 10.16983000, 76.43574000, 100, "Adi Shankara Institute Hostel"));
        return ResponseEntity.ok(ApiResponse.ok("Hostel configuration loaded", hostel));
    }

    /**
     * Retrieves all available hostels (used for registration dropdown).
     * GET /api/hostel/all
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Hostel>>> getAllHostels() {
        List<Hostel> hostels = hostelRepository.findAll();
        return ResponseEntity.ok(ApiResponse.ok("Hostels retrieved", hostels));
    }
}
