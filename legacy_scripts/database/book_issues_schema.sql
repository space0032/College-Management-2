-- ===========================================
-- LIBRARY ISSUE/RETURN TRACKING
-- ===========================================

CREATE TABLE IF NOT EXISTS book_issues (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    book_id INT NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE NULL,
    fine_amount DECIMAL(10,2) DEFAULT 0.00,
    status ENUM('ISSUED', 'RETURNED', 'OVERDUE') DEFAULT 'ISSUED',
    issued_by INT,
    returned_to INT,
    remarks VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (issued_by) REFERENCES users(id),
    FOREIGN KEY (returned_to) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Add indexes for performance
CREATE INDEX idx_book_issues_student ON book_issues(student_id);
CREATE INDEX idx_book_issues_book ON book_issues(book_id);
CREATE INDEX idx_book_issues_status ON book_issues(status);
CREATE INDEX idx_book_issues_due_date ON book_issues(due_date);

-- Add available_copies column to books table (if not exists)
ALTER TABLE books ADD COLUMN IF NOT EXISTS available_copies INT DEFAULT 0;
ALTER TABLE books ADD COLUMN IF NOT EXISTS total_copies INT DEFAULT 1;

-- Update existing books to have copy counts
UPDATE books SET total_copies = 1, available_copies = 1 WHERE total_copies IS NULL OR total_copies = 0;
