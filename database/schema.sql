-- ============================================================================
-- SMART HOSTEL ATTENDANCE MANAGEMENT SYSTEM
-- Database Schema (MySQL Compatible)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS smart_hostel_attendance;
USE smart_hostel_attendance;

-- 1. HOSTELS TABLE (Geographic & Geofence Coordinates)
CREATE TABLE IF NOT EXISTS hostels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hostel_name VARCHAR(150) NOT NULL UNIQUE,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    allowed_radius INT NOT NULL DEFAULT 1000 COMMENT 'Allowed distance in meters (1km geofence)',
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. STUDENTS TABLE
CREATE TABLE IF NOT EXISTS students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    course VARCHAR(100) NOT NULL DEFAULT 'B.Tech Computer Science and Engineering',
    department VARCHAR(100) NOT NULL DEFAULT 'Computer Science and Engineering',
    academic_year VARCHAR(50) NOT NULL DEFAULT '2023 - 2027',
    hostel_id BIGINT NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'STUDENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_students_hostel FOREIGN KEY (hostel_id) REFERENCES hostels(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. ADMINS TABLE (Hostel Wardens & Administrators)
CREATE TABLE IF NOT EXISTS admins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    hostel_id BIGINT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'ADMIN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_admins_hostel FOREIGN KEY (hostel_id) REFERENCES hostels(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. ATTENDANCE TABLE (Daily Records & Geolocation Audit)
CREATE TABLE IF NOT EXISTS attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    hostel_id BIGINT NULL,
    attendance_date DATE NOT NULL,
    attendance_time TIME NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    distance_from_hostel DECIMAL(10, 2) NOT NULL COMMENT 'Calculated Haversine distance in meters',
    location_status VARCHAR(30) NOT NULL DEFAULT 'VERIFIED' COMMENT 'VERIFIED | OUTSIDE_HOSTEL',
    attendance_status VARCHAR(30) NOT NULL DEFAULT 'PRESENT' COMMENT 'PRESENT | ABSENT | NOT_MARKED',
    verification_mode VARCHAR(50) NOT NULL DEFAULT 'GEOLOCATION',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_student_daily_attendance UNIQUE (student_id, attendance_date),
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    INDEX idx_attendance_date (attendance_date),
    INDEX idx_student_date (student_id, attendance_date),
    INDEX idx_hostel_date (hostel_id, attendance_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. STUDENT FACE PROFILES TABLE (Pretrained FaceNet Embeddings, Max 10 Photos/Student)
CREATE TABLE IF NOT EXISTS student_face_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    photo_index INT NOT NULL COMMENT 'Index value from 1 to 10 (max 10 photos per student)',
    embedding MEDIUMTEXT NOT NULL COMMENT 'Pretrained FaceNet 512-dimensional numerical embedding (JSON format)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_student_photo_index UNIQUE (student_id, photo_index),
    CONSTRAINT chk_photo_index CHECK (photo_index BETWEEN 1 AND 10),
    CONSTRAINT fk_face_profiles_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    INDEX idx_face_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

