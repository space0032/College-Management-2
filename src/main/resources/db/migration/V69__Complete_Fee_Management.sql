-- Complete, auditable fee-management workflow.

CREATE TABLE IF NOT EXISTS fee_transactions (
    id SERIAL PRIMARY KEY,
    transaction_id VARCHAR(80) NOT NULL UNIQUE,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    student_fee_id INTEGER REFERENCES student_fees(id) ON DELETE CASCADE,
    fee_payment_id INTEGER REFERENCES fee_payments(id) ON DELETE SET NULL,
    parent_transaction_id INTEGER REFERENCES fee_transactions(id) ON DELETE SET NULL,
    amount DECIMAL(12,2) NOT NULL,
    type VARCHAR(30) NOT NULL,
    description VARCHAR(500),
    payment_mode VARCHAR(30),
    created_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fee_transaction_amount_positive CHECK (amount > 0)
);

CREATE TABLE IF NOT EXISTS fee_payment_requests (
    id SERIAL PRIMARY KEY,
    student_fee_id INTEGER NOT NULL REFERENCES student_fees(id) ON DELETE CASCADE,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    amount DECIMAL(12,2) NOT NULL,
    payment_mode VARCHAR(30) NOT NULL,
    reference_number VARCHAR(120) NOT NULL,
    payment_date DATE NOT NULL,
    proof_path VARCHAR(500),
    status VARCHAR(20) DEFAULT 'PENDING',
    student_note VARCHAR(500),
    review_note VARCHAR(500),
    reviewed_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fee_request_amount_positive CHECK (amount > 0),
    CONSTRAINT fee_request_reference_unique UNIQUE (student_id, reference_number)
);

CREATE TABLE IF NOT EXISTS fee_assignment_batches (
    id SERIAL PRIMARY KEY,
    batch_reference VARCHAR(80) NOT NULL UNIQUE,
    source_type VARCHAR(20) NOT NULL,
    criteria_json TEXT,
    category_id INTEGER NOT NULL REFERENCES fee_categories(id),
    academic_year VARCHAR(20) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    due_date DATE,
    assigned_count INTEGER DEFAULT 0,
    skipped_count INTEGER DEFAULT 0,
    created_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS fee_reminders (
    id SERIAL PRIMARY KEY,
    student_fee_id INTEGER NOT NULL REFERENCES student_fees(id) ON DELETE CASCADE,
    student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    recipient_user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    reminder_type VARCHAR(30) NOT NULL,
    message VARCHAR(500) NOT NULL,
    due_date DATE,
    read_at TIMESTAMP,
    created_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_fee_tx_student ON fee_transactions(student_id);
CREATE INDEX IF NOT EXISTS idx_fee_tx_fee ON fee_transactions(student_fee_id);
CREATE INDEX IF NOT EXISTS idx_fee_request_student ON fee_payment_requests(student_id);
CREATE INDEX IF NOT EXISTS idx_fee_request_status ON fee_payment_requests(status);
CREATE INDEX IF NOT EXISTS idx_fee_reminder_user ON fee_reminders(recipient_user_id);
CREATE INDEX IF NOT EXISTS idx_student_fees_status_due ON student_fees(status, due_date);

INSERT INTO permissions (code, name, category) VALUES
('CREATE_FEES', 'Create Fee Charges', 'Finance'),
('ADJUST_FEES', 'Post Fee Adjustments', 'Finance'),
('REFUND_FEES', 'Post Fee Refunds', 'Finance'),
('BULK_ASSIGN_FEES', 'Bulk Assign Fees', 'Finance'),
('REVIEW_FEE_REQUESTS', 'Review Fee Payment Requests', 'Finance'),
('MANAGE_FEE_REMINDERS', 'Manage Fee Reminders', 'Finance')
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r
JOIN permissions p ON p.code IN (
    'CREATE_FEES', 'ADJUST_FEES', 'REFUND_FEES', 'BULK_ASSIGN_FEES',
    'REVIEW_FEE_REQUESTS', 'MANAGE_FEE_REMINDERS'
)
WHERE r.code IN ('ADMIN', 'FINANCE')
ON CONFLICT (role_id, permission_id) DO NOTHING;
