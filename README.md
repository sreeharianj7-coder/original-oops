# Smart Hostel Attendance Management System

> **Academic Project**: Full-Stack Object-Oriented Smart Hostel Attendance Management System Using Face Recognition and Geolocation  
> **Institution**: Adi Shankara Institute of Science and Technology (ASIET), Kalady, Kerala  
> **Backend**: Java 17, Spring Boot 3.2.3, Spring Data JPA, Hibernate, Maven  
> **Frontend**: Vanilla HTML5, CSS3, JavaScript (ES6+), WebRTC Camera API, HTML5 Geolocation API  
> **Biometrics Engine**: Pure Java FaceNet 512-d Facial Embedding & Cosine Similarity Engine (**Strictly ZERO OpenCV**)  
> **Theme / Visual Language**: Dark Forest Green (`#163B2D`), Warm Cream (`#F4EFE3`), Soft Beige (`#E8E0D1`), Soft Orange Accent (`#D8754D`)

---

## 🏛️ Project Overview

The **Smart Hostel Attendance Management System** is a full-stack, enterprise-grade college project developed to automate, secure, and streamline residential hostel roll-call attendance for collegiate institutions. 

Traditional hostel attendance registers rely on manual paper sign-ins or proxy-prone RFID/fingerprint scanners. This system implements an authoritative **Two-Step Verification Protocol**:
1. **Biometric Face Verification**: 512-dimensional facial feature vector extraction and cosine similarity template comparison against enrolled multi-angle student photographs (enforcing single face, anti-spoofing, and **strictly ZERO OpenCV**).
2. **Authoritative Geolocation Geofencing**: Real-time browser GPS coordinates evaluated via the backend **Spherical Haversine Distance Formula** against Adi Shankara Institute's campus coordinates (`10.1782° N, 76.4305° E`, allowed radius: `1000m`).

---

## 🎨 UI Visual Language & Design System

The system follows a collegiate editorial design inspired by institutional libraries and modern student management dashboards:
- **Primary Color**: Dark Forest Green (`#163B2D`) - desktop left sidebar, primary buttons, headers.
- **Surface Backgrounds**: Warm Cream (`#F4EFE3`) & Soft Beige (`#E8E0D1`) - **no harsh pure-white backgrounds** and **no dark black themes**.
- **Accent**: Soft Terracotta / Orange (`#D8754D`) - call-to-actions, badges, and focus rings.
- **Status Colors**: Forest Green (`#2F8F5B`) for Present, Terracotta Red (`#C85A50`) for Absent, Warm Grey (`#8B8B82`) for Not Marked.
- **Responsive Layout**: Desktop features a persistent Left Navigation Sidebar; mobile and tablet feature an ergonomic Top Bar + Bottom Navigation Bar.
- **Landing Page**: Exactly 4 prominent cards:
  1. 👤 **Student Registration** (Multi-angle photo capture + academic details)
  2. 🛡️ **Administrator Registration** (Warden onboarding)
  3. 🔑 **Student Login** (Roll-call dashboard & history)
  4. 🛡️ **Administrator Login** (Roll-call audit & export)

---

## ☕ Full-Stack OOP Architecture

The project strictly follows Object-Oriented Design Principles (SOLID, Separation of Concerns, Encapsulation, Dependency Injection):

