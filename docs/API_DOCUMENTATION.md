# REST API Documentation

Base URL: `http://localhost:8080/api`

---

## 1. Authentication APIs

### 1.1 Register Student
- **Endpoint**: `POST /api/auth/register`
- **Description**: Registers a new student account with BCrypt password hashing.
- **Request Body**:
```json
{
  "name": "Rahul Sharma",
  "studentId": "ASIET2024CS001",
  "email": "rahul.cs@adishankara.ac.in",
  "phone": "+91 98765 43210",
  "course": "B.Tech Computer Science and Engineering",
  "department": "Computer Science and Engineering",
  "academicYear": "2023 - 2027",
  "hostelId": 1,
  "roomNumber": "A-204",
  "password": "Password@123",
  "confirmPassword": "Password@123"
}
```
- **Response (201 Created)**:
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "token": "token_a74b1234-...",
    "student": {
      "id": 1,
      "studentId": "ASIET2024CS001",
      "name": "Rahul Sharma",
      "email": "rahul.cs@adishankara.ac.in"
    }
  }
}
```

### 1.2 Login Student
- **Endpoint**: `POST /api/auth/login`
- **Description**: Authenticates student credentials.
- **Request Body**:
```json
{
  "identifier": "ASIET2024CS001",
  "password": "Password@123"
}
```
- **Response (200 OK)**:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "token_98fa...",
    "student": { ... }
  }
}
```

---

## 2. Geolocation & Attendance APIs

### 2.1 Verify Location
- **Endpoint**: `POST /api/attendance/verify-location`
- **Description**: Validates GPS coordinates against configured hostel allowed radius.
- **Request Body**:
```json
{
  "studentId": "ASIET2024CS001",
  "latitude": 10.169845,
  "longitude": 76.435750,
  "accuracy": 5.0
}
```
- **Response (200 OK - Verified)**:
```json
{
  "success": true,
  "message": "Location verified successfully",
  "data": {
    "verified": true,
    "distance": 2.15,
    "allowedRadius": 100,
    "hostelName": "ASIET Main College Hostel",
    "status": "VERIFIED",
    "message": "Location verified successfully. You are 2.2m from ASIET Main College Hostel (Allowed: 100m)."
  }
}
```

### 2.2 Mark Attendance
- **Endpoint**: `POST /api/attendance/mark`
- **Description**: Authoritatively validates location and records attendance in MySQL.
- **Request Body**:
```json
{
  "studentId": "ASIET2024CS001",
  "latitude": 10.169845,
  "longitude": 76.435750,
  "accuracy": 5.0
}
```
- **Response (201 Created)**:
```json
{
  "success": true,
  "message": "Attendance Marked Successfully",
  "data": {
    "id": 14,
    "studentId": "ASIET2024CS001",
    "attendanceDate": "2026-09-09",
    "attendanceTime": "21:30:15",
    "latitude": 10.169845,
    "longitude": 76.435750,
    "distanceFromHostel": 2.15,
    "locationStatus": "VERIFIED",
    "attendanceStatus": "PRESENT",
    "verificationMode": "GEOLOCATION"
  }
}
```

### 2.3 Attendance History
- **Endpoint**: `GET /api/attendance/history?studentId=ASIET2024CS001`
- **Response (200 OK)**:
```json
{
  "success": true,
  "message": "Attendance history loaded",
  "data": [ ... ]
}
```

### 2.4 Attendance Statistics
- **Endpoint**: `GET /api/attendance/stats?studentId=ASIET2024CS001`
- **Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "totalDays": 14,
    "presentDays": 13,
    "attendancePercentage": 92.9,
    "todayMarked": true,
    "todayStatusBadge": "ATTENDANCE MARKED"
  }
}
```

---

## 3. Profile & Hostel APIs

### 3.1 Student Profile
- **Endpoint**: `GET /api/student/profile?studentId=ASIET2024CS001`

### 3.2 Hostel List
- **Endpoint**: `GET /api/hostel/all`
