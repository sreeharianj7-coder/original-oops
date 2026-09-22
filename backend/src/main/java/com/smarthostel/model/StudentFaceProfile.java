package com.smarthostel.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ============================================================================
 * STUDENT FACE PROFILE ENTITY
 * ============================================================================
 * Stores numerical 512-dimensional FaceNet embeddings for registered students.
 * Supports up to 10 photos per student (photoIndex: 1 to 10).
 * 
 * SECURITY NOTE:
 * Biometric numerical embeddings are strictly stored on the server/database.
 * Raw embeddings and raw images are NEVER sent to the client browser.
 */
@Entity
@Table(name = "student_face_profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uq_student_photo_index", columnNames = {"student_id", "photo_index"})
})
public class StudentFaceProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false, length = 50)
    private String studentId;

    @Column(name = "photo_index", nullable = false)
    private Integer photoIndex;

    @Lob
    @Column(name = "embedding", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String embedding;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public StudentFaceProfile() {
    }

    public StudentFaceProfile(String studentId, Integer photoIndex, String embedding) {
        this.studentId = studentId != null ? studentId.trim().toUpperCase() : null;
        this.photoIndex = photoIndex;
        this.embedding = embedding;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId != null ? studentId.trim().toUpperCase() : null;
    }

    public Integer getPhotoIndex() {
        return photoIndex;
    }

    public void setPhotoIndex(Integer photoIndex) {
        this.photoIndex = photoIndex;
    }

    public String getEmbedding() {
        return embedding;
    }

    public void setEmbedding(String embedding) {
        this.embedding = embedding;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
