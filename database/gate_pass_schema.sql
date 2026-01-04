-- Enhanced Gate Pass Table for Student Gate Pass Management
-- Phase 4: Gate Pass System

USE college_management;

-- Drop existing table if it exists
DROP TABLE IF EXISTS gate_passes;

-- Create enhanced gate pass table
CREATE TABLE IF NOT EXISTS gate_passes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    reason TEXT NOT NULL,
    destination VARCHAR(200),
    parent_contact VARCHAR(15),
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_by INT,
    approved_at TIMESTAMP NULL,
    approval_comment TEXT,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_student (student_id),
    INDEX idx_status (status),
    INDEX idx_requested_at (requested_at)
);
