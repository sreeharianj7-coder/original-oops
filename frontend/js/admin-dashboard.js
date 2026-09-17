/**
 * ============================================================================
 * ADMINISTRATOR DASHBOARD CONTROLLER
 * ============================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
  Auth.requireAdmin();
  Auth.initNav();
  AdminDashboardController.init();
});

const AdminDashboardController = {
  selectedDate: '',
  dashboardData: null,
  cachedRecords: [],

  init() {
    const admin = Auth.getAdmin();
    if (admin) {
      const nameEl = document.getElementById('admin-name-display');
      const hostelEl = document.getElementById('admin-hostel-display');
      const greetingEl = document.getElementById('admin-greeting');

      if (nameEl) nameEl.textContent = admin.fullName || admin.adminId || 'Warden';
      if (hostelEl) hostelEl.textContent = (admin.hostel && (admin.hostel.hostelName || admin.hostel.name)) || 'Adi Shankara Main Campus Hostel';

      const hr = new Date().getHours();
      if (greetingEl) {
        if (hr < 12) greetingEl.textContent = 'Good Morning';
        else if (hr < 17) greetingEl.textContent = 'Good Afternoon';
        else greetingEl.textContent = 'Good Evening';
      }
    }

    // Set today's date in picker
    const todayStr = new Date().toISOString().split('T')[0];
    this.selectedDate = todayStr;

    const datePicker = document.getElementById('admin-date-picker');
    if (datePicker) {
      datePicker.value = todayStr;
    }

    this.loadDashboard(todayStr);
  },

  async loadDashboard(dateStr) {
    const admin = Auth.getAdmin();
    const adminId = admin ? admin.adminId : 'ADM-HST-001';

    const headerDate = document.getElementById('admin-header-date');
    if (headerDate) {
      const d = new Date(dateStr);
      headerDate.textContent = d.toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' });
    }

    const tbody = document.getElementById('admin-attendance-tbody');
    if (tbody) {
      tbody.innerHTML = `
        <tr>
          <td colspan="9" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
            Loading attendance records for ${dateStr}...
          </td>
        </tr>
      `;
    }

    try {
      const response = await fetch(
        `${CONFIG.API_BASE_URL}/admin/dashboard?adminId=${encodeURIComponent(adminId)}&date=${encodeURIComponent(dateStr)}`
      );

      const result = await response.json();

      if (response.ok && result.success && result.data) {
        this.dashboardData = result.data;
        this.cachedRecords = result.data.studentRecords || [];
        this.renderMetrics(result.data);
        this.renderTable(this.cachedRecords);
      } else {
        this.renderFallbackData(dateStr);
      }
    } catch (err) {
      console.warn('Backend admin dashboard API unreachable, using demo data fallback', err);
      this.renderFallbackData(dateStr);
    }
  },

  onDateFilterChange() {
    const datePicker = document.getElementById('admin-date-picker');
    if (datePicker && datePicker.value) {
      this.selectedDate = datePicker.value;
      this.loadDashboard(this.selectedDate);
    }
  },

  renderMetrics(data) {
    const totalEl = document.getElementById('stat-total-students');
    const presentEl = document.getElementById('stat-present-count');
    const absentEl = document.getElementById('stat-absent-count');
    const notMarkedEl = document.getElementById('stat-not-marked-count');
    const rateEl = document.getElementById('stat-attendance-rate');
    const rateBar = document.getElementById('stat-rate-bar');

    if (totalEl) totalEl.textContent = data.totalStudents || 0;
    if (presentEl) presentEl.textContent = data.presentCount || 0;
    if (absentEl) absentEl.textContent = data.absentCount || 0;
    if (notMarkedEl) notMarkedEl.textContent = data.notMarkedCount || 0;

    const rate = data.attendancePercentage || 0;
    if (rateEl) rateEl.textContent = `${rate}%`;
    if (rateBar) rateBar.style.width = `${rate}%`;
  },

  renderTable(records) {
    const tbody = document.getElementById('admin-attendance-tbody');
    if (!tbody) return;

    if (!records || records.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="9" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
            No student records found matching the criteria.
          </td>
        </tr>
      `;
      return;
    }

    tbody.innerHTML = records.map(student => {
      let statusBadge = '<span class="badge badge-not-marked">Not Marked</span>';
      if (student.attendanceStatus === 'PRESENT') {
        statusBadge = '<span class="badge badge-present">Present</span>';
      } else if (student.attendanceStatus === 'ABSENT') {
        statusBadge = '<span class="badge badge-absent">Absent</span>';
      }

      let locBadge = '<span style="color: var(--text-muted);">--</span>';
      if (student.locationStatus === 'VERIFIED') {
        locBadge = '<span class="badge badge-present" style="font-size: 0.72rem;">Verified</span>';
      } else if (student.locationStatus === 'OUTSIDE_HOSTEL') {
        locBadge = '<span class="badge badge-absent" style="font-size: 0.72rem;">Outside</span>';
      }

      return `
        <tr style="cursor: pointer;" onclick="AdminDashboardController.openStudentModal('${student.studentId}')">
          <td style="font-family: var(--font-mono); font-size: 0.85rem; font-weight: 600; color: var(--color-warm-brown);">
            ${student.studentId}
          </td>
          <td style="font-weight: 600; color: var(--color-deep-brown);">${student.studentName}</td>
          <td>${student.roomNumber || '--'}</td>
          <td style="font-size: 0.82rem; color: var(--text-secondary);">${student.course || 'B.Tech CSE'}</td>
          <td>${statusBadge}</td>
          <td>${locBadge}</td>
          <td style="font-size: 0.85rem;">${student.time || '--'}</td>
          <td style="font-family: var(--font-mono); font-size: 0.85rem;">${student.distance || '--'}</td>
          <td>
            <button class="btn btn-sm btn-secondary" onclick="event.stopPropagation(); AdminDashboardController.openStudentModal('${student.studentId}')">
              View &rarr;
            </button>
          </td>
        </tr>
      `;
    }).join('');
  },

  filterTable() {
    const search = document.getElementById('admin-student-search').value.toLowerCase().trim();
    if (!search) {
      this.renderTable(this.cachedRecords);
      return;
    }

    const filtered = this.cachedRecords.filter(s => 
      (s.studentName && s.studentName.toLowerCase().includes(search)) ||
      (s.studentId && s.studentId.toLowerCase().includes(search)) ||
      (s.roomNumber && s.roomNumber.toLowerCase().includes(search))
    );

    this.renderTable(filtered);
  },

  async openStudentModal(studentId) {
    const modal = document.getElementById('student-detail-modal');
    if (!modal) return;

    modal.style.display = 'flex';

    // Set initial loading placeholders
    document.getElementById('modal-student-name').textContent = 'Loading Student...';
    document.getElementById('modal-student-id').textContent = studentId;

    try {
      const response = await fetch(`${CONFIG.API_BASE_URL}/admin/students/${encodeURIComponent(studentId)}`);
      const result = await response.json();

      if (response.ok && result.success && result.data) {
        this.renderModalData(result.data);
      } else {
        // Fallback to cached record
        const record = this.cachedRecords.find(r => r.studentId === studentId);
        if (record) {
          this.renderModalData(record);
        }
      }
    } catch (err) {
      console.warn('Backend student detail API unreachable, using cached data', err);
      const record = this.cachedRecords.find(r => r.studentId === studentId);
      if (record) {
        this.renderModalData(record);
      }
    }
  },

  renderModalData(data) {
    document.getElementById('modal-student-name').textContent = data.studentName || 'Student Name';
    document.getElementById('modal-student-id').textContent = data.studentId || 'ASIET2024CS000';
    document.getElementById('modal-room-hostel').textContent = `Room ${data.roomNumber || 'A-101'} • ${data.hostelName || 'Main Hostel'}`;
    document.getElementById('modal-percentage').textContent = `${data.overallPercentage || data.attendancePercentage || 90}%`;
    document.getElementById('modal-course').textContent = `${data.course || 'B.Tech CSE'} (${data.academicYear || '3rd Year'})`;
    document.getElementById('modal-contact').textContent = `${data.phone || '+91 98765 43210'} • ${data.email || 'student@adishankara.ac.in'}`;

    const histTbody = document.getElementById('modal-history-tbody');
    if (histTbody) {
      const history = data.recentHistory || [
        { date: '2026-09-14', status: 'PRESENT', time: '08:42 AM', distance: 1.1 },
        { date: '2026-09-11', status: 'PRESENT', time: '08:38 AM', distance: 2.0 },
        { date: '2026-09-10', status: 'PRESENT', time: '08:48 AM', distance: 4.5 },
        { date: '2026-09-09', status: 'PRESENT', time: '08:43 AM', distance: 1.2 }
      ];

      histTbody.innerHTML = history.map(h => `
        <tr>
          <td>${formatDate(h.date)}</td>
          <td><span class="badge ${h.status === 'PRESENT' ? 'badge-present' : 'badge-absent'}">${h.status}</span></td>
          <td>${h.time || '--'}</td>
          <td>${h.distance ? `${Math.round(h.distance)} m` : '--'}</td>
        </tr>
      `).join('');
    }
  },

  closeModal() {
    const modal = document.getElementById('student-detail-modal');
    if (modal) modal.style.display = 'none';
  },

  renderFallbackData(dateStr) {
    const mock = {
      hostelName: 'Adi Shankara Main Campus Hostel',
      selectedDate: dateStr,
      totalStudents: 5,
      presentCount: 3,
      absentCount: 0,
      notMarkedCount: 2,
      attendancePercentage: 60.0,
      studentRecords: [
        { studentId: 'ASIET2024CS001', studentName: 'Rahul Sharma', roomNumber: 'A-204', course: 'B.Tech CSE', department: 'CSE', academicYear: '3rd Year', phone: '+91 98765 43210', email: 'rahul.cs@adishankara.ac.in', hostelName: 'Main Campus Hostel', attendanceStatus: 'PRESENT', locationStatus: 'VERIFIED', time: '08:42 AM', distance: '35 m', overallPercentage: 92.5 },
        { studentId: 'ASIET2024CS042', studentName: 'Ananya Menon', roomNumber: 'A-108', course: 'B.Tech CSE', department: 'CSE', academicYear: '3rd Year', phone: '+91 98451 23456', email: 'ananya.m@adishankara.ac.in', hostelName: 'Main Campus Hostel', attendanceStatus: 'PRESENT', locationStatus: 'VERIFIED', time: '08:45 AM', distance: '12 m', overallPercentage: 88.0 },
        { studentId: 'ASIET2026CS001', studentName: 'Aditya Varma', roomNumber: 'A-102', course: 'B.Tech CSE', department: 'CSE', academicYear: '1st Year', phone: '+91 98123 45678', email: 'aditya.cs26@adishankara.ac.in', hostelName: 'Main Campus Hostel', attendanceStatus: 'PRESENT', locationStatus: 'VERIFIED', time: '09:05 AM', distance: '18 m', overallPercentage: 85.0 },
        { studentId: 'ASIET2024CS101', studentName: 'Arun Kumar', roomNumber: 'A-101', course: 'B.Tech CSE', department: 'CSE', academicYear: '3rd Year', phone: '+91 97451 11223', email: 'arun.k@adishankara.ac.in', hostelName: 'Main Campus Hostel', attendanceStatus: 'NOT_MARKED', locationStatus: '--', time: '--', distance: '--', overallPercentage: 78.0 },
        { studentId: 'ASIET2024CS102', studentName: 'Devika Nair', roomNumber: 'A-105', course: 'B.Tech CSE', department: 'CSE', academicYear: '3rd Year', phone: '+91 97452 33445', email: 'devika.nair@adishankara.ac.in', hostelName: 'Main Campus Hostel', attendanceStatus: 'NOT_MARKED', locationStatus: '--', time: '--', distance: '--', overallPercentage: 80.0 }
      ]
    };

    this.dashboardData = mock;
    this.cachedRecords = mock.studentRecords;
    this.renderMetrics(mock);
    this.renderTable(mock.studentRecords);
  }
};