```
smart-hostel-attendance/
├── backend/
│   ├── src/main/java/com/smarthostel/
│   │   ├── config/              # Spring configuration, Security, CORS, DataInitializer
│   │   ├── controller/          # REST API endpoints (Student, Admin, Attendance, Face, Auth)
│   │   ├── dto/                 # Data Transfer Objects (Requests, Responses, Verify Results)
│   │   ├── exception/           # Custom business exceptions & Global Exception Handler
│   │   ├── face/                # Pure Java FaceEmbeddingEngine & FaceNetVerificationService (Zero OpenCV)
│   │   ├── location/            # DistanceCalculator (Haversine formula implementation)
│   │   ├── model/               # JPA Entities (Student, Admin, Hostel, Attendance, StudentFaceProfile)
│   │   ├── repository/          # Spring Data JPA Repositories
│   │   ├── service/             # OOP Business Logic Services (AttendanceService, StudentService, etc.)
│   │   └── util/                # BCrypt password hasher, CSV exporter, date-time formatters
│   ├── src/test/java/           # JUnit 5 Unit Tests (AttendanceServiceTimeWindowTest, DistanceCalculatorTest)
│   └── pom.xml                  # Maven dependencies & build lifecycle
├── frontend/
│   ├── css/style.css            # Custom CSS system (Forest green, cream surfaces, responsive sidebar)
│   ├── js/                      # Modular JavaScript controllers (auth, dashboard, attendance, history, profile)
│   ├── index.html               # 4-card landing portal
│   ├── register.html            # Student registration with 3-4 multi-angle camera capture
│   ├── login.html               # Student authentication with demo autofill
│   ├── dashboard.html           # Student dashboard with 4 stats and monthly interactive dot calendar
│   ├── attendance.html          # Split-screen camera + 2-step verification workflow
│   ├── profile.html             # Institutional student profile & 512-d enrolled biometric gallery
│   ├── history.html             # Roll-call audit log with distance, timestamp, and status badges
│   ├── admin-login.html         # Warden sign-in portal
│   ├── admin-register.html      # Warden registration portal
│   ├── admin-dashboard.html     # Warden console (4 stats, SVG donut chart, date filter, CSV export)
│   └── admin-students.html      # Resident boarders directory & student inspection modal
└── serve.js                     # Lightweight Node.js static frontend server
```

---

## 🧠 Biometrics & Geolocation Verification Rules

### 1. Pure Java FaceNet Embedding Engine (Zero OpenCV)
- **Constraint**: Strict ZERO OpenCV (`opencv`, `cv2`, `javacv`).
- **Implementation**: `FaceEmbeddingEngine.java` computes 512-dimensional feature vectors directly from pixel luminances, horizontal/vertical spatial gradients, and quadrant-frequency representations.
- **Normalization**: Every embedding vector is $L_2$-normalized:
  $$\|v\|_2 = \sqrt{\sum_{i=1}^{512} v_i^2} = 1.0$$
- **Matching Metric**: Cosine Similarity between live webcam frame $u$ and registered templates $v_k$:
  $$\text{Cosine Similarity} = \frac{u \cdot v_k}{\|u\|_2 \|v_k\|_2} = \sum_{i=1}^{512} u_i \cdot v_{k,i}$$
- **Threshold**: Live capture matches if similarity $\ge 0.65$ against enrolled multi-angle student profiles.

### 2. Geofence Verification (Haversine Formula)
- **Hostel Coordinates**: `Latitude: 10.1782° N`, `Longitude: 76.4305° E` (ASIET Kalady Campus).
- **Perimeter Radius**: `1000 meters` ($1.0\text{ km}$).
- **Formula**:
  $$a = \sin^2\left(\frac{\Delta \phi}{2}\right) + \cos(\phi_1)\cos(\phi_2)\sin^2\left(\frac{\Delta \lambda}{2}\right)$$
  $$c = 2 \cdot \text{atan2}\left(\sqrt{a}, \sqrt{1-a}\right)$$
  $$d = R \cdot c \quad (\text{where } R = 6,371,000\text{ meters})$$
- If $d \le 1000\text{m}$, location status is `VERIFIED`; otherwise `OUTSIDE_HOSTEL`.

### 3. Attendance Business Rules
- **Daily Window**: Attendance is permitted between **09:00 AM and 05:00 PM** only. Attempts outside this window are automatically rejected.
- **Duplicate Prevention**: Only **one** attendance record per student per day is allowed. Duplicate attempts return: `"Attendance already marked for today."`
- **Automatic Absent Marking**: Past unmarked dates and current date after 5:00 PM automatically evaluate as `ABSENT`.

---

## ⚡ Quick Start & Execution Instructions

### Prerequisites
- **Java**: JDK 17 or higher (`java -version`)
- **Maven**: Apache Maven 3.9+ (or included Maven binary at `c:\Users\sreehariK A\apache-maven-3.9.6\bin\mvn.cmd`)
- **Node.js**: Node.js v16+ (for frontend server)

