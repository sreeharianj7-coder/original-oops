/**
 * ============================================================================
 * STUDENT DASHBOARD & MONTHLY ATTENDANCE CONTROLLER
 * ============================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
  Auth.requireStudent();

  const student = Auth.getStudent();
  if (student) {
    populateStudentHeader(student);
  }

  // Initialize interactive calendar
  CalendarManager.init();

  // Setup Month switcher button events
  const btnPrev = document.getElementById('btn-prev-month');
  const btnNext = document.getElementById('btn-next-month');
  if (btnPrev) btnPrev.addEventListener('click', () => CalendarManager.prevMonth());
  if (btnNext) btnNext.addEventListener('click', () => CalendarManager.nextMonth());
});

/**
 * Populates header greetings and sidebar identity
 */
function populateStudentHeader(student) {
  const greetingEl = document.getElementById('dashboard-greeting');
  const sidebarName = document.getElementById('sidebar-user-name');
  const sidebarId = document.getElementById('sidebar-user-id');
  const sidebarAvatar = document.getElementById('sidebar-avatar');

  const hr = new Date().getHours();
  let timeGreeting = 'Good Morning';
  if (hr >= 12 && hr < 17) timeGreeting = 'Good Afternoon';
  else if (hr >= 17) timeGreeting = 'Good Evening';

  const studentName = student.name || 'Student';

  if (greetingEl) {
    greetingEl.textContent = `${timeGreeting}, ${studentName}!`;
  }

  if (sidebarName) sidebarName.textContent = studentName;
  if (sidebarId) sidebarId.textContent = student.studentId || 'Resident Student';
  if (sidebarAvatar) sidebarAvatar.textContent = (studentName.charAt(0) || 'S').toUpperCase();
}

/**
 * Calendar Manager
 */
const CalendarManager = {
  currentYear: 2026,
  currentMonth: 9, // 1 - 12

  init() {
    const now = new Date();
    this.currentYear = now.getFullYear();
    this.currentMonth = now.getMonth() + 1;
    this.fetchAndRender();
  },

  prevMonth() {
    this.currentMonth--;
    if (this.currentMonth < 1) {
      this.currentMonth = 12;
      this.currentYear--;
    }
    this.fetchAndRender();
  },

  nextMonth() {
    this.currentMonth++;
    if (this.currentMonth > 12) {
      this.currentMonth = 1;
      this.currentYear++;
    }
    this.fetchAndRender();
  },

  async fetchAndRender() {
    const student = Auth.getStudent();
    if (!student) return;

    const monthTitle = document.getElementById('calendar-current-month');
    const container = document.getElementById('calendar-days-container');

    const monthNames = [
      'January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December'
    ];

    if (monthTitle) {
      monthTitle.textContent = `${monthNames[this.currentMonth - 1]} ${this.currentYear}`;
    }

    if (container) {
      container.innerHTML = '<div style="grid-column: span 7; text-align: center; padding: 2.5rem; color: var(--text-muted);">Loading attendance calendar...</div>';
    }

    try {
      const res = await fetch(
        `${CONFIG.API_BASE_URL}/attendance/student/${encodeURIComponent(student.studentId)}/month?year=${this.currentYear}&month=${this.currentMonth}`
      );
      const data = await res.json();

      if (res.ok && data.success && data.data) {
        this.renderCalendar(data.data);
        this.renderStats(data.data);
      } else {
        // Fallback to query param endpoint
        const fallbackRes = await fetch(
          `${CONFIG.API_BASE_URL}/student/attendance/monthly?studentId=${encodeURIComponent(student.studentId)}&year=${this.currentYear}&month=${this.currentMonth}`
        );
        const fallbackData = await fallbackRes.json();
        if (fallbackRes.ok && fallbackData.success && fallbackData.data) {
          this.renderCalendar(fallbackData.data);
          this.renderStats(fallbackData.data);
        } else {
          if (container) {
            container.innerHTML = '<div style="grid-column: span 7; text-align: center; padding: 2rem; color: var(--status-absent);">Unable to load attendance records.</div>';
          }
        }
      }
    } catch (err) {
      console.error('Calendar error:', err);
      if (container) {
        container.innerHTML = `<div style="grid-column: span 7; text-align: center; padding: 2rem; color: var(--text-muted);">Offline mode: ${err.message}</div>`;
      }
    }
  },

  renderCalendar(data) {
    const container = document.getElementById('calendar-days-container');
    if (!container) return;

    container.innerHTML = '';

    const firstDayIndex = new Date(data.year, data.month - 1, 1).getDay();

    // Leading empty cells
    for (let i = 0; i < firstDayIndex; i++) {
      const emptyCell = document.createElement('div');
      emptyCell.className = 'calendar-day-cell empty';
      container.appendChild(emptyCell);
    }

    // Days in month
    if (data.days && data.days.length > 0) {
      data.days.forEach(day => {
        const cell = document.createElement('div');
        cell.className = 'calendar-day-cell';
        if (day.today) cell.classList.add('today');

        let dotHtml = '';
        if (day.status === 'PRESENT') {
          dotHtml = '<span class="day-dot dot-present" title="Present"></span>';
        } else if (day.status === 'ABSENT') {
          dotHtml = '<span class="day-dot dot-absent" title="Absent"></span>';
        } else if (day.status === 'NOT_MARKED') {
          dotHtml = '<span class="day-dot dot-not-marked" title="Not Marked"></span>';
        }

        cell.innerHTML = `
          <span class="day-number">${day.dayNumber}</span>
          <div class="day-dot-container">${dotHtml}</div>
        `;

        cell.addEventListener('click', () => {
          showToast(`Date: ${day.date} — Status: ${day.status}${day.attendanceTime ? ' (' + day.attendanceTime + ')' : ''}`, 
            day.status === 'PRESENT' ? 'success' : day.status === 'ABSENT' ? 'error' : 'info');
        });

        container.appendChild(cell);
      });
    }
  },

  renderStats(data) {
    const presentEl = document.getElementById('stat-present');
    const absentEl = document.getElementById('stat-absent');
    const notMarkedEl = document.getElementById('stat-not-marked');
    const totalEl = document.getElementById('stat-total');
    const pctEl = document.getElementById('stat-percentage');

    if (presentEl) presentEl.textContent = data.presentCount ?? 0;
    if (absentEl) absentEl.textContent = data.absentCount ?? 0;
    if (notMarkedEl) notMarkedEl.textContent = data.notMarkedCount ?? 0;
    if (totalEl) totalEl.textContent = data.totalElapsedDays ?? 0;
    if (pctEl) pctEl.textContent = `Attendance: ${data.attendancePercentage ?? 0}%`;
  }
};
