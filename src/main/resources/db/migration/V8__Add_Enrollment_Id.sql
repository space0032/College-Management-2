-- V8: Add enrollment_id to students table for direct access
-- This avoids the need to join with users table just to get the enrollment ID

-- 1. Add Column
ALTER TABLE students ADD COLUMN IF NOT EXISTS enrollment_id VARCHAR(50);

-- 2. Backfill Data from Users table (username is the enrollment ID)
UPDATE students s
SET enrollment_id = u.username
FROM users u
WHERE s.user_id = u.id;

-- 3. Add Unique Constraint
ALTER TABLE students DROP CONSTRAINT IF EXISTS unique_student_enrollment_id;
ALTER TABLE students ADD CONSTRAINT unique_student_enrollment_id UNIQUE (enrollment_id);

-- 4. Add Index for fast searching
CREATE INDEX IF NOT EXISTS idx_student_enrollment_id ON students(enrollment_id);
