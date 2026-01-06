-- V5 Restore Employees Table and Missing Columns in Courses/Fees

-- 1. Restore Employees Table
CREATE TABLE IF NOT EXISTS employees (
    id SERIAL PRIMARY KEY,
    employee_id VARCHAR(50) UNIQUE NOT NULL, -- Often maps to username
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    designation VARCHAR(100),
    join_date DATE,
    salary DECIMAL(10,2),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Add columns to Courses for DAO compatibility
ALTER TABLE courses ADD COLUMN IF NOT EXISTS department VARCHAR(100); -- Legacy string column
ALTER TABLE courses ADD COLUMN IF NOT EXISTS course_type VARCHAR(50) DEFAULT 'CORE';

-- 3. Add column to Fee Categories
ALTER TABLE fee_categories ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;
