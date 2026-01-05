-- ============================================
-- COLLEGE MANAGEMENT SYSTEM - REALISTIC DATA SEED
-- ============================================
-- Execute this script manually via MySQL client or phpMyAdmin
-- This populates the database with realistic college scenario data
-- 
-- IMPORTANT: This will DELETE existing data (except admin user)
-- Backup your database before running!
-- ============================================

USE college_management;

-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Clear existing data (preserve admin) - with IF EXISTS checks
DELETE FROM enrollments WHERE 1=1;
DELETE FROM course_registrations WHERE 1=1;
DELETE FROM attendance WHERE 1=1;
DELETE FROM grades WHERE 1=1;
DELETE FROM submissions WHERE 1=1;
DELETE FROM fee_records WHERE 1=1;
DELETE FROM gate_passes WHERE 1=1;
DELETE FROM hostel_assignments WHERE 1=1;
DELETE FROM hostel_complaints WHERE 1=1;
DELETE FROM students WHERE id > 0;
DELETE FROM faculty WHERE id > 0;
DELETE FROM course_curriculum WHERE 1=1;
DELETE FROM timetable WHERE 1=1;
DELETE FROM courses WHERE id > 0;
DELETE FROM users WHERE id > 1;
DELETE FROM payroll_entries WHERE 1=1;
DELETE FROM employees WHERE 1=1;
DELETE FROM calendar_events WHERE 1=1;

SET FOREIGN_KEY_CHECKS = 1;

-- Get role IDs
SET @faculty_role = (SELECT id FROM roles WHERE code = 'FACULTY' LIMIT 1);
SET @student_role = (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1);
SET @warden_role = (SELECT id FROM roles WHERE code = 'WARDEN' LIMIT 1);
SET @finance_role = (SELECT id FROM roles WHERE code = 'FINANCE' LIMIT 1);

-- Departments
INSERT INTO departments (code, name, head_of_department, description) VALUES
('CSE', 'Computer Science & Engineering', 'Dr. Sarah Johnson', 'Department of Computer Science'),
('ECE', 'Electronics & Communication', 'Dr. Michael Chen', 'Department of ECE'),
('ME', 'Mechanical Engineering', 'Dr. Robert Martinez', 'Department of ME'),
('CE', 'Civil Engineering', 'Dr. Emily Wilson', 'Department of CE'),
('EEE', 'Electrical & Electronics', 'Dr. David Kumar', 'Department of EEE');

