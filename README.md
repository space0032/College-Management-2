# College Management System

A comprehensive Java Swing-based application for managing all college operations including student management, courses, library, fees, and more.

## Features

- **👤 Authentication**: Secure login with role-based access (Admin, Faculty, Student)
- **👨‍🎓 Student Management**: Add, edit, delete, and search student records
- **📚 Course Management**: Manage courses, subjects, and semesters
- **📖 Library Management**: Book catalog management with issue/return tracking
- **💰 Fee Management**: Track fee payments and generate receipts
- **📊 Dashboard**: Role-based navigation with statistics

## Technologies Used

- **Language**: Java
- **GUI Framework**: Java Swing with Nimbus Look and Feel
- **Database**: MySQL
- **Architecture**: MVC Pattern with DAO Layer

## Project Structure

```
src/com/college/
├── Main.java                  # Application entry point
├── utils/                     # Utility classes
│   ├── DatabaseConnection.java
│   ├── ValidationUtils.java
│   └── UIHelper.java
├── models/                    # Data models
│   ├── Student.java
│   ├── Faculty.java
│   ├── Course.java
│   └── Book.java
├── dao/                       # Data Access Objects
│   ├── StudentDAO.java
│   ├── CourseDAO.java
│   └── LibraryDAO.java
└── ui/                        # UI Components
    ├── LoginFrame.java
    ├── DashboardFrame.java
    └── [module panels]
```

## Setup Instructions

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- MySQL Server
- MySQL Connector/J (JDBC Driver)

### Database Setup

1. Install and start MySQL server:
```bash
sudo service mysql start
```

2. Create the database:
```bash
mysql -u root -p < database/schema.sql
```

3. Update database credentials in `DatabaseConnection.java`:
```java
private static final String USERNAME = "root";
private static final String PASSWORD = "your_password";
```

### MySQL Connector Setup

Download MySQL Connector/J and place it in the `lib/` directory:
```bash
cd lib
wget https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-j-8.0.33.tar.gz
tar -xzf mysql-connector-j-8.0.33.tar.gz
mv mysql-connector-j-8.0.33/mysql-connector-j-8.0.33.jar .
```

## Running the Application

### Option 1: Using the compile script
```bash
chmod +x compile.sh
./compile.sh
java -cp "bin:lib/mysql-connector-j-8.0.33.jar" com.college.Main
```

### Option 2: Manual compilation
```bash
# Compile
javac -d bin -cp "lib/mysql-connector-j-8.0.33.jar" src/com/college/**/*.java src/com/college/*.java

# Run
java -cp "bin:lib/mysql-connector-j-8.0.33.jar" com.college.Main
```

## Default Login Credentials

- **Username**: admin
- **Password**: admin123
- **Role**: ADMIN

## Usage Guide

1. **Login**: Use the default credentials or create new users
2. **Dashboard**: Navigate through different modules using the sidebar
3. **Student Management**: 
   - Click "Add Student" to add new students
   - Select a row and click "Edit" to modify
   - Use the search bar to find students
4. **Course Management**: Add and view courses
5. **Library**: Manage book catalog
6. **Fees**: Track fee payments and status

## Key Features

### User-Friendly Interface
- Modern Nimbus look and feel
- Color-coded buttons and status indicators
- Responsive table views with sorting

### Data Validation
- Email validation
- Phone number validation (10 digits)
- Required field checks
- Date format validation

### Security
- SHA-256 password hashing
- Role-based access control
- Prepared statements to prevent SQL injection

## Modules Overview

### 1. Authentication Module
- Secure login with password hashing
- Role-based authentication

### 2. Student Management
- Complete CRUD operations
- Search functionality
- Student details with enrollment information

### 3. Course Management
- Course catalog
- Credits and semester tracking

### 4. Library Management
- Book inventory management
- Issue and return tracking

### 5. Fee Management
- Fee collection tracking
- Payment status monitoring

## Troubleshooting

### Database Connection Issues
- Ensure MySQL server is running: `sudo service mysql status`
- Verify credentials in `DatabaseConnection.java`
- Check if database exists: `SHOW DATABASES;` in MySQL

### Compilation Errors
- Verify JDK installation: `java -version`
- Ensure MySQL connector JAR is in lib/ directory
- Check classpath includes both bin and lib directories

### Runtime Errors
- Check database schema is created properly
- Verify all tables exist: `USE college_management; SHOW TABLES;`
- Ensure default admin user exists

## Future Enhancements

- Attendance tracking module
- Grade management system
- Hostel management
- Timetable generation
- Report generation (PDF export)
- Email notifications
- Student portal
- Mobile responsive design

## License

This project is created for educational purposes.

## Author

Built with ❤️ using Java Swing