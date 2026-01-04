CREATE TABLE hostel_complaints (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50) DEFAULT 'General', -- Maintenance, Food, Discipline, etc.
    status VARCHAR(20) DEFAULT 'OPEN', -- OPEN, IN_PROGRESS, RESOLVED, REJECTED
    filed_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    resolved_date DATETIME,
    resolved_by INT, -- User ID of warden who resolved it
    remarks TEXT,
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (resolved_by) REFERENCES users(id)
);

-- Index for faster retrieval by status
CREATE INDEX idx_complaint_status ON hostel_complaints(status);
CREATE INDEX idx_complaint_student ON hostel_complaints(student_id);
