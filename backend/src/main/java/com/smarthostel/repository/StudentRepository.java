package com.smarthostel.repository;

import com.smarthostel.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentId(String studentId);
    Optional<Student> findByEmail(String email);
    boolean existsByStudentId(String studentId);
    boolean existsByEmail(String email);
    
    List<Student> findByHostelId(Long hostelId);
    
    @Query("SELECT s FROM Student s WHERE " +
           "(:hostelId IS NULL OR s.hostel.id = :hostelId) AND (" +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.studentId) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.roomNumber) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Student> searchStudents(@Param("hostelId") Long hostelId, @Param("query") String query);
}
