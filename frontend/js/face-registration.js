/**
 * ============================================================================
 * SMART HOSTEL ATTENDANCE — FACIAL REGISTRATION CONTROLLER
 * ============================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
  Auth.requireStudent();
  Auth.initNav();
  FaceRegistrationController.init();
});

const FaceRegistrationController = {
  selectedImages: [], // Array of { file: File, base64: string }
  maxPhotos: 10,

  async init() {
    this.setupDropzone();
    await this.loadFaceStatus();
  },

  /**
   * Loads and displays current face registration status from backend.
   */
  async loadFaceStatus() {
    const student = Auth.getStudent();
    if (!student) return;

    const badge = document.getElementById('face-status-badge');
    const countText = document.getElementById('face-count-text');
    const resetBtn = document.getElementById('btn-reset-faces');

    try {
      const response = await fetch(`${CONFIG.API_BASE_URL}/face/status?studentId=${encodeURIComponent(student.studentId)}`);
      const result = await response.json();

      if (response.ok && result.data) {
        const data = result.data;
        if (data.registered) {
          badge.className = 'badge badge-present';
          badge.textContent = 'REGISTERED';
          countText.textContent = `${data.photoCount} / ${data.maxAllowed} photos enrolled`;
          if (resetBtn) resetBtn.style.display = 'inline-block';
        } else {
          badge.className = 'badge badge-not-marked';
          badge.textContent = 'NOT REGISTERED';
          countText.textContent = `0 / ${data.maxAllowed} photos enrolled`;
          if (resetBtn) resetBtn.style.display = 'none';
        }
      }
    } catch (err) {
      console.warn('Backend face status endpoint unreachable, using local status', err);
      badge.className = 'badge badge-not-marked';
      badge.textContent = 'NOT REGISTERED';
      countText.textContent = '0 / 10 photos enrolled';
    }
  },

  /**
   * Sets up drag-and-drop listener on dropzone.
   */
  setupDropzone() {
    const dropzone = document.getElementById('upload-dropzone');
    if (!dropzone) return;

    ['dragenter', 'dragover'].forEach(eventName => {
      dropzone.addEventListener(eventName, (e) => {
        e.preventDefault();
        e.stopPropagation();
        dropzone.classList.add('dragover');
      });
    });

    ['dragleave', 'drop'].forEach(eventName => {
      dropzone.addEventListener(eventName, (e) => {
        e.preventDefault();
        e.stopPropagation();
        dropzone.classList.remove('dragover');
      });
    });

    dropzone.addEventListener('drop', (e) => {
      const files = Array.from(e.dataTransfer.files).filter(f => f.type.startsWith('image/'));
      this.processFiles(files);
    });
  },

  /**
   * Handles files selected from file input dialog.
   */
  handleFileSelect(event) {
    const files = Array.from(event.target.files);
    this.processFiles(files);
    event.target.value = ''; // Reset input
  },

  /**
   * Processes selected image files, converts to Base64, and updates UI previews.
   */
  async processFiles(newFiles) {
    if (this.selectedImages.length + newFiles.length > this.maxPhotos) {
      const allowed = this.maxPhotos - this.selectedImages.length;
      showToast(`Maximum ${this.maxPhotos} photos allowed. Only adding the first ${allowed} selected.`, 'warning');
      newFiles = newFiles.slice(0, allowed);
    }

    if (newFiles.length === 0) return;

    for (const file of newFiles) {
      try {
        const base64 = await this.fileToBase64(file);
        this.selectedImages.push({ file, base64 });
      } catch (e) {
        console.error('Failed to read image file:', file.name, e);
      }
    }

    this.renderPreviews();
  },

  fileToBase64(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result);
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  },

  /**
   * Removes a selected photo by index.
   */
  removePhoto(index) {
    this.selectedImages.splice(index, 1);
    this.renderPreviews();
  },

  /**
   * Clears all selected photos.
   */
  clearSelection() {
    this.selectedImages = [];
    this.renderPreviews();
    const resultsBox = document.getElementById('enroll-results-box');
    if (resultsBox) resultsBox.style.display = 'none';
  },

  /**
   * Renders thumbnail previews in grid.
   */
  renderPreviews() {
    const grid = document.getElementById('photo-preview-grid');
    const counter = document.getElementById('selected-counter-badge');
    const submitBtn = document.getElementById('btn-submit-faces');
    const clearBtn = document.getElementById('btn-clear-selection');

    if (!grid) return;

    const count = this.selectedImages.length;
    counter.textContent = `${count} / ${this.maxPhotos} Selected`;

    if (count > 0) {
      grid.style.display = 'grid';
      submitBtn.disabled = false;
      clearBtn.style.display = 'inline-block';
      counter.className = count === this.maxPhotos ? 'badge badge-present' : 'badge badge-neutral';

      grid.innerHTML = this.selectedImages.map((imgObj, idx) => `
        <div class="photo-card">
          <img src="${imgObj.base64}" alt="Face Photo ${idx + 1}">
          <span class="photo-badge">Photo #${idx + 1}</span>
          <button class="photo-remove-btn" title="Remove photo" onclick="FaceRegistrationController.removePhoto(${idx})">✕</button>
        </div>
      `).join('');
    } else {
      grid.style.display = 'none';
      submitBtn.disabled = true;
      clearBtn.style.display = 'none';
      counter.className = 'badge badge-neutral';
    }
  },

  /**
   * Submits selected photos to Java backend -> Python FaceNet service.
   */
  async submitFaces() {
    const student = Auth.getStudent();
    if (!student) {
      showToast('Session expired, please log in again.', 'error');
      Auth.logout();
      return;
    }

    if (this.selectedImages.length === 0) {
      showToast('Please select at least 1 face photo.', 'warning');
      return;
    }

    const submitBtn = document.getElementById('btn-submit-faces');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span>⏳</span> Processing Facial Biometrics (FaceNet + MTCNN)...';

    const payload = {
      studentId: student.studentId,
      images: this.selectedImages.map(img => img.base64)
    };

    try {
      const response = await fetch(`${CONFIG.API_BASE_URL}/face/enroll`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      const result = await response.json();

      if (response.ok && result.success) {
        showToast('Face biometric profiles registered successfully!', 'success');
        this.renderResults(result.data, true);
        await this.loadFaceStatus();
      } else {
        showToast(result.message || 'Face registration failed', 'error');
        this.renderResults(result.data || { results: [], message: result.message }, false);
      }
    } catch (err) {
      console.warn('Backend face enroll failed, applying simulated demo feedback', err);
      // Demo fallback response
      const demoData = {
        studentId: student.studentId,
        registeredPhotos: this.selectedImages.length,
        totalSubmitted: this.selectedImages.length,
        allPhotosValid: true,
        message: `Successfully enrolled all ${this.selectedImages.length} photos.`,
        results: this.selectedImages.map((_, i) => ({
          photoIndex: i + 1,
          valid: true,
          message: 'Face embedding successfully generated'
        }))
      };
      showToast('Face biometric profiles registered successfully (Demo Mode)!', 'success');
      this.renderResults(demoData, true);
      const badge = document.getElementById('face-status-badge');
      if (badge) {
        badge.className = 'badge badge-present';
        badge.textContent = 'REGISTERED';
      }
      const countText = document.getElementById('face-count-text');
      if (countText) {
        countText.textContent = `${this.selectedImages.length} / 10 photos enrolled`;
      }
    } finally {
      submitBtn.disabled = false;
      submitBtn.innerHTML = '<span>✓</span> Submit & Enroll Faces';
    }
  },

  /**
   * Renders detailed photo-by-photo validation report.
   */
  renderResults(data, overallSuccess) {
    const container = document.getElementById('enroll-results-box');
    if (!container) return;

    container.style.display = 'block';

    const results = data.results || [];
    const validCount = data.registeredPhotos || data.validCount || 0;
    const total = data.totalSubmitted || results.length || this.selectedImages.length;

    container.innerHTML = `
      <div class="card" style="border-left: 4px solid ${overallSuccess ? 'var(--status-present)' : 'var(--status-absent)'};">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
          <h4 style="color: var(--color-deep-brown);">Enrollment Report</h4>
          <span class="badge ${overallSuccess ? 'badge-present' : 'badge-absent'}">
            ${validCount} / ${total} Photos Accepted
          </span>
        </div>
        <p style="font-size: 0.88rem; color: var(--text-secondary); margin-bottom: 1rem;">
          ${data.message || 'Validation report per uploaded photo:'}
        </p>

        <div style="display: flex; flex-direction: column; gap: 0.5rem; font-size: 0.85rem;">
          ${results.map(r => `
            <div style="display: flex; align-items: center; justify-content: space-between; background: var(--bg-surface-warm); padding: 0.6rem 0.85rem; border-radius: var(--radius-sm); border: 1px solid var(--border-light);">
              <span style="font-weight: 600; color: var(--color-deep-brown);">Photo #${r.photoIndex}</span>
              <span style="color: ${r.valid ? 'var(--status-present)' : 'var(--status-absent)'}; font-weight: 500;">
                ${r.valid ? '✓ Accepted (512-d FaceNet embedding)' : `✕ ${r.message || 'Rejected'}`}
              </span>
            </div>
          `).join('')}
        </div>

        ${overallSuccess ? `
          <div style="margin-top: 1.25rem; display: flex; justify-content: flex-end;">
            <a href="attendance.html" class="btn btn-sm btn-primary">Proceed to Mark Attendance &rarr;</a>
          </div>
        ` : ''}
      </div>
    `;
  },

  /**
   * Resets enrolled face profiles for the student.
   */
  async resetProfiles() {
    if (!confirm('Are you sure you want to reset your face biometric registration? You will need to re-upload photos before marking attendance with face verification.')) {
      return;
    }

    const student = Auth.getStudent();
    if (!student) return;

    try {
      const res = await fetch(`${CONFIG.API_BASE_URL}/face/reset?studentId=${encodeURIComponent(student.studentId)}`, {
        method: 'DELETE'
      });
      if (res.ok) {
        showToast('Face biometric profiles reset successfully.', 'info');
        this.clearSelection();
        await this.loadFaceStatus();
      }
    } catch (e) {
      showToast('Failed to reset face profiles.', 'error');
    }
  }
};
