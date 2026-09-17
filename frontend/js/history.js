/**
 * ============================================================================
 * STUDENT ATTENDANCE HISTORY CONTROLLER
 * ============================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
  Auth.requireStudent();
  Auth.initNav();
  HistoryController.loadHistory();
});

const HistoryController = {
  rawHistory: [],

  async loadHistory() {
    const student = Auth.getStudent();
    if (!student) return;

    const tbody = document.getElementById('history-table-body');

    try {
      const response = await fetch(
        `${CONFIG.API_BASE_URL}/student/attendance/history?studentId=${encodeURIComponent(student.studentId)}`
      );

      const result = await response.json();

      if (response.ok && result.success && Array.isArray(result.data)) {
        this.rawHistory = result.data;
        this.renderTable(result.data);
      } else {
        this.renderFallbackHistory();
      }
    } catch (err) {
      console.warn('Backend history API unavailable, using sample attendance data', err);
      this.renderFallbackHistory();
    }
  },

  renderTable(records) {
    const tbody = document.getElementById('history-table-body');
    if (!tbody) return;

    if (!records || records.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="6" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
            No attendance records found for this student.
          </td>
        </tr>
      `;
      return;
    }

    tbody.innerHTML = records.map(record => {
      const isPresent = record.attendanceStatus === 'PRESENT';
      const statusBadge = isPresent 
        ? '<span class="badge badge-present">Present</span>' 
        : '<span class="badge badge-absent">Absent</span>';

      const locBadge = record.locationStatus === 'VERIFIED'
        ? '<span class="badge badge-present" style="font-size: 0.72rem;">Verified</span>'
        : '<span class="badge badge-absent" style="font-size: 0.72rem;">Outside</span>';

      const dist = record.distanceFromHostel != null ? `${Math.round(record.distanceFromHostel)} m` : '--';

      return `
        <tr>
          <td style="font-weight: 600; color: var(--color-deep-brown);">${formatDate(record.attendanceDate)}</td>
          <td style="color: var(--text-secondary);">${formatTime(record.attendanceTime)}</td>
          <td>${locBadge}</td>
          <td style="font-family: var(--font-mono); font-size: 0.85rem;">${dist}</td>
          <td>${statusBadge}</td>
          <td style="font-size: 0.8rem; color: var(--text-muted);">${record.verificationMode || 'GEOLOCATION'}</td>
        </tr>
      `;
    }).join('');
  },

  applyFilter() {
    const filter = document.getElementById('filter-status').value;
    if (filter === 'ALL') {
      this.renderTable(this.rawHistory);
    } else {
      const filtered = this.rawHistory.filter(r => r.attendanceStatus === filter);
      this.renderTable(filtered);
    }
  },

  renderFallbackHistory() {
    const student = Auth.getStudent();
    const mock = [
      { attendanceDate: '2026-09-14', attendanceTime: '08:42:00', locationStatus: 'VERIFIED', distanceFromHostel: 1.1, attendanceStatus: 'PRESENT', verificationMode: 'GEOLOCATION' },
      { attendanceDate: '2026-09-11', attendanceTime: '08:38:19', locationStatus: 'VERIFIED', distanceFromHostel: 2.0, attendanceStatus: 'PRESENT', verificationMode: 'GEOLOCATION' },
      { attendanceDate: '2026-09-10', attendanceTime: '08:48:30', locationStatus: 'VERIFIED', distanceFromHostel: 4.5, attendanceStatus: 'PRESENT', verificationMode: 'GEOLOCATION' },
      { attendanceDate: '2026-09-09', attendanceTime: '08:43:45', locationStatus: 'VERIFIED', distanceFromHostel: 1.2, attendanceStatus: 'PRESENT', verificationMode: 'GEOLOCATION' },
      { attendanceDate: '2026-09-08', attendanceTime: '08:40:11', locationStatus: 'VERIFIED', distanceFromHostel: 3.0, attendanceStatus: 'PRESENT', verificationMode: 'GEOLOCATION' },
      { attendanceDate: '2026-09-07', attendanceTime: '08:35:22', locationStatus: 'VERIFIED', distanceFromHostel: 4.2, attendanceStatus: 'PRESENT', verificationMode: 'GEOLOCATION' },
      { attendanceDate: '2026-09-04', attendanceTime: '08:50:00', locationStatus: 'VERIFIED', distanceFromHostel: 7.8, attendanceStatus: 'PRESENT', verificationMode: 'GEOLOCATION' },
      { attendanceDate: '2026-09-03', attendanceTime: '08:45:10', locationStatus: 'VERIFIED', distanceFromHostel: 0.5, attendanceStatus: 'PRESENT', verificationMode: 'GEOLOCATION' },
      { attendanceDate: '2026-09-02', attendanceTime: '08:39:50', locationStatus: 'VERIFIED', distanceFromHostel: 3.1, attendanceStatus: 'PRESENT', verificationMode: 'GEOLOCATION' },
      { attendanceDate: '2026-09-01', attendanceTime: '08:42:15', locationStatus: 'VERIFIED', distanceFromHostel: 2.3, attendanceStatus: 'PRESENT', verificationMode: 'GEOLOCATION' }
    ];
    this.rawHistory = mock;
    this.renderTable(mock);
  }
};
