-- ULTRA-SIMPLE SEED - Only Essential Tables
-- Run: sudo mysql college_management < seed_minimal.sql

USE college_management;

-- Role IDs
SET @faculty_role = (SELECT id FROM roles WHERE code = 'FACULTY' LIMIT 1);
SET @student_role = (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1);
SET @warden_role = (SELECT id FROM roles WHERE code = 'WARDEN' LIMIT 1);
SET @finance_role = (SELECT id FROM roles WHERE code = 'FINANCE' LIMIT 1);

-- Departments
INSERT IGNORE INTO departments (code, name, head_of_department, description) VALUES
('CSE', 'Computer Science & Engineering', 'Dr. Sarah Johnson', ''),
('ECE', 'Electronics & Communication', 'Dr. Michael Chen', ''),
('ME', 'Mechanical Engineering', 'Dr. Robert Martinez', '');

-- Users
INSERT IGNORE INTO users (username, password, role_id) VALUES
('sarah.johnson', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('michael.chen', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('CS21001', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CS21002', 'e10adc3949ba59abbe56e057f20f883e', @student_role);

-- Get IDs
SET @sarah = (SELECT id FROM users WHERE username = 'sarah.johnson');
SET @michael = (SELECT id FROM users WHERE username = 'michael.chen');
SET @s1 = (SELECT id FROM users WHERE username = 'CS21001');
SET @s2 = (SELECT id FROM users WHERE username = 'CS21002');

-- Faculty
INSERT IGNORE INTO faculty (user_id, name, email, phone, department, qualification, join_date) VALUES
(@sarah, 'Dr. Sarah Johnson', 'sarah.johnson@college.edu', '+91-9876543210', 'CSE', 'Ph.D.', '2010-01-01'),
(@michael, 'Dr. Michael Chen', 'michael.chen@college.edu', '+91-9876543211', 'ECE', 'Ph.D.', '2011-01-01');

-- Students
INSERT IGNORE INTO students (user_id, roll_number, first_name, last_name, email, department, semester, cgpa, status) VALUES
(@s1, 'CS21001', 'Rahul', 'Sharma', 'rahul@student.edu', 'CSE', 6, 8.5, 'ACTIVE'),
(@s2, 'CS21002', 'Priya', 'Patel', 'priya@student.edu', 'CSE', 6, 9.1, 'ACTIVE');

SELECT 'Data loaded!' AS Status;
