# College Management System - Login Credentials

## Application Login (For Using the System)

### Admin Login
- **Username:** `admin`
- **Password:** `admin123`
- **Role:** Select `ADMIN` from dropdown

### How to Login
1. Run the application: `./run.sh`
2. When the login window appears:
   - Enter username: `admin`
   - Enter password: `admin123`
   - Select role: `ADMIN`
   - Click "Login"

---

## Database Credentials (For MySQL Access)

### MySQL Application User
- **Database Name:** `college_management`
- **Username:** `collegeapp`
- **Password:** `college123`

### To Access MySQL Directly
```bash
mysql -u collegeapp -pcollege123 college_management
```

### MySQL Root Access (if needed)
```bash
sudo mysql
```

---

## Quick Reference

| System | Username | Password |
|--------|----------|----------|
| **Application** | admin | admin123 |
| **MySQL DB** | collegeapp | college123 |
| **MySQL Root** | root | (use sudo) |

---

## Running the Application

```bash
cd /home/space/VSC/CollegeManagement2/College-Management-2
./run.sh
```

Then login with: **admin** / **admin123** (role: ADMIN)
