# SMART HOSTEL ATTENDANCE MANAGEMENT SYSTEM USING FACE RECOGNITION AND GEOLOCATION

> **College Project**: Smart Hostel Attendance Management System Using Face Recognition and Geolocation  
> **Institution**: Adi Shankara Institute of Science and Technology, Kalady  
> **Version**: Multi-Factor Biometric & Geofenced Attendance System  
> **UI Aesthetic**: Light, Warm, Elegant Editorial Design (Ivory, Cream, Beige, Warm Brown)

---

## 🏛️ System Overview

The **Smart Hostel Attendance Management System (HostelTrack)** provides multi-factor roll-call verification for collegiate residential institutions by combining:
1. **Face Recognition Verification**: Pretrained FaceNet (`InceptionResnetV1` trained on VGGFace2) + `MTCNN` face detection/alignment. Extracts 512-dimensional numerical face embeddings from up to 10 photos per student and computes cosine similarity against live captured camera frames.
2. **Authoritative Geofence Validation**: Spherical Haversine distance verification computed on the Spring Boot backend against assigned hostel coordinates (1000m radius).
3. **Institutional Policy Enforcement**: 1st-year student time window restrictions (09:00 AM – 05:00 PM) and duplicate daily attendance prevention.
4. **Biometric Privacy Protection**: Raw photos and embeddings are never exposed to client browsers or administrators. Admins only see `Face Verification: Registered / Not Registered`.

---

## 🏗️ Architecture & Technology Stack

```
   Browser Client (Webcam + Geolocation API)
         │
         ▼ (Live Face JPEG Base64 + GPS Coordinates)
   Java Spring Boot Backend (Port 8080)
         │
         ├── Retrieves registered 512-d embeddings from MySQL (student_face_profiles)
         │
         ▼ (REST Call: Live Frame + Registered Embeddings)
   Python Facial Recognition Microservice (Port 5000)
         │
         ├── 1. MTCNN Face Detection & Alignment (PIL/Torch, Single Face Enforced)
         ├── 2. InceptionResnetV1 FaceNet Feature Extraction (512-d L2-Normalized Vector)
         └── 3. Cosine Similarity vs Enrolled Photos (Evaluated against FACE_MATCH_THRESHOLD)
         │
         ▼ (Result: matched, similarity score)
   Java Spring Boot Backend
         │
         ├── Validates Face Match
         ├── Validates GPS Geolocation (Haversine distance <= hostel radius)
         └── Records Verified Attendance in MySQL (attendance table)
```

### Technology Highlights
- **Backend Core**: Java 17, Spring Boot 3.2.3, Spring Data JPA, Hibernate, BCrypt.
- **Biometrics Microservice**: Python 3.9+, PyTorch, `facenet-pytorch`, Pillow (PIL), NumPy, Flask.
- **Strict Constraint**: **No OpenCV (`cv2`)** is used in any module. Image decoding and transformations are performed via Pillow and PyTorch tensors.
- **Database**: MySQL 8.0 / H2 in-memory mode.
- **Frontend**: Vanilla HTML5, CSS3, JavaScript (ES6+), Camera API (`navigator.mediaDevices.getUserMedia()`), Geolocation API (`navigator.geolocation`).

---

## 🚀 Setup & Execution Guide

### 1. Python Facial Recognition Microservice Setup

#### A. Create Python Virtual Environment
```bash
# In the project root directory:
python -m venv face-service/venv

# Activate virtual environment:
# Windows (PowerShell):
.\face-service\venv\Scripts\Activate.ps1
# Linux / macOS:
source face-service/venv/bin/activate
```

#### B. Install Dependencies
```bash
pip install -r face-service/requirements.txt
```

#### C. Run the Python Microservice
```bash
# Default port: 5000, default threshold: 0.70
python face-service/app.py
```
Service will start at `http://localhost:5000`. You can verify it by opening `http://localhost:5000/health`.

---

### 2. Java Spring Boot Backend Setup

#### A. Configure Environment Variables (Optional)
The backend reads from `application.properties` with sensible local defaults:
- `FACE_SERVICE_URL`: `http://localhost:5000` (URL of Python microservice)
- `FACE_MATCH_THRESHOLD`: `0.70` (Matching similarity threshold)
- `SPRING_DATASOURCE_URL`: `jdbc:mysql://localhost:3306/smart_hostel_attendance` (or uses H2 in-memory by default)

#### B. Run the Spring Boot Application
```bash
cd backend
mvn spring-boot:run
```
Backend runs on `http://localhost:8080`.

---

### 3. Frontend Web Application Setup

Run the lightweight Node.js static server:
```bash
node serve.js
```
Open `http://localhost:3000` in your web browser.

---

## 📸 Face Registration & Attendance Flow

### 1. Face Registration (`face-registration.html`)
- Log in as student (Demo: `ASIET2024CS001` / `Password@123`).
- Navigate to **Face Registration** in the top menu.
- Upload **up to 10 clear photos** of your face.
- View real-time thumbnail previews and remove unwanted photos.
- Click **Submit & Enroll Faces**.
- The backend validates that each photo contains **exactly one face** and stores the 512-dimensional FaceNet embeddings in MySQL.

### 2. Live Roll-Call Attendance (`attendance.html`)
- Navigate to **Mark Attendance**.
- Click **Open Camera** to activate your webcam.
- Align your face in the oval guide and click **Capture & Verify Face**.
- The system extracts your live face embedding, compares it with your registered embeddings via cosine similarity, checks your GPS location against the hostel radius, and records verified attendance.

---

## 🧪 Kaggle Experimentation Notebook

The repository includes a standalone Kaggle-compatible notebook:  
📂 `face-service/kaggle_facenet_experiment.ipynb`

### What it does:
1. Installs `facenet-pytorch`.
2. Loads pretrained `MTCNN` and `InceptionResnetV1` (VGGFace2).
3. Accepts student test photos (up to 10).
4. Detects face and rejects images with 0 or >1 faces.
5. Generates 512-d embeddings.
6. Accepts a live test photo, computes similarity, and outputs `MATCH / NO MATCH` with score.

> **Note**: Kaggle is used purely for experimentation and parameter tuning. The production application connects the Spring Boot backend directly to the Python microservice.

---

## ⚙️ Configuration & Threshold Tuning

| Variable | Default | Purpose |
| :--- | :--- | :--- |
| `FACE_SERVICE_URL` | `http://localhost:5000` | Address of Python FaceNet service |
| `FACE_MATCH_THRESHOLD` | `0.70` | Minimum cosine similarity required to grant a match (range: 0.0 - 1.0) |
| `face.max.photos` | `10` | Maximum allowed registered face photos per student |
| `app.hostel.default-radius` | `1000` | Allowed perimeter distance in meters (1km geofence) |

---

## 🔒 Security & Privacy Rules

1. **Biometric Privacy**: Face embeddings are stored securely on the backend in `student_face_profiles`. Raw photos are discarded after embedding generation.
2. **Administrator Views**: Administrators only see `Face Verification: Registered` or `Not Registered`. Numerical vectors and raw biometric data are protected.
3. **Authoritative Verification**: Decisions are made entirely on the backend; the browser client cannot force `matched = true`.

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
