-- ============================================================================
-- SMART HOSTEL ATTENDANCE MANAGEMENT SYSTEM
-- Seed / Sample Data Script
-- ============================================================================

USE smart_hostel_attendance;

-- 1. INSERT HOSTELS
INSERT INTO hostels (id, hostel_name, latitude, longitude, allowed_radius, description) VALUES
(1, 'Adi Shankara Institute Main Campus Hostel', 10.1706000, 76.4357000, 1000, 'Main Campus Hostel - Block A, Adi Shankara Institute, Kalady'),
(2, 'ASIET Boys Hostel (Block B)', 10.1708500, 76.4359200, 1000, 'Senior Boys Hostel, Mattoor-Kalady Campus'),
(3, 'ASIET Ladies Hostel (Block C)', 10.1714000, 76.4364000, 1000, 'Ladies Hostel, North Wing, Adi Shankara Campus')
ON DUPLICATE KEY UPDATE hostel_name=VALUES(hostel_name);

-- 2. INSERT SAMPLE ADMINISTRATORS
-- Password: "Password@123" (BCrypt hash: $2a$10$wN8Wlhq5Y9iGekQ0yqX4heoB03d7/i5h8hN99Q1.N.WcE6pAcm.9u or standard spring crypto)
-- $2a$10$7Z8lqV01QO87j1j3Y0YlIeD8Jg51Rj0lQ1R9xY2k0W3z6z8v0a2dG is equivalent for demo
INSERT INTO admins (id, admin_id, full_name, email, phone, hostel_id, password_hash, role) VALUES
(1, 'ADM-HST-001', 'Prof. K. Narayanan (Warden)', 'warden@adishankara.ac.in', '+91 94471 23456', 1, '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'ADMIN'),
(2, 'ADM-HST-002', 'Dr. Meera Radhakrishnan', 'meera.warden@adishankara.ac.in', '+91 94472 98765', 3, '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'ADMIN')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- 3. INSERT SAMPLE STUDENTS
INSERT INTO students (id, student_id, name, email, phone, course, department, academic_year, hostel_id, room_number, password_hash, role) VALUES
(1, 'ASIET2024CS001', 'Rahul Sharma', 'rahul.cs@adishankara.ac.in', '+91 98765 43210', 'B.Tech Computer Science and Engineering', 'Computer Science and Engineering', '3rd Year (2023-2027)', 1, 'A-204', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'STUDENT'),
(2, 'ASIET2024CS042', 'Ananya Menon', 'ananya.m@adishankara.ac.in', '+91 98451 23456', 'B.Tech Computer Science and Engineering', 'Computer Science and Engineering', '3rd Year (2023-2027)', 1, 'A-108', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'STUDENT'),
(3, 'ASIET2026CS001', 'Aditya Varma', 'aditya.cs26@adishankara.ac.in', '+91 98123 45678', 'B.Tech Computer Science and Engineering', 'Computer Science and Engineering', '1st Year (2026-2030)', 1, 'A-102', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'STUDENT'),
(4, 'ASIET2024CS101', 'Arun Kumar', 'arun.k@adishankara.ac.in', '+91 97451 11223', 'B.Tech Computer Science and Engineering', 'Computer Science and Engineering', '3rd Year (2023-2027)', 1, 'A-101', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'STUDENT'),
(5, 'ASIET2024CS102', 'Devika Nair', 'devika.nair@adishankara.ac.in', '+91 97452 33445', 'B.Tech Computer Science and Engineering', 'Computer Science and Engineering', '3rd Year (2023-2027)', 1, 'A-105', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'STUDENT')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 4. INSERT ATTENDANCE RECORDS (Past dates in September 2026 for testing Monthly Calendar & Admin Table)
INSERT INTO attendance (student_id, hostel_id, attendance_date, attendance_time, latitude, longitude, distance_from_hostel, location_status, attendance_status, verification_mode) VALUES
-- Student 1 (Rahul Sharma)
('ASIET2024CS001', 1, '2026-09-01', '08:42:15', 10.170610, 76.435710, 2.3, 'VERIFIED', 'PRESENT', 'GEOLOCATION'),
('ASIET2024CS001', 1, '2026-09-02', '08:39:50', 10.170590, 76.435690, 3.1, 'VERIFIED', 'PRESENT', 'GEOLOCATION'),
('ASIET2024CS001', 1, '2026-09-03', '08:45:10', 10.170600, 76.435700, 0.5, 'VERIFIED', 'PRESENT', 'GEOLOCATION'),
('ASIET2024CS001', 1, '2026-09-04', '08:50:00', 10.170650, 76.435750, 7.8, 'VERIFIED', 'PRESENT', 'GEOLOCATION'),
('ASIET2024CS001', 1, '2026-09-07', '08:35:22', 10.170580, 76.435680, 4.2, 'VERIFIED', 'PRESENT', 'GEOLOCATION'),
('ASIET2024CS001', 1, '2026-09-08', '08:40:11', 10.170620, 76.435720, 3.0, 'VERIFIED', 'PRESENT', 'GEOLOCATION'),
('ASIET2024CS001', 1, '2026-09-09', '08:43:45', 10.170605, 76.435705, 1.2, 'VERIFIED', 'PRESENT', 'GEOLOCATION'),
('ASIET2024CS001', 1, '2026-09-10', '08:48:30', 10.170630, 76.435730, 4.5, 'VERIFIED', 'PRESENT', 'GEOLOCATION'),
('ASIET2024CS001', 1, '2026-09-11', '08:38:19', 10.170595, 76.435695, 2.0, 'VERIFIED', 'PRESENT', 'GEOLOCATION'),
('ASIET2024CS001', 1, '2026-09-14', '08:42:00', 10.170600, 76.435700, 1.1, 'VERIFIED', 'PRESENT', 'GEOLOCATION'),

-- Student 2 (Ananya Menon)
('ASIET2024CS042', 1, '2026-09-01', '08:40:10', 10.170600, 76.435700, 1.5, 'VERIFIED', 'PRESENT', 'GEOLOCATION'),
('ASIET2024CS042', 1, '2026-09-02', '08:44:20', 10.170620, 76.435710, 2.8, 'VERIFIED', 'PRESENT', 'GEOLOCATION'),
('ASIET2024CS042', 1, '2026-09-08', '08:36:12', 10.170585, 76.435690, 3.4, 'VERIFIED', 'PRESENT', 'GEOLOCATION'),
('ASIET2024CS042', 1, '2026-09-14', '08:45:00', 10.170602, 76.435701, 0.8, 'VERIFIED', 'PRESENT', 'GEOLOCATION'),

-- Student 3 (Aditya Varma)
('ASIET2026CS001', 1, '2026-09-01', '09:15:30', 10.170610, 76.435705, 1.8, 'VERIFIED', 'PRESENT', 'GEOLOCATION'),
('ASIET2026CS001', 1, '2026-09-02', '09:20:15', 10.170590, 76.435685, 3.5, 'VERIFIED', 'PRESENT', 'GEOLOCATION'),
('ASIET2026CS001', 1, '2026-09-07', '09:10:00', 10.170600, 76.435700, 0.9, 'VERIFIED', 'PRESENT', 'GEOLOCATION'),
('ASIET2026CS001', 1, '2026-09-08', '09:12:45', 10.170615, 76.435715, 2.2, 'VERIFIED', 'PRESENT', 'GEOLOCATION'),
('ASIET2026CS001', 1, '2026-09-09', '09:18:20', 10.170630, 76.435730, 4.1, 'VERIFIED', 'PRESENT', 'GEOLOCATION'),
('ASIET2026CS001', 1, '2026-09-14', '09:05:00', 10.170600, 76.435700, 1.0, 'VERIFIED', 'PRESENT', 'GEOLOCATION')
ON DUPLICATE KEY UPDATE attendance_time=VALUES(attendance_time);
