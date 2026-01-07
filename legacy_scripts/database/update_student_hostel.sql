USE college_management;

-- Add is_hostelite column to students table
ALTER TABLE students ADD COLUMN is_hostelite BOOLEAN DEFAULT FALSE;
