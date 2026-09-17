# Smart Hostel Attendance Management System — REST API Documentation

This document outlines the complete REST API interface provided by the Java Spring Boot backend for the Smart Hostel Attendance Management System.

---

## Base URL
```http
http://localhost:8080/api
```

---

## 1. Authentication Endpoints

### 1.1 Student Registration
Register a new resident student account.

- **URL**: `/auth/student/register`
- **Method**: `POST`
- **Content-Type**: `application/json`
- **Request Body**:
```json
{
  "studentId": "ASIET2024CS001",
  "name": "Rahul Sharma",
  "email": "rahul.cs@adishankara.ac.in",
  "phone": "+91 98765 43210",
  "course": "B.Tech Computer Science and Engineering",
  "department": "Computer Science and Engineering",
  "academicYear": "3rd Year (2023-2027)",
  "hostelId": 1,
  "roomNumber": "A-204",
  "password": "Password@123",
  "confirmPassword": "Password@123"
}
```
- **Success Response (201 Created)**:
```json
{
  "success": true,
  "message": "Student registration successful",
  "data": {
    "token": "student_token_3f2b...",
    "user": {
      "id": 1,
      "studentId": "ASIET2024CS001",
      "name": "Rahul Sharma",
      "email": "rahul.cs@adishankara.ac.in",
      "role": "STUDENT"
    }
  }
}
```

### 1.2 Student Login
Authenticate student credentials.

- **URL**: `/auth/student/login`
- **Method**: `POST`
- **Request Body**:
```json
{
  "identifier": "ASIET2024CS001",
  "password": "Password@123"
}
```
- **Success Response (200 OK)**:
```json
{
  "success": true,
  "message": "Student login successful",
  "data": {
    "token": "student_token_8d1a...",
    "user": { ... }
  }
}
```

### 1.3 Administrator Registration
Register a new hostel administrator / warden account.

- **URL**: `/auth/admin/register`
- **Method**: `POST`
- **Request Body**:
```json
{
  "adminId": "ADM-HST-001",
  "fullName": "Prof. K. Narayanan (Warden)",
  "email": "warden@adishankara.ac.in",
  "phone": "+91 94471 23456",
  "hostelId": 1,
  "password": "Password@123",
  "confirmPassword": "Password@123"
}
```
- **Success Response (201 Created)**:
```json
{
  "success": true,
  "message": "Administrator registration successful",
  "data": {
    "token": "admin_token_9c4e...",
    "user": {
      "id": 1,
      "adminId": "ADM-HST-001",
      "fullName": "Prof. K. Narayanan (Warden)",
      "role": "ADMIN"
    }
  }
}
```

### 1.4 Administrator Login
Authenticate hostel administrator credentials.

- **URL**: `/auth/admin/login`
- **Method**: `POST`
- **Request Body**:
```json
{
  "identifier": "ADM-HST-001",
  "password": "Password@123"
}
```

---

## 2. Student Portal Endpoints

### 2.1 Get Student Profile
- **URL**: `/student/profile?studentId=ASIET2024CS001`
- **Method**: `GET`
- **Response (200 OK)**:
```json
{
  "success": true,
  "message": "Student profile loaded successfully",
  "data": {
    "student": {
      "studentId": "ASIET2024CS001",
      "name": "Rahul Sharma",
      "email": "rahul.cs@adishankara.ac.in",
      "course": "B.Tech Computer Science and Engineering",
      "department": "Computer Science and Engineering",
      "academicYear": "3rd Year (2023-2027)",
      "roomNumber": "A-204",
      "hostel": { "hostelName": "Adi Shankara Institute Main Campus Hostel" }
    },
    "college": {
      "collegeName": "Adi Shankara Institute of Science and Technology",
      "program": "B.Tech Computer Science and Engineering",
      "department": "Computer Science and Engineering"
    }
  }
}
```