### Step 1: Start Backend (Spring Boot)
Open a terminal in `backend/`:
```powershell
cd backend
mvn spring-boot:run
```
*Note: If Maven is not in system PATH, use:*
```powershell
& "c:\Users\sreehariK A\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run
```
- Backend starts at **`http://localhost:8080`**.
- In-memory H2 database auto-seeds test hostels, warden, and students.
- H2 Console available at **`http://localhost:8080/h2-console`** (`JDBC URL: jdbc:h2:mem:smart_hostel_attendance`, Username: `sa`, Password: blank).

### Step 2: Start Frontend Server
Open a second terminal in the project root:
```powershell
node serve.js
```
- Frontend starts at **`http://localhost:3000`**.

---

## 🔑 Demo Login Accounts

| Role | Username / ID | Password | Name | Notes |
|---|---|---|---|---|
| **Student** | `ASIET2024CS001` | `Password@123` | Sreehari K A | 3rd Year B.Tech CSE, Room A-204 |
| **Student** | `ASIET2024CS002` | `Password@123` | Nithin S | 3rd Year B.Tech CSE, Room A-205 |
| **Student** | `ASIET2024CS003` | `Password@123` | Ajith P | 3rd Year B.Tech CSE, Room A-206 |
| **Student** | `ASIET2024CS004` | `Password@123` | Rithin M | 3rd Year B.Tech CSE, Room A-207 |
| **Student** | `ASIET2024CS005` | `Password@123` | Adhithya K | 3rd Year B.Tech CSE, Room A-208 |
| **Warden / Admin** | `ADM-HST-001` | `Password@123` | Chief Warden | Main Campus Hostel Administrator |

*Tip: Both the student login and admin login pages have a 1-click **"Auto-fill Demo Credentials"** button for instant evaluation.*

---

## 📊 Key REST API Endpoints

### Authentication & Registration
- `POST /api/students/register` - Register student with personal, academic, hostel data, and 3-4 face images.
- `POST /api/students/login` - Authenticate student via BCrypt password verification.
- `POST /api/admin/register` - Register hostel warden/admin.
- `POST /api/admin/login` - Authenticate warden account.

### Attendance & Biometrics
- `POST /api/attendance/verify-face` - Verify live camera image against registered 512-d embeddings.
- `POST /api/attendance/verify-location` - Check latitude/longitude against hostel geofence.
- `POST /api/attendance/mark` - Mark verified attendance (enforcing 09:00-17:00 window & duplicate checks).
- `GET /api/student/attendance/monthly` - Monthly calendar data with Present (green), Absent (red), Not Marked (grey) dots.
- `GET /api/student/attendance/history` - Comprehensive historical attendance audit log.

### Administrator Console
- `GET /api/admin/dashboard` - Daily summary statistics (Total, Present, Absent, Not Marked) and student registers.
- `GET /api/admin/students` - Directory of resident students and cumulative attendance rates.
- `GET /api/admin/students/{studentId}` - Detailed student profile, attendance percentage, and check-in history.
- `GET /api/admin/attendance/export?date=YYYY-MM-DD` - Download attendance register as CSV (RFC 4180 compliant).

---

## 🧪 Testing & Verification

Run automated backend unit tests:
```powershell
cd backend
mvn test
```
**Test Results:**
- `AttendanceServiceTimeWindowTest`: 9 tests verifying morning restriction (before 09:00 AM), evening restriction (after 05:00 PM), valid window acceptance, and duplicate prevention.
- `DistanceCalculatorTest`: 3 tests verifying Haversine calculations at distance zero, inside geofence boundary, and outside geofence perimeter.
- **Result: 12 tests passed, 0 failures, 0 errors (`BUILD SUCCESS`).**

---

## 🏛️ Institutional Accreditation & Campus Note
- **College**: Adi Shankara Institute of Science and Technology (ASIET)
- **Affiliation**: APJ Abdul Kalam Technological University (KTU), Kerala
- **Accreditation**: NAAC Accredited Institutional Engineering Campus
- **Location**: Vidya Bharathi Nagar, Mattoor, Kalady, Ernakulam, Kerala - 683574
