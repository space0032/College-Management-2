-- Create test users with username abc and password abc for all roles
-- Password 'abc' hashed with SHA-256
-- Note: Since each user can only have one role, we create separate users for each role

USE college_management;

-- Password hash for 'abc' using SHA-256: ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad

-- 1. ABC Student user
INSERT INTO users (username, password, role) VALUES 
('abc-student', 'ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad', 'STUDENT')
ON DUPLICATE KEY UPDATE password='ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad';

-- 2. ABC Admin user
INSERT INTO users (username, password, role) VALUES 
('abc-admin', 'ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad', 'ADMIN')
ON DUPLICATE KEY UPDATE password='ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad';

-- 3. ABC Faculty user
INSERT INTO users (username, password, role) VALUES 
('abc-faculty', 'ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad', 'FACULTY')
ON DUPLICATE KEY UPDATE password='ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad';

-- 4. ABC Warden user
INSERT INTO users (username, password, role) VALUES 
('abc-warden', 'ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad', 'WARDEN')
ON DUPLICATE KEY UPDATE password='ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad';

-- Create student profile for abc-student
INSERT INTO students (name, email, phone, department, semester, enrollment_date, user_id, username, address)
SELECT 'ABC Test Student', 'abc.student@test.com', '1234567890', 'Computer Science', 3, CURDATE(), 
       id, 'STU001', '123 Test Street'
FROM users WHERE username = 'abc-student'
ON DUPLICATE KEY UPDATE name='ABC Test Student';

-- Create faculty profile for abc-faculty
INSERT INTO faculty (name, email, phone, department, qualification, join_date, user_id)
SELECT 'ABC Test Faculty', 'abc.faculty@test.com', '1234567891', 'Computer Science', 
       'PhD', CURDATE(), id
FROM users WHERE username = 'abc-faculty'
ON DUPLICATE KEY UPDATE name='ABC Test Faculty';

-- Create warden profile for abc-warden (assign to first hostel if exists)
INSERT INTO wardens (name, email, phone, hostel_id, user_id)
SELECT 'ABC Test Warden', 'abc.warden@test.com', '1234567892', 
       IFNULL((SELECT MIN(id) FROM hostels LIMIT 1), 1), id
FROM users WHERE username = 'abc-warden'
ON DUPLICATE KEY UPDATE name='ABC Test Warden';

-- Display created users
SELECT username, role FROM users WHERE username LIKE 'abc-%';

-- Instructions:
-- Login credentials:
-- Student:  abc-student / abc
-- Admin:    abc-admin   / abc
-- Faculty:  abc-faculty / abc
-- Warden:   abc-warden  / abc
