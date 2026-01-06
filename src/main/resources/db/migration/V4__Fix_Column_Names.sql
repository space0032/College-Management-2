-- V4 Fix Faculty Column and Add Department Columns

-- 1. Rename joining_date to join_date (Faculty)
-- Using simple SQL assuming state is clean from V1
ALTER TABLE faculty RENAME COLUMN joining_date TO join_date;

-- 2. Add description, head_of_department, timestamps to departments
ALTER TABLE departments ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE departments ADD COLUMN IF NOT EXISTS head_of_department VARCHAR(100);
ALTER TABLE departments ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE departments ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
