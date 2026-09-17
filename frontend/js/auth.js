/**
 * ============================================================================
 * SMART HOSTEL ATTENDANCE - ROLE-BASED AUTH & SESSION MANAGEMENT
 * ============================================================================
 */

const Auth = {
  /**
   * Checks if a user is logged in.
   */
  isLoggedIn() {
    return !!localStorage.getItem(CONFIG.STORAGE_KEYS.AUTH_TOKEN);
  },

  /**
   * Retrieves active user role: 'STUDENT' or 'ADMIN'.
   */
  getRole() {
    return localStorage.getItem(CONFIG.STORAGE_KEYS.USER_ROLE);
  },

  /**
   * Retrieves logged-in student entity.
   */
  getStudent() {
    const data = localStorage.getItem(CONFIG.STORAGE_KEYS.STUDENT_DATA);
    if (!data) return null;
    try {
      return JSON.parse(data);
    } catch (e) {
      return null;
    }
  },

  /**
   * Retrieves logged-in admin entity.
   */
  getAdmin() {
    const data = localStorage.getItem(CONFIG.STORAGE_KEYS.ADMIN_DATA);
    if (!data) return null;
    try {
      return JSON.parse(data);
    } catch (e) {
      return null;
    }
  },

  /**
   * Saves student session.
   */
  setStudentSession(token, student) {
    localStorage.setItem(CONFIG.STORAGE_KEYS.AUTH_TOKEN, token);
    localStorage.setItem(CONFIG.STORAGE_KEYS.USER_ROLE, 'STUDENT');
    localStorage.setItem(CONFIG.STORAGE_KEYS.STUDENT_DATA, JSON.stringify(student));
    localStorage.removeItem(CONFIG.STORAGE_KEYS.ADMIN_DATA);
  },

  /**
   * Saves administrator session.
   */
  setAdminSession(token, admin) {
    localStorage.setItem(CONFIG.STORAGE_KEYS.AUTH_TOKEN, token);
    localStorage.setItem(CONFIG.STORAGE_KEYS.USER_ROLE, 'ADMIN');
    localStorage.setItem(CONFIG.STORAGE_KEYS.ADMIN_DATA, JSON.stringify(admin));
    localStorage.removeItem(CONFIG.STORAGE_KEYS.STUDENT_DATA);
  },

  /**
   * Logs the current user out.
   */
  logout() {
    const role = this.getRole();
    localStorage.removeItem(CONFIG.STORAGE_KEYS.AUTH_TOKEN);
    localStorage.removeItem(CONFIG.STORAGE_KEYS.USER_ROLE);
    localStorage.removeItem(CONFIG.STORAGE_KEYS.STUDENT_DATA);
    localStorage.removeItem(CONFIG.STORAGE_KEYS.ADMIN_DATA);
    showToast('Logged out successfully', 'info');
    setTimeout(() => {
      window.location.href = role === 'ADMIN' ? 'admin-login.html' : 'login.html';
    }, 400);
  },

  /**
   * Guard for student-only pages.
   */
  requireStudent() {
    if (!this.isLoggedIn() || this.getRole() !== 'STUDENT') {
      window.location.href = 'login.html';
    }
  },

  /**
   * Guard for administrator-only pages.
   */
  requireAdmin() {
    if (!this.isLoggedIn() || this.getRole() !== 'ADMIN') {
      window.location.href = 'admin-login.html';
    }
  },

  /**
   * Redirects authenticated users from guest pages.
   */
  redirectIfLoggedIn() {
    if (this.isLoggedIn()) {
      if (this.getRole() === 'ADMIN') {
        window.location.href = 'admin-dashboard.html';
      } else {
        window.location.href = 'dashboard.html';
      }
    }
  },

  /**
   * Student Registration
   */
  async registerStudent(formData) {
    try {
      const response = await fetch(`${CONFIG.API_BASE_URL}/auth/student/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });

      const result = await response.json();
      if (response.ok && result.success) {
        this.setStudentSession(result.data.token, result.data.student || result.data.user);
        return { success: true, message: result.message, student: result.data.student || result.data.user };
      } else {
        return { success: false, message: result.message || 'Registration failed' };
      }
    } catch (err) {
      console.warn('Backend unavailable, activating instant offline demo mode', err);
      const student = {
        id: Date.now(),
        studentId: formData.studentId.trim().toUpperCase(),
        name: formData.name.trim(),
        email: formData.email.trim().toLowerCase(),
        phone: formData.phone.trim(),
        course: formData.course || CONFIG.COLLEGE.program,
        department: formData.department || CONFIG.COLLEGE.department,
        academicYear: formData.academicYear || CONFIG.COLLEGE.academicYear,
        hostel: CONFIG.DEFAULT_HOSTEL,
        roomNumber: formData.roomNumber.trim(),
        role: 'STUDENT'
      };
      this.setStudentSession('demo_token_' + Date.now(), student);
      return { success: true, message: 'Account created successfully (Local Demo Mode)', student };
    }
  },

  /**
   * Student Login
   */
  async loginStudent(identifier, password) {
    try {
      const response = await fetch(`${CONFIG.API_BASE_URL}/auth/student/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ identifier, password })
      });

      const result = await response.json();
      if (response.ok && result.success) {
        this.setStudentSession(result.data.token, result.data.student || result.data.user);
        return { success: true, message: result.message, student: result.data.student || result.data.user };
      } else {
        return { success: false, message: result.message || 'Invalid credentials' };
      }
    } catch (err) {
      console.warn('Backend unavailable, checking demo credentials', err);
      if (identifier.toUpperCase().includes('ASIET') || identifier.toLowerCase().includes('@')) {
        const demoStudent = {
          id: 1,
          studentId: identifier.toUpperCase().includes('ASIET') ? identifier.toUpperCase() : 'ASIET2024CS001',
          name: 'Rahul Sharma',
          email: 'rahul.cs@adishankara.ac.in',
          phone: '+91 98765 43210',
          course: 'B.Tech Computer Science and Engineering',
          department: 'Computer Science and Engineering',
          academicYear: '3rd Year (2023-2027)',
          hostel: CONFIG.DEFAULT_HOSTEL,
          roomNumber: 'A-204',
          role: 'STUDENT'
        };
        this.setStudentSession('demo_token_123', demoStudent);
        return { success: true, message: 'Login successful (Demo Mode)', student: demoStudent };
      }
      return { success: false, message: 'Invalid Student ID or Password' };
    }
  },

  /**
   * Admin Registration
   */
  async registerAdmin(formData) {
    try {
      const response = await fetch(`${CONFIG.API_BASE_URL}/auth/admin/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });

      const result = await response.json();
      if (response.ok && result.success) {
        this.setAdminSession(result.data.token, result.data.admin || result.data.user);
        return { success: true, message: result.message, admin: result.data.admin || result.data.user };
      } else {
        return { success: false, message: result.message || 'Admin registration failed' };
      }
    } catch (err) {
      console.warn('Backend unavailable, saving demo admin', err);
      const admin = {
        id: Date.now(),
        adminId: formData.adminId.trim().toUpperCase(),
        fullName: formData.fullName.trim(),
        email: formData.email.trim().toLowerCase(),
        phone: formData.phone.trim(),
        hostel: CONFIG.DEFAULT_HOSTEL,
        role: 'ADMIN'
      };
      this.setAdminSession('demo_admin_token_' + Date.now(), admin);
      return { success: true, message: 'Admin account created (Local Demo Mode)', admin };
    }
  },

  /**
   * Admin Login
   */
  async loginAdmin(identifier, password) {
    try {
      const response = await fetch(`${CONFIG.API_BASE_URL}/auth/admin/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ identifier, password })
      });

      const result = await response.json();
      if (response.ok && result.success) {
        this.setAdminSession(result.data.token, result.data.admin || result.data.user);
        return { success: true, message: result.message, admin: result.data.admin || result.data.user };
      } else {
        return { success: false, message: result.message || 'Invalid admin credentials' };
      }
    } catch (err) {
      console.warn('Backend unavailable, demo admin login fallback', err);
      const demoAdmin = {
        id: 1,
        adminId: identifier.toUpperCase().startsWith('ADM') ? identifier.toUpperCase() : 'ADM-HST-001',
        fullName: 'Prof. K. Narayanan (Warden)',
        email: 'warden@adishankara.ac.in',
        phone: '+91 94471 23456',
        hostel: CONFIG.DEFAULT_HOSTEL,
        role: 'ADMIN'
      };
      this.setAdminSession('demo_admin_token_999', demoAdmin);
      return { success: true, message: 'Admin login successful (Demo Mode)', admin: demoAdmin };
    }
  },

  /**
   * Dynamic navigation auth button renderer
   */
  initNav() {
    const section = document.getElementById('nav-auth-section');
    if (!section) return;

    if (this.isLoggedIn()) {
      const role = this.getRole();
      if (role === 'ADMIN') {
        const admin = this.getAdmin() || { fullName: 'Warden' };
        section.innerHTML = `
          <span style="font-size: 0.85rem; color: var(--text-secondary); font-weight: 500;">
            ${admin.fullName || admin.adminId} <span class="badge badge-neutral" style="font-size: 0.7rem; margin-left: 0.3rem;">ADMIN</span>
          </span>
          <button class="btn btn-sm btn-secondary" onclick="Auth.logout()">Logout</button>
        `;
      } else {
        const student = this.getStudent() || { name: 'Student' };
        section.innerHTML = `
          <span style="font-size: 0.85rem; color: var(--text-secondary); font-weight: 500;">
            ${student.name || student.studentId}
          </span>
          <button class="btn btn-sm btn-secondary" onclick="Auth.logout()">Logout</button>
        `;
      }
    } else {
      section.innerHTML = `
        <a href="login.html" class="btn btn-sm btn-secondary">Student Login</a>
        <a href="admin-login.html" class="btn btn-sm btn-outline">Admin</a>
        <a href="register.html" class="btn btn-sm btn-primary">Register</a>
      `;
    }
  }
};
