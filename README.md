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
- **Data Seeding**: Automated seeding of realistic demo data (100 Students, 30 Faculty) via `DataSeeder` utility.

### 👥 Role-Based Access Control
- **Admin**: Complete system oversight, user management, and system logs.
- **Faculty**: Course management, attendance marking, grades, and payroll access.
- **Student**: View personalized timetable, attendance, grades, and fees.
- **Warden**: Hostel room allocation and gate pass management.
- **Finance**: Manage fee collections, view transaction reports, and handle payroll approvals.
- **HR**: **(New)** Manage employee records, contracts, and payroll generation.

### 📚 Core Modules
- **Institute**: Manage Students, Faculty, Courses, Departments.
- **Academic**: Attendance, Grades, Timetables, Assignments.
- **Hostel**: Room allocation, Warden management, Gate Passes.
- **Human Resources**: Employee profiles, Salary management, Leave tracking.
- **Payroll**: Automated payroll generation, Bonus/Deduction tracking, Payslip management.
- **Reports**: Visual analytics for Attendance, Fees, and Grades (with CSV export).
- **Permissions**: Granular, categorized permission system (System, Academic, Hostel, HR, Finance).

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

2. **Configure Environment**
   Create a `.env` file from the template:
   ```bash
   cp .env.example .env
   # Edit .env with your database credentials
   ```
   
   **Important**: Never commit `.env` to version control!

3. **Database Setup**
   Simply create an empty database. The app handles the tables!
   ```sql
   CREATE DATABASE college_db;
   CREATE USER 'college_user'@'localhost' IDENTIFIED BY 'your_secure_password';
   GRANT ALL PRIVILEGES ON college_db.* TO 'college_user'@'localhost';
   FLUSH PRIVILEGES;
   ```
   *(The MigrationRunner handles table creation on first launch).*

4. **Build the Project**
   This script compiles the code and resources.
   ```bash
   ./build.sh
   ```

5. **Run the Application**
   ```bash
   ./run.sh
   ```

---

## 🔒 Security

This project follows security best practices:
- Password hashing with SHA-256 (salted hashing available)
- SQL injection protection via PreparedStatements
- Environment-based configuration
- Secure credential management

**See [SECURITY.md](SECURITY.md) for detailed security information and best practices.**

---

## 🔑 Login Credentials

All seeded test users have password: **`password123`** (Default)

### Admin
- **User**: `admin`
- **Pass**: `admin123`

### Faculty
- **Users**: `f101`, `f102`...

### Student
- **Users**: `s2024001`, `s2024002`...

### Staff / Warden
- **Users**: `w501`, `w502`...

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