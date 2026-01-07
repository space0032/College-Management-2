-- Department Management Schema - Fixed Version
-- Handles existing constraints gracefully

USE college_management;

-- Create departments table if not exists
CREATE TABLE IF NOT EXISTS departments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(20) NOT NULL UNIQUE,
    description TEXT,
    head_of_department VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Drop existing constraint if it exists (to avoid duplicate error)
SET @constraint_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS 
    WHERE CONSTRAINT_SCHEMA = 'college_management' 
    AND TABLE_NAME = 'courses' 
    AND CONSTRAINT_NAME = 'fk_course_department');

SET @drop_sql = IF(@constraint_exists > 0, 
    'ALTER TABLE courses DROP FOREIGN KEY fk_course_department', 
    'SELECT "No constraint to drop"');

PREPARE stmt FROM @drop_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add department_id column if not exists
ALTER TABLE courses ADD COLUMN IF NOT EXISTS department_id INT;

-- Add foreign key constraint
ALTER TABLE courses 
ADD CONSTRAINT fk_course_department 
    FOREIGN KEY (department_id) REFERENCES departments(id) 
    ON DELETE SET NULL;

-- Create index for faster queries
CREATE INDEX IF NOT EXISTS idx_course_department ON courses(department_id);

-- Insert sample departments (IGNORE to avoid duplicates)
INSERT IGNORE INTO departments (name, code, description) VALUES
('Computer Science', 'CS', 'Computer Science and Engineering Department'),
('Electrical Engineering', 'EE', 'Electrical and Electronics Engineering'),
('Mechanical Engineering', 'ME', 'Mechanical Engineering Department'),
('Civil Engineering', 'CE', 'Civil Engineering Department'),
('General Studies', 'GEN', 'General and Foundation Courses');

SELECT 'Department schema created successfully!' AS Status;
SELECT CONCAT('Total Departments: ', COUNT(*)) AS Result FROM departments;
