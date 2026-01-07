-- Reset Database with Fresh Test Data
-- Password: 123 for all users

USE college_management;

-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Delete existing data (in reverse dependency order)
DELETE FROM attendance;
DELETE FROM grades;
DELETE FROM assignments;
DELETE FROM submissions;
DELETE FROM hostel_allocations;
DELETE FROM wardens;
DELETE FROM faculty;
DELETE FROM students;
DELETE FROM users;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Password hash for '123' using SHA-256
SET @password_hash = 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3';

-- ======================
-- CREATE SUPER ADMIN
-- ======================
INSERT INTO users (username, password, role) VALUES ('admin', @password_hash, 'ADMIN');

-- ======================
-- CREATE 5 STUDENTS (using enrollment numbers as usernames)
-- ======================
INSERT INTO users (username, password, role) VALUES 
('CS2023001', @password_hash, 'STUDENT'),
('CS2023002', @password_hash, 'STUDENT'),
('EE2022001', @password_hash, 'STUDENT'),
('ME2024001', @password_hash, 'STUDENT'),
('CS2023003', @password_hash, 'STUDENT');

INSERT INTO students (name, email, phone, department, semester, enrollment_date, user_id, address) VALUES
('Alice Johnson', 'alice@college.edu', '9876543210', 'Computer Science', 3, CURDATE(), (SELECT id FROM users WHERE username='CS2023001'), '123 College St'),
('Bob Smith', 'bob@college.edu', '9876543211', 'Computer Science', 3, CURDATE(), (SELECT id FROM users WHERE username='CS2023002'), '124 College St'),
('Carol Williams', 'carol@college.edu', '9876543212', 'Electrical Engineering', 4, CURDATE(), (SELECT id FROM users WHERE username='EE2022001'), '125 College St'),
('David Brown', 'david@college.edu', '9876543213', 'Mechanical Engineering', 2, CURDATE(), (SELECT id FROM users WHERE username='ME2024001'), '126 College St'),
('Emma Davis', 'emma@college.edu', '9876543214', 'Computer Science', 3, CURDATE(), (SELECT id FROM users WHERE username='CS2023003'), '127 College St');

-- ======================
-- CREATE 5 FACULTY (using faculty IDs as usernames)
-- ======================
INSERT INTO users (username, password, role) VALUES 
('FAC001', @password_hash, 'FACULTY'),
('FAC002', @password_hash, 'FACULTY'),
('FAC003', @password_hash, 'FACULTY'),
('FAC004', @password_hash, 'FACULTY'),
('FAC005', @password_hash, 'FACULTY');

INSERT INTO faculty (name, email, phone, department, qualification, join_date, user_id) VALUES
('Dr. John Wilson', 'john.wilson@college.edu', '9876540001', 'Computer Science', 'PhD in Computer Science', CURDATE(), (SELECT id FROM users WHERE username='FAC001')),
('Dr. Sarah Martinez', 'sarah.martinez@college.edu', '9876540002', 'Computer Science', 'PhD in Software Engineering', CURDATE(), (SELECT id FROM users WHERE username='FAC002')),
('Dr. Michael Lee', 'michael.lee@college.edu', '9876540003', 'Electrical Engineering', 'PhD in Electronics', CURDATE(), (SELECT id FROM users WHERE username='FAC003')),
('Dr. Jennifer Garcia', 'jennifer.garcia@college.edu', '9876540004', 'Mechanical Engineering', 'PhD in Robotics', CURDATE(), (SELECT id FROM users WHERE username='FAC004')),
('Dr. Robert Taylor', 'robert.taylor@college.edu', '9876540005', 'Computer Science', 'PhD in AI', CURDATE(), (SELECT id FROM users WHERE username='FAC005'));

-- ======================
-- CREATE 2 WARDENS
-- ======================
INSERT INTO users (username, password, role) VALUES 
('WARDEN01', @password_hash, 'WARDEN'),
('WARDEN02', @password_hash, 'WARDEN');

INSERT INTO wardens (name, email, phone, hostel_id, user_id) VALUES
('Mr. James Anderson', 'james.anderson@college.edu', '9876550001', 1, (SELECT id FROM users WHERE username='WARDEN01')),
('Ms. Linda Thomas', 'linda.thomas@college.edu', '9876550002', 2, (SELECT id FROM users WHERE username='WARDEN02'));

-- ======================
-- SUMMARY
-- ======================
SELECT 'Database Reset Complete!' as Status;
SELECT COUNT(*) as Total_Users FROM users;
SELECT role, COUNT(*) as Count FROM users GROUP BY role;
