# Timetable System - Database Setup Guide

## Running the Database Migrations

You need to run these SQL scripts to add department/semester support to the timetable system.

### Step 1: Update Students Table
Run this first to add department and semester columns:

```bash
mysql -u root -p college_management < database/update_students_schema.sql
```

### Step 2: Create Timetable Table
Run this to create the new timetable table:

```bash
mysql -u root -p college_management < database/timetable_schema.sql
```

### Alternative: Manual SQL Execution

If you prefer, you can run the SQL commands manually:

```sql
-- 1. Update students table
USE college_management;

ALTER TABLE students 
ADD COLUMN IF NOT EXISTS department VARCHAR(100) DEFAULT 'General',
ADD COLUMN IF NOT EXISTS semester INT DEFAULT 1;

UPDATE students SET department = 'General' WHERE department IS NULL;
UPDATE students SET semester = 1 WHERE semester IS NULL;

-- 2. Create timetable table
CREATE TABLE IF NOT EXISTS timetable (
    id INT PRIMARY KEY AUTO_INCREMENT,
    department VARCHAR(100) NOT NULL,
    semester INT NOT NULL,
    day_of_week ENUM('Monday','Tuesday','Wednesday','Thursday','Friday') NOT NULL,
    time_slot VARCHAR(20) NOT NULL,
    subject VARCHAR(100),
    faculty_name VARCHAR(100),
    room_number VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_slot (department, semester, day_of_week, time_slot)
);

CREATE INDEX idx_dept_sem ON timetable(department, semester);
CREATE INDEX idx_day ON timetable(day_of_week);
```

## After Running  Migrations

1. **Update Student Information**: Admin should update students' department and semester in Student Management
2. **Create Timetables**: Admin/Faculty can now create timetables for each department and semester
3. **Students Can View**: Students will automatically see timetables for their department and semester

## How It Works

### For Admin/Faculty:
- Select Department from dropdown (e.g., "Computer Science", "General")
- Select Semester (1-8)
- Click on cells to edit timetable
- Click "Save Changes" to persist

### For Students:
- Automatically shows their department and semester
- View-only mode (cannot edit)
- Refreshes automatically

## Sample Data (Optional)

To populate sample departments:

```sql
-- Set departments for existing students
UPDATE students SET department = 'Computer Science', semester = 3 WHERE id = 1;
UPDATE students SET department = 'Electrical Engineering', semester = 2 WHERE id = 2;
UPDATE students SET department = 'General', semester = 1 WHERE id = 3;
```

## Verification

After setup, verify:
1. Students table has `department` and `semester` columns
2. Timetable table exists
3. Students can log in and see "Department: X | Semester: Y" in timetable
4. Admin can select different departments/semesters

---

**Status**: Ready to use! Compile and run the application after running migrations.
