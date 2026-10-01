/**
 * ============================================================================
 * TWO-STEP ATTENDANCE WORKFLOW CONTROLLER
 * STEP 1: Real Face Biometric Match (face-api.js client-side)
 * STEP 2: GPS Geolocation Check (ASIET Campus, 1.5 km radius)
 * ============================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
  Auth.requireStudent();

  const student = Auth.getStudent();
  if (student) {
    const sidebarName = document.getElementById('sidebar-user-name');
    const sidebarId   = document.getElementById('sidebar-user-id');
    const sidebarAvatar = document.getElementById('sidebar-avatar');

    let studentName = student.name;
    if (!studentName || Auth.isStudentIdString(studentName, student.studentId)) {
      const reg = Auth.findRegisteredStudent(student.studentId);
      if (reg && reg.name && !Auth.isStudentIdString(reg.name, reg.studentId)) {
        studentName = reg.name;
      }
    }
    if (!studentName || Auth.isStudentIdString(studentName, student.studentId)) {
      studentName = 'Student';
    }

    if (sidebarName) sidebarName.textContent = studentName;
    if (sidebarId)   sidebarId.textContent   = student.studentId || 'Resident Student';
    if (sidebarAvatar) sidebarAvatar.textContent = (studentName.charAt(0) || 'S').toUpperCase();
  }

  AttendanceManager.init();
});

const AttendanceManager = {
  mediaStream: null,
  capturedFaceBase64: null,
  faceVerified: false,
  locationVerified: false,
  currentLatitude: null,
  currentLongitude: null,
  currentAccuracy: 10.0,
  faceApiReady: false,

  // ✅ CORRECT ASIET CAMPUS COORDINATES (Mattoor, Kalady, Kerala)
  CAMPUS_LAT: 10.1706,
  CAMPUS_LON: 76.4357,
  ALLOWED_RADIUS: 1500, // meters

  // face-api.js model source
  MODEL_URL: 'https://raw.githubusercontent.com/justadudewhohacks/face-api.js/master/weights',

  init() {
    const btnToggle  = document.getElementById('btn-toggle-camera');
    const btnCapture = document.getElementById('btn-capture-verify');
    const btnRetake  = document.getElementById('btn-retake');
    const btnMark    = document.getElementById('btn-mark-attendance');

    if (btnToggle)  btnToggle.addEventListener('click',  () => this.toggleCamera());
    if (btnCapture) btnCapture.addEventListener('click', () => this.captureAndVerifyFace());
    if (btnRetake)  btnRetake.addEventListener('click',  () => this.retake());
    if (btnMark)    btnMark.addEventListener('click',    () => this.submitAttendance());

    // Start loading face recognition models in background
    this.preloadFaceModels();
  },

  // ─── Pre-load face-api.js models silently in background ───────────────────
  async preloadFaceModels() {
    if (typeof faceapi === 'undefined') {
      console.warn('face-api.js not loaded from CDN.');
      return;
    }
    try {
      await Promise.all([
        faceapi.nets.ssdMobilenetv1.loadFromUri(this.MODEL_URL),
        faceapi.nets.faceLandmark68Net.loadFromUri(this.MODEL_URL),
        faceapi.nets.faceRecognitionNet.loadFromUri(this.MODEL_URL)
      ]);
      this.faceApiReady = true;
      console.log('✅ Face recognition models loaded successfully.');
    } catch (err) {
      console.warn('Face recognition models could not be loaded from CDN:', err.message);
      // Try alternate URL
      try {
        const ALT_URL = 'https://vladmandic.github.io/face-api/model';
        await Promise.all([
          faceapi.nets.ssdMobilenetv1.loadFromUri(ALT_URL),
          faceapi.nets.faceLandmark68Net.loadFromUri(ALT_URL),
          faceapi.nets.faceRecognitionNet.loadFromUri(ALT_URL)
        ]);
        this.faceApiReady = true;
        console.log('✅ Face recognition models loaded from alternate URL.');
      } catch (e) {
        console.warn('Alternate face model URL also failed:', e.message);
        this.faceApiReady = false;
      }
    }
  },

  // ─── Camera Controls ───────────────────────────────────────────────────────
  async toggleCamera() {
    if (this.mediaStream) {
      this.stopCamera();
    } else {
      await this.startCamera();
    }
  },

  async startCamera() {
    const video       = document.getElementById('camera-video');
    const placeholder = document.getElementById('camera-placeholder');
    const guide       = document.getElementById('face-guide');
    const pill        = document.getElementById('camera-pill');
    const pillText    = document.getElementById('pill-text');
    const badge       = document.getElementById('camera-badge');
    const btnToggle   = document.getElementById('btn-toggle-camera');
    const btnCapture  = document.getElementById('btn-capture-verify');

    try {
      if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        throw new Error('Webcam API is not supported by your browser.');
      }

      this.mediaStream = await navigator.mediaDevices.getUserMedia({
        video: { width: { ideal: 640 }, height: { ideal: 480 }, facingMode: 'user' }
      });

      video.srcObject = this.mediaStream;
      video.style.display = 'block';
      if (placeholder) placeholder.style.display = 'none';
      if (guide)       guide.style.display       = 'block';
      if (pill)        pill.style.display        = 'flex';
      if (pillText)    pillText.textContent       = 'Camera Active — Align Face';
      if (badge) {
        badge.textContent = 'LIVE CAMERA';
        badge.className   = 'badge badge-present';
      }
      if (btnToggle)  btnToggle.innerHTML  = '<span>🛑</span> Stop Camera';
      if (btnCapture) btnCapture.style.display = 'inline-flex';

      showToast('Camera active. Center your face and click "Capture & Verify".', 'info');
    } catch (err) {
      console.error('Camera error:', err);
      showToast('Camera access denied or unavailable: ' + err.message, 'error');
    }
  },

  stopCamera() {
    if (this.mediaStream) {
      this.mediaStream.getTracks().forEach(t => t.stop());
      this.mediaStream = null;
    }
    const video       = document.getElementById('camera-video');
    const placeholder = document.getElementById('camera-placeholder');
    const guide       = document.getElementById('face-guide');
    const pill        = document.getElementById('camera-pill');
    const badge       = document.getElementById('camera-badge');
    const btnToggle   = document.getElementById('btn-toggle-camera');
    const btnCapture  = document.getElementById('btn-capture-verify');

    if (video)       video.style.display       = 'none';
    if (placeholder) placeholder.style.display = 'block';
    if (guide)       guide.style.display       = 'none';
    if (pill)        pill.style.display        = 'none';
    if (badge) {
      badge.textContent = 'CAMERA OFFLINE';
      badge.className   = 'badge badge-not-marked';
    }
    if (btnToggle)  btnToggle.innerHTML       = '<span>📹</span> Start Camera';
    if (btnCapture) btnCapture.style.display  = 'none';
  },

  // ─── STEP 1: Capture & Face Verification ───────────────────────────────────
  async captureAndVerifyFace() {
    const video  = document.getElementById('camera-video');
    const canvas = document.getElementById('captured-canvas');

    if (!video || !this.mediaStream) {
      showToast('Please start the camera first.', 'warning');
      return;
    }

    // Capture snapshot
    canvas.width  = video.videoWidth  || 640;
    canvas.height = video.videoHeight || 480;
    const ctx = canvas.getContext('2d');
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
    this.capturedFaceBase64 = canvas.toDataURL('image/jpeg', 0.85);

    video.style.display  = 'none';
    canvas.style.display = 'block';

    const btnCapture = document.getElementById('btn-capture-verify');
    const btnRetake  = document.getElementById('btn-retake');
    if (btnCapture) btnCapture.style.display = 'none';
    if (btnRetake)  btnRetake.style.display  = 'inline-flex';

    this.setFaceCardState('verifying', 'Verifying Identity...', 'Running face recognition — comparing live photo with registered face profiles...');

    const student      = Auth.getStudent();
    const storedPhotos = JSON.parse(localStorage.getItem(`profile_photos_${student.studentId}`) || '[]');

    if (storedPhotos.length === 0) {
      // No registered photos — reject
      this.faceVerified = false;
      this.setFaceCardState('failed', '✕ No Registered Face',
        'No face photos registered for your account. Please complete face registration on the Register page first.');
      showToast('No face registered. Please register your face first.', 'error');
      this.checkAllVerifications();
      return;
    }

    // Try client-side face-api.js
    if (this.faceApiReady) {
      await this.verifyFaceClientSide(student, storedPhotos, canvas);
    } else {
      // Models not ready yet — try loading now
      this.setFaceCardState('verifying', 'Loading Face Recognition...', 'Downloading face recognition model (first-time, ~6MB)...');
      showToast('Loading face recognition model for first time...', 'info');
      await this.preloadFaceModels();

      if (this.faceApiReady) {
        await this.verifyFaceClientSide(student, storedPhotos, canvas);
      } else {
        // Fall back to backend
        await this.verifyFaceViaBackend(student);
      }
    }
  },

  // Client-side face verification using face-api.js descriptors
  async verifyFaceClientSide(student, storedPhotos, liveCanvas) {
    try {
      this.setFaceCardState('verifying', 'Analysing Face...', 'Detecting facial landmarks and computing 128-d embedding...');

      // Detect face in live capture
      const liveDetection = await faceapi
        .detectSingleFace(liveCanvas, new faceapi.SsdMobilenetv1Options({ minConfidence: 0.5 }))
        .withFaceLandmarks()
        .withFaceDescriptor();

      if (!liveDetection) {
        this.faceVerified = false;
        this.setFaceCardState('failed', '✕ No Face Detected',
          'No face detected in the photo. Please ensure good lighting, face the camera directly, remove glasses/mask if worn, and try again.');
        showToast('No face detected. Try better lighting and face the camera directly.', 'error');
        this.checkAllVerifications();
        return;
      }

      // Build reference descriptors from registered photos
      const refDescriptors = [];
      for (const photoUrl of storedPhotos) {
        try {
          const img = new Image();
          img.src = photoUrl;
          await new Promise((resolve, reject) => {
            img.onload  = resolve;
            img.onerror = reject;
            setTimeout(reject, 5000); // 5s timeout per image
          });
          const det = await faceapi
            .detectSingleFace(img, new faceapi.SsdMobilenetv1Options({ minConfidence: 0.4 }))
            .withFaceLandmarks()
            .withFaceDescriptor();
          if (det) refDescriptors.push(det.descriptor);
        } catch (e) {
          console.warn('Could not process registered photo:', e.message);
        }
      }

      if (refDescriptors.length === 0) {
        // Could not extract faces from registered photos — fall back to backend
        console.warn('No faces found in registered photos, falling back to backend.');
        await this.verifyFaceViaBackend(student);
        return;
      }

      // Find minimum Euclidean distance against all registered descriptors
      let bestDistance = Infinity;
      for (const desc of refDescriptors) {
        const d = faceapi.euclideanDistance(liveDetection.descriptor, desc);
        if (d < bestDistance) bestDistance = d;
      }

      // Threshold: 0.55 (strict — default is 0.6)
      const THRESHOLD = 0.55;
      const confidence = Math.max(0, Math.round((1 - bestDistance / THRESHOLD) * 100));

      if (bestDistance <= THRESHOLD) {
        this.faceVerified = true;
        this.setFaceCardState('success', '✓ Identity Verified',
          `Face matches registered profile (Confidence: ${confidence}%). Proceeding to location check...`);
        showToast('Identity verified! Checking location...', 'success');
        await this.verifyLocation();
      } else {
        this.faceVerified = false;
        this.setFaceCardState('failed', '✕ Identity Mismatch',
          `Face does NOT match the registered student profile (Score: ${confidence}%). Only the registered student can mark attendance.`);
        showToast('Face verification failed — this does not match the registered student.', 'error');
        this.checkAllVerifications();
      }

    } catch (err) {
      console.error('Client-side face verification error:', err);
      // Fall back to backend
      await this.verifyFaceViaBackend(student);
    }
  },

  // Backend face verification fallback
  async verifyFaceViaBackend(student) {
    try {
      const res = await fetch(`${CONFIG.API_BASE_URL}/attendance/verify-face`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ studentId: student.studentId, faceImage: this.capturedFaceBase64 })
      });
      const data = await res.json();

      if (res.ok && data.success && data.data && data.data.matched) {
        this.faceVerified = true;
        const score = data.data.similarityScore ? (data.data.similarityScore * 100).toFixed(1) + '%' : 'High';
        this.setFaceCardState('success', '✓ Face Verified', `Face matches registered profile (Similarity: ${score})`);
        showToast('Face verified! Checking location...', 'success');
        await this.verifyLocation();
      } else {
        this.faceVerified = false;
        const msg = (data && data.data && data.data.message) || (data && data.message) || 'Face does not match registered profile.';
        this.setFaceCardState('failed', '✕ Face Verification Failed', msg);
        showToast(msg, 'error');
        this.checkAllVerifications();
      }
    } catch (err) {
      // Both client-side and backend failed
      this.faceVerified = false;
      this.setFaceCardState('failed', '✕ Verification Service Unavailable',
        'Face recognition service is offline. Ensure face-api.js models load (internet required) or start the backend server.');
      showToast('Face recognition unavailable. Check internet connection or start the backend.', 'error');
      this.checkAllVerifications();
    }
  },

  // ─── STEP 2: GPS Location Verification ─────────────────────────────────────
  async verifyLocation() {
    this.setLocationCardState('checking', 'Checking Location...', 'Acquiring your GPS coordinates...');

    let lat, lon, accuracy;

    try {
      const pos = await this.getCurrentGpsPosition();
      lat      = pos.coords.latitude;
      lon      = pos.coords.longitude;
      accuracy = pos.coords.accuracy || 10.0;
    } catch (err) {
      this.locationVerified = false;
      this.setLocationCardState('failed', '✕ GPS Access Denied',
        'Location permission denied. Please allow location access in your browser settings and try again.');
      showToast('Allow location access in browser settings to mark attendance.', 'error');
      this.checkAllVerifications();
      return;
    }

    this.currentLatitude  = lat;
    this.currentLongitude = lon;
    this.currentAccuracy  = accuracy;

    // Client-side Haversine distance check
    const dist = Math.round(calculateHaversineDistance(lat, lon, this.CAMPUS_LAT, this.CAMPUS_LON));

    // Try backend first
    try {
      const res = await fetch(`${CONFIG.API_BASE_URL}/attendance/verify-location`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          studentId: Auth.getStudent().studentId,
          latitude: lat, longitude: lon, accuracy
        })
      });
      const data = await res.json();

      if (res.ok && data.success && data.data && data.data.verified) {
        this.locationVerified = true;
        const d = Math.round(data.data.distance);
        this.setLocationCardState('success', '✓ Location Verified',
          `Inside ASIET campus (${d}m from campus center, allowed: ${data.data.allowedRadius}m)`);
        showToast('Location verified!', 'success');
      } else {
        this.locationVerified = false;
        const msg = (data && data.data && data.data.message) ||
          `You are ${dist}m from ASIET campus (allowed radius: ${this.ALLOWED_RADIUS}m). Attendance must be marked from within campus.`;
        this.setLocationCardState('failed', '✕ Outside Campus', msg);
        showToast(msg, 'error');
      }
    } catch (err) {
      // Backend offline — use client-side Haversine result
      if (dist <= this.ALLOWED_RADIUS) {
        this.locationVerified = true;
        this.setLocationCardState('success', '✓ Location Verified',
          `Inside ASIET campus (${dist}m from Mattoor, Kalady campus center, allowed: ${this.ALLOWED_RADIUS}m)`);
        showToast('Location verified — within campus!', 'success');
      } else {
        this.locationVerified = false;
        this.setLocationCardState('failed', '✕ Outside Campus',
          `You are ${dist}m from ASIET campus (Mattoor, Kalady). Attendance requires being within ${this.ALLOWED_RADIUS}m. Your GPS: ${lat.toFixed(5)}°N, ${lon.toFixed(5)}°E`);
        showToast(`Outside campus. You are ${dist}m away (limit: ${this.ALLOWED_RADIUS}m).`, 'error');
      }
    }

    this.checkAllVerifications();
  },

  getCurrentGpsPosition() {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('Geolocation is not supported by your browser.'));
        return;
      }
      navigator.geolocation.getCurrentPosition(resolve, reject, {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0
      });
    });
  },

  // ─── Retake Photo ───────────────────────────────────────────────────────────
  retake() {
    const video      = document.getElementById('camera-video');
    const canvas     = document.getElementById('captured-canvas');
    const btnCapture = document.getElementById('btn-capture-verify');
    const btnRetake  = document.getElementById('btn-retake');

    if (canvas) canvas.style.display = 'none';
    if (video)  video.style.display  = 'block';
    if (btnCapture) btnCapture.style.display = 'inline-flex';
    if (btnRetake)  btnRetake.style.display  = 'none';

    this.faceVerified      = false;
    this.locationVerified  = false;
    this.capturedFaceBase64 = null;

    this.setFaceCardState('ready', 'Face Verification',
      'Awaiting live photo capture. Align your face and click "Capture & Verify".');
    this.setLocationCardState('ready', 'Location Verification',
      `Checks GPS against ASIET Campus Center (Lat: ${this.CAMPUS_LAT}, Lon: ${this.CAMPUS_LON}, Radius: ${this.ALLOWED_RADIUS}m).`);
    this.checkAllVerifications();
  },

  // ─── Enable / Disable Mark Button ──────────────────────────────────────────
  checkAllVerifications() {
    const btnMark = document.getElementById('btn-mark-attendance');
    const banner  = document.getElementById('attendance-status-banner');

    if (this.faceVerified && this.locationVerified) {
      if (btnMark) {
        btnMark.disabled   = false;
        btnMark.className  = 'btn btn-primary btn-block btn-lg';
      }
      if (banner) {
        banner.style.display    = 'block';
        banner.style.background = 'var(--status-present-bg)';
        banner.style.color      = 'var(--status-present)';
        banner.style.border     = '1px solid var(--status-present-border)';
        banner.innerHTML        = '✓ Ready to Confirm — Face identity verified & inside campus. Click [MARK ATTENDANCE] to finalize.';
      }
    } else {
      if (btnMark) btnMark.disabled = true;
      if (banner)  banner.style.display = 'none';
    }
  },

  // ─── Submit Attendance ──────────────────────────────────────────────────────
  async submitAttendance() {
    if (!this.faceVerified || !this.locationVerified) {
      showToast('Both face identity and location must be verified first.', 'error');
      return;
    }

    const student = Auth.getStudent();
    const btnMark = document.getElementById('btn-mark-attendance');
    const banner  = document.getElementById('attendance-status-banner');

    btnMark.disabled = true;
    btnMark.innerHTML = '<span>⌛</span> Recording attendance...';

    try {
      const res = await fetch(`${CONFIG.API_BASE_URL}/attendance/mark`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          studentId:  student.studentId,
          latitude:   this.currentLatitude,
          longitude:  this.currentLongitude,
          accuracy:   this.currentAccuracy,
          faceImage:  this.capturedFaceBase64
        })
      });
      const data = await res.json();

      if (res.status === 201 && data.success) {
        this._showAttendanceSuccess(btnMark, banner, 'Attendance successfully recorded!');
      } else if (res.status === 409) {
        btnMark.innerHTML = '<span>⚠️</span> ALREADY MARKED TODAY';
        if (banner) {
          banner.style.display = 'block';
          banner.style.background = 'var(--status-absent-bg)';
          banner.style.color  = 'var(--status-absent)';
          banner.style.border = '1px solid var(--status-absent-border)';
          banner.innerHTML = '⚠️ <strong>Attendance already marked for today.</strong> Duplicate submissions are blocked.';
        }
        showToast('Attendance already marked for today.', 'warning');
      } else {
        const msg = data.message || 'Unable to record attendance.';
        btnMark.disabled = false;
        btnMark.innerHTML = '<span>✓</span> MARK ATTENDANCE';
        if (banner) {
          banner.style.display = 'block';
          banner.style.background = 'var(--status-absent-bg)';
          banner.style.color  = 'var(--status-absent)';
          banner.style.border = '1px solid var(--status-absent-border)';
          banner.innerHTML = `✕ <strong>Attendance Rejected:</strong> ${msg}`;
        }
        showToast(msg, 'error');
      }
    } catch (err) {
      // Backend offline — save locally (face + location already verified above)
      console.warn('Backend offline — saving attendance locally.', err);
      this._saveDemoAttendance(student);
      this._showAttendanceSuccess(btnMark, banner, 'Attendance recorded locally (Demo Mode — backend offline)!');
    }
  },

  _saveDemoAttendance(student) {
    const now     = new Date();
    const dateStr = now.toLocaleDateString('en-CA'); // YYYY-MM-DD
    const timeStr = now.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit', hour12: true });
    const record  = {
      studentId: student ? student.studentId : 'DEMO',
      date: dateStr,
      time: timeStr,
      status: 'PRESENT',
      location: `Lat: ${this.currentLatitude?.toFixed(4)}, Lon: ${this.currentLongitude?.toFixed(4)}`,
      distance: Math.round(calculateHaversineDistance(
        this.currentLatitude, this.currentLongitude, this.CAMPUS_LAT, this.CAMPUS_LON
      )),
      mode: 'FACE_AND_GEOLOCATION'
    };
    const existing = JSON.parse(localStorage.getItem('demo_attendance') || '[]');
    // Prevent duplicate for same date
    const alreadyExists = existing.some(r => r.date === dateStr && r.studentId === record.studentId);
    if (!alreadyExists) {
      existing.unshift(record);
      localStorage.setItem('demo_attendance', JSON.stringify(existing));
    }
    localStorage.setItem('demo_attendance_today', dateStr);
  },

  _showAttendanceSuccess(btnMark, banner, message) {
    btnMark.innerHTML  = '<span>✓</span> ATTENDANCE RECORDED';
    btnMark.className  = 'btn btn-forest btn-block btn-lg';
    if (banner) {
      banner.style.display    = 'block';
      banner.style.background = 'var(--status-present-bg)';
      banner.style.color      = 'var(--status-present)';
      banner.style.border     = '1px solid var(--status-present-border)';
      banner.innerHTML        = `🎉 <strong>[✓ Attendance Marked]</strong> ${message}`;
    }
    showToast(message, 'success');
    this.stopCamera();
    setTimeout(() => { window.location.href = 'dashboard.html'; }, 1800);
  },

  // ─── Card State Helpers ─────────────────────────────────────────────────────
  setFaceCardState(state, title, desc) {
    const card        = document.getElementById('card-step-face');
    const icon        = document.getElementById('face-status-icon');
    const header      = document.getElementById('face-status-header');
    const description = document.getElementById('face-status-desc');

    if (card) {
      card.className = 'verification-card';
      if (state === 'success')   card.classList.add('success');
      else if (state === 'failed') card.classList.add('failed');
    }
    if (icon) {
      if (state === 'success')   icon.textContent = '✓';
      else if (state === 'failed') icon.textContent = '✕';
      else if (state === 'verifying') icon.textContent = '⚡';
      else icon.textContent = '⏳';
    }
    if (header)      header.textContent      = title;
    if (description) description.textContent = desc;
  },

  setLocationCardState(state, title, desc) {
    const card        = document.getElementById('card-step-location');
    const icon        = document.getElementById('location-status-icon');
    const header      = document.getElementById('location-status-header');
    const description = document.getElementById('location-status-desc');

    if (card) {
      card.className = 'verification-card';
      if (state === 'success')   card.classList.add('success');
      else if (state === 'failed') card.classList.add('failed');
    }
    if (icon) {
      if (state === 'success')   icon.textContent = '✓';
      else if (state === 'failed') icon.textContent = '✕';
      else if (state === 'checking') icon.textContent = '📡';
      else icon.textContent = '⏳';
    }
    if (header)      header.textContent      = title;
    if (description) description.textContent = desc;
  }
};
