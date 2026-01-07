-- Combined Migration Script for Timetable Enhancement
-- Run this script to add department/semester support
-- Usage: mysql -u root -p college_management < database/apply_timetable_migrations.sql

USE college_management;

-- Step 1: Add department and semester columns to students table
ALTER TABLE students 
ADD COLUMN IF NOT EXISTS department VARCHAR(100) DEFAULT 'General',
ADD COLUMN IF NOT EXISTS semester INT DEFAULT 1;

-- Update existing students to have default values
UPDATE students SET department = 'General' WHERE department IS NULL OR department = '';
UPDATE students SET semester = 1 WHERE semester IS NULL OR semester = 0;

-- Step 2: Create timetable table
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

-- Create indexes for faster queries
CREATE INDEX IF NOT EXISTS idx_dept_sem ON timetable(department, semester);
CREATE INDEX IF NOT EXISTS idx_day ON timetable(day_of_week);

-- Verify changes
SELECT 'Migration completed successfully!' AS Status;
SELECT COUNT(*) as student_count FROM students;
DESCRIBE students;
DESCRIBE timetable;
