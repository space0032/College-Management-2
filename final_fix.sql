-- Final Fix for Timetable Table
-- Run as: sudo mysql college_management < final_fix.sql

USE college_management;

-- Drop and recreate timetable table to ensure it's correct
DROP TABLE IF EXISTS timetable;

CREATE TABLE timetable (
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

-- Verify students table has department and semester
ALTER TABLE students ADD COLUMN IF NOT EXISTS department VARCHAR(100) DEFAULT 'General';
ALTER TABLE students ADD COLUMN IF NOT EXISTS semester INT DEFAULT 1;

UPDATE students SET department = 'General' WHERE department IS NULL OR department = '';
UPDATE students SET semester = 1 WHERE semester IS NULL OR semester = 0;

-- Show confirmation
SELECT '✓ Timetable table created successfully!' AS Status;
DESCRIBE timetable;
SELECT '✓ Students table updated!' AS Status;
SHOW COLUMNS FROM students LIKE 'department';
SHOW COLUMNS FROM students LIKE 'semester';
