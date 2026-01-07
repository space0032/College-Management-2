-- Create announcements table for system-wide announcements
CREATE TABLE IF NOT EXISTS announcements (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    target_audience ENUM('ALL', 'STUDENTS', 'FACULTY', 'STUDENTS_FACULTY') DEFAULT 'ALL',
    priority ENUM('LOW', 'NORMAL', 'HIGH', 'URGENT') DEFAULT 'NORMAL',
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_active (is_active),
    INDEX idx_target (target_audience),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample announcements
INSERT INTO announcements (title, content, target_audience, priority, created_by) VALUES
('Welcome to New Semester', 'Welcome all students to the new academic semester. Classes will begin from next Monday.', 'STUDENTS', 'HIGH', 1),
('Faculty Meeting', 'All faculty members are requested to attend the department meeting on Friday at 3 PM.', 'FACULTY', 'NORMAL', 1),
('Library Hours Extended', 'Library hours have been extended. Now open from 8 AM to 10 PM on weekdays.', 'ALL', 'NORMAL', 1);
