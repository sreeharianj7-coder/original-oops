package com.smarthostel.service;

import com.smarthostel.model.Student;
import com.smarthostel.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * STUDENT SERVICE (Student Details & Management Layer)
 * ============================================================================
 */
@Service
public class StudentService {

    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Optional<Student> findByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }
}