### 2.2 Get Monthly Attendance Calendar Data
- **URL**: `/student/attendance/monthly?studentId=ASIET2024CS001&year=2026&month=9`
- **Method**: `GET`
- **Response (200 OK)**:
```json
{
  "success": true,
  "message": "Monthly attendance records loaded",
  "data": {
    "month": 9,
    "monthName": "September",
    "year": 2026,
    "present": 10,
    "absent": 0,
    "notMarked": 4,
    "attendancePercentage": 71.4,
    "totalWorkingDays": 14,
    "days": [
      {
        "date": "2026-09-01",
        "dayOfMonth": 1,
        "dayOfWeek": "TUESDAY",
        "status": "PRESENT",
        "time": "08:42 AM",
        "distance": 2.3,
        "locationStatus": "VERIFIED",
        "isToday": false,
        "isFuture": false
      },
      {
        "date": "2026-09-05",
        "dayOfMonth": 5,
        "dayOfWeek": "SATURDAY",
        "status": "NOT_MARKED",
        "time": null,
        "distance": null,
        "locationStatus": null,
        "isToday": false,
        "isFuture": false
      },
      {
        "date": "2026-09-20",
        "dayOfMonth": 20,
        "dayOfWeek": "SUNDAY",
        "status": "FUTURE",
        "time": null,
        "distance": null,
        "locationStatus": null,
        "isToday": false,
        "isFuture": true
      }
    ]
  }
}
```

---

## 3. Geolocation & Attendance Marking Endpoints

### 3.1 Verify Location
Calculates distance using the mathematical Haversine formula against the student's assigned hostel.

- **URL**: `/attendance/verify-location`
- **Method**: `POST`
- **Request Body**:
```json
{
  "studentId": "ASIET2024CS001",
  "latitude": 10.170610,
  "longitude": 76.435710,
  "accuracy": 15.0
}
```
- **Response (200 OK)**:
```json
{
  "success": true,
  "message": "Location verified successfully",
  "data": {
    "verified": true,
    "distance": 2.5,
    "allowedRadius": 1000,
    "hostelName": "Adi Shankara Institute Main Campus Hostel",
    "hostelLatitude": 10.1706,
    "hostelLongitude": 76.4357,
    "status": "VERIFIED",
    "message": "Location verified successfully. You are 2.5m from hostel (Allowed: 1000m)."
  }
}
```

### 3.2 Mark Attendance
Performs authoritative backend location validation and persists daily roll-call.

- **URL**: `/attendance/mark`
- **Method**: `POST`
- **Request Body**:
```json
{
  "studentId": "ASIET2024CS001",
  "latitude": 10.170610,
  "longitude": 76.435710,
  "accuracy": 15.0
}
```
- **Response (201 Created)**:
```json
{
  "success": true,
  "message": "Attendance Marked Successfully",
  "data": {
    "id": 12,
    "studentId": "ASIET2024CS001",
    "attendanceDate": "2026-09-14",
    "attendanceTime": "08:42:00",
    "distanceFromHostel": 2.5,
    "locationStatus": "VERIFIED",
    "attendanceStatus": "PRESENT",
    "verificationMode": "GEOLOCATION"
  }
}
```

---

## 4. Administrator Portal Endpoints

### 4.1 Admin Dashboard
Returns aggregated metrics for the 4 summary cards and full student roll-call table.

- **URL**: `/admin/dashboard?adminId=ADM-HST-001&date=2026-09-14`
- **Method**: `GET`
- **Response (200 OK)**:
```json
{
  "success": true,
  "message": "Admin dashboard overview retrieved",
  "data": {
    "hostelId": 1,
    "hostelName": "Adi Shankara Institute Main Campus Hostel",
    "adminName": "Prof. K. Narayanan (Warden)",
    "adminId": "ADM-HST-001",
    "selectedDate": "2026-09-14",
    "totalStudents": 5,
    "presentCount": 3,
    "absentCount": 0,
    "notMarkedCount": 2,
    "attendancePercentage": 60.0,
    "studentRecords": [
      {
        "studentId": "ASIET2024CS001",
        "studentName": "Rahul Sharma",
        "roomNumber": "A-204",
        "course": "B.Tech CSE",
        "attendanceStatus": "PRESENT",
        "locationStatus": "VERIFIED",
        "time": "08:42 AM",
        "distance": "2 m",
        "overallPercentage": 92.5
      }
    ]
  }
}
```

### 4.2 Search Students
- **URL**: `/admin/students?hostelId=1&query=Rahul`
- **Method**: `GET`

### 4.3 Student Detail Lookup
- **URL**: `/admin/students/{studentId}`
- **Method**: `GET`
- **Response (200 OK)**: Full profile and history breakdown.
