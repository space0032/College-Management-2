CREATE TABLE IF NOT EXISTS fee_transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL UNIQUE,
    student_id INT NOT NULL,
    fee_payment_id INT, -- Link to specific payment if applicable
    amount DECIMAL(10,2) NOT NULL,
    type ENUM('PAYMENT', 'REFUND', 'WAIVER', 'FINE') NOT NULL,
    description VARCHAR(255),
    payment_mode ENUM('CASH', 'ONLINE', 'CHEQUE', 'TRANSFER') DEFAULT 'CASH',
    transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by INT, -- User ID of the staff who recorded it
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (fee_payment_id) REFERENCES fees(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE INDEX idx_trans_student ON fee_transactions(student_id);
CREATE INDEX idx_trans_date ON fee_transactions(transaction_date);
