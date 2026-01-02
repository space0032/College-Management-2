-- Quick Migration Fix
-- Run this as: sudo mysql < fix_timetable.sql

USE college_management;

-- Add columns to students table
ALTER TABLE students ADD COLUMN IF NOT EXISTS department VARCHAR(100) DEFAULT 'General';
ALTER TABLE students ADD COLUMN IF NOT EXISTS semester INT DEFAULT 1;

-- Set defaults for existing students  
UPDATE students SET department = 'General' WHERE department IS NULL OR department = '';
UPDATE students SET semester = 1 WHERE semester IS NULL OR semester = 0;

-- Create timetable table
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

-- Show confirmation
SELECT 'SUCCESS: Database updated!' AS Result;
SELECT COUNT(*) AS student_count FROM students;
