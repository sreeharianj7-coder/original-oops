/**
 * ============================================================================
 * STUDENT PROFILE CONTROLLER
 * ============================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
  Auth.requireStudent();

  const student = Auth.getStudent();
  if (student) {
    populateSidebar(student);
    renderProfile(student);
    renderBiometricGallery(student);
  }

  // Fetch updated student profile from backend
  fetchLiveProfile(student);
  fetchFaceStatus(student);
});

function populateSidebar(student) {
  const avatarEl = document.getElementById('sidebar-avatar');
  const nameEl = document.getElementById('sidebar-user-name');
  const idEl = document.getElementById('sidebar-user-id');

  const initial = (student.name || 'S').charAt(0).toUpperCase();
  if (avatarEl) avatarEl.textContent = initial;
  if (nameEl) nameEl.textContent = student.name || 'Student';
  if (idEl) idEl.textContent = student.studentId || 'Resident';

  const heroAvatar = document.getElementById('profile-hero-avatar');
  if (heroAvatar) heroAvatar.textContent = initial;
}

function renderProfile(student) {
  // Hero section
  const heroName = document.getElementById('prof-hero-name');
  const heroId = document.getElementById('prof-hero-id');
  const heroDept = document.getElementById('prof-hero-dept');

  if (heroName) heroName.textContent = student.name || 'Student';
  if (heroId) heroId.textContent = student.studentId || 'ASIET2024CS001';
  if (heroDept) heroDept.textContent = `${student.course || 'B.Tech CSE'} • ${student.department || 'Computer Science & Engineering'}`;

  // Card 1: Personal Details
  const fullName = document.getElementById('prof-full-name');
  const idVal = document.getElementById('prof-id-val');
  const gender = document.getElementById('prof-gender');
  const dob = document.getElementById('prof-dob');
  const email = document.getElementById('prof-email');
  const phone = document.getElementById('prof-phone');

  if (fullName) fullName.textContent = student.name || '--';
  if (idVal) idVal.textContent = student.studentId || '--';
  if (gender) gender.textContent = student.gender || 'Not specified';
  if (dob) dob.textContent = student.dateOfBirth || 'Not specified';
  if (email) email.textContent = student.email || '--';
  if (phone) phone.textContent = student.phone || '--';

  // Card 2: Academic & Hostel Allocation
  const course = document.getElementById('prof-course');
  const dept = document.getElementById('prof-dept');
  const year = document.getElementById('prof-year');
  const semDiv = document.getElementById('prof-sem-div');
  const hostel = document.getElementById('prof-hostel');
  const room = document.getElementById('prof-room');

  if (course) course.textContent = student.course || 'B.Tech Computer Science and Engineering';
  if (dept) dept.textContent = student.department ? `Department of ${student.department}` : 'Computer Science and Engineering';
  if (year) year.textContent = student.academicYear || '3rd Year';
  
  const semStr = student.semester ? `Sem ${student.semester}` : 'Semester 5';
  const divStr = student.division ? `Div ${student.division}` : 'Div A';
  if (semDiv) semDiv.textContent = `${semStr} • ${divStr}`;

  const hostelName = (student.hostel && (student.hostel.hostelName || student.hostel.name)) || 'Adi Shankara Main Campus Hostel';
  const blockStr = student.block ? ` (${student.block})` : '';
  if (hostel) hostel.textContent = hostelName + blockStr;
  if (room) room.textContent = student.roomNumber ? `Room ${student.roomNumber}` : 'Room A-204';
}

function renderBiometricGallery(student) {
  const container = document.getElementById('bio-gallery-container');
  if (!container) return;

  const studentId = student.studentId;
  let photos = [];
  try {
    const stored = localStorage.getItem(`profile_photos_${studentId}`);
    if (stored) {
      photos = JSON.parse(stored);
    }
  } catch (e) {
    console.warn('Could not read photos from localStorage', e);
  }

  const angleLabels = [
    'Angle 1 • Frontal Neutral',
    'Angle 2 • Left Angle (~15°)',
    'Angle 3 • Right Angle (~15°)',
    'Angle 4 • Slight Tilt / Up'
  ];

  if (photos && photos.length > 0) {
    container.innerHTML = photos.map((imgSrc, idx) => `
      <div class="bio-photo-card">
        <img src="${imgSrc}" alt="Biometric Face Template ${idx + 1}" class="bio-photo-img">
        <div class="bio-photo-label">${angleLabels[idx] || `Angle ${idx + 1}`}</div>
      </div>
    `).join('');
  } else {
    // Generate clean illustrated multi-angle cards
    container.innerHTML = angleLabels.map((label, idx) => `
      <div class="bio-photo-card">
        <div style="height: 120px; display: flex; flex-direction: column; align-items: center; justify-content: center; background: #e8e2d5; color: var(--color-primary);">
          <span style="font-size: 2.2rem;">👤</span>
          <span style="font-size: 0.72rem; font-weight: 600; color: var(--color-accent); margin-top: 0.25rem;">512-d Enrolled</span>
        </div>
        <div class="bio-photo-label">${label}</div>
      </div>
    `).join('');
  }
}

async function fetchLiveProfile(cachedStudent) {
  if (!cachedStudent || !cachedStudent.studentId) return;

  try {
    const response = await fetch(`${CONFIG.API_BASE_URL}/student/profile?studentId=${encodeURIComponent(cachedStudent.studentId)}`);
    const result = await response.json();

    if (response.ok && result.success && result.data && result.data.student) {
      const updated = result.data.student;
      renderProfile(updated);
      populateSidebar(updated);
      localStorage.setItem(CONFIG.STORAGE_KEYS.STUDENT_DATA, JSON.stringify(updated));
    }
  } catch (err) {
    console.warn('Backend profile API unreachable, using cached session data', err);
  }
}

async function fetchFaceStatus(student) {
  if (!student || !student.studentId) return;
  const badge = document.getElementById('prof-face-status-badge');
  if (!badge) return;

  try {
    const response = await fetch(`${CONFIG.API_BASE_URL}/face/status?studentId=${encodeURIComponent(student.studentId)}`);
    const result = await response.json();
    if (response.ok && result.data) {
      const data = result.data;
      if (data.registered) {
        badge.className = 'badge badge-present';
        badge.textContent = `FaceNet Enrolled (${data.photoCount} Vectors)`;
      } else {
        badge.className = 'badge badge-not-marked';
        badge.textContent = 'Not Registered';
      }
    }
  } catch (e) {
    // Keep default
  }
}
