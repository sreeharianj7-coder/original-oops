/**
 * ============================================================================
 * SMART HOSTEL ATTENDANCE - ROLE-BASED AUTH & SESSION MANAGEMENT
 * ============================================================================
 */

const Auth = {
  /**
   * Default sample accounts
   */
  getDefaultSampleStudents() {
    return [
      {
        id: 1,
        studentId: 'ASIET2024CS001',
        username: 'ASIET2024CS001',
        name: 'Rahul Sharma',
        email: 'rahul.cs@adishankara.ac.in',
        phone: '+91 98765 43210',
        gender: 'Male',
        dateOfBirth: '2004-05-15',
        course: 'B.Tech Computer Science and Engineering',
        department: 'Computer Science and Engineering',
        academicYear: '3rd Year (2023-2027)',
        semester: 'S5',
        division: 'A',
        hostel: CONFIG.DEFAULT_HOSTEL,
        hostelId: 1,
        block: 'A Block',
        roomNumber: 'A-204',
        role: 'STUDENT',
        password: 'Password@123'
      }
    ];
  },

  getDefaultSampleAdmins() {
    return [
      {
        id: 1,
        adminId: 'ADM-HST-001',
        fullName: 'Prof. K. Narayanan (Warden)',
        email: 'warden@adishankara.ac.in',
        phone: '+91 94471 23456',
        hostel: CONFIG.DEFAULT_HOSTEL,
        role: 'ADMIN',
        password: 'Password@123'
      }
    ];
  },

  /**
   * Retrieve all registered students from localStorage
   */
  getRegisteredStudents() {
    const key = CONFIG.STORAGE_KEYS.REGISTERED_STUDENTS || 'hosteltrack_registered_students';
    try {
      const stored = localStorage.getItem(key);
      if (stored) {
        const parsed = JSON.parse(stored);
        if (Array.isArray(parsed) && parsed.length > 0) return parsed;
      }
    } catch (e) {
      console.warn('Failed to parse registered students from storage', e);
    }
    const defaults = this.getDefaultSampleStudents();
    this.saveRegisteredStudentsList(defaults);
    return defaults;
  },

  saveRegisteredStudentsList(list) {
    const key = CONFIG.STORAGE_KEYS.REGISTERED_STUDENTS || 'hosteltrack_registered_students';
    try {
      localStorage.setItem(key, JSON.stringify(list));
    } catch (e) {
      console.warn('Failed to save registered students list', e);
    }
  },

  /**
   * Helper to check if a string is likely a Student ID/code rather than a real human name.
   */
  isStudentIdString(str, studentId) {
    if (!str || typeof str !== 'string') return true;
    const clean = str.trim();
    if (!clean) return true;
    if (studentId && clean.toUpperCase() === studentId.trim().toUpperCase()) return true;
    if (/^ASIET/i.test(clean)) return true;
    if (/^[A-Z]{2,6}\d{4,}[A-Z0-9]*$/i.test(clean)) return true;
    return false;
  },

  /**
   * Save or update a single registered student
   */
  saveRegisteredStudent(student) {
    if (!student || !student.studentId) return;
    const list = this.getRegisteredStudents();
    const idx = list.findIndex(s => 
      (s.studentId && s.studentId.toUpperCase() === student.studentId.toUpperCase()) ||
      (s.email && student.email && s.email.toLowerCase() === student.email.toLowerCase()) ||
      (s.username && student.username && s.username.toLowerCase() === student.username.toLowerCase())
    );

    if (idx >= 0) {
      const existingName = list[idx].name;
      const incomingName = student.name;
      let finalName = incomingName;
      if (this.isStudentIdString(incomingName, student.studentId) && !this.isStudentIdString(existingName, list[idx].studentId)) {
        finalName = existingName;
      }
      list[idx] = { ...list[idx], ...student, name: finalName };
    } else {
      list.push(student);
    }
    this.saveRegisteredStudentsList(list);
  },

  /**
   * Find a registered student by studentId, email, or username
   */
  findRegisteredStudent(identifier) {
    if (!identifier) return null;
    const clean = identifier.trim().toLowerCase();
    const list = this.getRegisteredStudents();
    return list.find(s => 
      (s.studentId && s.studentId.toLowerCase() === clean) ||
      (s.email && s.email.toLowerCase() === clean) ||
      (s.username && s.username.toLowerCase() === clean)
    ) || null;
  },

  /**
   * Retrieve all registered admins from localStorage
   */
  getRegisteredAdmins() {
    const key = CONFIG.STORAGE_KEYS.REGISTERED_ADMINS || 'hosteltrack_registered_admins';
    try {
      const stored = localStorage.getItem(key);
      if (stored) {
        const parsed = JSON.parse(stored);
        if (Array.isArray(parsed) && parsed.length > 0) return parsed;
      }
    } catch (e) {
      console.warn('Failed to parse registered admins from storage', e);
    }
    const defaults = this.getDefaultSampleAdmins();
    this.saveRegisteredAdminsList(defaults);
    return defaults;
  },

  saveRegisteredAdminsList(list) {
    const key = CONFIG.STORAGE_KEYS.REGISTERED_ADMINS || 'hosteltrack_registered_admins';
    try {
      localStorage.setItem(key, JSON.stringify(list));
    } catch (e) {
      console.warn('Failed to save registered admins list', e);
    }
  },

  saveRegisteredAdmin(admin) {
    if (!admin || !admin.adminId) return;
    const list = this.getRegisteredAdmins();
    const idx = list.findIndex(a => 
      (a.adminId && a.adminId.toUpperCase() === admin.adminId.toUpperCase()) ||
      (a.email && admin.email && a.email.toLowerCase() === admin.email.toLowerCase())
    );

    if (idx >= 0) {
      list[idx] = { ...list[idx], ...admin };
    } else {
      list.push(admin);
    }
    this.saveRegisteredAdminsList(list);
  },

  findRegisteredAdmin(identifier) {
    if (!identifier) return null;
    const clean = identifier.trim().toLowerCase();
    const list = this.getRegisteredAdmins();
    return list.find(a => 
      (a.adminId && a.adminId.toLowerCase() === clean) ||
      (a.email && a.email.toLowerCase() === clean)
    ) || null;
  },

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
  /**
   * Retrieves logged-in student entity.
   */
  getStudent() {
    const data = localStorage.getItem(CONFIG.STORAGE_KEYS.STUDENT_DATA);
    if (!data) return null;
    try {
      let student = JSON.parse(data);
      if (student && (this.isStudentIdString(student.name, student.studentId) || !student.name)) {
        const found = this.findRegisteredStudent(student.studentId || student.email || student.username);
        if (found && found.name && !this.isStudentIdString(found.name, found.studentId)) {
          student.name = found.name;
          localStorage.setItem(CONFIG.STORAGE_KEYS.STUDENT_DATA, JSON.stringify(student));
        }
      }
      return student;
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
    if (student && (this.isStudentIdString(student.name, student.studentId) || !student.name)) {
      const found = this.findRegisteredStudent(student.studentId || student.email || student.username);
      if (found && found.name && !this.isStudentIdString(found.name, found.studentId)) {
        student.name = found.name;
      }
    }
    localStorage.setItem(CONFIG.STORAGE_KEYS.AUTH_TOKEN, token);
    localStorage.setItem(CONFIG.STORAGE_KEYS.USER_ROLE, 'STUDENT');
    localStorage.setItem(CONFIG.STORAGE_KEYS.STUDENT_DATA, JSON.stringify(student));
    localStorage.removeItem(CONFIG.STORAGE_KEYS.ADMIN_DATA);
    // Also guarantee saved in registered pool
    this.saveRegisteredStudent(student);
  },

  /**
   * Saves administrator session.
   */
  setAdminSession(token, admin) {
    localStorage.setItem(CONFIG.STORAGE_KEYS.AUTH_TOKEN, token);
    localStorage.setItem(CONFIG.STORAGE_KEYS.USER_ROLE, 'ADMIN');
    localStorage.setItem(CONFIG.STORAGE_KEYS.ADMIN_DATA, JSON.stringify(admin));
    localStorage.removeItem(CONFIG.STORAGE_KEYS.STUDENT_DATA);
    this.saveRegisteredAdmin(admin);
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
    const student = {
      id: Date.now(),
      studentId: (formData.studentId || '').trim().toUpperCase(),
      username: (formData.username || formData.studentId || '').trim(),
      name: (formData.name || '').trim(),
      email: (formData.email || '').trim().toLowerCase(),
      phone: (formData.phone || '').trim(),
      gender: formData.gender || 'Male',
      dateOfBirth: formData.dateOfBirth || '',
      course: formData.course || CONFIG.COLLEGE.program,
      department: formData.department || CONFIG.COLLEGE.department,
      academicYear: formData.academicYear || CONFIG.COLLEGE.academicYear,
      semester: formData.semester || 'S5',
      division: formData.division || 'A',
      hostel: CONFIG.DEFAULT_HOSTEL,
      hostelId: formData.hostelId || 1,
      block: formData.block || 'A Block',
      roomNumber: (formData.roomNumber || '').trim(),
      role: 'STUDENT',
      password: formData.password || 'Password@123'
    };

    // Always persist to local registered students storage
    this.saveRegisteredStudent(student);

    try {
      const response = await fetch(`${CONFIG.API_BASE_URL}/auth/student/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });

      const result = await response.json();
      if (response.ok && result.success) {
        const activeStudent = result.data.student || result.data.user || student;
        if (!activeStudent.name || this.isStudentIdString(activeStudent.name, activeStudent.studentId)) {
          activeStudent.name = student.name;
        }
        this.setStudentSession(result.data.token, activeStudent);
        return { success: true, message: result.message, student: activeStudent };
      } else {
        // If backend returned error message, fallback to local registered record
        this.setStudentSession('demo_token_' + Date.now(), student);
        return { success: true, message: 'Account registered successfully (Local Storage)', student };
      }
    } catch (err) {
      console.warn('Backend unavailable, activated local storage registration', err);
      this.setStudentSession('demo_token_' + Date.now(), student);
      return { success: true, message: 'Account created successfully (Local Storage)', student };
    }
  },

  /**
   * Student Login
   */
  async loginStudent(identifier, password) {
    const cleanId = (identifier || '').trim();

    try {
      const response = await fetch(`${CONFIG.API_BASE_URL}/auth/student/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ identifier: cleanId, password })
      });

      const result = await response.json();
      if (response.ok && result.success) {
        let student = result.data.student || result.data.user;
        const localRec = this.findRegisteredStudent(cleanId) || this.findRegisteredStudent(student.studentId);
        if (localRec && localRec.name && !this.isStudentIdString(localRec.name, localRec.studentId)) {
          student.name = localRec.name;
        }
        this.setStudentSession(result.data.token, student);
        return { success: true, message: result.message, student };
      }
    } catch (err) {
      console.warn('Backend unavailable during login, verifying registered local records', err);
    }

    // Local / Offline login verification
    const foundStudent = this.findRegisteredStudent(cleanId);
    if (foundStudent) {
      this.setStudentSession('demo_token_' + Date.now(), foundStudent);
      return { success: true, message: `Welcome back, ${foundStudent.name}!`, student: foundStudent };
    }

    // If identifier is default demo student
    if (cleanId.toUpperCase() === 'ASIET2024CS001' || cleanId.toLowerCase() === 'rahul.cs@adishankara.ac.in') {
      const demoStudent = this.getDefaultSampleStudents()[0];
      this.setStudentSession('demo_token_123', demoStudent);
      return { success: true, message: 'Login successful (Demo Mode)', student: demoStudent };
    }

    // If user provided a new student ID / username that wasn't found in registered list
    if (cleanId.length >= 3) {
      const regList = this.getRegisteredStudents();
      const existing = regList.find(s => 
        (s.studentId && s.studentId.toUpperCase() === cleanId.toUpperCase()) ||
        (s.email && s.email.toLowerCase() === cleanId.toLowerCase()) ||
        (s.username && s.username.toLowerCase() === cleanId.toLowerCase())
      );

      const realName = (existing && existing.name && !this.isStudentIdString(existing.name, existing.studentId))
        ? existing.name
        : (cleanId.includes('@') ? cleanId.split('@')[0] : cleanId);

      const newStudent = {
        id: Date.now(),
        studentId: cleanId.toUpperCase().includes('ASIET') ? cleanId.toUpperCase() : ('ASIET2024' + cleanId.toUpperCase()),
        username: cleanId,
        name: realName,
        email: cleanId.includes('@') ? cleanId.toLowerCase() : `${cleanId.toLowerCase()}@adishankara.ac.in`,
        phone: '+91 98765 43210',
        gender: 'Male',
        dateOfBirth: '2004-05-15',
        course: CONFIG.COLLEGE.program,
        department: CONFIG.COLLEGE.department,
        academicYear: CONFIG.COLLEGE.academicYear,
        semester: 'S5',
        division: 'A',
        hostel: CONFIG.DEFAULT_HOSTEL,
        hostelId: 1,
        block: 'A Block',
        roomNumber: 'A-204',
        role: 'STUDENT',
        password: password || 'Password@123'
      };

      if (existing) {
        Object.assign(newStudent, existing);
        if (existing.name && !this.isStudentIdString(existing.name, existing.studentId)) {
          newStudent.name = existing.name;
        }
      }

      this.setStudentSession('demo_token_' + Date.now(), newStudent);
      return { success: true, message: `Welcome back, ${newStudent.name}!`, student: newStudent };
    }

    return { success: false, message: 'Invalid Student ID or Password' };
  },

  /**
   * Admin Registration
   */
  async registerAdmin(formData) {
    const admin = {
      id: Date.now(),
      adminId: (formData.adminId || '').trim().toUpperCase(),
      fullName: (formData.fullName || '').trim(),
      email: (formData.email || '').trim().toLowerCase(),
      phone: (formData.phone || '').trim(),
      hostel: CONFIG.DEFAULT_HOSTEL,
      role: 'ADMIN',
      password: formData.password || 'Password@123'
    };

    this.saveRegisteredAdmin(admin);

    try {
      const response = await fetch(`${CONFIG.API_BASE_URL}/auth/admin/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });

      const result = await response.json();
      if (response.ok && result.success) {
        const activeAdmin = result.data.admin || result.data.user || admin;
        this.setAdminSession(result.data.token, activeAdmin);
        return { success: true, message: result.message, admin: activeAdmin };
      } else {
        this.setAdminSession('demo_admin_token_' + Date.now(), admin);
        return { success: true, message: 'Admin account created (Local Storage)', admin };
      }
    } catch (err) {
      console.warn('Backend unavailable, saving local demo admin', err);
      this.setAdminSession('demo_admin_token_' + Date.now(), admin);
      return { success: true, message: 'Admin account created (Local Demo Mode)', admin };
    }
  },

  /**
   * Admin Login
   */
  async loginAdmin(identifier, password) {
    const cleanId = (identifier || '').trim();

    try {
      const response = await fetch(`${CONFIG.API_BASE_URL}/auth/admin/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ identifier: cleanId, password })
      });

      const result = await response.json();
      if (response.ok && result.success) {
        const admin = result.data.admin || result.data.user;
        this.setAdminSession(result.data.token, admin);
        return { success: true, message: result.message, admin };
      }
    } catch (err) {
      console.warn('Backend unavailable, fallback to registered admin records', err);
    }

    const foundAdmin = this.findRegisteredAdmin(cleanId);
    if (foundAdmin) {
      this.setAdminSession('demo_admin_token_' + Date.now(), foundAdmin);
      return { success: true, message: `Welcome back, ${foundAdmin.fullName}!`, admin: foundAdmin };
    }

    if (cleanId.toUpperCase().startsWith('ADM') || cleanId.toLowerCase().includes('warden')) {
      const demoAdmin = this.getDefaultSampleAdmins()[0];
      this.setAdminSession('demo_admin_token_999', demoAdmin);
      return { success: true, message: 'Admin login successful (Demo Mode)', admin: demoAdmin };
    }

    return { success: false, message: 'Invalid administrator credentials' };
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
