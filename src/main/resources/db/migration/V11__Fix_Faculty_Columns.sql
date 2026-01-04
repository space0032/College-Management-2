-- Fix faculty table - ADD missing columns (table created by V1)
-- V1 creates faculty with: id, user_id, name, email, phone, department, designation, joining_date
-- We need to ADD: qualification, join_date

ALTER TABLE faculty
ADD COLUMN IF NOT EXISTS qualification VARCHAR(100),
ADD COLUMN IF NOT EXISTS join_date DATE;

-- Migrate data from old columns to new columns if they exist
UPDATE faculty SET qualification = designation WHERE qualification IS NULL AND designation IS NOT NULL;
UPDATE faculty SET join_date = joining_date WHERE join_date IS NULL AND joining_date IS NOT NULL;

-- Fix students table - ensure is_hostelite column exists
ALTER TABLE students
ADD COLUMN IF NOT EXISTS is_hostelite BOOLEAN DEFAULT FALSE;

-- Fix employees table - ensure status column exists
ALTER TABLE employees
ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ACTIVE';
