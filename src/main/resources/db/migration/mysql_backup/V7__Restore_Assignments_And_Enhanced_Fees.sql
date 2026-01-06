-- Restore Assignments
CREATE TABLE IF NOT EXISTS assignments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    due_date DATETIME,
    created_by INT,
    semester INT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Restore Enhanced Fee System Tables
CREATE TABLE IF NOT EXISTS fee_categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    base_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS student_fees (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    category_id INT NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    paid_amount DECIMAL(10,2) DEFAULT 0.00,
    due_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, PARTIAL, PAID
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES fee_categories(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS fee_payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_fee_id INT NOT NULL,
    payment_date DATE NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_mode VARCHAR(20) DEFAULT 'CASH',
    transaction_id VARCHAR(100),
    receipt_number VARCHAR(50) UNIQUE,
    received_by INT,
    remarks TEXT,
    FOREIGN KEY (student_fee_id) REFERENCES student_fees(id) ON DELETE CASCADE,
    FOREIGN KEY (received_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Insert some default fee categories if empty
INSERT IGNORE INTO fee_categories (category_name, base_amount, description) VALUES 
('Tuition Fee', 50000.00, 'Semester Tuition Fee'),
('Hostel Fee', 25000.00, 'Semester Hostel Fee'),
('Library Fee', 2000.00, 'Annual Library Fee'),
('Exam Fee', 1500.00, 'Semester Examination Fee');
