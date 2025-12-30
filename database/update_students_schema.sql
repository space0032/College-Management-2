-- Update Students Table
-- Add department and semester fields

ALTER TABLE students 
ADD COLUMN IF NOT EXISTS department VARCHAR(100) DEFAULT 'General',
ADD COLUMN IF NOT EXISTS semester INT DEFAULT 1;

-- Update existing students to have default values
UPDATE students SET department = 'General' WHERE department IS NULL;
UPDATE students SET semester = 1 WHERE semester IS NULL;
