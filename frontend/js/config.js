/**
 * ============================================================================
 * SMART HOSTEL ATTENDANCE - FRONTEND CONFIGURATION & UTILITIES
 * ============================================================================
 */

const CONFIG = {
  // Spring Boot Backend Base URL (Configurable for local & Vercel deployments)
  API_BASE_URL: (window.APP_CONFIG && window.APP_CONFIG.API_BASE_URL) || 
                (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1' 
                  ? 'http://localhost:8080/api' 
                  : '/api'),
  
  // Institution Identity
  COLLEGE: {
    name: 'Adi Shankara Institute of Science and Technology',
    program: 'B.Tech Computer Science and Engineering',
    department: 'Computer Science and Engineering',
    location: 'Mattoor, Kalady, Ernakulam, Kerala - 683574',
    academicYear: '2023 - 2027'
  },

  // Default Campus Hostel Coordinates & Radius (ASIET Demonstration Center)
  DEFAULT_HOSTEL: {
    id: 1,
    name: 'Adi Shankara Institute of Engineering & Technology',
    latitude: 10.1782,
    longitude: 76.4305,
    allowedRadius: 1000, // in meters (1km geofence)
    description: 'Vidya Bharathi Nagar, Mattoor, Kalady, Kerala'
  },

  // Storage Keys for Role-Based Session Management
  STORAGE_KEYS: {
    AUTH_TOKEN: 'hosteltrack_token',
    USER_ROLE: 'hosteltrack_role', // 'STUDENT' or 'ADMIN'
    STUDENT_DATA: 'hosteltrack_student',
    ADMIN_DATA: 'hosteltrack_admin',
    ACTIVE_HOSTEL: 'hosteltrack_active_hostel'
  }
};

/**
 * Toast Notification Utility (Light Warm Editorial Theme)
 */
function showToast(message, type = 'info', duration = 4000) {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  
  let icon = 'ℹ️';
  if (type === 'success') icon = '✓';
  if (type === 'error') icon = '✕';
  if (type === 'warning') icon = '⚠';

  toast.innerHTML = `
    <div style="font-weight: 700; font-size: 1rem; color: ${type === 'success' ? 'var(--status-present)' : type === 'error' ? 'var(--status-absent)' : 'var(--color-sand)'};">${icon}</div>
    <div style="flex-grow: 1; font-size: 0.88rem; line-height: 1.4;">${message}</div>
    <button style="background: none; border: none; color: var(--color-sand); cursor: pointer; font-size: 1rem; padding: 0 0.2rem;" onclick="this.parentElement.remove()">✕</button>
  `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, duration);
}

/**
 * Date / Time Formatting Helpers
 */
function formatDate(dateStr) {
  if (!dateStr) return 'N/A';
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
}

function formatTime(timeStr) {
  if (!timeStr) return 'N/A';
  if (timeStr.includes(':')) {
    const parts = timeStr.split(':');
    let h = parseInt(parts[0], 10);
    const m = parts[1];
    const ampm = h >= 12 ? 'PM' : 'AM';
    h = h % 12 || 12;
    return `${h.toString().padStart(2, '0')}:${m} ${ampm}`;
  }
  return timeStr;
}

/**
 * Mathematical Haversine Distance Calculation
 * (Client-side helper for UI preview; backend performs authoritative verification)
 */
function calculateHaversineDistance(lat1, lon1, lat2, lon2) {
  const R = 6371000; // Earth radius in meters
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = 
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * 
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  const distance = R * c;
  return Math.round(distance * 10) / 10;
}
