/**
 * ============================================================================
 * STUDENT PROFILE CONTROLLER
 * ============================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
  Auth.requireStudent();
  Auth.initNav();

  const student = Auth.getStudent();
  if (student) {
    renderProfile(student);
  }

  // Fetch updated student profile from backend if available
  fetchLiveProfile(student);
  fetchFaceStatus(student);
});

function renderProfile(student) {
  const nameEl = document.getElementById('prof-name');
  const idEl = document.getElementById('prof-student-id');
  const emailEl = document.getElementById('prof-email');
  const phoneEl = document.getElementById('prof-phone');
  const yearEl = document.getElementById('prof-year');
  const courseEl = document.getElementById('prof-course');
  const deptEl = document.getElementById('prof-dept');
  const hostelEl = document.getElementById('prof-hostel');
  const roomEl = document.getElementById('prof-room');

  if (nameEl) nameEl.textContent = student.name || 'Rahul Sharma';
  if (idEl) idEl.textContent = student.studentId || 'ASIET2024CS001';
  if (emailEl) emailEl.textContent = student.email || 'rahul.cs@adishankara.ac.in';
  if (phoneEl) phoneEl.textContent = student.phone || '+91 98765 43210';
  if (yearEl) yearEl.textContent = student.academicYear || '3rd Year (2023-2027)';
  if (courseEl) courseEl.textContent = student.course || 'B.Tech Computer Science and Engineering';
  if (deptEl) deptEl.textContent = `Department of ${student.department || 'Computer Science and Engineering'}`;
  if (hostelEl) hostelEl.textContent = (student.hostel && (student.hostel.hostelName || student.hostel.name)) || 'Main Campus Hostel';
  if (roomEl) roomEl.textContent = student.roomNumber || 'A-204';
}

async function fetchLiveProfile(cachedStudent) {
  if (!cachedStudent || !cachedStudent.studentId) return;

  try {
    const response = await fetch(`${CONFIG.API_BASE_URL}/student/profile?studentId=${encodeURIComponent(cachedStudent.studentId)}`);
    const result = await response.json();

    if (response.ok && result.success && result.data && result.data.student) {
      renderProfile(result.data.student);
      // Update local storage
      localStorage.setItem(CONFIG.STORAGE_KEYS.STUDENT_DATA, JSON.stringify(result.data.student));
    }
  } catch (err) {
    console.warn('Backend profile API unreachable, using cached session data', err);
  }
}

async function fetchFaceStatus(student) {
  if (!student || !student.studentId) return;
  const badge = document.getElementById('prof-face-badge');
  const info = document.getElementById('prof-face-info');
  if (!badge) return;

  try {
    const response = await fetch(`${CONFIG.API_BASE_URL}/face/status?studentId=${encodeURIComponent(student.studentId)}`);
    const result = await response.json();
    if (response.ok && result.data) {
      const data = result.data;
      if (data.registered) {
        badge.className = 'badge badge-present';
        badge.textContent = 'Registered';
        if (info) info.textContent = `${data.photoCount} of ${data.maxAllowed} FaceNet 512-d templates enrolled.`;
      } else {
        badge.className = 'badge badge-not-marked';
        badge.textContent = 'Not Registered';
        if (info) info.textContent = 'No face photos registered yet. Register photos to enable face verification.';
      }
    }
  } catch (e) {
    // Default
  }
}

