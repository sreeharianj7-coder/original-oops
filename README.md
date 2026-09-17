# SMART HOSTEL ATTENDANCE MANAGEMENT SYSTEM USING GEOLOCATION

> **College Project**: Smart Hostel Attendance Management System  
> **Institution**: Adi Shankara Institute of Science and Technology, Kalady  
> **Current Version**: Phase 1 (Role-Based Authentication, Geolocation Validation & Monthly Attendance Calendar)  
> **UI Aesthetic**: Light, Warm, Elegant Editorial Design (Superbook-Inspired)

---

## 🏛️ System Overview

The **Smart Hostel Attendance Management System (HostelTrack)** digitizes college hostel attendance roll-calls through secure authentication, role-based authorization, and high-precision browser geolocation verification.

### Core Value Proposition:
1. **Zero Proxy Attendance**: Geolocation verification ensures students can only mark attendance when physically within their designated hostel block (1000m perimeter).
2. **Authoritative Backend Haversine Engine**: The Java Spring Boot backend independently computes distance using spherical trigonometry; frontend coordinates cannot bypass validation.
3. **Monthly Student Attendance Calendar**: Visual calendar integrated into the student dashboard featuring daily status dots (Present, Absent, Not Marked) backed by real database records.
4. **Hostel Administrator Dashboard**: Real-time roll-call oversight for hostel wardens with 4 summary cards (Total Students, Present, Absent, Not Marked), date filtering, search, and student history lookup.
5. **Future Biometrics Roadmap**: Face Recognition is clearly demarcated as a future module without unnecessary dependencies.

---

## 🎨 Visual Design Language (Light & Warm)

The application follows a **light, warm, minimal, and editorial design system**:

- **Warm Ivory**: `#F8F3EA` (Main Background)
- **Warm Cream**: `#EFE5D5` (Surface & Panels)
- **Soft Beige**: `#E5D7C4` (Borders & Dividers)
- **Sand & Taupe**: `#D7C2A8`, `#B5A18C`
- **Warm Brown**: `#8B6B4A` (Accents & Highlights)
- **Deep Brown**: `#3F3025` (Headings & Primary Actions)
- **Status Colors (Soft, Academic)**:
  - **Present**: Muted Green (`#4A7C59` / `●` Green Dot)
  - **Absent**: Muted Terracotta (`#B9664E` / `●` Red Dot)
  - **Not Marked**: Muted Slate (`#5B7B8C` / `●` Blue Dot)
  - **Future Dates**: No dot / Subtle neutral

---

## 👥 Two User Portals & Roles

| Feature / Page | Student Portal (`STUDENT`) | Administrator Portal (`ADMIN`) |
| :--- | :--- | :--- |
| **Authentication** | `/login.html`, `/register.html` | `/admin-login.html`, `/admin-register.html` |
| **Dashboard** | Residential profile, today's roll-call status | 4 major metrics, attendance rate bar, date filter |
| **Calendar** | Monthly calendar with navigation & status dots | Date-based attendance register table |
| **Attendance Action** | 3-step GPS location verification | Live hostel monitoring & student audit |
| **History / Reports** | Personal attendance audit log | Comprehensive resident student directory & modal |
| **Profile** | College & hostel room allocation details | Warden credentials & hostel jurisdiction |

---

## 📂 Project Repository Structure

```
smart-hostel-attendance/
├── frontend/                     # Vercel-ready Frontend
│   ├── index.html                # Public Landing Page (HostelTrack)
│   ├── login.html                # Student Login
│   ├── register.html             # Student Registration
│   ├── dashboard.html            # Student Dashboard & Monthly Calendar
│   ├── attendance.html           # 3-Step Geolocation Attendance Marking
│   ├── history.html              # Student Attendance History
│   ├── profile.html              # Student Profile
│   ├── admin-login.html          # Administrator Login
│   ├── admin-register.html       # Administrator Registration
│   ├── admin-dashboard.html      # Administrator Dashboard & Monitoring
│   ├── admin-students.html       # Administrator Student Directory
│   ├── admin-profile.html        # Administrator Profile
│   ├── css/
│   │   └── style.css             # Light Warm Editorial Design System
│   └── js/
│       ├── config.js             # API URL resolution & utilities
│       ├── auth.js               # Role-based session management
│       ├── dashboard.js          # Monthly Calendar & statistics controller
│       ├── attendance.js         # Geolocation verification workflow
│       ├── history.js            # Student history controller
│       ├── profile.js            # Student profile controller
│       └── admin-dashboard.js    # Admin dashboard controller
├── backend/                      # Java 17 + Spring Boot 3 REST API
│   ├── pom.xml                   # Maven Build Specification
│   └── src/
│       └── main/
│           ├── java/com/smarthostel/
│           │   ├── SmartHostelApplication.java
│           │   ├── config/       # DataInitializer, CorsConfig
│           │   ├── controller/   # Auth, Student, Admin, Attendance, Hostel
│           │   ├── dto/          # Request & Response DTOs
│           │   ├── model/        # Student, Admin, Hostel, Attendance
│           │   ├── repository/   # JPA Repositories
│           │   ├── service/      # Auth, Admin, Attendance, Location, Student
│           │   └── util/         # DistanceCalculator (Haversine), PasswordHasher (BCrypt)
│           └── resources/
│               ├── application.properties
│               └── application-mysql.properties
├── database/
│   ├── schema.sql                # MySQL DDL (hostels, students, admins, attendance)
│   ├── seed.sql                  # Comprehensive sample data & history
│   └── sample_data.sql           # Sample data mirror
├── docs/
│   └── API.md                    # Complete REST API specifications
├── serve.js                      # Local Node.js static preview server
├── .env.example                  # Environment variables template
├── .gitignore                    # Git ignore rules
└── README.md                     # Documentation
```

