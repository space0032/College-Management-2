-- ================================================================
-- COMPREHENSIVE COLLEGE DATA SEED
-- 100 Students | 30 Faculty | 20 Staff | 5 Departments
-- ================================================================
-- Run: sudo mysql college_management < realistic_college_data.sql
-- ================================================================

USE college_management;

-- Clear old data (PRESERVE admin user with ID=1)
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM students WHERE user_id > 1;
DELETE FROM faculty WHERE user_id > 1;
DELETE FROM users WHERE id > 1;

-- Skip these if they don't exist - not critical
-- DELETE FROM calendar_events WHERE 1=1;
-- DELETE FROM timetable WHERE 1=1;
-- DELETE FROM courses WHERE id > 0;

SET FOREIGN_KEY_CHECKS = 1;

-- Get role IDs
SET @admin_role = (SELECT id FROM roles WHERE code = 'ADMIN' LIMIT 1);
SET @faculty_role = (SELECT id FROM roles WHERE code = 'FACULTY' LIMIT 1);
SET @student_role = (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1);
SET @hod_role = (SELECT id FROM roles WHERE code = 'HOD' LIMIT 1);
SET @warden_role = (SELECT id FROM roles WHERE code = 'WARDEN' LIMIT 1);
SET @finance_role = (SELECT id FROM roles WHERE code = 'FINANCE' LIMIT 1);
SET @exam_coord_role = (SELECT id FROM roles WHERE code = 'EXAM_COORD' LIMIT 1);
SET @lab_asst_role = (SELECT id FROM roles WHERE code = 'LAB_ASST' LIMIT 1);

-- Departments
DELETE FROM departments WHERE 1=1;
INSERT INTO departments (code, name, head_of_department, description) VALUES
('CSE', 'Computer Science & Engineering', 'Dr. Rajesh Kumar', 'Leading department in CS education'),
('ECE', 'Electronics & Communication', 'Dr. Priya Sharma', 'Excellence in Electronics'),
('ME', 'Mechanical Engineering', 'Dr. Amit Singh', 'Innovation in Mechanical Systems'),
('CE', 'Civil Engineering', 'Dr. Meera Reddy', 'Building Tomorrow'),
('EEE', 'Electrical & Electronics', 'Dr. Vikram Patel', 'Power and Electronics');

-- ================================================================
-- FACULTY (30 total)
-- ================================================================

-- Passwords: faculty123 = e10adc3949ba59abbe56e057f20f883e

-- HODs (5 - Professors)
INSERT INTO users (username, password, role_id) VALUES
('rajesh.kumar', 'e10adc3949ba59abbe56e057f20f883e', @hod_role),
('priya.sharma', 'e10adc3949ba59abbe56e057f20f883e', @hod_role),
('amit.singh', 'e10adc3949ba59abbe56e057f20f883e', @hod_role),
('meera.reddy', 'e10adc3949ba59abbe56e057f20f883e', @hod_role),
('vikram.patel', 'e10adc3949ba59abbe56e057f20f883e', @hod_role);

