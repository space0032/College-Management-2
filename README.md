# 🎓 College Management System

A comprehensive, modernized JavaFX-based college management system with role-based access control, automated database migrations, and a beautiful UI powered by **AtlantaFX**.

![Java](https://img.shields.io/badge/Java-17+-orange?style=flat-square)
![JavaFX](https://img.shields.io/badge/JavaFX-17+-blue?style=flat-square)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square)
![Build](https://img.shields.io/badge/Build-Maven%20%7C%20Bash-green?style=flat-square)
![Status](https://img.shields.io/badge/Status-Production%20Ready-brightgreen?style=flat-square)

---

## ✨ Key Features

### 🎨 Modern UI & Experience
- **AtlantaFX Theme**: tailored, modern look and feel.
- **Responsive Navigation**: Role-specific dashboards and sidebars.

### 🛠️ Technical Enhancements
- **Native Migrations**: Custom `MigrationRunner` automatically updates the database schema on startup. No manual SQL scripts needed.
- **Transactional Integrity**: `EnrollmentDAO` ensures data consistency across users and students.
- **CI/CD**: Automated build and testing pipeline via GitHub Actions.
- **Comprehensive Testing**: JUnit 5 & Mockito test suite ensuring core logic stability.

### 👥 Role-Based Access Control
- **Admin**: Complete system oversight, user management, and system logs.
- **Faculty**: Course management, attendance marking, grading.
- **Student**: View personalized timetable, attendance, grades, and fees.
- **Warden**: Hostel room allocation and gate pass management.
- **Finance**: **(New)** Manage fee collections, view transaction reports, and handle receipts.

### 📚 Core Modules
- **Institute**: Manage Students, Faculty, Courses, Departments.
- **Academic**: Attendance, Grades, Timetables, Assignments.
- **Hostel**: Room allocation, Warden management, Gate Passes.
- **Reports**: Visual analytics for Attendance, Fees, and Grades (with CSV export).

---

## 🚀 Quick Start

### Prerequisites
- **Java JDK 17+**
- **MySQL 8.0+**
- **Maven** (Optional, for dependency updates)
- **Git**

### Installation & Running

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd College-Management-2
   ```

2. **Database Setup**
   Simply create an empty database. The app handles the tables!
   ```sql
   CREATE DATABASE college_db;
   -- That's it! The MigrationRunner handles the rest on first launch.
   ```
   *(Ensure your `src/main/java/com/college/utils/DatabaseConnection.java` or environment variables point to this DB).*

3. **Build the Project**
   This script compiles the code and resources.
   ```bash
   ./build.sh
   ```

4. **Run the Application**
   ```bash
   ./run.sh
   ```

5. **Run Tests** (Optional)
   ```bash
   ./test.sh
   ```

---

## 🔑 Login Credentials

All test users have password: **`123`**

### Admin
- **User**: `admin`

### Faculty
- **Users**: `FAC001`, `FAC002`...

### Student
- **Users**: `CS2026001`, `CS2026002`...

### Finance **(New)**
- **User**: `finance_admin`
- **Access**: Fee collection, Dashboard statistics.

### Warden
- **Users**: `WARDEN01`...

---

## 📁 Project Structure

```
College-Management-2/
├── src/main/java/com/college/
│   ├── api/                    # Native REST API Handlers
│   ├── dao/                    # Data Access Objects (SQL)
│   ├── fx/views/               # JavaFX UI Controllers
│   ├── models/                 # POJOs
│   ├── utils/                  # Helpers (MigrationRunner, Session, etc.)
│   └── Launcher.java           # App Entry Point
├── src/main/resources/
│   └── db/migration/           # SQL Migration Scripts (V1__...)
├── src/test/java/              # JUnit Tests
├── .github/workflows/          # CI/CD Configuration
├── lib/                        # Managed Dependencies (Mockito, AtlantaFX, etc.)
├── build.sh                    # Main Build Script
├── run.sh                      # Main Run Script
├── test.sh                     # Test Runner Script
└── pom.xml                     # Maven Project Configuration
```

---

## 💻 Technology Stack

- **Language**: Java 17
- **UI Framework**: JavaFX + AtlantaFX (Theme)
- **Database**: MySQL 8.0 (JDBC)
- **Testing**: JUnit 5, Mockito
- **Build**: Custom Bash Scripts + Maven (for dependency resolution)
- **CI/CD**: GitHub Actions

---

## 🤝 Contributing

1. Fork the repo.
2. Create your feature branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes.
4. Push to the branch.
5. Open a Pull Request.

---

**Built with ❤️ for educational excellence.**