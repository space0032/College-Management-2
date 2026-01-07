-- Timetable Table Schema
-- Create table for department and semester-based timetables

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
CREATE INDEX idx_dept_sem ON timetable(department, semester);
CREATE INDEX idx_day ON timetable(day_of_week);
