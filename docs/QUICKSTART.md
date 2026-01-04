# 🚀 College Management System - Quick Start Guide

## ✅ Your Application is NOW RUNNING!

The College Management System is successfully running with the login screen displayed.

## 🔑 Login Credentials

Use these credentials to login:

- **Username:** `admin`
- **Password:** `admin123`
- **Role:** Select `ADMIN` from dropdown

## 📊 Database Information

- **Database Name:** `college_management`
- **MySQL User:** `collegeapp`
- **MySQL Password:** `college123`
- **Tables Created:** 13 tables
- **Sample Data:** Included (3 courses, 5 hostel rooms, 3 books)

## 🎯 How to Use

### 1. **Login**
- The login window should be visible on your screen
- Enter username: `admin`
- Enter password: `admin123`
- Select role: `ADMIN`
- Click "Login"

### 2. **Navigate Modules**
After login, you'll see the dashboard with sidebar menu:

- **🏠 Home** - Dashboard with statistics
- **👨‍🎓 Student Management** - Add, edit, view, delete students
- **📚 Course Management** - Manage courses and subjects  
- **📖 Library Management** - Book catalog and issue/return
- **💰 Fee Management** - Track fees and payments

### 3. **Student Management**
- Click "Add Student" to create new student records
- Fill in: Name, Email, Phone, Course, Batch, Enrollment Date, Address
- Use "Search" to find students
- Select row and click "Edit" to modify
- Select row and click "Delete" to remove

### 4. **Other Modules**
- **Courses:** Add new courses with code, name, credits, department, semester
- **Library:** Add books to catalog, view availability
- **Fees:** View fee status and payment tracking

## 🔄 How to Run Again Later

Anytime you want to run the application:

```bash
cd /home/space/VSC/CollegeManagement2/College-Management-2
./run.sh
```

Or manually:
```bash
java -cp "bin:lib/*" com.college.Main
```

## ⚙️ Configuration Files

### Database Connection
**File:** `src/com/college/utils/DatabaseConnection.java`
```java
private static final String USERNAME = "collegeapp";
private static final String PASSWORD = "college123";
```

### Database Schema
**File:** `database/setup_user.sql` - Contains all table definitions

## 🛠️ Recompile After Changes

If you modify any Java files:
```bash
./compile.sh
./run.sh
```

## 📝 Application Features

✅ **Authentication**
- Secure SHA-256 password hashing
- Role-based access (Admin/Faculty/Student)

✅ **Student Management**
- Full CRUD operations
- Email/phone validation
- Search functionality

✅ **Course Management**
- Course catalog
- Credits and semester tracking

✅ **Library Management**
- Book inventory
- Availability tracking

✅ **Fee Management**
- Payment tracking
- Status monitoring

✅ **Modern UI**
- Nimbus look and feel
- Color-coded buttons
- Responsive tables

## 🎨 UI Color Scheme

- **Primary (Blue):** Main actions, navigation
- **Success (Green):** Add, save, refresh
- **Danger (Red):** Delete, cancel
- **Warning (Orange):** Pending items

## 📚 Sample Data Included

### Default Users
- Admin user: `admin` / `admin123`

### Sample Courses
1. Data Structures (CS101)
2. Database Management (CS102)
3. Web Development (CS103)

### Sample Books
1. Introduction to Algorithms
2. Clean Code  
3. Design Patterns

### Hostel Rooms
- 5 rooms (A101, A102, A103, B201, B202)

## ❓ Troubleshooting

### If Application Doesn't Start
```bash
# Check MySQL is running
systemctl status mysql

# Test database connection
mysql -u collegeapp -pcollege123 -e "USE college_management; SHOW TABLES;"

# Recompile
./compile.sh
```

### If Login Fails
- Ensure you're using exact credentials: `admin` / `admin123`
- Select `ADMIN` role from dropdown
- Check database has admin user

### Reset Database
```bash
sudo mysql < database/setup_user.sql
```

## 🎉 You're All Set!

The application is running and ready to use. Start by logging in with the admin credentials and exploring the different modules!

**Next Steps:**
1. Login with admin credentials
2. Add some sample students
3. Explore the course catalog
4. Check out the library module
5. Customize as needed!

---

**Need Help?** Check the full documentation in `README.md`
