# Facial Recognition Microservice (FaceNet + MTCNN)

Pretrained deep facial recognition microservice for the **Smart Hostel Attendance Management System**.

## Key Architecture & Design Highlights
- **Pretrained Architecture**: `InceptionResnetV1` (pretrained on VGGFace2) + `MTCNN` face detector.
- **Zero Model Training**: Zero custom CNN training required; performs zero-shot feature extraction into 512-dimensional L2-normalized vectors.
- **Strictly No OpenCV**: Uses `Pillow (PIL)` and `PyTorch` for image decoding, cropping, and transformations. No `cv2` dependencies.
- **Configurable Matching**: Dynamic `FACE_MATCH_THRESHOLD` environment variable (default: `0.70`).
- **Single Face Validation**: Enforces exactly 1 face per image. Images with 0 faces or multiple faces are strictly rejected with helpful feedback messages.
- **Per-Student Limit**: Maximum 10 face photos per student.

---

## Directory Structure
```
face-service/
├── app.py                         # Flask REST API endpoints (/face/enroll, /face/verify, /health)
├── face_engine.py                 # MTCNN detection, PIL image handling & FaceNet feature extraction
├── embedding_service.py           # Multi-photo comparison & Cosine Similarity logic
├── requirements.txt               # Python dependencies (Torch, Pillow, Flask, etc.)
├── kaggle_facenet_experiment.ipynb# Kaggle experimentation notebook (12-step test flow)
└── README.md                      # Service documentation
```

---

## Setup & Running Locally

### 1. Create Python Virtual Environment
```bash
# In the project root or face-service/ directory:
python -m venv face-service/venv

# Activate virtual environment:
# Windows (PowerShell):
.\face-service\venv\Scripts\Activate.ps1
# Linux/macOS:
source face-service/venv/bin/activate
```

### 2. Install Dependencies
```bash
pip install -r face-service/requirements.txt
```

### 3. Run the Microservice
```bash
# Default port: 5000
python face-service/app.py

# Or with custom port & threshold:
PORT=5000 FACE_MATCH_THRESHOLD=0.70 python face-service/app.py
```

---

## API Reference

### 1. Health Check
- **Endpoint**: `GET /health` or `GET /face/status`
- **Response**:
```json
{
  "status": "UP",
  "service": "FaceNet Facial Biometric Service",
  "model": "InceptionResnetV1 (Pretrained on VGGFace2)",
  "detector": "MTCNN (Multi-task Cascaded Convolutional Networks)",
  "threshold": 0.70,
  "maxPhotosPerStudent": 10,
  "cv2Used": false
}
```

### 2. Face Enrollment (Max 10 Photos)
- **Endpoint**: `POST /face/enroll`
- **Body**:
```json
{
  "studentId": "ASIET2024CS001",
  "images": [
    "data:image/jpeg;base64,...",
    "data:image/jpeg;base64,..."
  ]
}
```
- **Response**:
```json
{
  "success": true,
  "studentId": "ASIET2024CS001",
  "totalSubmitted": 2,
  "validCount": 2,
  "registeredPhotos": 2,
  "allPhotosValid": true,
  "results": [
    {
      "photoIndex": 1,
      "valid": true,
      "embedding": [0.041, -0.012, "... 512 floats ..."],
      "message": "Face embedding extracted successfully"
    }
  ]
}
```

### 3. Live Face Verification
- **Endpoint**: `POST /face/verify`
- **Body**:
```json
{
  "studentId": "ASIET2024CS001",
  "liveImage": "data:image/jpeg;base64,...",
  "registeredEmbeddings": [
    [0.041, -0.012, "... 512 floats ..."]
  ]
}
```
- **Response**:
```json
{
  "success": true,
  "studentId": "ASIET2024CS001",
  "matched": true,
  "similarity": 0.8842,
  "threshold": 0.70,
  "bestMatchIndex": 1,
  "message": "Face verification successful",
  "details": [
    { "photoIndex": 1, "similarity": 0.8842 }
  ]
}
```

---

## Kaggle Usage Note
The included `kaggle_facenet_experiment.ipynb` is designed to be uploaded directly to Kaggle.com to test and validate the FaceNet matching pipeline. It demonstrates the complete 12-step flow without requiring any model training. The production system connects the Java Spring Boot backend to this Python microservice over local/containerized REST API calls.
