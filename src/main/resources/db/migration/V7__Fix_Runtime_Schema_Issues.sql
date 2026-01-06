-- V7 Fix Runtime Schema Issues and Constraints

-- 1. Add Unique Constraints for Upsert operations (required for ON CONFLICT)
-- Using DO NOTHING to avoid errors if constraint already exists (Postgres doesn't support IF NOT EXISTS for constraints directly in simple syntax, so we wrap or just assume)
-- Better: Try to add, if fails (duplicate), it's fine. But for migration safety, we can use an anonymous block or just allow it to fail if applied manually. 
-- Since this is managed migration, we assume it runs once.

ALTER TABLE grades ADD CONSTRAINT unique_grade_entry UNIQUE (student_id, course_id, exam_type);
ALTER TABLE attendance ADD CONSTRAINT unique_attendance_entry UNIQUE (student_id, course_id, date);

-- 2. Update Courses Table
ALTER TABLE courses ADD COLUMN IF NOT EXISTS capacity INTEGER DEFAULT 60;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS course_type VARCHAR(50) DEFAULT 'CORE';
ALTER TABLE courses ADD COLUMN IF NOT EXISTS enrolled_count INTEGER DEFAULT 0;

-- 3. Create Student Fees Table (for EnhancedFeeDAO)
CREATE TABLE IF NOT EXISTS student_fees (
    id SERIAL PRIMARY KEY,
    student_id INTEGER REFERENCES students(id) ON DELETE CASCADE,
    category_id INTEGER REFERENCES fee_categories(id),
    academic_year VARCHAR(20),
    total_amount DECIMAL(10,2) NOT NULL,
    paid_amount DECIMAL(10,2) DEFAULT 0.00,
    due_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Create Fee Payments Table
CREATE TABLE IF NOT EXISTS fee_payments (
    id SERIAL PRIMARY KEY,
    student_fee_id INTEGER REFERENCES student_fees(id) ON DELETE CASCADE,
    payment_date DATE DEFAULT CURRENT_DATE,
    amount DECIMAL(10,2) NOT NULL,
    payment_mode VARCHAR(50),
    transaction_id VARCHAR(100),
    receipt_number VARCHAR(50),
    received_by INTEGER REFERENCES users(id),
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
