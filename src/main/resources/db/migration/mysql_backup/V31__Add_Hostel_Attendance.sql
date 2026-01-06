-- Create hostel_attendance table
CREATE TABLE IF NOT EXISTS hostel_attendance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    hostel_id INT NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PRESENT', -- PRESENT, ABSENT, LEAVE, LATE
    remarks TEXT,
    marked_by INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (hostel_id) REFERENCES hostels(id) ON DELETE CASCADE,
    FOREIGN KEY (marked_by) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE KEY unique_hostel_attendance (student_id, date)
);

-- Index for performance
CREATE INDEX idx_hostel_attendance_date ON hostel_attendance(date);
CREATE INDEX idx_hostel_attendance_hostel ON hostel_attendance(hostel_id);
