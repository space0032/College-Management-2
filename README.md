# 🎓 College Management System

A comprehensive JavaFX-based college management system with role-based access control, featuring auto-enrollment, bulk operations, and advanced analytics.

![Java](https://img.shields.io/badge/Java-17+-orange?style=flat-square)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=flat-square)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square)
![Status](https://img.shields.io/badge/Status-Production%20Ready-brightgreen?style=flat-square)

---

## ✨ Key Features

### 📚 Auto-Enrollment System
- **Students**: Automatic enrollment number generation (e.g., `CS2026001`, `EE2026002`)
- **Faculty**: Automatic faculty ID generation (e.g., `FAC001`, `FAC002`)
- **Credentials**: Auto-generated usernames with customizable passwords
- **Consistency**: Unified enrollment system across the platform

### 👥 Role-Based Access Control
- **Admin Portal**: Complete system management and oversight
- **Faculty Portal**: Course management, attendance marking, grade entry
- **Student Portal**: View courses, attendance, grades, assignments
- **Warden Portal**: Hostel management and gate pass approvals

### 🏫 Institute Management
- **Student Management**: CRUD operations with enrollment tracking
- **Faculty Management**: Profile management with department assignment
- **Course Management**: Course creation and faculty assignment
- **Department Management**: Organizational structure

### 📊 Academic Features
- **Bulk Attendance**: Mark attendance for entire classes at once
- **Bulk Grades**: Enter grades for multiple students efficiently
- **Assignments**: Create, submit, and grade assignments
- **Timetable**: Weekly schedule management
- **Reports**: Comprehensive analytics with CSV export

### 🏠 Hostel Management
- **Room Allocation**: Assign students to hostel rooms
- **Warden Management**: Complete warden profile and assignment system
- **Gate Pass**: Student gate pass request and approval workflow
- **Occupancy Tracking**: Monitor room availability

### 💰 Fee Management
- **Fee Assignment**: Multiple fee categories (Tuition, Hostel, Library, etc.)
- **Payment Tracking**: Record and monitor payments
- **Payment History**: Complete transaction trail
- **Status Tracking**: PENDING → PARTIAL → PAID

### 📈 Reports & Analytics
- **Attendance Reports**: Course-wise and student-wise analytics
- **Grade Reports**: Performance tracking and distribution
- **Fee Reports**: Payment status and collection summaries
- **CSV Export**: Export all reports for external analysis

---

## 🚀 Quick Start

### Prerequisites
- Java Development Kit (JDK) 17 or higher
- MySQL 8.0 or higher
- Linux/Windows/macOS

### Installation

1. **Clone the repository**
```bash
git clone <repository-url>
cd College-Management-2
```

2. **Set up MySQL database**
```bash
# Start MySQL
sudo service mysql start

# Create database
mysql -u root -p

# In MySQL shell:
CREATE DATABASE college_db;
USE college_db;
SOURCE database/schema.sql;
SOURCE database/reset_test_data.sql;
EXIT;
```

3. **Compile the application**
```bash
./compile-fx.sh
```

4. **Run the application**
```bash
./run-fx.sh
```

---

## 🔑 Login Credentials

All test users have password: **`123`**

### Admin Access
- **Username**: `admin`
- **Access**: Full system control

### Faculty Access
- **Username**: `FAC001`, `FAC002`, `FAC003`, `FAC004`, `FAC005`
- **Access**: Course management, attendance, grades

### Student Access
- **Username**: `CS2023001`, `CS2023002`, `CS2023003`, `CS2023004`, `CS2023005`
- **Access**: View courses, attendance, grades, assignments

### Warden Access
- **Username**: `WARDEN01`, `WARDEN02`
- **Access**: Hostel management, gate passes

---

## 📁 Project Structure

```
College-Management-2/
├── src/com/college/
│   ├── dao/                    # Data Access Objects
│   │   ├── StudentDAO.java
│   │   ├── FacultyDAO.java
│   │   ├── AttendanceDAO.java
│   │   └── ...
│   ├── models/                 # Entity Models
│   │   ├── Student.java
│   │   ├── Faculty.java
│   │   ├── Course.java
│   │   └── ...
│   ├── fx/views/              # JavaFX UI Views
│   │   ├── DashboardView.java
│   │   ├── StudentManagementView.java
│   │   ├── AttendanceView.java
│   │   ├── GradesView.java
│   │   └── ...
│   └── utils/                 # Utility Classes
│       ├── SessionManager.java
│       ├── EnrollmentGenerator.java
│       └── DatabaseConnection.java
├── database/
│   ├── schema.sql             # Database schema
│   └── reset_test_data.sql    # Test data
├── lib/                       # External libraries
├── bin/                       # Compiled classes
├── compile-fx.sh              # Compilation script
├── run-fx.sh                  # Run script
└── README.md
```

---

## 💻 Technology Stack

- **Frontend**: JavaFX 21
- **Backend**: Java 17 (OOP, MVC Architecture)
- **Database**: MySQL 8.0
- **Charts**: JFreeChart (for analytics)
- **Build**: Shell scripts for compilation

---

## 🎯 Usage Guide

### For Administrators

1. **Manage Students**:
   - Add new students with auto-generated enrollment numbers
   - View enrollment numbers in bold (first column)
   - Edit student information
   - Delete with confirmation dialogs

2. **Manage Faculty**:
   - Add faculty with auto-generated IDs (FAC###)
   - View faculty IDs in bold (first column)
   - Assign roles and departments
   - Delete with confirmation

3. **Institute Management**:
   - Access all modules from Institute Management menu
   - No duplicate attendance/grades in sidebar
   - Streamlined navigation

### For Faculty

1. **Mark Attendance**: Bulk attendance marking for entire classes
2. **Enter Grades**: Bulk grade entry with validation
3. **Manage Courses**: Create and assign courses
4. **View Reports**: Access analytics and export data

### For Students

1. **View Enrollment**: See your enrollment number prominently
2. **Track Attendance**: Monitor attendance percentage
3. **Check Grades**: View marks and performance
4. **Submit Assignments**: Complete and submit work

---

## 🔧 Configuration

### Database Connection
Edit `src/com/college/utils/DatabaseConnection.java`:

```java
private static final String URL = "jdbc:mysql://localhost:3306/college_db";
private static final String USER = "root";
private static final String PASSWORD = "your_password";
```

---

## 📊 Key Improvements

### Latest Updates ✨

1. **Auto-Enrollment System**
   - Students: CS2026001, CS2026002...
   - Faculty: FAC001, FAC002...
   - Consistent username generation

2. **UI Enhancements**
   - Enrollment Number: Bold, first column in student table
   - Faculty ID: Bold, first column in faculty table
   - Clean admin navigation (no duplicate menus)

3. **Bulk Operations**
   - Bulk attendance marking for classes
   - Bulk grade entry for courses
   - Searchable student selection

4. **Code Quality**
   - Removed unused imports
   - Proper error handling
   - Delete confirmations

---

## 🐛 Troubleshooting

### Database Connection Issues
```bash
# Check MySQL is running
sudo service mysql status

# Verify credentials
mysql -u root -p college_db
```

### Compilation Errors
```bash
# Clean and recompile
rm -rf bin/*
./compile-fx.sh
```

### JavaFX Runtime Issues
Ensure `lib/javafx-sdk-21.0.2/` exists with all required libraries.

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📝 License

This project is available for educational and commercial use.

---

## 👨‍💻 Support

For issues or questions:
- Review the code documentation
- Check the troubleshooting section
- Refer to inline comments

---

## ⭐ Features Checklist

- ✅ Auto-enrollment for students and faculty
- ✅ Role-based dashboards
- ✅ Bulk attendance marking
- ✅ Bulk grade entry
- ✅ Searchable student selection
- ✅ Hostel management with wardens
- ✅ Gate pass system
- ✅ Fee management
- ✅ Reports with CSV export
- ✅ Delete confirmations
- ✅ Clean, intuitive UI

---

**Built with ❤️ for educational institutions**

🌟 **Star this repository if you find it helpful!**