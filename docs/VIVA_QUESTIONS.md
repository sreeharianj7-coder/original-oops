# College Project Viva & Presentation Questions

### Q1: What is the main objective of this project?
**Answer:** The objective is to automate college hostel attendance through secure student authentication and authoritative server-side geolocation verification, replacing manual paper registers with an auditable digital system.

---

### Q2: What OOP concepts are implemented in this project?
**Answer:**
1. **Encapsulation:** Model entities (`Student`, `Hostel`, `Attendance`) use private fields with getter/setter accessors.
2. **Abstraction:** Service interfaces (`FaceVerificationService`) define functionality without exposing implementation details.
3. **Inheritance & Polymorphism:** Repository interfaces inherit from Spring Data's `JpaRepository`.
4. **Single Responsibility Principle (SRP):** Haversine distance calculations are isolated in `DistanceCalculator.java`, cryptography in `PasswordHasher.java`, and HTTP routing in Controllers.

---

### Q3: How is geolocation verified securely?
**Answer:** The browser acquires GPS coordinates via `navigator.geolocation` and forwards them to the Java backend. The backend retrieves the student's assigned hostel coordinates from MySQL, computes great-circle distance using the **Haversine formula**, and checks if the distance is $\le \text{allowedRadius}$ (e.g., 100 meters).

---

### Q4: Why is distance computed on the backend rather than the frontend?
**Answer:** Frontend JavaScript can be tampered with in browser developer tools (e.g. altering `locationVerified = true`). Performing the calculation authoritatively on the Java backend ensures tamper-proof verification.

---

### Q5: How are passwords secured?
**Answer:** Passwords are never stored in plain text. They are salted and hashed using **BCrypt** (`BCryptPasswordEncoder`) with work factor 10.

---

### Q6: What is the current status of the Face Recognition module?
**Answer:** Face Recognition is part of the future development roadmap (Phase 2). In this current phase (~50-60%), the system focuses on Student Registration/Login, Geolocation Verification, and Attendance History. An architectural interface (`FaceVerificationService.java`) is prepared as an OOP contract.