-- Faculty Users (password: faculty123 hashed with MD5 = e10adc3949ba59abbe56e057f20f883e)
INSERT INTO users (username, password, role_id) VALUES
('sarah.johnson', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('michael.chen', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('robert.martinez', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('emily.wilson', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('david.kumar', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('james.anderson', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('lisa.thompson', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('john.davis', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role);

-- Warden and Finance
INSERT INTO users (username, password, role_id) VALUES
('warden.kumar', 'e10adc3949ba59abbe56e057f20f883e', @warden_role),
('finance.manager', 'e10adc3949ba59abbe56e057f20f883e', @finance_role);

-- Faculty Details
SET @sarah_id = (SELECT id FROM users WHERE username = 'sarah.johnson');
SET @michael_id = (SELECT id FROM users WHERE username = 'michael.chen');
SET @robert_id = (SELECT id FROM users WHERE username = 'robert.martinez');
SET @emily_id = (SELECT id FROM users WHERE username = 'emily.wilson');
SET @david_id = (SELECT id FROM users WHERE username = 'david.kumar');
SET @james_id = (SELECT id FROM users WHERE username = 'james.anderson');
SET @lisa_id = (SELECT id FROM users WHERE username = 'lisa.thompson');
SET @john_id = (SELECT id FROM users WHERE username = 'john.davis');

INSERT INTO faculty (user_id, name, email, phone, department, designation, specialization, experience_years, qualifications) VALUES
(@sarah_id, 'Dr. Sarah Johnson', 'sarah.johnson@college.edu', '+91-9876543210', 'CSE', 'Professor', 'AI, ML', 15, 'Ph.D. CS'),
(@michael_id, 'Dr. Michael Chen', 'michael.chen@college.edu', '+91-9876543211', 'ECE', 'Associate Professor', 'VLSI', 12, 'Ph.D. Electronics'),
(@robert_id, 'Dr. Robert Martinez', 'robert.martinez@college.edu', '+91-9876543212', 'ME', 'Professor', 'Robotics', 18, 'Ph.D. ME'),
(@emily_id, 'Dr. Emily Wilson', 'emily.wilson@college.edu', '+91-9876543213', 'CE', 'Associate Professor', 'Structures', 10, 'Ph.D. CE'),
(@david_id, 'Dr. David Kumar', 'david.kumar@college.edu', '+91-9876543214', 'EEE', 'Professor', 'Power Systems', 14, 'Ph.D. EEE'),
(@james_id, 'Dr. James Anderson', 'james.anderson@college.edu', '+91-9876543215', 'CSE', 'Assistant Professor', 'Data Science', 6, 'Ph.D. CS'),
(@lisa_id, 'Dr. Lisa Thompson', 'lisa.thompson@college.edu', '+91-9876543216', 'CSE', 'Assistant Professor', 'Cybersecurity', 5, 'Ph.D. InfoSec'),
(@john_id, 'Dr. John Davis', 'john.davis@college.edu', '+91-9876543217', 'ECE', 'Assistant Professor', 'IoT', 7, 'Ph.D. Electronics');

-- Employee Records (for payroll)
INSERT INTO employees (employee_id, first_name, last_name, email, phone, designation, join_date, salary, status) VALUES
('sarah.johnson', 'Sarah', 'Johnson', 'sarah.johnson@college.edu', '+91-9876543210', 'Professor', '2010-07-01', 125000.00, 'ACTIVE'),
('michael.chen', 'Michael', 'Chen', 'michael.chen@college.edu', '+91-9876543211', 'Associate Professor', '2013-08-15', 105000.00, 'ACTIVE'),
('robert.martinez', 'Robert', 'Martinez', 'robert.martinez@college.edu', '+91-9876543212', 'Professor', '2007-06-01', 135000.00, 'ACTIVE'),
('emily.wilson', 'Emily', 'Wilson', 'emily.wilson@college.edu', '+91-9876543213', 'Associate Professor', '2015-09-10', 100000.00, 'ACTIVE'),
('david.kumar', 'David', 'Kumar', 'david.kumar@college.edu', '+91-9876543214', 'Professor', '2011-07-20', 130000.00, 'ACTIVE'),
('james.anderson', 'James', 'Anderson', 'james.anderson@college.edu', '+91-9876543215', 'Assistant Professor', '2019-08-01', 75000.00, 'ACTIVE'),
('lisa.thompson', 'Lisa', 'Thompson', 'lisa.thompson@college.edu', '+91-9876543216', 'Assistant Professor', '2020-07-15', 72000.00, 'ACTIVE'),
('john.davis', 'John', 'Davis', 'john.davis@college.edu', '+91-9876543217', 'Assistant Professor', '2018-09-01', 78000.00, 'ACTIVE'),
('warden.kumar', 'Rajesh', 'Kumar', 'warden.kumar@college.edu', '+91-9876543220', 'Warden', '2016-01-10', 85000.00, 'ACTIVE'),
('finance.manager', 'Sunita', 'Verma', 'finance@college.edu', '+91-9876543221', 'Finance Manager', '2014-03-15', 95000.00, 'ACTIVE');

-- Student Users (password: student123)
INSERT INTO users (username, password, role_id) VALUES
('CS21001', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CS21002', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CS21003', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CS21004', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('ECE21001', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('ECE21002', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('ME21001', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('ME21002', 'e10adc3949ba59abbe56e057f20f883e', @student_role);

-- Students
SET @cs1 = (SELECT id FROM users WHERE username = 'CS21001');
SET @cs2 = (SELECT id FROM users WHERE username = 'CS21002');
SET @cs3 = (SELECT id FROM users WHERE username = 'CS21003');
SET @cs4 = (SELECT id FROM users WHERE username = 'CS21004');
SET @ece1 = (SELECT id FROM users WHERE username = 'ECE21001');
SET @ece2 = (SELECT id FROM users WHERE username = 'ECE21002');
SET @me1 = (SELECT id FROM users WHERE username = 'ME21001');
SET @me2 = (SELECT id FROM users WHERE username = 'ME21002');

INSERT INTO students (user_id, roll_number, first_name, last_name, email, phone, date_of_birth, gender, address, department, semester, cgpa, status) VALUES
(@cs1, 'CS21001', 'Rahul', 'Sharma', 'rahul.sharma@student.college.edu', '+91-9123456701', '2003-05-15', 'Male', 'Mumbai', 'CSE', 6, 8.5, 'ACTIVE'),
(@cs2, 'CS21002', 'Priya', 'Patel', 'priya.patel@student.college.edu', '+91-9123456702', '2003-08-22', 'Female', 'Pune', 'CSE', 6, 9.1, 'ACTIVE'),
(@cs3, 'CS21003', 'Arjun', 'Reddy', 'arjun.reddy@student.college.edu', '+91-9123456703', '2003-03-10', 'Male', 'Bangalore', 'CSE', 6, 7.8, 'ACTIVE'),
(@cs4, 'CS21004', 'Sneha', 'Desai', 'sneha.desai@student.college.edu', '+91-9123456704', '2003-11-05', 'Female', 'Delhi', 'CSE', 6, 8.9, 'ACTIVE'),
(@ece1, 'ECE21001', 'Vikram', 'Singh', 'vikram.singh@student.college.edu', '+91-9123456705', '2003-07-18', 'Male', 'Hyderabad', 'ECE', 6, 8.2, 'ACTIVE'),
(@ece2, 'ECE21002', 'Ananya', 'Krishnan', 'ananya.k@student.college.edu', '+91-9123456706', '2003-09-25', 'Female', 'Chennai', 'ECE', 6, 8.7, 'ACTIVE'),
(@me1, 'ME21001', 'Aditya', 'Mehta', 'aditya.mehta@student.college.edu', '+91-9123456707', '2003-04-12', 'Male', 'Kolkata', 'ME', 6, 7.9, 'ACTIVE'),
(@me2, 'ME21002', 'Kavya', 'Nair', 'kavya.nair@student.college.edu', '+91-9123456708', '2003-12-08', 'Female', 'Kochi', 'ME', 6, 8.4, 'ACTIVE');

-- Courses
SET @dept_cse = (SELECT id FROM departments WHERE code = 'CSE');
SET @dept_ece = (SELECT id FROM departments WHERE code = 'ECE');
SET @dept_me = (SELECT id FROM departments WHERE code = 'ME');
SET @fac_sarah = (SELECT id FROM faculty WHERE name = 'Dr. Sarah Johnson');
SET @fac_james = (SELECT id FROM faculty WHERE name = 'Dr. James Anderson');
SET @fac_lisa = (SELECT id FROM faculty WHERE name = 'Dr. Lisa Thompson');
SET @fac_michael = (SELECT id FROM faculty WHERE name = 'Dr. Michael Chen');
SET @fac_john = (SELECT id FROM faculty WHERE name = 'Dr. John Davis');
SET @fac_robert = (SELECT id FROM faculty WHERE name = 'Dr. Robert Martinez');

INSERT INTO courses (code, name, description, department_id, credits, semester, instructor_id) VALUES
('CS301', 'Database Management Systems', 'RDBMS, SQL, Normalization', @dept_cse, 4, 6, @fac_sarah),
('CS302', 'Operating Systems', 'Process, Memory, File Systems', @dept_cse, 4, 6, @fac_james),
('CS303', 'Computer Networks', 'TCP/IP, Routing, Security', @dept_cse, 4, 6, @fac_lisa),
('ECE301', 'Digital Signal Processing', 'Signals, Filters, FFT', @dept_ece, 4, 6, @fac_michael),
('ECE302', 'Microprocessors', '8086, ARM, Embedded', @dept_ece, 4, 6, @fac_john),
('ME301', 'Thermodynamics', 'Heat, Engines, Cycles', @dept_me, 4, 6, @fac_robert);

-- Calendar Events
INSERT INTO calendar_events (title, event_date, event_type, description) VALUES
('Republic Day', '2026-01-26', 'HOLIDAY', 'National Holiday'),
('Mid-Sem Exams', '2026-02-15', 'EXAM', 'Mid-semester exams'),
('Holi', '2026-03-14', 'HOLIDAY', 'Festival of Colors'),
('Tech Fest', '2026-03-20', 'EVENT', 'Annual Technical Festival'),
('Final Exams', '2026-05-01', 'EXAM', 'End semester exams');

-- Sample Timetable (CSE Semester 6)
INSERT INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('CSE', 6, 'Monday', '9:00-10:00', 'DBMS', 'Dr. Sarah Johnson', '301'),
('CSE', 6, 'Monday', '10:00-11:00', 'OS', 'Dr. James Anderson', '302'),
('CSE', 6, 'Monday', '11:00-12:00', 'Networks', 'Dr. Lisa Thompson', '303'),
('CSE', 6, 'Tuesday', '9:00-10:00', 'DBMS Lab', 'Dr. Sarah Johnson', 'Lab1'),
('CSE', 6, 'Wednesday', '9:00-10:00', 'Networks', 'Dr. Lisa Thompson', '303'),
('CSE', 6, 'Thursday', '9:00-10:00', 'OS Lab', 'Dr. James Anderson', 'Lab2'),
('CSE', 6, 'Friday', '9:00-10:00', 'Networks Lab', 'Dr. Lisa Thompson', 'Lab3');

SELECT 'Data seeding completed successfully!' AS Status;
