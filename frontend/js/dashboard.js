/**
 * ============================================================================
 * STUDENT DASHBOARD & MONTHLY ATTENDANCE CONTROLLER
 * - Loads student photo from localStorage registered face images
 * - Builds attendance calendar from backend OR localStorage demo records
 * ============================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
  Auth.requireStudent();

  const student = Auth.getStudent();
  if (student) {
    populateStudentHeader(student);
    loadStudentPhoto(student);
  }

  CalendarManager.init();

  const btnPrev = document.getElementById('btn-prev-month');
  const btnNext = document.getElementById('btn-next-month');
  if (btnPrev) btnPrev.addEventListener('click', () => CalendarManager.prevMonth());
  if (btnNext) btnNext.addEventListener('click', () => CalendarManager.nextMonth());
});

/**
 * Populates header greetings and sidebar identity
 */
function populateStudentHeader(student) {
  const greetingEl   = document.getElementById('dashboard-greeting');
  const sidebarName  = document.getElementById('sidebar-user-name');
  const sidebarId    = document.getElementById('sidebar-user-id');
  const sidebarAvatar = document.getElementById('sidebar-avatar');

  const hr = new Date().getHours();
  let timeGreeting = 'Good Morning';
  if (hr >= 12 && hr < 17) timeGreeting = 'Good Afternoon';
  else if (hr >= 17) timeGreeting = 'Good Evening';

  const studentName = student.name || 'Student';

  if (greetingEl)    greetingEl.textContent    = `${timeGreeting}, ${studentName}!`;
  if (sidebarName)   sidebarName.textContent   = studentName;
  if (sidebarId)     sidebarId.textContent     = student.studentId || 'Resident Student';
  if (sidebarAvatar) sidebarAvatar.textContent = (studentName.charAt(0) || 'S').toUpperCase();
}

/**
 * Loads the student's registered face photo into the sidebar.
 * Falls back to the initial-letter avatar if no photo is stored.
 */
function loadStudentPhoto(student) {
  const avatarEl = document.getElementById('sidebar-avatar');
  const photoEl  = document.getElementById('sidebar-photo');
  if (!photoEl) return;

  const stored = localStorage.getItem(`profile_photos_${student.studentId}`);
  if (!stored) return;

  try {
    const photos = JSON.parse(stored);
    if (photos && photos.length > 0) {
      photoEl.src          = photos[0]; // first registered photo
      photoEl.style.display = 'block';
      if (avatarEl) avatarEl.classList.add('has-photo'); // hides letter avatar via CSS
    }
  } catch (e) {
    // No photo — leave letter avatar visible
  }
}

/**
 * Calendar Manager
 */
