-- ===========================================
-- ATTENDANCE MANAGEMENT TABLES
-- ===========================================

-- Add attendance table
CREATE TABLE IF NOT EXISTS attendance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    date DATE NOT NULL,
    status ENUM('PRESENT', 'ABSENT', 'LATE') NOT NULL,
    remarks VARCHAR(255),
    marked_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (marked_by) REFERENCES users(id),
    UNIQUE KEY unique_attendance (student_id, course_id, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Add indexes for better performance
CREATE INDEX idx_attendance_student ON attendance(student_id);
CREATE INDEX idx_attendance_course ON attendance(course_id);
CREATE INDEX idx_attendance_date ON attendance(date);
CREATE INDEX idx_attendance_status ON attendance(status);

-- Sample attendance data (optional)
-- Uncomment to insert sample data after creating students and courses
/*
INSERT INTO attendance (student_id, course_id, date, status, marked_by) VALUES
(1, 1, '2024-01-15', 'PRESENT', 1),
(1, 1, '2024-01-16', 'PRESENT', 1),
(1, 1, '2024-01-17', 'ABSENT', 1),
(1, 2, '2024-01-15', 'PRESENT', 1),
(1, 2, '2024-01-16', 'LATE', 1);
*/
