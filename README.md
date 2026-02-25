# 🎓 College Management System

A comprehensive, modernized JavaFX and React-based college management system with role-based access control, automated database migrations, and a beautiful UI powered by React and **AtlantaFX**.

![Java](https://img.shields.io/badge/Java-17+-orange?style=flat-square)
![JavaFX](https://img.shields.io/badge/JavaFX-17+-blue?style=flat-square)
![React](https://img.shields.io/badge/React-18+-61DAFB?style=flat-square)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=flat-square)
![Build](https://img.shields.io/badge/Build-Maven%20%7C%20Bash-green?style=flat-square)
![Status](https://img.shields.io/badge/Status-Production%20Ready-brightgreen?style=flat-square)

---

## ✨ Key Features

### 🌐 Hybrid Architecture
This project is built as a complete hybrid platform, offering both a standalone JavaFX Desktop Application AND a modern React Web Application interconnected via a custom Java HTTP API.

### 🎨 Modern UI & Experience
- **React Frontend:** Modern web app with fluid navigation, intuitive cards, and clean dashboards.
- **JavaFX Glassmorphism:** A stunning dark-themed UI on the desktop with glass-effect cards and transparent tables.
- **Native Routing:** Smooth single-page application experience with role-based component rendering.

### 🛠️ Technical Enhancements
- **Native Migrations:** Custom `MigrationRunner` automatically updates the database schema on startup using pure Java.
- **RESTful API:** Lightweight, native Java HTTP server (`com.sun.net.httpserver`) providing stateless API endpoints.
- **Performance Optimized:** N+1 query resolutions alongside lazy loading across both Desktop and Web layers.

### 👥 Role-Based Access Control
- **Admin**: Complete system oversight, user management, institute settings, and employee workflows.
- **Faculty**: Academic management, workload tracking, attendance logs, and result publications.
- **Student**: Personalized hub for academics, clubs, events, fees, and secure leave requests.

---

## 📚 Global Modules

The web and desktop applications have achieved 1:1 feature parity. Available modules include:

### Academics & Staffing
- **Institute Management** (Role management, RBAC, Settings)
- **Employee Management** (Non-teaching staff, Salary)
- **Student & Faculty Management** (CRUD, Deep search)
- **Course & Department Records**
- **Attendance** (Single + bulk marking, Analytics)
- **Timetable & Faculty Workload** (Pie charts, Credit distribution)
- **Leave Approvals** (Multi-tier staff/student leave workflow)

### Campus & Facilities
- **Hostel Management** (Hostels, Rooms, Allocations)
- **Gate Pass Management** (Request, Approve, Tracking)
- **Visitor Tracking** (Entry logging, ID proofs, Check-in/out)
- **Room Availability** (Real-time checking, Schedules)
- **Library** (Book management, Issues, Fine tracking)
- **Fees & Ledger** (Pending fees, Payment gateways)

### Student Life & Progression
- **Student Activities Hub** (Central dashboard for Campus Life)
- **Clubs & Societies** (Browse, Memberships, Executive roles)
- **Events & Workshops** (Ticketing, Registration, Banners)
- **Grades & Transcripts** (Transcripts, GPA Calculation)
- **Assignments** (Course submissions, Due dates, Grading)
- **Resource Management** (Upload/download lectures, Notes)
- **Scholarships** (Browse programs, Track application status)
- **Crowdfunding Campaigns** (Deploy initiatives, Donations)
- **Placement & Careers** (Company catalogs, Track interview statuses)

### General Utilities
- **Academic Calendar**
- **Global Announcements & Notifications**
- **Profile & Password Management** (Security controls)
- **Advanced Reports & Analytics**

---

## 🚀 Quick Start

### Prerequisites
- **Java JDK 17+**
- **PostgreSQL 15+**
- **Node.js (for Web UI)**
- **Maven** (Optional)

### Installation & Configuration

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd College-Management-2
   ```

2. **Configure Environment**
   Create a `.env` file from the template and edit your PostgreSQL credentials:
   ```bash
   cp .env.example .env
   ```

3. **Database Setup**
   Simply create an empty database. The application's native Migration Runner handles all schema creation dynamically!
   ```sql
   CREATE DATABASE college_db;
   ```

### 🖥️ Running the Desktop Application
The classic, fully-featured JavaFX application running natively on the desktop:
```bash
# Build the project
./build.sh

# Run the app
./run.sh
```

### 🌐 Running the Web Ecosystem
To launch the Web portal, run the Backend API and the Frontend React Server.

**1. Start the Java REST API Server (Port 7000):**
```bash
./run-api.sh
```

**2. Start the React Frontend (Port 3000):**
```bash
cd web-app
npm install
npm start
```
The web dashboard will be accessible at `http://localhost:3000`.

---

## 🔑 Default Login Credentials

By default, the Migration Script seeds the database with test accounts. Initial passwords are `123`, except for the Admin account.

**Admin**
- `admin` / `admin123`

**Standard Faculty**
- `FAC001` / `123`
- `FAC002` / `123`

**Standard Student**
- `TES2026001` / `123`

*(Note: Create Finance/Warden roles internally via the Admin User Portal).*

---

## 🔒 Security
This modern ecosystem adheres strictly to:
- **Zero-Dependency Encryption**: Passwords salted and hashed securely using standalone Java `MessageDigest` configurations.
- **API Guarding**: Bearer routing sessions verified via internal `ProtectedHandler` layers mitigating unauthenticated access.
- **SQL Guards**: Heavy Reliance on Parameterized Statements preventing all edge-case Injection attempts.

**See [SECURITY.md](SECURITY.md) for detailed security information.**

---

## 🤝 Contributing
1. Fork the repo.
2. Create your feature branch (`git checkout -b feature/EpicEnhancement`).
3. Commit your changes.
4. Push to the branch.
5. Open a Pull Request.

---
**Built with ❤️ for educational excellence.**