const CalendarManager = {
  currentYear:  2026,
  currentMonth: 9, // 1–12

  init() {
    const now          = new Date();
    this.currentYear   = now.getFullYear();
    this.currentMonth  = now.getMonth() + 1;
    this.fetchAndRender();
  },

  prevMonth() {
    this.currentMonth--;
    if (this.currentMonth < 1) { this.currentMonth = 12; this.currentYear--; }
    this.fetchAndRender();
  },

  nextMonth() {
    this.currentMonth++;
    if (this.currentMonth > 12) { this.currentMonth = 1; this.currentYear++; }
    this.fetchAndRender();
  },

  async fetchAndRender() {
    const student   = Auth.getStudent();
    if (!student) return;

    const monthTitle = document.getElementById('calendar-current-month');
    const container  = document.getElementById('calendar-days-container');

    const monthNames = [
      'January','February','March','April','May','June',
      'July','August','September','October','November','December'
    ];

    if (monthTitle) {
      monthTitle.textContent = `${monthNames[this.currentMonth - 1]} ${this.currentYear}`;
    }
    if (container) {
      container.innerHTML = '<div style="grid-column: span 7; text-align: center; padding: 2.5rem; color: var(--text-muted);">Loading attendance calendar...</div>';
    }

    // Try backend first
    try {
      const res = await fetch(
        `${CONFIG.API_BASE_URL}/attendance/student/${encodeURIComponent(student.studentId)}/month?year=${this.currentYear}&month=${this.currentMonth}`,
        { signal: AbortSignal.timeout(5000) }
      );
      const data = await res.json();

      if (res.ok && data.success && data.data) {
        this.renderCalendar(data.data);
        this.renderStats(data.data);
        return;
      }
    } catch (err) {
      console.warn('Backend unavailable — building calendar from local records.', err.message);
    }

    // ── Offline fallback: build calendar from localStorage demo records ──────
    const calendarData = this.buildLocalCalendar(student.studentId, this.currentYear, this.currentMonth);
    this.renderCalendar(calendarData);
    this.renderStats(calendarData);
  },

  /**
   * Builds a MonthlyAttendanceResponse-compatible object from localStorage
   * demo_attendance records.
   */
  buildLocalCalendar(studentId, year, month) {
    const demoRecords   = JSON.parse(localStorage.getItem('demo_attendance') || '[]');
    const now           = new Date();
    const today         = now.toLocaleDateString('en-CA'); // YYYY-MM-DD

    const startDate   = new Date(year, month - 1, 1);
    const daysInMonth = new Date(year, month, 0).getDate();

    // Map: "YYYY-MM-DD" -> record
    const recordMap = {};
    for (const r of demoRecords) {
      if (r.studentId === studentId) {
        recordMap[r.date] = r;
      }
    }

    let presentCount   = 0;
    let absentCount    = 0;
    let notMarkedCount = 0;
    let elapsedDays    = 0;

    const days = [];
    for (let d = 1; d <= daysInMonth; d++) {
      const dateObj = new Date(year, month - 1, d);
      const dateStr = dateObj.toLocaleDateString('en-CA');
      const isToday  = dateStr === today;
      const isFuture = dateStr > today;

      let status      = 'FUTURE';
      let attendanceTime = null;

      if (recordMap[dateStr]) {
        status         = 'PRESENT';
        attendanceTime = recordMap[dateStr].time || null;
        presentCount++;
        elapsedDays++;
      } else if (isFuture) {
        status = 'FUTURE';
      } else if (isToday) {
        const hr = now.getHours();
        // After 11:59 PM mark as absent, else not marked
        status = (hr >= 23 && now.getMinutes() >= 59) ? 'ABSENT' : 'NOT_MARKED';
        if (status === 'ABSENT') absentCount++;
        else notMarkedCount++;
        elapsedDays++;
      } else {
        // Past day with no record = Absent
        status = 'ABSENT';
        absentCount++;
        elapsedDays++;
      }

      days.push({
        date:           dateStr,
        dayNumber:      d,
        dayOfWeek:      dateObj.toLocaleDateString('en-US', { weekday: 'long' }).toUpperCase(),
        status,
        attendanceTime,
        today:          isToday,
        future:         isFuture
      });
    }

    const percentage = elapsedDays > 0
      ? Math.round((presentCount / elapsedDays) * 1000) / 10
      : 100.0;

    return {
      month,
      monthName: new Date(year, month - 1, 1).toLocaleString('en-US', { month: 'long' }),
      year,
      presentCount,
      absentCount,
      notMarkedCount,
      attendancePercentage: percentage,
      totalElapsedDays: elapsedDays,
      days
    };
  },

  renderCalendar(data) {
    const container = document.getElementById('calendar-days-container');
    if (!container) return;
    container.innerHTML = '';

    const firstDayIndex = new Date(data.year, data.month - 1, 1).getDay();

    // Leading empty cells
    for (let i = 0; i < firstDayIndex; i++) {
      const empty = document.createElement('div');
      empty.className = 'calendar-day-cell empty';
      container.appendChild(empty);
    }

    const dayList = data.days || data.dayList || [];
    dayList.forEach(day => {
      const cell = document.createElement('div');
      cell.className = 'calendar-day-cell';
      if (day.today) cell.classList.add('today');

      let dotHtml = '';
      if (day.status === 'PRESENT') {
        dotHtml = '<span class="day-dot dot-present" title="Present"></span>';
      } else if (day.status === 'ABSENT') {
        dotHtml = '<span class="day-dot dot-absent" title="Absent"></span>';
      } else if (day.status === 'NOT_MARKED') {
        dotHtml = '<span class="day-dot dot-not-marked" title="Not Marked Yet"></span>';
      }

      cell.innerHTML = `
        <span class="day-number">${day.dayNumber}</span>
        <div class="day-dot-container">${dotHtml}</div>
      `;

      cell.addEventListener('click', () => {
        const timeNote = day.attendanceTime ? ` (${day.attendanceTime})` : '';
        const type = day.status === 'PRESENT' ? 'success'
                   : day.status === 'ABSENT'  ? 'error'
                   : 'info';
        showToast(`${day.date} — ${day.status}${timeNote}`, type);
      });

      container.appendChild(cell);
    });
  },

  renderStats(data) {
    const presentEl    = document.getElementById('stat-present');
    const absentEl     = document.getElementById('stat-absent');
    const notMarkedEl  = document.getElementById('stat-not-marked');
    const totalEl      = document.getElementById('stat-total');
    const pctEl        = document.getElementById('stat-percentage');

    if (presentEl)   presentEl.textContent   = data.presentCount   ?? 0;
    if (absentEl)    absentEl.textContent    = data.absentCount    ?? 0;
    if (notMarkedEl) notMarkedEl.textContent = data.notMarkedCount ?? 0;
    if (totalEl)     totalEl.textContent     = data.totalElapsedDays ?? 0;
    if (pctEl)       pctEl.textContent       = `Attendance: ${data.attendancePercentage ?? 0}%`;
  }
};
