package com.smarthostel.repository;

import com.smarthostel.model.StudentFaceProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ============================================================================
 * STUDENT FACE PROFILE REPOSITORY
 * ============================================================================
 */
@Repository
public interface StudentFaceProfileRepository extends JpaRepository<StudentFaceProfile, Long> {

    List<StudentFaceProfile> findByStudentIdOrderByPhotoIndexAsc(String studentId);

    long countByStudentId(String studentId);

    boolean existsByStudentId(String studentId);

    void deleteByStudentId(String studentId);
}
