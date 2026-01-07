-- Enhanced Fee Management Schema

-- Fee Categories Table
CREATE TABLE IF NOT EXISTS fee_categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    base_amount DECIMAL(10,2) NOT NULL,
    description VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Student Fees Table
CREATE TABLE IF NOT EXISTS student_fees (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    category_id INT NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    paid_amount DECIMAL(10,2) DEFAULT 0.00,
    status ENUM('PENDING', 'PARTIAL', 'PAID') DEFAULT 'PENDING',
    due_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES fee_categories(id),
    INDEX idx_student (student_id),
    INDEX idx_status (status),
    INDEX idx_academic_year (academic_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Fee Payments Table
CREATE TABLE IF NOT EXISTS fee_payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_fee_id INT NOT NULL,
    payment_date DATE NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_mode ENUM('CASH', 'ONLINE', 'CHEQUE', 'CARD') DEFAULT 'CASH',
    transaction_id VARCHAR(100),
    receipt_number VARCHAR(50) UNIQUE,
    received_by INT,
    remarks VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_fee_id) REFERENCES student_fees(id) ON DELETE CASCADE,
    INDEX idx_payment_date (payment_date),
    INDEX idx_receipt (receipt_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert default fee categories
INSERT INTO fee_categories (category_name, base_amount, description) VALUES
('Tuition Fee', 50000.00, 'Semester tuition fee'),
('Hostel Fee', 15000.00, 'Hostel accommodation fee per semester'),
('Library Fee', 2000.00, 'Library access and book deposit'),
('Sports Fee', 3000.00, 'Sports and recreation facilities'),
('Lab Fee', 5000.00, 'Laboratory usage and equipment'),
('Examination Fee', 1500.00, 'Examination and assessment fee')
ON DUPLICATE KEY UPDATE category_name=category_name;