---

## 🚀 Local Development Setup

### 1. Prerequisites
- **Java**: JDK 17 or higher
- **Maven**: 3.8+ (optional if using pre-compiled jar/IDE)
- **Node.js**: (v16+ for local static frontend server)
- **MySQL**: (Optional: Backend defaults to in-memory H2 with auto-seeding for instant zero-config testing)

### 2. Database Setup (MySQL Mode)
Open MySQL terminal or MySQL Workbench:
```sql
SOURCE database/schema.sql;
SOURCE database/seed.sql;
```

### 3. Start Spring Boot Backend
In the `backend/` directory:
```bash
# Default mode (Uses H2 auto-seeding with instant test data)
mvn spring-boot:run

# Or with MySQL profile
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```
The backend starts at `http://localhost:8080`.

### 4. Start Frontend
In the root project directory:
```bash
node serve.js
```
Open `http://localhost:3000` in your web browser.

---

## 🔑 Pre-Seeded Demo Accounts

### Student Account:
- **Student ID**: `ASIET2024CS001`
- **Password**: `Password@123`
- **Hostel**: Adi Shankara Institute Main Campus Hostel (Room A-204)

### Administrator Account (Hostel Warden):
- **Admin ID**: `ADM-HST-001`
- **Password**: `Password@123`
- **Role**: Hostel Administrator

---

## 🌐 Deployment Architecture (GitHub + Vercel)

```
GitHub Repository
│
├── frontend/  ───►  Deploy to VERCEL (Static Hosting)
│                    Environment Variable: VITE_API_BASE_URL or window.APP_CONFIG
│
└── backend/   ───►  Deploy to Railway / Render / AWS / Docker
                     Connects to Cloud MySQL
```

### Deploying Frontend on Vercel:
1. Push repository to GitHub.
2. Link repository in Vercel Dashboard.
3. Set **Root Directory** to `frontend`.
4. The frontend automatically connects to the configured backend API URL or runs in self-contained demo mode.

---

## 📐 Mathematical Geolocation Formula (Haversine)

The server calculates great-circle distance between student GPS $(lat_1, lon_1)$ and hostel location $(lat_2, lon_2)$:

$$\Delta lat = lat_2 - lat_1$$
$$\Delta lon = lon_2 - lon_1$$
$$a = \sin^2\left(\frac{\Delta lat}{2}\right) + \cos(lat_1) \cdot \cos(lat_2) \cdot \sin^2\left(\frac{\Delta lon}{2}\right)$$
$$c = 2 \cdot \text{atan2}\left(\sqrt{a}, \sqrt{1-a}\right)$$
$$d = R \cdot c \quad (R = 6,371,000 \text{ m})$$

Validation check: $\text{distance} \le \text{allowedRadius}$ (1000 meters).

---

## 📋 Testing & Verification Checklist

- [x] **Light Warm Design**: Zero dark/neon themes; uses ivory, cream, sand, warm brown, and muted status indicators.
- [x] **Two Portals**: Student and Administrator portals isolated with backend role checks.
- [x] **Monthly Attendance Calendar**: Month navigation, day status dots (Present: Green, Absent: Red, Not Marked: Blue, Future: No dot), and clickable date popovers.
- [x] **Geolocation Attendance**: 3-step interface with live GPS coordinates, backend distance validation, and duplicate prevention.
- [x] **Admin Dashboard**: 4 summary cards, attendance rate progress ring/bar, date picker filter, student search, and student detail popup.
- [x] **Responsive Layouts**: Tested across 1440px, 1024px, 768px, and 390px mobile screens.
- [x] **Future Module Notice**: Face Recognition clearly marked as "Coming Soon" with zero OpenCV dependencies.
