package com.smarthostel.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ============================================================================
 * HOSTEL ENTITY (Java OOP Model Layer)
 * ============================================================================
 * 
 * Represents hostel metadata, geographic coordinates, and allowed radius.
 * Demonstrates OOP Encapsulation with private fields and public accessors.
 */
@Entity
@Table(name = "hostels")
public class Hostel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hostel_name", nullable = false, unique = true, length = 150)
    private String hostelName;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "allowed_radius", nullable = false)
    private Integer allowedRadius = 1000; // in meters (1km geofence)

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Default Constructor (Required by JPA)
    public Hostel() {
    }

    // Parameterized Constructor (OOP Object Instantiation)
    public Hostel(String hostelName, Double latitude, Double longitude, Integer allowedRadius, String description) {
        this.hostelName = hostelName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.allowedRadius = allowedRadius != null ? allowedRadius : 1000;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getters and Setters (Encapsulation) ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHostelName() {
        return hostelName;
    }

    public void setHostelName(String hostelName) {
        this.hostelName = hostelName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getAllowedRadius() {
        return allowedRadius;
    }

    public void setAllowedRadius(Integer allowedRadius) {
        this.allowedRadius = allowedRadius;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Hostel{" +
                "id=" + id +
                ", hostelName='" + hostelName + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", allowedRadius=" + allowedRadius +
                '}';
    }
}
