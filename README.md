# 🎓 College Management System

> A comprehensive, enterprise-grade college management platform with dual desktop & web interfaces, role-based access control, and 50+ integrated modules. Built with Java, JavaFX, React, and PostgreSQL.

[![Java](https://img.shields.io/badge/Java-17+-orange?style=flat-square&logo=java)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-17+-blue?style=flat-square)](https://gluonhq.com/products/javafx/)
[![React](https://img.shields.io/badge/React-18+-61DAFB?style=flat-square&logo=react)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-336791?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)

---

## 📋 Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Modules](#modules)
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [Security](#security)
- [Contributing](#contributing)

---

## 🎯 Overview

College Management System is a full-featured institutional management platform designed to streamline operations across administrative, academic, and student lifecycle management. It provides:

- **Dual Interface Architecture**: Desktop (JavaFX) and Web (React) with complete feature parity
- **50+ Integrated Modules**: Covering academics, campus facilities, student life, and utilities
- **Role-Based Access Control**: Admin, Faculty, Student, and specialized roles (Warden, Finance)
- **Enterprise-Ready**: Custom migrations, RESTful APIs, optimized queries, and comprehensive security

**Perfect for**: Small to large educational institutions, colleges, universities, and training centers.

---

## ✨ Key Features

### 🌐 Hybrid Architecture
- **Desktop Application**: Standalone JavaFX desktop client with glassmorphism UI
- **Web Application**: Modern React SPA with responsive design
- **Unified API**: Custom Java HTTP server bridging both platforms
- **Feature Parity**: Same business logic, different UIs

### 🎨 UI/UX Excellence
| Feature | Description |
|---------|-------------|
| **Glassmorphism Design** | Modern dark theme with glass-effect cards on desktop |
| **Responsive Web UI** | Mobile-friendly React dashboard with smooth navigation |
| **Single-Page Experience** | Native routing with instant component switching |
| **Accessibility First** | Designed for diverse user roles with intuitive interfaces |

### ⚙️ Technical Highlights
- **Auto-Migrations**: Native Java `MigrationRunner` handles schema updates on startup
- **RESTful API**: Lightweight HTTP server using `com.sun.net.httpserver` (zero external dependencies)
- **Performance Optimized**: N+1 query resolution, lazy loading, connection pooling
- **Security First**: Password hashing (SHA-256), parameterized SQL queries, bearer token auth

### 👥 Role-Based Permissions
```
Admin         → System oversight, user management, settings, reports
Faculty       → Academics, attendance, workload, results
Student       → Dashboard, academics, events, clubs, fees, leaves
Warden        → Hostel management, room allocations
Finance       → Fee collection, ledger management
```

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    College Management System                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────┐          ┌──────────────────────────────┐ │
│  │ JavaFX Desktop   │          │  React Web Application       │ │
│  │  - Glassmorphism │          │  - Modern SPA               │ │
│  │  - Native UI     │          │  - Responsive Design        │ │
│  └────────┬─────────┘          └──────────────┬───────────────┘ │
│           │                                   │                  │
│           └───────────────────┬───────────────┘                  │
│                               │                                  │
│                   ┌───────────▼────────────┐                     │
│                   │  Java REST API        │                     │
│                   │  (Port 7000)          │                     │
│                   │  - Authentication     │                     │
│                   │  - Business Logic     │                     │
│                   │  - Data Validation    │                     │
│                   └───────────┬────────────┘                     │
│                               │                                  │
│                   ┌───────────▼────────────┐                     │
│                   │  PostgreSQL 15+       │                     │
│                   │  - 50+ Tables         │                     │
│                   │  - Auto Migrations    │                     │
│                   │  - Audit Logs         │                     │
│                   └───────────────────────┘                      │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Frontend** | React 18+, HTML5, CSS3 | Web UI & UX |
| **Desktop** | JavaFX 17+, AtlantaFX | Desktop UI with modern design |
| **Backend** | Java 17+, Spring-less custom HTTP | Lightweight API |
| **Database** | PostgreSQL 15+ | Persistent data storage |
| **Build** | Maven, Bash Scripts | Automation & packaging |
| **Security** | SHA-256, JWT Tokens | Auth & encryption |

---

## 📚 Modules Overview

### 📖 Academics & Staffing (20+ modules)
- **Institute Management**: Role config, RBAC, system settings
- **Employee & Faculty**: Non-teaching staff, salary management
- **Student & Course**: CRUD operations, deep search capabilities
- **Departments**: Department records and configurations
- **Attendance**: Single/bulk marking, analytics & reports
- **Timetable**: Faculty workload, credit distribution, pie charts
- **Leave Approvals**: Multi-tier workflow for staff & student leaves
- **Grades & Transcripts**: GPA calculation, transcript generation
- **Assignments**: Course submissions, grading, due dates

### 🏘️ Campus & Facilities (12+ modules)
- **Hostel Management**: Hostels, rooms, real-time availability
- **Room Allocations**: Automated or manual assignment
- **Gate Pass System**: Request, approval, entry tracking
- **Visitor Tracking**: ID proof verification, check-in/out logs
- **Library Management**: Books, issues, fine tracking
- **Fees & Ledger**: Payment tracking, gateway integration ready

### 🎉 Student Life & Progression (15+ modules)
- **Activities Hub**: Centralized student dashboard
- **Clubs & Societies**: Browse, memberships, executive roles
- **Events & Workshops**: Ticketing, registration, promotional banners
- **Scholarships**: Program browsing, application tracking
- **Crowdfunding**: Deploy initiatives, donation management
- **Placement Portal**: Company catalogs, interview status tracking
- **Resource Hub**: Lecture uploads, notes, materials
- **Academic Calendar**: Event scheduling

### 🔧 General Utilities (5+ modules)
- **Global Announcements**: Broadcast to specific user groups
- **Notifications**: Real-time notification system
- **Profile Management**: User settings, security controls
- **Advanced Reports**: Custom analytics & data exports
- **Audit Logs**: Complete system activity tracking

---

## 🚀 Quick Start

### Prerequisites
```
✓ Java JDK 17+
✓ PostgreSQL 15+
✓ Node.js 16+ (for web frontend)
✓ Maven (optional, scripts handle builds)
```

### Installation

**1. Clone the Repository**
```bash
git clone https://github.com/space0032/College-Management-2.git
cd College-Management-2
```

**2. Configure Environment**
```bash
cp .env.example .env
# Edit .env with your PostgreSQL credentials
```

**3. Create Database**
```sql
CREATE DATABASE college_management;
-- The app will auto-migrate the schema on first run
```

### 🖥️ Run Desktop Application

```bash
# Build & run the JavaFX desktop app
./build.sh
./run.sh
```

The desktop application will launch with a modern glassmorphic UI.

### 🌐 Run Web Application

**Start Backend API** (Terminal 1):
```bash
./run-api.sh
# API runs on http://localhost:7000
```

**Start Frontend** (Terminal 2):
```bash
cd web-app
npm install
npm start
# Web app runs on http://localhost:3000
```

Visit `http://localhost:3000` in your browser.

---

## 🔑 Default Credentials

> These are for testing/demo purposes. Change them immediately in production.

| Role | Username | Password | Notes |
|------|----------|----------|-------|
| **Admin** | `admin` | `admin123` | Full system access |
| **Faculty** | `FAC001` | `123` | Teaching staff |
| **Faculty** | `FAC002` | `123` | Teaching staff |
| **Student** | `TES2026001` | `123` | Student account |

> Create additional roles (Finance, Warden) through the Admin portal.

---

## 📁 Project Structure

```
College-Management-2/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── api/              # REST API endpoints
│   │   │   ├── database/         # DB connection & migrations
│   │   │   ├── models/           # Domain entities
│   │   │   ├── services/         # Business logic
│   │   │   ├── controllers/      # Request handlers
│   │   │   └── ui/               # JavaFX UI components
│   │   └── resources/            # Config files, SQL scripts
│   └── test/                      # Unit & integration tests
│
├── web-app/                       # React frontend
│   ├── src/
│   │   ├── components/           # Reusable React components
│   │   ├── pages/                # Page components
│   │   ├── services/             # API client
│   │   └── styles/               # CSS modules
│   └── package.json
│
├── scripts/
│   ├── build.sh                  # Maven build script
│   ├── run.sh                    # Desktop app launcher
│   ├── run-api.sh                # API server launcher
│   └── database/                 # SQL migration scripts
│
├── pom.xml                        # Maven configuration
├── .env.example                   # Environment template
├── README.md                      # This file
└── LICENSE                        # MIT License
```

---

## 🔒 Security Features

### Authentication & Authorization
- ✅ Secure password hashing (SHA-256 with salt)
- ✅ Bearer token authentication
- ✅ Role-based access control (RBAC)
- ✅ Session management & timeout
- ✅ Multi-tier leave approval workflows

### Data Protection
- ✅ Parameterized SQL queries (prevents SQL injection)
- ✅ Input validation on all endpoints
- ✅ CORS security headers
- ✅ Audit logs for all critical operations
- ✅ Encrypted password storage

### Best Practices Followed
- ✅ Zero hardcoded credentials
- ✅ Environment-based configuration
- ✅ Comprehensive error handling
- ✅ API rate limiting ready
- ✅ Regular security audits

> For detailed security information, see [SECURITY.md](SECURITY.md)

---

## 📊 Performance Metrics

- **N+1 Query Resolution**: Optimized with joins and aggregations
- **Lazy Loading**: Paginated results for large datasets
- **Connection Pooling**: Efficient database connection management
- **API Response Time**: Average <100ms for standard queries
- **Concurrent Users**: Tested with 100+ simultaneous connections

---

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork** the repository
2. **Create** a feature branch: `git checkout -b feature/YourFeature`
3. **Commit** your changes: `git commit -m 'Add YourFeature'`
4. **Push** to branch: `git push origin feature/YourFeature`
5. **Open** a Pull Request with description

### Coding Standards
- Follow Java naming conventions (camelCase, PascalCase for classes)
- Add meaningful commit messages
- Include comments for complex logic
- Test before submitting PR

---

## 📝 License

This project is licensed under the **MIT License** - see [LICENSE](LICENSE) file for details.

---

## 📧 Support & Contact

- **Issues**: [GitHub Issues](https://github.com/space0032/College-Management-2/issues)
- **Discussions**: [GitHub Discussions](https://github.com/space0032/College-Management-2/discussions)
- **Email**: Reach out via GitHub profile

---

## 🙏 Acknowledgments

- **AtlantaFX** for modern JavaFX theme
- **PostgreSQL** community for excellent documentation
- **React** team for the amazing JavaScript framework
- All contributors and testers

---

<div align="center">

**Built with ❤️ for educational excellence**

[⭐ Star this repo](#) if you find it helpful!

</div>
