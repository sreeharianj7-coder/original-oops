# Smart Hostel Attendance Backend (Spring Boot + Java OOP)

This module implements the Java Spring Boot REST API for the **Smart Hostel Attendance Management System Using Face Recognition and Geolocation**.

## Architecture & Technology Stack
- **Framework**: Spring Boot 3.2.x
- **Language**: Java 17+
- **Persistence**: Spring Data JPA & Hibernate
- **Database**: MySQL (Production profile) / H2 in-memory (Default development profile)
- **Security**: BCrypt Password Hashing (`PasswordHasher.java`)
- **Geolocation Algorithm**: Haversine Formula (`DistanceCalculator.java`)
- **Biometric Roadmap**: `FaceVerificationService.java` (Interface placeholder for future phase)

## Key REST API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Register new student with hashed password |
| `POST` | `/api/auth/login` | Student authentication |
| `GET` | `/api/student/profile` | Logged-in student details & college info |
| `GET` | `/api/hostel/current` | Active hostel coordinates & permitted radius |
| `GET` | `/api/hostel/all` | List all hostel blocks |
| `POST` | `/api/attendance/verify-location` | Server-side Haversine distance verification |
| `POST` | `/api/attendance/mark` | Record verified attendance in database |
| `GET` | `/api/attendance/history` | Logged-in student attendance logs |
| `GET` | `/api/attendance/stats` | Summary statistics and today's badge status |

## Running the Backend

### Default Profile (H2 In-Memory):
```bash
mvn spring-boot:run
```

### MySQL Profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```
