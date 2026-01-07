-- Migration script to update grades table schema
-- Adds missing columns for enhanced grade management

USE college_management;

-- Add missing columns if they don't exist
ALTER TABLE grades 
ADD COLUMN IF NOT EXISTS marks_obtained DECIMAL(5,2) AFTER exam_type,
ADD COLUMN IF NOT EXISTS max_marks DECIMAL(5,2) AFTER marks_obtained,
ADD COLUMN IF NOT EXISTS grade_letter VARCHAR(5) AFTER max_marks,
ADD COLUMN IF NOT EXISTS percentage DECIMAL(5,2) AFTER grade_letter,
ADD COLUMN IF NOT EXISTS remarks VARCHAR(255) AFTER percentage,
ADD COLUMN IF NOT EXISTS exam_date DATE AFTER remarks,
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP AFTER exam_date,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

-- Update exam_type to ENUM if it's not already
ALTER TABLE grades 
MODIFY COLUMN exam_type ENUM('MID_TERM', 'END_TERM', 'ASSIGNMENT', 'QUIZ', 'PROJECT') NOT NULL;

-- Copy data from old columns to new if they exist
UPDATE grades SET marks_obtained = marks WHERE marks_obtained IS NULL AND marks IS NOT NULL;
UPDATE grades SET grade_letter = grade WHERE grade_letter IS NULL AND grade IS NOT NULL;