-- Associate Professors (10)
INSERT INTO users (username, password, role_id) VALUES
('anita.desai', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('suresh.iyer', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('kavita.nair', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('rahul.mishra', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('deepa.menon', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('arun.gupta', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('sneha.kapoor', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('manoj.verma', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('pooja.joshi', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('sanjay.rao', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role);

-- Assistant Professors (15)
INSERT INTO users (username, password, role_id) VALUES
('neha.agarwal', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('arjun.mehta', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('ritu.singh', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('karan.bhat', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('priyanka.das', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('vivek.saxena', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('swati.pillai', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('rohan.malhotra', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('ishita.bansal', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('akash.pandey', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('divya.krishnan', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('nikhil.choudhary', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('tanya.bhatt', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('gaurav.shah', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role),
('sunita.yadav', 'e10adc3949ba59abbe56e057f20f883e', @faculty_role);

-- Faculty Profiles
INSERT INTO faculty (user_id, name, email, phone, department, qualification, join_date) VALUES
((SELECT id FROM users WHERE username = 'rajesh.kumar'), 'Dr. Rajesh Kumar', 'rajesh.kumar@college.edu', '+91-9876500001', 'CSE', 'Ph.D. Computer Science', '2010-07-01'),
((SELECT id FROM users WHERE username = 'priya.sharma'), 'Dr. Priya Sharma', 'priya.sharma@college.edu', '+91-9876500002', 'ECE', 'Ph.D. Electronics', '2011-08-15'),
((SELECT id FROM users WHERE username = 'amit.singh'), 'Dr. Amit Singh', 'amit.singh@college.edu', '+91-9876500003', 'ME', 'Ph.D. Mechanical', '2009-06-01'),
((SELECT id FROM users WHERE username = 'meera.reddy'), 'Dr. Meera Reddy', 'meera.reddy@college.edu', '+91-9876500004', 'CE', 'Ph.D. Civil', '2012-09-10'),
((SELECT id FROM users WHERE username = 'vikram.patel'), 'Dr. Vikram Patel', 'vikram.patel@college.edu', '+91-9876500005', 'EEE', 'Ph.D. Electrical', '2013-07-20'),
((SELECT id FROM users WHERE username = 'anita.desai'), 'Dr. Anita Desai', 'anita.desai@college.edu', '+91-9876500006', 'CSE', 'Ph.D. AI/ML', '2015-01-15'),
((SELECT id FROM users WHERE username = 'suresh.iyer'), 'Dr. Suresh Iyer', 'suresh.iyer@college.edu', '+91-9876500007', 'CSE', 'Ph.D. Database Systems', '2016-03-20'),
((SELECT id FROM users WHERE username = 'kavita.nair'), 'Dr. Kavita Nair', 'kavita.nair@college.edu', '+91-9876500008', 'ECE', 'Ph.D. VLSI', '2014-05-10'),
((SELECT id FROM users WHERE username = 'rahul.mishra'), 'Dr. Rahul Mishra', 'rahul.mishra@college.edu', '+91-9876500009', 'ECE', 'Ph.D. Signal Processing', '2015-08-25'),
((SELECT id FROM users WHERE username = 'deepa.menon'), 'Dr. Deepa Menon', 'deepa.menon@college.edu', '+91-9876500010', 'ME', 'Ph.D. Robotics', '2016-02-14'),
((SELECT id FROM users WHERE username = 'arun.gupta'), 'Dr. Arun Gupta', 'arun.gupta@college.edu', '+91-9876500011', 'ME', 'Ph.D. Thermodynamics', '2017-06-30'),
((SELECT id FROM users WHERE username = 'sneha.kapoor'), 'Dr. Sneha Kapoor', 'sneha.kapoor@college.edu', '+91-9876500012', 'CE', 'Ph.D. Structural', '2015-09-15'),
((SELECT id FROM users WHERE username = 'manoj.verma'), 'Dr. Manoj Verma', 'manoj.verma@college.edu', '+91-9876500013', 'CE', 'Ph.D. Geotechnical', '2016-11-20'),
((SELECT id FROM users WHERE username = 'pooja.joshi'), 'Dr. Pooja Joshi', 'pooja.joshi@college.edu', '+91-9876500014', 'EEE', 'Ph.D. Power Systems', '2017-01-10'),
((SELECT id FROM users WHERE username = 'sanjay.rao'), 'Dr. Sanjay Rao', 'sanjay.rao@college.edu', '+91-9876500015', 'EEE', 'Ph.D. Renewable Energy', '2018-03-05'),
((SELECT id FROM users WHERE username = 'neha.agarwal'), 'Neha Agarwal', 'neha.agarwal@college.edu', '+91-9876500016', 'CSE', 'M.Tech CS', '2020-07-01'),
((SELECT id FROM users WHERE username = 'arjun.mehta'), 'Arjun Mehta', 'arjun.mehta@college.edu', '+91-9876500017', 'CSE', 'M.Tech CS', '2020-08-15'),
((SELECT id FROM users WHERE username = 'ritu.singh'), 'Ritu Singh', 'ritu.singh@college.edu', '+91-9876500018', 'CSE', 'M.Tech CS', '2021-01-10'),
((SELECT id FROM users WHERE username = 'karan.bhat'), 'Karan Bhat', 'karan.bhat@college.edu', '+91-9876500019', 'CSE', 'M.Tech CS', '2021-07-20'),
((SELECT id FROM users WHERE username = 'priyanka.das'), 'Priyanka Das', 'priyanka.das@college.edu', '+91-9876500020', 'ECE', 'M.Tech ECE', '2020-06-15'),
((SELECT id FROM users WHERE username = 'vivek.saxena'), 'Vivek Saxena', 'vivek.saxena@college.edu', '+91-9876500021', 'ECE', 'M.Tech ECE', '2021-02-10'),
((SELECT id FROM users WHERE username = 'swati.pillai'), 'Swati Pillai', 'swati.pillai@college.edu', '+91-9876500022', 'ECE', 'M.Tech ECE', '2021-08-01'),
((SELECT id FROM users WHERE username = 'rohan.malhotra'), 'Rohan Malhotra', 'rohan.malhotra@college.edu', '+91-9876500023', 'ME', 'M.Tech ME', '2020-09-05'),
((SELECT id FROM users WHERE username = 'ishita.bansal'), 'Ishita Bansal', 'ishita.bansal@college.edu', '+91-9876500024', 'ME', 'M.Tech ME', '2021-01-20'),
((SELECT id FROM users WHERE username = 'akash.pandey'), 'Akash Pandey', 'akash.pandey@college.edu', '+91-9876500025', 'CE', 'M.Tech CE', '2020-10-15'),
((SELECT id FROM users WHERE username = 'divya.krishnan'), 'Divya Krishnan', 'divya.krishnan@college.edu', '+91-9876500026', 'CE', 'M.Tech CE', '2021-05-10'),
((SELECT id FROM users WHERE username = 'nikhil.choudhary'), 'Nikhil Choudhary', 'nikhil.choudhary@college.edu', '+91-9876500027', 'EEE', 'M.Tech EEE', '2020-11-01'),
((SELECT id FROM users WHERE username = 'tanya.bhatt'), 'Tanya Bhatt', 'tanya.bhatt@college.edu', '+91-9876500028', 'EEE', 'M.Tech EEE', '2021-04-15'),
((SELECT id FROM users WHERE username = 'gaurav.shah'), 'Gaurav Shah', 'gaurav.shah@college.edu', '+91-9876500029', 'CSE', 'M.Tech CS', '2021-06-20'),
((SELECT id FROM users WHERE username = 'sunita.yadav'), 'Sunita Yadav', 'sunita.yadav@college.edu', '+91-9876500030', 'ECE', 'M.Tech ECE', '2021-09-10');

-- ================================================================
-- SPECIAL USERS (3)
-- ================================================================

INSERT INTO users (username, password, role_id) VALUES
('warden.krishna', 'e10adc3949ba59abbe56e057f20f883e', @warden_role),
('finance.manager', 'e10adc3949ba59abbe56e057f20f883e', @finance_role),
('exam.coordinator', 'e10adc3949ba59abbe56e057f20f883e', @exam_coord_role);

-- ================================================================
-- STAFF (20 total)
-- ================================================================

-- Password: staff123 = 6ccb4b7c39fdadaa5d8365d68f877089

-- Lab Assistants (5)
INSERT INTO users (username, password, role_id) VALUES
('lab.ram', '6ccb4b7c39fdadaa5d8365d68f877089', @lab_asst_role),
('lab.sita', '6ccb4b7c39fdadaa5d8365d68f877089', @lab_asst_role),
('lab.mohan', '6ccb4b7c39fdadaa5d8365d68f877089', @lab_asst_role),
('lab.radha', '6ccb4b7c39fdadaa5d8365d68f877089', @lab_asst_role),
('lab.kumar', '6ccb4b7c39fdadaa5d8365d68f877089', @lab_asst_role);

-- NOTE: For workers, cleaners, library staff, security - we'd need to create these roles first
-- For now, they'll use LAB_ASST or FACULTY role as placeholder
-- Or add them manually via the application

SELECT 'Faculty and staff inserted!' AS Status;

-- ================================================================
-- STUDENTS (100 total)
-- ================================================================

-- Password: student123 = e10adc3949ba59abbe56e057f20f883e

-- CSE Students (40)
INSERT INTO users (username, password, role_id) VALUES
('CS22001', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CS22002', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CS22003', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CS22004', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CS22005', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CS22006', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CS22007', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CS22008', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CS22009', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CS22010', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CS21001', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CS21002', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CS21003', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CS21004', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CS21005', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CS21006', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CS21007', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CS21008', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CS21009', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CS21010', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CS20001', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CS20002', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CS20003', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CS20004', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CS20005', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CS20006', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CS20007', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CS20008', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CS20009', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CS20010', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CS19001', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CS19002', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CS19003', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CS19004', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CS19005', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CS19006', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CS19007', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CS19008', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CS19009', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CS19010', 'e10adc3949ba59abbe56e057f20f883e', @student_role);

-- ECE Students (25)
INSERT INTO users (username, password, role_id) VALUES
('EC22001', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('EC22002', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('EC22003', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('EC22004', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('EC22005', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('EC22006', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('EC21001', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('EC21002', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('EC21003', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('EC21004', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('EC21005', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('EC21006', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('EC20001', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('EC20002', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('EC20003', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('EC20004', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('EC20005', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('EC20006', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('EC19001', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('EC19002', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('EC19003', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('EC19004', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('EC19005', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('EC19006', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('EC19007', 'e10adc3949ba59abbe56e057f20f883e', @student_role);

-- ME Students (20)
INSERT INTO users (username, password, role_id) VALUES
('ME22001', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('ME22002', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('ME22003', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('ME22004', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('ME22005', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('ME21001', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('ME21002', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('ME21003', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('ME21004', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('ME21005', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('ME20001', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('ME20002', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('ME20003', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('ME20004', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('ME20005', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('ME19001', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('ME19002', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('ME19003', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('ME19004', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('ME19005', 'e10adc3949ba59abbe56e057f20f883e', @student_role);

-- CE Students (10)
INSERT INTO users (username, password, role_id) VALUES
('CE22001', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CE22002', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CE21001', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CE21002', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CE21003', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CE20001', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CE20002', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CE19001', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('CE19002', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('CE19003', 'e10adc3949ba59abbe56e057f20f883e', @student_role);

-- EEE Students (5)
INSERT INTO users (username, password, role_id) VALUES
('EE22001', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('EE21001', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('EE21002', 'e10adc3949ba59abbe56e057f20f883e', @student_role), ('EE20001', 'e10adc3949ba59abbe56e057f20f883e', @student_role),
('EE19001', 'e10adc3949ba59abbe56e057f20f883e', @student_role);

SELECT 'Students inserted!' AS Status;

-- Partial student profiles (due to size - add more via application)
INSERT INTO students (user_id, name, email, phone, course, batch, enrollment_date, department, semester) VALUES
((SELECT id FROM users WHERE username = 'CS22001'), 'Aarav Sharma', 'cs22001@student.edu', '+91-9100000001', 'B.Tech CSE', '2022', '2022-08-01', 'CSE', 2),
((SELECT id FROM users WHERE username = 'CS22002'), 'Vivaan Patel', 'cs22002@student.edu', '+91-9100000002', 'B.Tech CSE', '2022', '2022-08-01', 'CSE', 2),
((SELECT id FROM users WHERE username = 'CS21001'), 'Aditya Kumar', 'cs21001@student.edu', '+91-9100000011', 'B.Tech CSE', '2021', '2021-08-01', 'CSE', 4),
((SELECT id FROM users WHERE username = 'CS21002'), 'Saanvi Reddy', 'cs21002@student.edu', '+91-9100000012', 'B.Tech CSE', '2021', '2021-08-01', 'CSE', 4),
((SELECT id FROM users WHERE username = 'EC22001'), 'Reyansh Singh', 'ec22001@student.edu', '+91-9100001001', 'B.Tech ECE', '2022', '2022-08-01', 'ECE', 2),
((SELECT id FROM users WHERE username = 'EC21001'), 'Diya Mehta', 'ec21001@student.edu', '+91-9100001011', 'B.Tech ECE', '2021', '2021-08-01', 'ECE', 4),
((SELECT id FROM users WHERE username = 'ME22001'), 'Arjun Gupta', 'me22001@student.edu', '+91-9100002001', 'B.Tech ME', '2022', '2022-08-01', 'ME', 2),
((SELECT id FROM users WHERE username = 'ME21001'), 'Ananya Desai', 'me21001@student.edu', '+91-9100002011', 'B.Tech ME', '2021', '2021-08-01', 'ME', 4);

SELECT 'Sample student profiles created. Add more via the application!' AS Status;

SELECT 'SEED COMPLETE - 100 Students | 30 Faculty | 5 Staff Created!' AS FinalStatus;
