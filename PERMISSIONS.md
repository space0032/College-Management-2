# 🔐 Roles & Permissions Guide

The College Management System uses a robust Role-Based Access Control (RBAC) system. This document outlines the capabilities of each role.

## 👥 Roles Overview

| Role | Description | Key Capabilities |
|------|-------------|------------------|
| **ADMIN** | System Administrator | Full access to all modules, user management, system settings. |
| **FACULTY** | Teaching Staff | Manage courses, syllabus, resources, grading, and attendance. |
| **STUDENT** | Enrolled Student | View own profile, grades, attendance, join clubs, download resources. |
| **WARDEN** | Hostel Warden | Manage hostel rooms, gate passes, and resident students. |
| **FINANCE** | Finance Officer | Manage fee structures, collect fees, generate financial reports. |

---

## 🛠️ Detailed Permissions

### 🎓 Student
*   **Academics**: View own grades, attendance, timetable.
*   **Learning**: View and download syllabi and learning resources.
*   **Activities**: browse events, join clubs (request membership), view club activities.
*   **Hostel**: View room details, request gate passes (if hostelite).
*   **Fees**: View fee status and payment history.

### 👨‍🏫 Faculty
*   **Course Management**: View assigned courses, students.
*   **Learning Resources**: Upload syllabus (PDF), upload learning materials (DOCX/PDF).
*   **Grading**: Enter and edit student grades.
*   **Attendance**: Mark student attendance for classes.
*   **Activities**: Create and manage events and clubs.
*   **Profile**: Manage own profile and workload.

### 🏠 Warden
*   **Hostel Management**: Manage hostel buildings, rooms, and bed allocation.
*   **Residents**: View list of hostel students.
*   **Gate Pass**: Approve or reject student gate pass requests.

### 💰 Finance
*   **Fee Management**: Create fee structures (Tuition, Hostel, etc.).
*   **Collection**: Record fee payments from students.
*   **Reports**: View financial reports and fee dues.

### ⚙️ Admin
*   **User Management**: Create/Edit/Delete Users, Faculty, Students, Staff.
*   **System**: View audit logs, manage institute settings.
*   **Overrides**: Can perform most actions of other roles (except specific personal views).

---

## ➕ Secondary Roles

Every user keeps one **primary role**, which drives what's displayed (portal selection, dashboard, role labels). A user may also be assigned zero or more **secondary roles**, which contribute **permission perks only** — the effective permission set is the union of the primary and secondary roles.

*   **Perks**: A student granted a secondary `WARDEN` role can access hostel-management permissions while still appearing as a *Student* in the UI.
*   **No display change**: Secondary roles never change the primary role label, portal, or dashboard.
*   **Guardrails**:
    *   `ADMIN` cannot be assigned as a secondary role.
    *   A user cannot be assigned their own primary role as a secondary role.
    *   Assigning secondary roles requires `UPDATE_USER`, and a user can only grant secondary roles whose permissions they already hold.
*   **Where managed**: Admin users with `VIEW_USER`/`UPDATE_USER` manage secondary roles from **Roles → Users** (the "Secondary roles" column). Users can view their secondary roles on their profile page.
*   **Evaluation**: Permission checks evaluate the effective union automatically across the web app and desktop client; no extra login step is needed.

---

## 📝 Activity & Club Workflows

### Club Membership
1.  **Join Request**: A Student clicks "Join" on a Club. Status becomes `PENDING`.
2.  **Approval**: The Club President or Faculty Coordinator views the "Pending Approvals" list.
3.  **Action**: They explicitly **Approve** or **Reject** the request.
4.  **Result**: 
    *   *Approved*: Student becomes a member and can view exclusive club content.
    *   *Rejected*: Student is notified and cannot access member features.

### Event Registration
1.  **Register**: Student clicks "Register" for an upcoming event.
2.  **Tracking**: System tracks the registration count against `Max Participants`.
3.  **Attendance**: During the event, Faculty/Admin marks the student as `ATTENDED`.

---

## 📚 Learning Portal
*   **Syllabus**: Faculty uploads versioned syllabus files. Students see the latest version.
*   **Resources**: Faculty uploads notes/slides. Resources can be tagged by Category (e.g., "Notes", "Lab Manual").
*   **Search**: Students can search resources by title or category.

