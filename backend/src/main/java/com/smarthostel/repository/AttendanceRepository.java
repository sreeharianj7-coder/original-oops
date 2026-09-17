package com.smarthostel.repository;

import com.smarthostel.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    Optional<Attendance> findByStudentIdAndAttendanceDate(String studentId, LocalDate attendanceDate);
    
    boolean existsByStudentIdAndAttendanceDate(String studentId, LocalDate attendanceDate);
    
    List<Attendance> findByStudentIdOrderByAttendanceDateDescAttendanceTimeDesc(String studentId);
    
    List<Attendance> findByStudentIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
            String studentId, LocalDate startDate, LocalDate endDate
    );
    
    List<Attendance> findByAttendanceDateOrderByAttendanceTimeAsc(LocalDate attendanceDate);
    
    long countByStudentId(String studentId);
    
    long countByStudentIdAndAttendanceStatus(String studentId, String attendanceStatus);

    @Query("SELECT a FROM Attendance a WHERE a.attendanceDate = :date AND a.studentId IN (" +
           "SELECT s.studentId FROM Student s WHERE s.hostel.id = :hostelId)")
    List<Attendance> findByAttendanceDateAndHostelId(@Param("date") LocalDate date, @Param("hostelId") Long hostelId);
}
