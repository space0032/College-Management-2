-- =====================================================
-- COMPREHENSIVE SAMPLE DATA FOR COLLEGE MANAGEMENT SYSTEM
-- 100 Students, 30 Faculty, Courses, Fees, Timetables
-- =====================================================

USE college_management;

-- =====================================================
-- STUDENT DATA (100 Students)
-- =====================================================

-- Computer Science Semester 1 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs001', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Atharv Reddy', 'atharv.reddy.cs001@student.college.edu', '9874759017', 'B.Tech Computer Science', '2024', '2024-07-15', '95 Main Street, City', 'Computer Science', 1, 1, (SELECT id FROM users WHERE username = 'cs001'));

-- Computer Science Semester 1 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs002', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Tara Kumar', 'tara.kumar.cs002@student.college.edu', '9830233492', 'B.Tech Computer Science', '2024', '2024-07-15', '963 Main Street, City', 'Computer Science', 1, 1, (SELECT id FROM users WHERE username = 'cs002'));

-- Computer Science Semester 1 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs003', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Ishaan Agarwal', 'ishaan.agarwal.cs003@student.college.edu', '9860872059', 'B.Tech Computer Science', '2024', '2024-07-15', '468 Main Street, City', 'Computer Science', 1, 1, (SELECT id FROM users WHERE username = 'cs003'));

-- Computer Science Semester 2 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs004', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Angel Nair', 'angel.nair.cs004@student.college.edu', '9842017884', 'B.Tech Computer Science', '2024', '2024-07-15', '988 Main Street, City', 'Computer Science', 2, 1, (SELECT id FROM users WHERE username = 'cs004'));

-- Computer Science Semester 2 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs005', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Ekant Gupta', 'ekant.gupta.cs005@student.college.edu', '9815077188', 'B.Tech Computer Science', '2024', '2024-07-15', '376 Main Street, City', 'Computer Science', 2, 1, (SELECT id FROM users WHERE username = 'cs005'));

-- Computer Science Semester 2 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs006', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Advait Prabhu', 'advait.prabhu.cs006@student.college.edu', '9850255332', 'B.Tech Computer Science', '2024', '2024-07-15', '898 Main Street, City', 'Computer Science', 2, 1, (SELECT id FROM users WHERE username = 'cs006'));

-- Computer Science Semester 2 Student 4
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs007', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Aadhya Gupta', 'aadhya.gupta.cs007@student.college.edu', '9886776382', 'B.Tech Computer Science', '2024', '2024-07-15', '265 Main Street, City', 'Computer Science', 2, 1, (SELECT id FROM users WHERE username = 'cs007'));

-- Computer Science Semester 3 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs008', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Advik Mehta', 'advik.mehta.cs008@student.college.edu', '9871166443', 'B.Tech Computer Science', '2023', '2023-07-15', '506 Main Street, City', 'Computer Science', 3, 0, (SELECT id FROM users WHERE username = 'cs008'));

-- Computer Science Semester 3 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs009', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Ananya Nair', 'ananya.nair.cs009@student.college.edu', '9883932142', 'B.Tech Computer Science', '2023', '2023-07-15', '743 Main Street, City', 'Computer Science', 3, 1, (SELECT id FROM users WHERE username = 'cs009'));

-- Computer Science Semester 3 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs010', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Aadhya Agarwal', 'aadhya.agarwal.cs010@student.college.edu', '9888681192', 'B.Tech Computer Science', '2023', '2023-07-15', '867 Main Street, City', 'Computer Science', 3, 0, (SELECT id FROM users WHERE username = 'cs010'));

-- Computer Science Semester 3 Student 4
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs011', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Kiaan Gupta', 'kiaan.gupta.cs011@student.college.edu', '9826284299', 'B.Tech Computer Science', '2023', '2023-07-15', '706 Main Street, City', 'Computer Science', 3, 0, (SELECT id FROM users WHERE username = 'cs011'));

-- Computer Science Semester 4 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs012', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Harsh Patel', 'harsh.patel.cs012@student.college.edu', '9845167167', 'B.Tech Computer Science', '2023', '2023-07-15', '372 Main Street, City', 'Computer Science', 4, 1, (SELECT id FROM users WHERE username = 'cs012'));

-- Computer Science Semester 4 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs013', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Kabir Kumar', 'kabir.kumar.cs013@student.college.edu', '9814589440', 'B.Tech Computer Science', '2023', '2023-07-15', '364 Main Street, City', 'Computer Science', 4, 1, (SELECT id FROM users WHERE username = 'cs013'));

-- Computer Science Semester 4 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs014', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Krishna Sharma', 'krishna.sharma.cs014@student.college.edu', '9854269447', 'B.Tech Computer Science', '2023', '2023-07-15', '86 Main Street, City', 'Computer Science', 4, 1, (SELECT id FROM users WHERE username = 'cs014'));

-- Computer Science Semester 4 Student 4
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs015', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Jhanvi Das', 'jhanvi.das.cs015@student.college.edu', '9824031809', 'B.Tech Computer Science', '2023', '2023-07-15', '735 Main Street, City', 'Computer Science', 4, 0, (SELECT id FROM users WHERE username = 'cs015'));

-- Computer Science Semester 5 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs016', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Arjun Hegde', 'arjun.hegde.cs016@student.college.edu', '9829396680', 'B.Tech Computer Science', '2022', '2022-07-15', '389 Main Street, City', 'Computer Science', 5, 1, (SELECT id FROM users WHERE username = 'cs016'));

-- Computer Science Semester 5 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs017', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Om Gupta', 'om.gupta.cs017@student.college.edu', '9897857130', 'B.Tech Computer Science', '2022', '2022-07-15', '473 Main Street, City', 'Computer Science', 5, 1, (SELECT id FROM users WHERE username = 'cs017'));

-- Computer Science Semester 5 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs018', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Manav Nair', 'manav.nair.cs018@student.college.edu', '9824565192', 'B.Tech Computer Science', '2022', '2022-07-15', '126 Main Street, City', 'Computer Science', 5, 1, (SELECT id FROM users WHERE username = 'cs018'));

-- Computer Science Semester 5 Student 4
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs019', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Ananya Shetty', 'ananya.shetty.cs019@student.college.edu', '9892563923', 'B.Tech Computer Science', '2022', '2022-07-15', '421 Main Street, City', 'Computer Science', 5, 1, (SELECT id FROM users WHERE username = 'cs019'));

-- Computer Science Semester 6 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs020', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Manav Prabhu', 'manav.prabhu.cs020@student.college.edu', '9887142299', 'B.Tech Computer Science', '2022', '2022-07-15', '344 Main Street, City', 'Computer Science', 6, 1, (SELECT id FROM users WHERE username = 'cs020'));

-- Computer Science Semester 6 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs021', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Keshav Nair', 'keshav.nair.cs021@student.college.edu', '9871143790', 'B.Tech Computer Science', '2022', '2022-07-15', '283 Main Street, City', 'Computer Science', 6, 0, (SELECT id FROM users WHERE username = 'cs021'));

-- Computer Science Semester 6 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs022', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Pihu Das', 'pihu.das.cs022@student.college.edu', '9836369693', 'B.Tech Computer Science', '2022', '2022-07-15', '707 Main Street, City', 'Computer Science', 6, 1, (SELECT id FROM users WHERE username = 'cs022'));

-- Computer Science Semester 6 Student 4
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs023', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Aayan Kumar', 'aayan.kumar.cs023@student.college.edu', '9856831222', 'B.Tech Computer Science', '2022', '2022-07-15', '631 Main Street, City', 'Computer Science', 6, 1, (SELECT id FROM users WHERE username = 'cs023'));

-- Computer Science Semester 7 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs024', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Anika Kamath', 'anika.kamath.cs024@student.college.edu', '9885364654', 'B.Tech Computer Science', '2021', '2021-07-15', '584 Main Street, City', 'Computer Science', 7, 1, (SELECT id FROM users WHERE username = 'cs024'));

-- Computer Science Semester 7 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs025', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Vivaan Prabhu', 'vivaan.prabhu.cs025@student.college.edu', '9821140086', 'B.Tech Computer Science', '2021', '2021-07-15', '695 Main Street, City', 'Computer Science', 7, 0, (SELECT id FROM users WHERE username = 'cs025'));

-- Computer Science Semester 7 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs026', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Arnav Patel', 'arnav.patel.cs026@student.college.edu', '9856151327', 'B.Tech Computer Science', '2021', '2021-07-15', '363 Main Street, City', 'Computer Science', 7, 1, (SELECT id FROM users WHERE username = 'cs026'));

-- Computer Science Semester 7 Student 4
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs027', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Sai Nair', 'sai.nair.cs027@student.college.edu', '9823454010', 'B.Tech Computer Science', '2021', '2021-07-15', '353 Main Street, City', 'Computer Science', 7, 0, (SELECT id FROM users WHERE username = 'cs027'));

-- Computer Science Semester 8 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs028', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Kiara Verma', 'kiara.verma.cs028@student.college.edu', '9858387772', 'B.Tech Computer Science', '2021', '2021-07-15', '471 Main Street, City', 'Computer Science', 8, 1, (SELECT id FROM users WHERE username = 'cs028'));

-- Computer Science Semester 8 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs029', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Kavya Shah', 'kavya.shah.cs029@student.college.edu', '9836576221', 'B.Tech Computer Science', '2021', '2021-07-15', '918 Main Street, City', 'Computer Science', 8, 0, (SELECT id FROM users WHERE username = 'cs029'));

-- Computer Science Semester 8 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('cs030', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Keshav Nair', 'keshav.nair.cs030@student.college.edu', '9886342193', 'B.Tech Computer Science', '2021', '2021-07-15', '902 Main Street, City', 'Computer Science', 8, 1, (SELECT id FROM users WHERE username = 'cs030'));

-- Electrical Engineering Semester 1 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee031', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Anvi Bhat', 'anvi.bhat.ee031@student.college.edu', '9830024049', 'B.Tech Electrical Engineering', '2024', '2024-07-15', '614 Main Street, City', 'Electrical Engineering', 1, 1, (SELECT id FROM users WHERE username = 'ee031'));

-- Electrical Engineering Semester 1 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee032', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Naman Joshi', 'naman.joshi.ee032@student.college.edu', '9841921244', 'B.Tech Electrical Engineering', '2024', '2024-07-15', '674 Main Street, City', 'Electrical Engineering', 1, 0, (SELECT id FROM users WHERE username = 'ee032'));

-- Electrical Engineering Semester 1 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee033', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Sara Hegde', 'sara.hegde.ee033@student.college.edu', '9874121357', 'B.Tech Electrical Engineering', '2024', '2024-07-15', '726 Main Street, City', 'Electrical Engineering', 1, 0, (SELECT id FROM users WHERE username = 'ee033'));

-- Electrical Engineering Semester 2 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee034', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Diya Menon', 'diya.menon.ee034@student.college.edu', '9847962148', 'B.Tech Electrical Engineering', '2024', '2024-07-15', '461 Main Street, City', 'Electrical Engineering', 2, 1, (SELECT id FROM users WHERE username = 'ee034'));

-- Electrical Engineering Semester 2 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee035', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Arjun Acharya', 'arjun.acharya.ee035@student.college.edu', '9845234830', 'B.Tech Electrical Engineering', '2024', '2024-07-15', '22 Main Street, City', 'Electrical Engineering', 2, 0, (SELECT id FROM users WHERE username = 'ee035'));

-- Electrical Engineering Semester 2 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee036', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Tara Mehta', 'tara.mehta.ee036@student.college.edu', '9866119493', 'B.Tech Electrical Engineering', '2024', '2024-07-15', '541 Main Street, City', 'Electrical Engineering', 2, 1, (SELECT id FROM users WHERE username = 'ee036'));

-- Electrical Engineering Semester 2 Student 4
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee037', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Saanvi Bhat', 'saanvi.bhat.ee037@student.college.edu', '9865444889', 'B.Tech Electrical Engineering', '2024', '2024-07-15', '312 Main Street, City', 'Electrical Engineering', 2, 0, (SELECT id FROM users WHERE username = 'ee037'));

-- Electrical Engineering Semester 3 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee038', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Jhanvi Pillai', 'jhanvi.pillai.ee038@student.college.edu', '9828479837', 'B.Tech Electrical Engineering', '2023', '2023-07-15', '188 Main Street, City', 'Electrical Engineering', 3, 0, (SELECT id FROM users WHERE username = 'ee038'));

-- Electrical Engineering Semester 3 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee039', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Shaurya Krishnan', 'shaurya.krishnan.ee039@student.college.edu', '9883344187', 'B.Tech Electrical Engineering', '2023', '2023-07-15', '991 Main Street, City', 'Electrical Engineering', 3, 0, (SELECT id FROM users WHERE username = 'ee039'));

-- Electrical Engineering Semester 3 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee040', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Ananya Murthy', 'ananya.murthy.ee040@student.college.edu', '9881667961', 'B.Tech Electrical Engineering', '2023', '2023-07-15', '940 Main Street, City', 'Electrical Engineering', 3, 1, (SELECT id FROM users WHERE username = 'ee040'));

-- Electrical Engineering Semester 3 Student 4
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee041', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Lakshay Verma', 'lakshay.verma.ee041@student.college.edu', '9835831048', 'B.Tech Electrical Engineering', '2023', '2023-07-15', '935 Main Street, City', 'Electrical Engineering', 3, 0, (SELECT id FROM users WHERE username = 'ee041'));

-- Electrical Engineering Semester 4 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee042', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Ishaan Das', 'ishaan.das.ee042@student.college.edu', '9813574895', 'B.Tech Electrical Engineering', '2023', '2023-07-15', '676 Main Street, City', 'Electrical Engineering', 4, 1, (SELECT id FROM users WHERE username = 'ee042'));

-- Electrical Engineering Semester 4 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee043', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Manav Singh', 'manav.singh.ee043@student.college.edu', '9844005350', 'B.Tech Electrical Engineering', '2023', '2023-07-15', '326 Main Street, City', 'Electrical Engineering', 4, 1, (SELECT id FROM users WHERE username = 'ee043'));

-- Electrical Engineering Semester 4 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee044', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Vivaan Kamath', 'vivaan.kamath.ee044@student.college.edu', '9895545793', 'B.Tech Electrical Engineering', '2023', '2023-07-15', '797 Main Street, City', 'Electrical Engineering', 4, 1, (SELECT id FROM users WHERE username = 'ee044'));

-- Electrical Engineering Semester 4 Student 4
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee045', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Ved Desai', 'ved.desai.ee045@student.college.edu', '9895146744', 'B.Tech Electrical Engineering', '2023', '2023-07-15', '591 Main Street, City', 'Electrical Engineering', 4, 1, (SELECT id FROM users WHERE username = 'ee045'));

-- Electrical Engineering Semester 5 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee046', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Advik Iyer', 'advik.iyer.ee046@student.college.edu', '9859798460', 'B.Tech Electrical Engineering', '2022', '2022-07-15', '231 Main Street, City', 'Electrical Engineering', 5, 1, (SELECT id FROM users WHERE username = 'ee046'));

-- Electrical Engineering Semester 5 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee047', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Advait Pillai', 'advait.pillai.ee047@student.college.edu', '9850262533', 'B.Tech Electrical Engineering', '2022', '2022-07-15', '839 Main Street, City', 'Electrical Engineering', 5, 1, (SELECT id FROM users WHERE username = 'ee047'));

-- Electrical Engineering Semester 5 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee048', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Arjun Prabhu', 'arjun.prabhu.ee048@student.college.edu', '9836434079', 'B.Tech Electrical Engineering', '2022', '2022-07-15', '7 Main Street, City', 'Electrical Engineering', 5, 1, (SELECT id FROM users WHERE username = 'ee048'));

-- Electrical Engineering Semester 5 Student 4
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee049', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Sara Reddy', 'sara.reddy.ee049@student.college.edu', '9821848636', 'B.Tech Electrical Engineering', '2022', '2022-07-15', '156 Main Street, City', 'Electrical Engineering', 5, 1, (SELECT id FROM users WHERE username = 'ee049'));

-- Electrical Engineering Semester 6 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee050', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Advik Das', 'advik.das.ee050@student.college.edu', '9824581893', 'B.Tech Electrical Engineering', '2022', '2022-07-15', '775 Main Street, City', 'Electrical Engineering', 6, 1, (SELECT id FROM users WHERE username = 'ee050'));

-- Electrical Engineering Semester 6 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee051', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Anvi Gupta', 'anvi.gupta.ee051@student.college.edu', '9826744453', 'B.Tech Electrical Engineering', '2022', '2022-07-15', '197 Main Street, City', 'Electrical Engineering', 6, 1, (SELECT id FROM users WHERE username = 'ee051'));

-- Electrical Engineering Semester 6 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee052', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Aadhya Rao', 'aadhya.rao.ee052@student.college.edu', '9873585406', 'B.Tech Electrical Engineering', '2022', '2022-07-15', '708 Main Street, City', 'Electrical Engineering', 6, 1, (SELECT id FROM users WHERE username = 'ee052'));

-- Electrical Engineering Semester 6 Student 4
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee053', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Ved Das', 'ved.das.ee053@student.college.edu', '9896661663', 'B.Tech Electrical Engineering', '2022', '2022-07-15', '569 Main Street, City', 'Electrical Engineering', 6, 0, (SELECT id FROM users WHERE username = 'ee053'));

-- Electrical Engineering Semester 7 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee054', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Keshav Acharya', 'keshav.acharya.ee054@student.college.edu', '9860761469', 'B.Tech Electrical Engineering', '2021', '2021-07-15', '331 Main Street, City', 'Electrical Engineering', 7, 1, (SELECT id FROM users WHERE username = 'ee054'));

-- Electrical Engineering Semester 7 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee055', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Naman Acharya', 'naman.acharya.ee055@student.college.edu', '9885580150', 'B.Tech Electrical Engineering', '2021', '2021-07-15', '351 Main Street, City', 'Electrical Engineering', 7, 1, (SELECT id FROM users WHERE username = 'ee055'));

-- Electrical Engineering Semester 7 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee056', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Vivaan Krishnan', 'vivaan.krishnan.ee056@student.college.edu', '9822047980', 'B.Tech Electrical Engineering', '2021', '2021-07-15', '52 Main Street, City', 'Electrical Engineering', 7, 0, (SELECT id FROM users WHERE username = 'ee056'));

-- Electrical Engineering Semester 7 Student 4
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee057', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Vihaan Nair', 'vihaan.nair.ee057@student.college.edu', '9880428008', 'B.Tech Electrical Engineering', '2021', '2021-07-15', '98 Main Street, City', 'Electrical Engineering', 7, 1, (SELECT id FROM users WHERE username = 'ee057'));

-- Electrical Engineering Semester 8 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee058', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Aarav Shetty', 'aarav.shetty.ee058@student.college.edu', '9837366181', 'B.Tech Electrical Engineering', '2021', '2021-07-15', '970 Main Street, City', 'Electrical Engineering', 8, 1, (SELECT id FROM users WHERE username = 'ee058'));

-- Electrical Engineering Semester 8 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee059', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Advik Acharya', 'advik.acharya.ee059@student.college.edu', '9868770186', 'B.Tech Electrical Engineering', '2021', '2021-07-15', '51 Main Street, City', 'Electrical Engineering', 8, 1, (SELECT id FROM users WHERE username = 'ee059'));

-- Electrical Engineering Semester 8 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ee060', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Vivaan Iyer', 'vivaan.iyer.ee060@student.college.edu', '9882687149', 'B.Tech Electrical Engineering', '2021', '2021-07-15', '572 Main Street, City', 'Electrical Engineering', 8, 0, (SELECT id FROM users WHERE username = 'ee060'));

-- Mechanical Engineering Semester 1 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me061', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Vivaan Iyer', 'vivaan.iyer.me061@student.college.edu', '9816452475', 'B.Tech Mechanical Engineering', '2024', '2024-07-15', '848 Main Street, City', 'Mechanical Engineering', 1, 0, (SELECT id FROM users WHERE username = 'me061'));

-- Mechanical Engineering Semester 1 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me062', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Anika Nair', 'anika.nair.me062@student.college.edu', '9884176680', 'B.Tech Mechanical Engineering', '2024', '2024-07-15', '624 Main Street, City', 'Mechanical Engineering', 1, 0, (SELECT id FROM users WHERE username = 'me062'));

-- Mechanical Engineering Semester 1 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me063', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Aarohi Hegde', 'aarohi.hegde.me063@student.college.edu', '9865100829', 'B.Tech Mechanical Engineering', '2024', '2024-07-15', '701 Main Street, City', 'Mechanical Engineering', 1, 1, (SELECT id FROM users WHERE username = 'me063'));

-- Mechanical Engineering Semester 2 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me064', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Pranav Krishnan', 'pranav.krishnan.me064@student.college.edu', '9896195358', 'B.Tech Mechanical Engineering', '2024', '2024-07-15', '930 Main Street, City', 'Mechanical Engineering', 2, 0, (SELECT id FROM users WHERE username = 'me064'));

-- Mechanical Engineering Semester 2 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me065', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Sara Gupta', 'sara.gupta.me065@student.college.edu', '9859063640', 'B.Tech Mechanical Engineering', '2024', '2024-07-15', '155 Main Street, City', 'Mechanical Engineering', 2, 1, (SELECT id FROM users WHERE username = 'me065'));

-- Mechanical Engineering Semester 2 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me066', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Atharv Iyer', 'atharv.iyer.me066@student.college.edu', '9883835617', 'B.Tech Mechanical Engineering', '2024', '2024-07-15', '155 Main Street, City', 'Mechanical Engineering', 2, 0, (SELECT id FROM users WHERE username = 'me066'));

-- Mechanical Engineering Semester 2 Student 4
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me067', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Ansh Patel', 'ansh.patel.me067@student.college.edu', '9856082252', 'B.Tech Mechanical Engineering', '2024', '2024-07-15', '490 Main Street, City', 'Mechanical Engineering', 2, 1, (SELECT id FROM users WHERE username = 'me067'));

-- Mechanical Engineering Semester 3 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me068', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Shaurya Acharya', 'shaurya.acharya.me068@student.college.edu', '9821440618', 'B.Tech Mechanical Engineering', '2023', '2023-07-15', '289 Main Street, City', 'Mechanical Engineering', 3, 0, (SELECT id FROM users WHERE username = 'me068'));

-- Mechanical Engineering Semester 3 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me069', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Angel Rao', 'angel.rao.me069@student.college.edu', '9859211204', 'B.Tech Mechanical Engineering', '2023', '2023-07-15', '147 Main Street, City', 'Mechanical Engineering', 3, 1, (SELECT id FROM users WHERE username = 'me069'));

-- Mechanical Engineering Semester 3 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me070', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Harsh Reddy', 'harsh.reddy.me070@student.college.edu', '9855049453', 'B.Tech Mechanical Engineering', '2023', '2023-07-15', '670 Main Street, City', 'Mechanical Engineering', 3, 0, (SELECT id FROM users WHERE username = 'me070'));

-- Mechanical Engineering Semester 3 Student 4
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me071', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Pranav Shetty', 'pranav.shetty.me071@student.college.edu', '9863743404', 'B.Tech Mechanical Engineering', '2023', '2023-07-15', '888 Main Street, City', 'Mechanical Engineering', 3, 0, (SELECT id FROM users WHERE username = 'me071'));

-- Mechanical Engineering Semester 4 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me072', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Keshav Sharma', 'keshav.sharma.me072@student.college.edu', '9894269070', 'B.Tech Mechanical Engineering', '2023', '2023-07-15', '115 Main Street, City', 'Mechanical Engineering', 4, 0, (SELECT id FROM users WHERE username = 'me072'));

-- Mechanical Engineering Semester 4 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me073', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Pari Gupta', 'pari.gupta.me073@student.college.edu', '9811116111', 'B.Tech Mechanical Engineering', '2023', '2023-07-15', '626 Main Street, City', 'Mechanical Engineering', 4, 1, (SELECT id FROM users WHERE username = 'me073'));

-- Mechanical Engineering Semester 4 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me074', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Atharv Prabhu', 'atharv.prabhu.me074@student.college.edu', '9893949030', 'B.Tech Mechanical Engineering', '2023', '2023-07-15', '80 Main Street, City', 'Mechanical Engineering', 4, 1, (SELECT id FROM users WHERE username = 'me074'));

-- Mechanical Engineering Semester 4 Student 4
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me075', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Kabir Agarwal', 'kabir.agarwal.me075@student.college.edu', '9851048680', 'B.Tech Mechanical Engineering', '2023', '2023-07-15', '569 Main Street, City', 'Mechanical Engineering', 4, 0, (SELECT id FROM users WHERE username = 'me075'));

-- Mechanical Engineering Semester 5 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me076', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Naman Acharya', 'naman.acharya.me076@student.college.edu', '9835429773', 'B.Tech Mechanical Engineering', '2022', '2022-07-15', '545 Main Street, City', 'Mechanical Engineering', 5, 1, (SELECT id FROM users WHERE username = 'me076'));

-- Mechanical Engineering Semester 5 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me077', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Saanvi Agarwal', 'saanvi.agarwal.me077@student.college.edu', '9879740862', 'B.Tech Mechanical Engineering', '2022', '2022-07-15', '966 Main Street, City', 'Mechanical Engineering', 5, 0, (SELECT id FROM users WHERE username = 'me077'));

-- Mechanical Engineering Semester 5 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me078', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Angel Verma', 'angel.verma.me078@student.college.edu', '9879533084', 'B.Tech Mechanical Engineering', '2022', '2022-07-15', '573 Main Street, City', 'Mechanical Engineering', 5, 0, (SELECT id FROM users WHERE username = 'me078'));

-- Mechanical Engineering Semester 5 Student 4
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me079', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Angel Patel', 'angel.patel.me079@student.college.edu', '9828109129', 'B.Tech Mechanical Engineering', '2022', '2022-07-15', '777 Main Street, City', 'Mechanical Engineering', 5, 0, (SELECT id FROM users WHERE username = 'me079'));

-- Mechanical Engineering Semester 6 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me080', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Kavya Murthy', 'kavya.murthy.me080@student.college.edu', '9826247230', 'B.Tech Mechanical Engineering', '2022', '2022-07-15', '736 Main Street, City', 'Mechanical Engineering', 6, 1, (SELECT id FROM users WHERE username = 'me080'));

-- Mechanical Engineering Semester 6 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me081', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Advait Agarwal', 'advait.agarwal.me081@student.college.edu', '9871759337', 'B.Tech Mechanical Engineering', '2022', '2022-07-15', '323 Main Street, City', 'Mechanical Engineering', 6, 1, (SELECT id FROM users WHERE username = 'me081'));

-- Mechanical Engineering Semester 6 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me082', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Diya Verma', 'diya.verma.me082@student.college.edu', '9821218031', 'B.Tech Mechanical Engineering', '2022', '2022-07-15', '819 Main Street, City', 'Mechanical Engineering', 6, 1, (SELECT id FROM users WHERE username = 'me082'));

-- Mechanical Engineering Semester 6 Student 4
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me083', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Lakshay Kamath', 'lakshay.kamath.me083@student.college.edu', '9859838969', 'B.Tech Mechanical Engineering', '2022', '2022-07-15', '839 Main Street, City', 'Mechanical Engineering', 6, 0, (SELECT id FROM users WHERE username = 'me083'));

-- Mechanical Engineering Semester 7 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me084', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Sara Singh', 'sara.singh.me084@student.college.edu', '9836073313', 'B.Tech Mechanical Engineering', '2021', '2021-07-15', '235 Main Street, City', 'Mechanical Engineering', 7, 0, (SELECT id FROM users WHERE username = 'me084'));

-- Mechanical Engineering Semester 7 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me085', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Riya Bhat', 'riya.bhat.me085@student.college.edu', '9834448590', 'B.Tech Mechanical Engineering', '2021', '2021-07-15', '531 Main Street, City', 'Mechanical Engineering', 7, 0, (SELECT id FROM users WHERE username = 'me085'));

-- Mechanical Engineering Semester 7 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me086', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Diya Shetty', 'diya.shetty.me086@student.college.edu', '9840006434', 'B.Tech Mechanical Engineering', '2021', '2021-07-15', '267 Main Street, City', 'Mechanical Engineering', 7, 1, (SELECT id FROM users WHERE username = 'me086'));

-- Mechanical Engineering Semester 7 Student 4
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me087', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Jhanvi Bhat', 'jhanvi.bhat.me087@student.college.edu', '9873752236', 'B.Tech Mechanical Engineering', '2021', '2021-07-15', '40 Main Street, City', 'Mechanical Engineering', 7, 0, (SELECT id FROM users WHERE username = 'me087'));

-- Mechanical Engineering Semester 8 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me088', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Shaurya Singh', 'shaurya.singh.me088@student.college.edu', '9879250908', 'B.Tech Mechanical Engineering', '2021', '2021-07-15', '838 Main Street, City', 'Mechanical Engineering', 8, 1, (SELECT id FROM users WHERE username = 'me088'));

-- Mechanical Engineering Semester 8 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me089', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Sara Joshi', 'sara.joshi.me089@student.college.edu', '9843766800', 'B.Tech Mechanical Engineering', '2021', '2021-07-15', '946 Main Street, City', 'Mechanical Engineering', 8, 1, (SELECT id FROM users WHERE username = 'me089'));

-- Mechanical Engineering Semester 8 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('me090', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Vihaan Reddy', 'vihaan.reddy.me090@student.college.edu', '9863014660', 'B.Tech Mechanical Engineering', '2021', '2021-07-15', '854 Main Street, City', 'Mechanical Engineering', 8, 1, (SELECT id FROM users WHERE username = 'me090'));

-- Civil Engineering Semester 1 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce091', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Navya Agarwal', 'navya.agarwal.ce091@student.college.edu', '9854146060', 'B.Tech Civil Engineering', '2024', '2024-07-15', '703 Main Street, City', 'Civil Engineering', 1, 1, (SELECT id FROM users WHERE username = 'ce091'));

-- Civil Engineering Semester 1 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce092', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Riya Murthy', 'riya.murthy.ce092@student.college.edu', '9821191454', 'B.Tech Civil Engineering', '2024', '2024-07-15', '126 Main Street, City', 'Civil Engineering', 1, 1, (SELECT id FROM users WHERE username = 'ce092'));

-- Civil Engineering Semester 1 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce093', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Kiaan Shetty', 'kiaan.shetty.ce093@student.college.edu', '9834983157', 'B.Tech Civil Engineering', '2024', '2024-07-15', '727 Main Street, City', 'Civil Engineering', 1, 0, (SELECT id FROM users WHERE username = 'ce093'));

-- Civil Engineering Semester 2 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce094', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Arjun Kamath', 'arjun.kamath.ce094@student.college.edu', '9839250205', 'B.Tech Civil Engineering', '2024', '2024-07-15', '695 Main Street, City', 'Civil Engineering', 2, 1, (SELECT id FROM users WHERE username = 'ce094'));

-- Civil Engineering Semester 2 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce095', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Manav Patel', 'manav.patel.ce095@student.college.edu', '9852307620', 'B.Tech Civil Engineering', '2024', '2024-07-15', '206 Main Street, City', 'Civil Engineering', 2, 0, (SELECT id FROM users WHERE username = 'ce095'));

-- Civil Engineering Semester 2 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce096', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Harsh Agarwal', 'harsh.agarwal.ce096@student.college.edu', '9897621266', 'B.Tech Civil Engineering', '2024', '2024-07-15', '937 Main Street, City', 'Civil Engineering', 2, 0, (SELECT id FROM users WHERE username = 'ce096'));

-- Civil Engineering Semester 2 Student 4
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce097', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Aayan Bhat', 'aayan.bhat.ce097@student.college.edu', '9811683091', 'B.Tech Civil Engineering', '2024', '2024-07-15', '680 Main Street, City', 'Civil Engineering', 2, 0, (SELECT id FROM users WHERE username = 'ce097'));

-- Civil Engineering Semester 3 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce098', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Shanaya Prabhu', 'shanaya.prabhu.ce098@student.college.edu', '9863653434', 'B.Tech Civil Engineering', '2023', '2023-07-15', '544 Main Street, City', 'Civil Engineering', 3, 1, (SELECT id FROM users WHERE username = 'ce098'));

-- Civil Engineering Semester 3 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce099', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Ananya Prabhu', 'ananya.prabhu.ce099@student.college.edu', '9817993470', 'B.Tech Civil Engineering', '2023', '2023-07-15', '500 Main Street, City', 'Civil Engineering', 3, 1, (SELECT id FROM users WHERE username = 'ce099'));

-- Civil Engineering Semester 3 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce100', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Krishna Agarwal', 'krishna.agarwal.ce100@student.college.edu', '9844132354', 'B.Tech Civil Engineering', '2023', '2023-07-15', '610 Main Street, City', 'Civil Engineering', 3, 1, (SELECT id FROM users WHERE username = 'ce100'));

-- Civil Engineering Semester 3 Student 4
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce101', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Shaurya Prabhu', 'shaurya.prabhu.ce101@student.college.edu', '9847327723', 'B.Tech Civil Engineering', '2023', '2023-07-15', '434 Main Street, City', 'Civil Engineering', 3, 0, (SELECT id FROM users WHERE username = 'ce101'));

-- Civil Engineering Semester 4 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce102', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Advait Murthy', 'advait.murthy.ce102@student.college.edu', '9843106954', 'B.Tech Civil Engineering', '2023', '2023-07-15', '545 Main Street, City', 'Civil Engineering', 4, 0, (SELECT id FROM users WHERE username = 'ce102'));

-- Civil Engineering Semester 4 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce103', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Ananya Singh', 'ananya.singh.ce103@student.college.edu', '9850812538', 'B.Tech Civil Engineering', '2023', '2023-07-15', '679 Main Street, City', 'Civil Engineering', 4, 1, (SELECT id FROM users WHERE username = 'ce103'));

-- Civil Engineering Semester 4 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce104', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Ananya Shah', 'ananya.shah.ce104@student.college.edu', '9860459843', 'B.Tech Civil Engineering', '2023', '2023-07-15', '272 Main Street, City', 'Civil Engineering', 4, 0, (SELECT id FROM users WHERE username = 'ce104'));

-- Civil Engineering Semester 4 Student 4
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce105', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Shanaya Prabhu', 'shanaya.prabhu.ce105@student.college.edu', '9880174004', 'B.Tech Civil Engineering', '2023', '2023-07-15', '821 Main Street, City', 'Civil Engineering', 4, 0, (SELECT id FROM users WHERE username = 'ce105'));

-- Civil Engineering Semester 5 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce106', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Kiaan Menon', 'kiaan.menon.ce106@student.college.edu', '9857891689', 'B.Tech Civil Engineering', '2022', '2022-07-15', '851 Main Street, City', 'Civil Engineering', 5, 1, (SELECT id FROM users WHERE username = 'ce106'));

-- Civil Engineering Semester 5 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce107', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Diya Patel', 'diya.patel.ce107@student.college.edu', '9886531345', 'B.Tech Civil Engineering', '2022', '2022-07-15', '117 Main Street, City', 'Civil Engineering', 5, 0, (SELECT id FROM users WHERE username = 'ce107'));

-- Civil Engineering Semester 5 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce108', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Pranav Shetty', 'pranav.shetty.ce108@student.college.edu', '9831467898', 'B.Tech Civil Engineering', '2022', '2022-07-15', '728 Main Street, City', 'Civil Engineering', 5, 1, (SELECT id FROM users WHERE username = 'ce108'));

-- Civil Engineering Semester 5 Student 4
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce109', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Vihaan Shetty', 'vihaan.shetty.ce109@student.college.edu', '9823497732', 'B.Tech Civil Engineering', '2022', '2022-07-15', '886 Main Street, City', 'Civil Engineering', 5, 1, (SELECT id FROM users WHERE username = 'ce109'));

-- Civil Engineering Semester 6 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce110', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Kabir Hegde', 'kabir.hegde.ce110@student.college.edu', '9814984366', 'B.Tech Civil Engineering', '2022', '2022-07-15', '539 Main Street, City', 'Civil Engineering', 6, 1, (SELECT id FROM users WHERE username = 'ce110'));

-- Civil Engineering Semester 6 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce111', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Reyansh Das', 'reyansh.das.ce111@student.college.edu', '9861574982', 'B.Tech Civil Engineering', '2022', '2022-07-15', '592 Main Street, City', 'Civil Engineering', 6, 0, (SELECT id FROM users WHERE username = 'ce111'));

-- Civil Engineering Semester 6 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce112', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Kiara Rao', 'kiara.rao.ce112@student.college.edu', '9838208160', 'B.Tech Civil Engineering', '2022', '2022-07-15', '390 Main Street, City', 'Civil Engineering', 6, 1, (SELECT id FROM users WHERE username = 'ce112'));

-- Civil Engineering Semester 6 Student 4
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce113', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Tara Singh', 'tara.singh.ce113@student.college.edu', '9873254969', 'B.Tech Civil Engineering', '2022', '2022-07-15', '502 Main Street, City', 'Civil Engineering', 6, 1, (SELECT id FROM users WHERE username = 'ce113'));

-- Civil Engineering Semester 7 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce114', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Advik Krishnan', 'advik.krishnan.ce114@student.college.edu', '9831440882', 'B.Tech Civil Engineering', '2021', '2021-07-15', '584 Main Street, City', 'Civil Engineering', 7, 1, (SELECT id FROM users WHERE username = 'ce114'));

-- Civil Engineering Semester 7 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce115', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Aayan Patel', 'aayan.patel.ce115@student.college.edu', '9827049945', 'B.Tech Civil Engineering', '2021', '2021-07-15', '459 Main Street, City', 'Civil Engineering', 7, 0, (SELECT id FROM users WHERE username = 'ce115'));

-- Civil Engineering Semester 7 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce116', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Dhruv Sharma', 'dhruv.sharma.ce116@student.college.edu', '9870210441', 'B.Tech Civil Engineering', '2021', '2021-07-15', '762 Main Street, City', 'Civil Engineering', 7, 1, (SELECT id FROM users WHERE username = 'ce116'));

-- Civil Engineering Semester 7 Student 4
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce117', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Ansh Pillai', 'ansh.pillai.ce117@student.college.edu', '9811193878', 'B.Tech Civil Engineering', '2021', '2021-07-15', '382 Main Street, City', 'Civil Engineering', 7, 1, (SELECT id FROM users WHERE username = 'ce117'));

-- Civil Engineering Semester 8 Student 1
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce118', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Tara Verma', 'tara.verma.ce118@student.college.edu', '9832600431', 'B.Tech Civil Engineering', '2021', '2021-07-15', '596 Main Street, City', 'Civil Engineering', 8, 0, (SELECT id FROM users WHERE username = 'ce118'));

-- Civil Engineering Semester 8 Student 2
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce119', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Angel Acharya', 'angel.acharya.ce119@student.college.edu', '9836981987', 'B.Tech Civil Engineering', '2021', '2021-07-15', '105 Main Street, City', 'Civil Engineering', 8, 1, (SELECT id FROM users WHERE username = 'ce119'));

-- Civil Engineering Semester 8 Student 3
INSERT IGNORE INTO users (username, password, role, role_id) VALUES ('ce120', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));
INSERT IGNORE INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES
('Reyansh Das', 'reyansh.das.ce120@student.college.edu', '9827523728', 'B.Tech Civil Engineering', '2021', '2021-07-15', '602 Main Street, City', 'Civil Engineering', 8, 1, (SELECT id FROM users WHERE username = 'ce120'));

-- Total students created: 120

-- =====================================================
-- COURSES DATA
-- =====================================================

INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Engineering Mathematics I', 'CS-MAT101', 4, 'Computer Science', 1, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Programming in C', 'CS101', 4, 'Computer Science', 1, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Engineering Physics', 'CS-PHY101', 3, 'Computer Science', 1, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('English Communication', 'CS-ENG101', 2, 'Computer Science', 1, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Engineering Mathematics II', 'CS-MAT102', 4, 'Computer Science', 2, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Data Structures', 'CS102', 4, 'Computer Science', 2, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Digital Electronics', 'CS103', 4, 'Computer Science', 2, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Engineering Graphics', 'CS104', 3, 'Computer Science', 2, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Discrete Mathematics', 'CS201', 4, 'Computer Science', 3, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Computer Organization', 'CS202', 4, 'Computer Science', 3, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Object Oriented Programming', 'CS203', 4, 'Computer Science', 3, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Database Management', 'CS204', 4, 'Computer Science', 3, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Design & Analysis of Algorithms', 'CS301', 4, 'Computer Science', 4, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Operating Systems', 'CS302', 4, 'Computer Science', 4, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Computer Networks', 'CS303', 4, 'Computer Science', 4, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Software Engineering', 'CS304', 3, 'Computer Science', 4, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Machine Learning', 'CS401', 4, 'Computer Science', 5, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Compiler Design', 'CS402', 4, 'Computer Science', 5, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Web Technologies', 'CS403', 3, 'Computer Science', 5, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Elective I', 'CS-E401', 3, 'Computer Science', 5, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Artificial Intelligence', 'CS501', 4, 'Computer Science', 6, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Cloud Computing', 'CS502', 3, 'Computer Science', 6, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Network Security', 'CS503', 3, 'Computer Science', 6, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Elective II', 'CS-E501', 3, 'Computer Science', 6, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Big Data Analytics', 'CS601', 3, 'Computer Science', 7, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('IoT', 'CS602', 3, 'Computer Science', 7, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Blockchain', 'CS603', 3, 'Computer Science', 7, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Major Project I', 'CS-PRJ701', 4, 'Computer Science', 7, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Capstone Project', 'CS-PRJ801', 8, 'Computer Science', 8, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Industry Internship', 'CS-INT801', 4, 'Computer Science', 8, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Seminar', 'CS-SEM801', 2, 'Computer Science', 8, (SELECT id FROM departments WHERE code = 'CS' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Engineering Mathematics I', 'EE-MAT101', 4, 'Electrical Engineering', 1, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Basic Electrical', 'EE101', 4, 'Electrical Engineering', 1, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Engineering Physics', 'EE-PHY101', 3, 'Electrical Engineering', 1, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('English Communication', 'EE-ENG101', 2, 'Electrical Engineering', 1, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Engineering Mathematics II', 'EE-MAT102', 4, 'Electrical Engineering', 2, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Circuit Theory', 'EE102', 4, 'Electrical Engineering', 2, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Electronic Devices', 'EE103', 4, 'Electrical Engineering', 2, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Engineering Mechanics', 'EE104', 3, 'Electrical Engineering', 2, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Network Analysis', 'EE201', 4, 'Electrical Engineering', 3, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Analog Electronics', 'EE202', 4, 'Electrical Engineering', 3, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Electromagnetic Theory', 'EE203', 4, 'Electrical Engineering', 3, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Signals & Systems', 'EE204', 4, 'Electrical Engineering', 3, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Power Systems I', 'EE301', 4, 'Electrical Engineering', 4, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Control Systems', 'EE302', 4, 'Electrical Engineering', 4, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Digital Electronics', 'EE303', 4, 'Electrical Engineering', 4, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Electrical Machines I', 'EE304', 4, 'Electrical Engineering', 4, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Power Systems II', 'EE401', 4, 'Electrical Engineering', 5, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Microprocessors', 'EE402', 4, 'Electrical Engineering', 5, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Power Electronics', 'EE403', 4, 'Electrical Engineering', 5, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Electrical Machines II', 'EE404', 4, 'Electrical Engineering', 5, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('High Voltage Engineering', 'EE501', 3, 'Electrical Engineering', 6, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Digital Signal Processing', 'EE502', 3, 'Electrical Engineering', 6, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Instrumentation', 'EE503', 3, 'Electrical Engineering', 6, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Elective I', 'EE-E501', 3, 'Electrical Engineering', 6, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Renewable Energy', 'EE601', 3, 'Electrical Engineering', 7, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Smart Grid', 'EE602', 3, 'Electrical Engineering', 7, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('VLSI Design', 'EE603', 3, 'Electrical Engineering', 7, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Major Project I', 'EE-PRJ701', 4, 'Electrical Engineering', 7, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Capstone Project', 'EE-PRJ801', 8, 'Electrical Engineering', 8, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Industry Internship', 'EE-INT801', 4, 'Electrical Engineering', 8, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Seminar', 'EE-SEM801', 2, 'Electrical Engineering', 8, (SELECT id FROM departments WHERE code = 'EE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Engineering Mathematics I', 'ME-MAT101', 4, 'Mechanical Engineering', 1, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Engineering Mechanics', 'ME101', 4, 'Mechanical Engineering', 1, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Engineering Physics', 'ME-PHY101', 3, 'Mechanical Engineering', 1, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('English Communication', 'ME-ENG101', 2, 'Mechanical Engineering', 1, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Engineering Mathematics II', 'ME-MAT102', 4, 'Mechanical Engineering', 2, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Thermodynamics', 'ME102', 4, 'Mechanical Engineering', 2, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Material Science', 'ME103', 4, 'Mechanical Engineering', 2, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Manufacturing Processes', 'ME104', 4, 'Mechanical Engineering', 2, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Fluid Mechanics', 'ME201', 4, 'Mechanical Engineering', 3, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Strength of Materials', 'ME202', 4, 'Mechanical Engineering', 3, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Kinematics of Machines', 'ME203', 4, 'Mechanical Engineering', 3, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Engineering Drawing', 'ME204', 3, 'Mechanical Engineering', 3, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Heat Transfer', 'ME301', 4, 'Mechanical Engineering', 4, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Dynamics of Machines', 'ME302', 4, 'Mechanical Engineering', 4, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Machine Design I', 'ME303', 4, 'Mechanical Engineering', 4, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Manufacturing Technology', 'ME304', 4, 'Mechanical Engineering', 4, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Automobile Engineering', 'ME401', 3, 'Mechanical Engineering', 5, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Machine Design II', 'ME402', 4, 'Mechanical Engineering', 5, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Metrology', 'ME403', 3, 'Mechanical Engineering', 5, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Industrial Engineering', 'ME404', 3, 'Mechanical Engineering', 5, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('CAD/CAM', 'ME501', 3, 'Mechanical Engineering', 6, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Mechatronics', 'ME502', 3, 'Mechanical Engineering', 6, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Finite Element Analysis', 'ME503', 3, 'Mechanical Engineering', 6, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Elective I', 'ME-E501', 3, 'Mechanical Engineering', 6, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Robotics', 'ME601', 3, 'Mechanical Engineering', 7, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Refrigeration & AC', 'ME602', 3, 'Mechanical Engineering', 7, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Advanced Manufacturing', 'ME603', 3, 'Mechanical Engineering', 7, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Major Project I', 'ME-PRJ701', 4, 'Mechanical Engineering', 7, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Capstone Project', 'ME-PRJ801', 8, 'Mechanical Engineering', 8, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Industry Internship', 'ME-INT801', 4, 'Mechanical Engineering', 8, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Seminar', 'ME-SEM801', 2, 'Mechanical Engineering', 8, (SELECT id FROM departments WHERE code = 'ME' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Engineering Mathematics I', 'CE-MAT101', 4, 'Civil Engineering', 1, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Engineering Mechanics', 'CE101', 4, 'Civil Engineering', 1, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Engineering Physics', 'CE-PHY101', 3, 'Civil Engineering', 1, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('English Communication', 'CE-ENG101', 2, 'Civil Engineering', 1, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Engineering Mathematics II', 'CE-MAT102', 4, 'Civil Engineering', 2, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Building Materials', 'CE102', 4, 'Civil Engineering', 2, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Surveying', 'CE103', 4, 'Civil Engineering', 2, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Fluid Mechanics', 'CE104', 4, 'Civil Engineering', 2, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Strength of Materials', 'CE201', 4, 'Civil Engineering', 3, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Structural Analysis I', 'CE202', 4, 'Civil Engineering', 3, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Geotechnical Engineering I', 'CE203', 4, 'Civil Engineering', 3, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Hydraulics', 'CE204', 4, 'Civil Engineering', 3, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Concrete Technology', 'CE301', 4, 'Civil Engineering', 4, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Structural Analysis II', 'CE302', 4, 'Civil Engineering', 4, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Geotechnical Engineering II', 'CE303', 4, 'Civil Engineering', 4, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Transportation Engineering I', 'CE304', 4, 'Civil Engineering', 4, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Design of RC Structures', 'CE401', 4, 'Civil Engineering', 5, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Steel Structures', 'CE402', 4, 'Civil Engineering', 5, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Foundation Engineering', 'CE403', 4, 'Civil Engineering', 5, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Water Resources Engineering', 'CE404', 3, 'Civil Engineering', 5, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Environmental Engineering', 'CE501', 3, 'Civil Engineering', 6, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Construction Management', 'CE502', 3, 'Civil Engineering', 6, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Earthquake Engineering', 'CE503', 3, 'Civil Engineering', 6, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Elective I', 'CE-E501', 3, 'Civil Engineering', 6, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Advanced Structural Design', 'CE601', 3, 'Civil Engineering', 7, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('GIS & Remote Sensing', 'CE602', 3, 'Civil Engineering', 7, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Project Management', 'CE603', 3, 'Civil Engineering', 7, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Major Project I', 'CE-PRJ701', 4, 'Civil Engineering', 7, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Capstone Project', 'CE-PRJ801', 8, 'Civil Engineering', 8, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Industry Internship', 'CE-INT801', 4, 'Civil Engineering', 8, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
INSERT IGNORE INTO courses (name, code, credits, department, semester, department_id) VALUES
('Seminar', 'CE-SEM801', 2, 'Civil Engineering', 8, (SELECT id FROM departments WHERE code = 'CE' LIMIT 1));
-- Total courses created: 125

-- =====================================================
-- TIMETABLE DATA
-- =====================================================

-- Timetable for Computer Science Semester 1
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 1, 'Monday', '08:00-09:00', 'Programming in C', 'Dr. Patel', '203');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 1, 'Monday', '09:00-10:00', 'Engineering Physics', 'Prof. Sharma', 'Lab-4');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 1, 'Monday', '10:00-11:00', 'Engineering Mathematics I', 'Dr. Reddy', 'Lab-3');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 1, 'Monday', '11:00-12:00', 'English Communication', 'Dr. Kumar', '202');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 1, 'Tuesday', '12:00-13:00', 'Engineering Physics', 'Dr. Patel', '201');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 1, 'Tuesday', '13:00-14:00', 'English Communication', 'Dr. Patel', '101');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 1, 'Tuesday', '14:00-15:00', 'Engineering Mathematics I', 'Dr. Reddy', '103');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 1, 'Tuesday', '08:00-09:00', 'Programming in C', 'Prof. Singh', '101');

-- Timetable for Computer Science Semester 2
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 2, 'Monday', '08:00-09:00', 'Engineering Mathematics II', 'Dr. Reddy', '201');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 2, 'Monday', '09:00-10:00', 'Digital Electronics', 'Dr. Patel', '201');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 2, 'Monday', '10:00-11:00', 'Data Structures', 'Prof. Singh', '105');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 2, 'Monday', '11:00-12:00', 'Engineering Graphics', 'Dr. Kumar', '205');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 2, 'Tuesday', '12:00-13:00', 'Engineering Mathematics II', 'Dr. Patel', '203');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 2, 'Tuesday', '13:00-14:00', 'Digital Electronics', 'Dr. Reddy', '204');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 2, 'Tuesday', '14:00-15:00', 'Data Structures', 'Dr. Reddy', '202');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 2, 'Tuesday', '08:00-09:00', 'Engineering Graphics', 'Dr. Patel', 'Lab-1');

-- Timetable for Computer Science Semester 3
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 3, 'Monday', '08:00-09:00', 'Object Oriented Programming', 'Dr. Patel', 'Lab-2');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 3, 'Monday', '09:00-10:00', 'Discrete Mathematics', 'Prof. Sharma', 'Lab-4');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 3, 'Monday', '10:00-11:00', 'Computer Organization', 'Dr. Patel', '202');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 3, 'Monday', '11:00-12:00', 'Database Management', 'Dr. Reddy', '105');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 3, 'Tuesday', '12:00-13:00', 'Database Management', 'Prof. Sharma', '102');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 3, 'Tuesday', '13:00-14:00', 'Object Oriented Programming', 'Prof. Sharma', '104');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 3, 'Tuesday', '14:00-15:00', 'Discrete Mathematics', 'Prof. Sharma', '201');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 3, 'Tuesday', '08:00-09:00', 'Computer Organization', 'Dr. Kumar', '204');

-- Timetable for Computer Science Semester 4
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 4, 'Monday', '08:00-09:00', 'Software Engineering', 'Dr. Reddy', '101');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 4, 'Monday', '09:00-10:00', 'Design & Analysis of Algorithms', 'Dr. Patel', '104');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 4, 'Monday', '10:00-11:00', 'Computer Networks', 'Dr. Kumar', '204');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 4, 'Monday', '11:00-12:00', 'Operating Systems', 'Dr. Reddy', '102');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 4, 'Tuesday', '12:00-13:00', 'Design & Analysis of Algorithms', 'Prof. Singh', '204');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 4, 'Tuesday', '13:00-14:00', 'Computer Networks', 'Prof. Sharma', '104');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 4, 'Tuesday', '14:00-15:00', 'Operating Systems', 'Prof. Sharma', 'Lab-3');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 4, 'Tuesday', '08:00-09:00', 'Software Engineering', 'Dr. Kumar', 'Lab-3');

-- Timetable for Computer Science Semester 5
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 5, 'Monday', '08:00-09:00', 'Web Technologies', 'Dr. Patel', '201');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 5, 'Monday', '09:00-10:00', 'Compiler Design', 'Dr. Reddy', '205');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 5, 'Monday', '10:00-11:00', 'Elective I', 'Prof. Singh', 'Lab-1');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 5, 'Monday', '11:00-12:00', 'Machine Learning', 'Dr. Kumar', '104');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 5, 'Tuesday', '12:00-13:00', 'Machine Learning', 'Dr. Kumar', 'Lab-1');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 5, 'Tuesday', '13:00-14:00', 'Web Technologies', 'Prof. Singh', '202');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 5, 'Tuesday', '14:00-15:00', 'Compiler Design', 'Dr. Patel', 'Lab-3');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 5, 'Tuesday', '08:00-09:00', 'Elective I', 'Dr. Reddy', '101');

-- Timetable for Computer Science Semester 6
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 6, 'Monday', '08:00-09:00', 'Elective II', 'Prof. Singh', '104');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 6, 'Monday', '09:00-10:00', 'Artificial Intelligence', 'Dr. Patel', 'Lab-2');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 6, 'Monday', '10:00-11:00', 'Network Security', 'Prof. Singh', '204');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 6, 'Monday', '11:00-12:00', 'Cloud Computing', 'Prof. Singh', '102');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 6, 'Tuesday', '12:00-13:00', 'Cloud Computing', 'Prof. Singh', '104');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 6, 'Tuesday', '13:00-14:00', 'Network Security', 'Dr. Patel', 'Lab-4');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 6, 'Tuesday', '14:00-15:00', 'Artificial Intelligence', 'Dr. Kumar', '202');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 6, 'Tuesday', '08:00-09:00', 'Elective II', 'Prof. Sharma', '104');

-- Timetable for Computer Science Semester 7
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 7, 'Monday', '08:00-09:00', 'IoT', 'Dr. Reddy', 'Lab-3');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 7, 'Monday', '09:00-10:00', 'Big Data Analytics', 'Prof. Sharma', '203');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 7, 'Monday', '10:00-11:00', 'Blockchain', 'Dr. Patel', '202');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 7, 'Monday', '11:00-12:00', 'Major Project I', 'Prof. Singh', '103');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 7, 'Tuesday', '12:00-13:00', 'Blockchain', 'Prof. Singh', '205');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 7, 'Tuesday', '13:00-14:00', 'Major Project I', 'Prof. Sharma', 'Lab-4');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 7, 'Tuesday', '14:00-15:00', 'Big Data Analytics', 'Dr. Kumar', '201');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 7, 'Tuesday', '08:00-09:00', 'IoT', 'Dr. Reddy', '102');

-- Timetable for Computer Science Semester 8
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 8, 'Monday', '08:00-09:00', 'Industry Internship', 'Prof. Sharma', 'Lab-3');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 8, 'Monday', '09:00-10:00', 'Capstone Project', 'Prof. Sharma', '105');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 8, 'Monday', '10:00-11:00', 'Seminar', 'Dr. Kumar', 'Lab-3');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 8, 'Tuesday', '11:00-12:00', 'Industry Internship', 'Prof. Singh', '203');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 8, 'Tuesday', '12:00-13:00', 'Seminar', 'Prof. Sharma', '102');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 8, 'Tuesday', '13:00-14:00', 'Capstone Project', 'Dr. Kumar', '202');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 8, 'Wednesday', '14:00-15:00', 'Industry Internship', 'Dr. Kumar', 'Lab-4');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Computer Science', 8, 'Wednesday', '08:00-09:00', 'Seminar', 'Dr. Kumar', '105');

-- Timetable for Electrical Engineering Semester 1
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 1, 'Monday', '08:00-09:00', 'English Communication', 'Prof. Sharma', '103');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 1, 'Monday', '09:00-10:00', 'Basic Electrical', 'Prof. Singh', '101');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 1, 'Monday', '10:00-11:00', 'Engineering Physics', 'Dr. Patel', 'Lab-2');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 1, 'Monday', '11:00-12:00', 'Engineering Mathematics I', 'Prof. Sharma', '202');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 1, 'Tuesday', '12:00-13:00', 'Basic Electrical', 'Dr. Patel', '103');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 1, 'Tuesday', '13:00-14:00', 'Engineering Physics', 'Prof. Singh', '204');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 1, 'Tuesday', '14:00-15:00', 'English Communication', 'Prof. Sharma', '101');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 1, 'Tuesday', '08:00-09:00', 'Engineering Mathematics I', 'Dr. Patel', '205');

-- Timetable for Electrical Engineering Semester 2
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 2, 'Monday', '08:00-09:00', 'Electronic Devices', 'Prof. Singh', 'Lab-2');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 2, 'Monday', '09:00-10:00', 'Engineering Mechanics', 'Prof. Singh', '102');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 2, 'Monday', '10:00-11:00', 'Circuit Theory', 'Dr. Kumar', '204');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 2, 'Monday', '11:00-12:00', 'Engineering Mathematics II', 'Dr. Kumar', 'Lab-2');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 2, 'Tuesday', '12:00-13:00', 'Circuit Theory', 'Dr. Kumar', '202');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 2, 'Tuesday', '13:00-14:00', 'Engineering Mathematics II', 'Dr. Patel', 'Lab-2');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 2, 'Tuesday', '14:00-15:00', 'Electronic Devices', 'Prof. Sharma', 'Lab-1');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 2, 'Tuesday', '08:00-09:00', 'Engineering Mechanics', 'Prof. Sharma', '101');

-- Timetable for Electrical Engineering Semester 3
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 3, 'Monday', '08:00-09:00', 'Signals & Systems', 'Prof. Singh', 'Lab-4');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 3, 'Monday', '09:00-10:00', 'Analog Electronics', 'Dr. Reddy', 'Lab-2');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 3, 'Monday', '10:00-11:00', 'Electromagnetic Theory', 'Prof. Sharma', 'Lab-3');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 3, 'Monday', '11:00-12:00', 'Network Analysis', 'Dr. Patel', '105');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 3, 'Tuesday', '12:00-13:00', 'Signals & Systems', 'Prof. Sharma', 'Lab-2');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 3, 'Tuesday', '13:00-14:00', 'Network Analysis', 'Prof. Sharma', '205');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 3, 'Tuesday', '14:00-15:00', 'Analog Electronics', 'Dr. Patel', '204');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 3, 'Tuesday', '08:00-09:00', 'Electromagnetic Theory', 'Dr. Kumar', '201');

-- Timetable for Electrical Engineering Semester 4
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 4, 'Monday', '08:00-09:00', 'Electrical Machines I', 'Dr. Reddy', '104');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 4, 'Monday', '09:00-10:00', 'Digital Electronics', 'Prof. Sharma', '201');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 4, 'Monday', '10:00-11:00', 'Control Systems', 'Dr. Reddy', '204');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 4, 'Monday', '11:00-12:00', 'Power Systems I', 'Dr. Patel', '102');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 4, 'Tuesday', '12:00-13:00', 'Electrical Machines I', 'Dr. Kumar', '205');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 4, 'Tuesday', '13:00-14:00', 'Power Systems I', 'Prof. Sharma', '101');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 4, 'Tuesday', '14:00-15:00', 'Control Systems', 'Dr. Kumar', '105');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 4, 'Tuesday', '08:00-09:00', 'Digital Electronics', 'Dr. Patel', 'Lab-2');

-- Timetable for Electrical Engineering Semester 5
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 5, 'Monday', '08:00-09:00', 'Power Systems II', 'Dr. Kumar', '104');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 5, 'Monday', '09:00-10:00', 'Microprocessors', 'Dr. Kumar', 'Lab-2');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 5, 'Monday', '10:00-11:00', 'Electrical Machines II', 'Dr. Patel', '104');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 5, 'Monday', '11:00-12:00', 'Power Electronics', 'Dr. Kumar', '102');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 5, 'Tuesday', '12:00-13:00', 'Electrical Machines II', 'Dr. Reddy', 'Lab-1');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 5, 'Tuesday', '13:00-14:00', 'Power Electronics', 'Dr. Patel', '103');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 5, 'Tuesday', '14:00-15:00', 'Microprocessors', 'Dr. Reddy', '205');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 5, 'Tuesday', '08:00-09:00', 'Power Systems II', 'Dr. Reddy', '105');

-- Timetable for Electrical Engineering Semester 6
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 6, 'Monday', '08:00-09:00', 'High Voltage Engineering', 'Dr. Kumar', '205');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 6, 'Monday', '09:00-10:00', 'Elective I', 'Dr. Kumar', '204');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 6, 'Monday', '10:00-11:00', 'Instrumentation', 'Dr. Kumar', 'Lab-4');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 6, 'Monday', '11:00-12:00', 'Digital Signal Processing', 'Dr. Patel', '204');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 6, 'Tuesday', '12:00-13:00', 'Elective I', 'Dr. Reddy', '102');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 6, 'Tuesday', '13:00-14:00', 'Instrumentation', 'Prof. Sharma', '203');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 6, 'Tuesday', '14:00-15:00', 'Digital Signal Processing', 'Dr. Reddy', '101');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 6, 'Tuesday', '08:00-09:00', 'High Voltage Engineering', 'Dr. Patel', 'Lab-3');

-- Timetable for Electrical Engineering Semester 7
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 7, 'Monday', '08:00-09:00', 'Major Project I', 'Prof. Sharma', '205');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 7, 'Monday', '09:00-10:00', 'Renewable Energy', 'Prof. Singh', '205');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 7, 'Monday', '10:00-11:00', 'Smart Grid', 'Prof. Singh', '204');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 7, 'Monday', '11:00-12:00', 'VLSI Design', 'Dr. Patel', '205');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 7, 'Tuesday', '12:00-13:00', 'VLSI Design', 'Prof. Sharma', '204');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 7, 'Tuesday', '13:00-14:00', 'Smart Grid', 'Dr. Kumar', '205');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 7, 'Tuesday', '14:00-15:00', 'Major Project I', 'Prof. Sharma', 'Lab-4');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 7, 'Tuesday', '08:00-09:00', 'Renewable Energy', 'Dr. Reddy', '203');

-- Timetable for Electrical Engineering Semester 8
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 8, 'Monday', '08:00-09:00', 'Capstone Project', 'Dr. Reddy', 'Lab-4');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 8, 'Monday', '09:00-10:00', 'Industry Internship', 'Prof. Singh', 'Lab-4');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 8, 'Monday', '10:00-11:00', 'Seminar', 'Prof. Sharma', '202');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 8, 'Tuesday', '11:00-12:00', 'Seminar', 'Dr. Patel', '201');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 8, 'Tuesday', '12:00-13:00', 'Industry Internship', 'Dr. Reddy', 'Lab-4');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 8, 'Tuesday', '13:00-14:00', 'Capstone Project', 'Dr. Kumar', '202');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 8, 'Wednesday', '14:00-15:00', 'Industry Internship', 'Dr. Reddy', 'Lab-4');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Electrical Engineering', 8, 'Wednesday', '08:00-09:00', 'Capstone Project', 'Dr. Kumar', '101');

-- Timetable for Mechanical Engineering Semester 1
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 1, 'Monday', '08:00-09:00', 'Engineering Physics', 'Prof. Sharma', 'Lab-3');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 1, 'Monday', '09:00-10:00', 'Engineering Mathematics I', 'Prof. Sharma', '205');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 1, 'Monday', '10:00-11:00', 'Engineering Mechanics', 'Dr. Patel', '103');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 1, 'Monday', '11:00-12:00', 'English Communication', 'Prof. Sharma', 'Lab-2');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 1, 'Tuesday', '12:00-13:00', 'Engineering Mathematics I', 'Prof. Singh', '201');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 1, 'Tuesday', '13:00-14:00', 'English Communication', 'Prof. Sharma', '201');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 1, 'Tuesday', '14:00-15:00', 'Engineering Physics', 'Dr. Patel', '201');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 1, 'Tuesday', '08:00-09:00', 'Engineering Mechanics', 'Prof. Sharma', 'Lab-3');

-- Timetable for Mechanical Engineering Semester 2
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 2, 'Monday', '08:00-09:00', 'Engineering Mathematics II', 'Prof. Sharma', '202');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 2, 'Monday', '09:00-10:00', 'Manufacturing Processes', 'Dr. Patel', '204');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 2, 'Monday', '10:00-11:00', 'Thermodynamics', 'Dr. Reddy', '204');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 2, 'Monday', '11:00-12:00', 'Material Science', 'Dr. Reddy', '203');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 2, 'Tuesday', '12:00-13:00', 'Material Science', 'Dr. Kumar', '101');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 2, 'Tuesday', '13:00-14:00', 'Thermodynamics', 'Prof. Singh', 'Lab-2');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 2, 'Tuesday', '14:00-15:00', 'Manufacturing Processes', 'Prof. Singh', 'Lab-4');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 2, 'Tuesday', '08:00-09:00', 'Engineering Mathematics II', 'Dr. Patel', 'Lab-4');

-- Timetable for Mechanical Engineering Semester 3
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 3, 'Monday', '08:00-09:00', 'Engineering Drawing', 'Dr. Reddy', '202');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 3, 'Monday', '09:00-10:00', 'Fluid Mechanics', 'Prof. Singh', '203');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 3, 'Monday', '10:00-11:00', 'Kinematics of Machines', 'Dr. Patel', 'Lab-1');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 3, 'Monday', '11:00-12:00', 'Strength of Materials', 'Dr. Reddy', '205');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 3, 'Tuesday', '12:00-13:00', 'Fluid Mechanics', 'Dr. Reddy', '202');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 3, 'Tuesday', '13:00-14:00', 'Strength of Materials', 'Dr. Kumar', 'Lab-3');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 3, 'Tuesday', '14:00-15:00', 'Kinematics of Machines', 'Prof. Singh', '101');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 3, 'Tuesday', '08:00-09:00', 'Engineering Drawing', 'Dr. Patel', '204');

-- Timetable for Mechanical Engineering Semester 4
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 4, 'Monday', '08:00-09:00', 'Manufacturing Technology', 'Prof. Singh', 'Lab-4');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 4, 'Monday', '09:00-10:00', 'Heat Transfer', 'Prof. Sharma', '201');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 4, 'Monday', '10:00-11:00', 'Machine Design I', 'Dr. Patel', '103');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 4, 'Monday', '11:00-12:00', 'Dynamics of Machines', 'Dr. Reddy', '204');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 4, 'Tuesday', '12:00-13:00', 'Machine Design I', 'Prof. Singh', '104');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 4, 'Tuesday', '13:00-14:00', 'Heat Transfer', 'Prof. Singh', 'Lab-3');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 4, 'Tuesday', '14:00-15:00', 'Manufacturing Technology', 'Dr. Kumar', 'Lab-4');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 4, 'Tuesday', '08:00-09:00', 'Dynamics of Machines', 'Prof. Sharma', 'Lab-3');

-- Timetable for Mechanical Engineering Semester 5
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 5, 'Monday', '08:00-09:00', 'Industrial Engineering', 'Dr. Reddy', '103');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 5, 'Monday', '09:00-10:00', 'Metrology', 'Dr. Kumar', 'Lab-4');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 5, 'Monday', '10:00-11:00', 'Automobile Engineering', 'Prof. Singh', '201');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 5, 'Monday', '11:00-12:00', 'Machine Design II', 'Dr. Patel', 'Lab-3');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 5, 'Tuesday', '12:00-13:00', 'Metrology', 'Prof. Singh', 'Lab-3');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 5, 'Tuesday', '13:00-14:00', 'Industrial Engineering', 'Prof. Sharma', 'Lab-2');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 5, 'Tuesday', '14:00-15:00', 'Automobile Engineering', 'Prof. Singh', '102');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 5, 'Tuesday', '08:00-09:00', 'Machine Design II', 'Prof. Sharma', 'Lab-1');

-- Timetable for Mechanical Engineering Semester 6
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 6, 'Monday', '08:00-09:00', 'Elective I', 'Prof. Sharma', 'Lab-3');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 6, 'Monday', '09:00-10:00', 'Finite Element Analysis', 'Prof. Sharma', 'Lab-4');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 6, 'Monday', '10:00-11:00', 'CAD/CAM', 'Dr. Patel', '104');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 6, 'Monday', '11:00-12:00', 'Mechatronics', 'Prof. Sharma', 'Lab-4');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 6, 'Tuesday', '12:00-13:00', 'CAD/CAM', 'Dr. Patel', 'Lab-1');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 6, 'Tuesday', '13:00-14:00', 'Elective I', 'Dr. Kumar', '101');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 6, 'Tuesday', '14:00-15:00', 'Finite Element Analysis', 'Prof. Singh', '204');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 6, 'Tuesday', '08:00-09:00', 'Mechatronics', 'Dr. Kumar', '104');

-- Timetable for Mechanical Engineering Semester 7
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 7, 'Monday', '08:00-09:00', 'Refrigeration & AC', 'Dr. Kumar', '203');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 7, 'Monday', '09:00-10:00', 'Advanced Manufacturing', 'Prof. Singh', 'Lab-2');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 7, 'Monday', '10:00-11:00', 'Robotics', 'Prof. Singh', '202');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 7, 'Monday', '11:00-12:00', 'Major Project I', 'Dr. Reddy', '202');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 7, 'Tuesday', '12:00-13:00', 'Robotics', 'Prof. Sharma', 'Lab-1');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 7, 'Tuesday', '13:00-14:00', 'Major Project I', 'Prof. Sharma', 'Lab-3');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 7, 'Tuesday', '14:00-15:00', 'Advanced Manufacturing', 'Prof. Sharma', '205');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 7, 'Tuesday', '08:00-09:00', 'Refrigeration & AC', 'Dr. Reddy', '103');

-- Timetable for Mechanical Engineering Semester 8
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 8, 'Monday', '08:00-09:00', 'Capstone Project', 'Dr. Kumar', '103');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 8, 'Monday', '09:00-10:00', 'Seminar', 'Dr. Patel', 'Lab-4');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 8, 'Monday', '10:00-11:00', 'Industry Internship', 'Prof. Sharma', 'Lab-2');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 8, 'Tuesday', '11:00-12:00', 'Industry Internship', 'Prof. Sharma', '201');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 8, 'Tuesday', '12:00-13:00', 'Capstone Project', 'Dr. Reddy', '202');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 8, 'Tuesday', '13:00-14:00', 'Seminar', 'Dr. Patel', '103');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 8, 'Wednesday', '14:00-15:00', 'Capstone Project', 'Dr. Kumar', '104');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Mechanical Engineering', 8, 'Wednesday', '08:00-09:00', 'Seminar', 'Prof. Singh', 'Lab-2');

-- Timetable for Civil Engineering Semester 1
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 1, 'Monday', '08:00-09:00', 'Engineering Mathematics I', 'Prof. Singh', '102');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 1, 'Monday', '09:00-10:00', 'Engineering Physics', 'Prof. Sharma', '205');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 1, 'Monday', '10:00-11:00', 'English Communication', 'Dr. Reddy', '101');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 1, 'Monday', '11:00-12:00', 'Engineering Mechanics', 'Dr. Patel', 'Lab-4');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 1, 'Tuesday', '12:00-13:00', 'Engineering Physics', 'Dr. Patel', '103');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 1, 'Tuesday', '13:00-14:00', 'Engineering Mechanics', 'Prof. Sharma', '103');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 1, 'Tuesday', '14:00-15:00', 'Engineering Mathematics I', 'Dr. Patel', 'Lab-3');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 1, 'Tuesday', '08:00-09:00', 'English Communication', 'Prof. Singh', '103');

-- Timetable for Civil Engineering Semester 2
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 2, 'Monday', '08:00-09:00', 'Fluid Mechanics', 'Dr. Reddy', 'Lab-3');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 2, 'Monday', '09:00-10:00', 'Surveying', 'Prof. Sharma', '104');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 2, 'Monday', '10:00-11:00', 'Building Materials', 'Prof. Singh', '202');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 2, 'Monday', '11:00-12:00', 'Engineering Mathematics II', 'Dr. Kumar', 'Lab-3');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 2, 'Tuesday', '12:00-13:00', 'Engineering Mathematics II', 'Prof. Singh', '204');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 2, 'Tuesday', '13:00-14:00', 'Fluid Mechanics', 'Dr. Kumar', '102');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 2, 'Tuesday', '14:00-15:00', 'Building Materials', 'Prof. Singh', '101');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 2, 'Tuesday', '08:00-09:00', 'Surveying', 'Prof. Sharma', '202');

-- Timetable for Civil Engineering Semester 3
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 3, 'Monday', '08:00-09:00', 'Hydraulics', 'Dr. Patel', '204');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 3, 'Monday', '09:00-10:00', 'Structural Analysis I', 'Dr. Patel', 'Lab-1');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 3, 'Monday', '10:00-11:00', 'Geotechnical Engineering I', 'Prof. Singh', '105');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 3, 'Monday', '11:00-12:00', 'Strength of Materials', 'Prof. Sharma', '203');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 3, 'Tuesday', '12:00-13:00', 'Structural Analysis I', 'Prof. Singh', '205');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 3, 'Tuesday', '13:00-14:00', 'Hydraulics', 'Dr. Reddy', '201');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 3, 'Tuesday', '14:00-15:00', 'Geotechnical Engineering I', 'Prof. Singh', 'Lab-4');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 3, 'Tuesday', '08:00-09:00', 'Strength of Materials', 'Dr. Patel', '101');

-- Timetable for Civil Engineering Semester 4
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 4, 'Monday', '08:00-09:00', 'Structural Analysis II', 'Dr. Patel', '103');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 4, 'Monday', '09:00-10:00', 'Geotechnical Engineering II', 'Dr. Patel', '101');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 4, 'Monday', '10:00-11:00', 'Transportation Engineering I', 'Dr. Kumar', '203');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 4, 'Monday', '11:00-12:00', 'Concrete Technology', 'Dr. Patel', '102');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 4, 'Tuesday', '12:00-13:00', 'Structural Analysis II', 'Prof. Sharma', '103');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 4, 'Tuesday', '13:00-14:00', 'Geotechnical Engineering II', 'Dr. Patel', 'Lab-4');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 4, 'Tuesday', '14:00-15:00', 'Concrete Technology', 'Dr. Kumar', '201');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 4, 'Tuesday', '08:00-09:00', 'Transportation Engineering I', 'Dr. Reddy', '204');

-- Timetable for Civil Engineering Semester 5
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 5, 'Monday', '08:00-09:00', 'Foundation Engineering', 'Dr. Patel', '203');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 5, 'Monday', '09:00-10:00', 'Steel Structures', 'Prof. Sharma', 'Lab-2');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 5, 'Monday', '10:00-11:00', 'Design of RC Structures', 'Dr. Reddy', '202');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 5, 'Monday', '11:00-12:00', 'Water Resources Engineering', 'Prof. Sharma', 'Lab-4');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 5, 'Tuesday', '12:00-13:00', 'Steel Structures', 'Dr. Kumar', '204');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 5, 'Tuesday', '13:00-14:00', 'Foundation Engineering', 'Dr. Reddy', 'Lab-3');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 5, 'Tuesday', '14:00-15:00', 'Water Resources Engineering', 'Prof. Singh', '104');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 5, 'Tuesday', '08:00-09:00', 'Design of RC Structures', 'Dr. Patel', '102');

-- Timetable for Civil Engineering Semester 6
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 6, 'Monday', '08:00-09:00', 'Environmental Engineering', 'Prof. Sharma', 'Lab-1');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 6, 'Monday', '09:00-10:00', 'Elective I', 'Prof. Singh', 'Lab-3');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 6, 'Monday', '10:00-11:00', 'Earthquake Engineering', 'Prof. Sharma', 'Lab-3');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 6, 'Monday', '11:00-12:00', 'Construction Management', 'Prof. Sharma', 'Lab-4');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 6, 'Tuesday', '12:00-13:00', 'Earthquake Engineering', 'Prof. Singh', '102');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 6, 'Tuesday', '13:00-14:00', 'Construction Management', 'Prof. Singh', '205');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 6, 'Tuesday', '14:00-15:00', 'Elective I', 'Dr. Kumar', 'Lab-3');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 6, 'Tuesday', '08:00-09:00', 'Environmental Engineering', 'Prof. Sharma', 'Lab-1');

-- Timetable for Civil Engineering Semester 7
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 7, 'Monday', '08:00-09:00', 'Advanced Structural Design', 'Prof. Sharma', '104');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 7, 'Monday', '09:00-10:00', 'Major Project I', 'Prof. Sharma', '201');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 7, 'Monday', '10:00-11:00', 'Project Management', 'Dr. Reddy', '104');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 7, 'Monday', '11:00-12:00', 'GIS & Remote Sensing', 'Prof. Singh', '203');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 7, 'Tuesday', '12:00-13:00', 'Project Management', 'Dr. Kumar', '102');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 7, 'Tuesday', '13:00-14:00', 'Advanced Structural Design', 'Prof. Sharma', '205');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 7, 'Tuesday', '14:00-15:00', 'Major Project I', 'Prof. Sharma', '103');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 7, 'Tuesday', '08:00-09:00', 'GIS & Remote Sensing', 'Dr. Patel', '104');

-- Timetable for Civil Engineering Semester 8
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 8, 'Monday', '08:00-09:00', 'Capstone Project', 'Prof. Sharma', 'Lab-2');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 8, 'Monday', '09:00-10:00', 'Industry Internship', 'Prof. Sharma', 'Lab-1');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 8, 'Monday', '10:00-11:00', 'Seminar', 'Dr. Reddy', '201');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 8, 'Tuesday', '11:00-12:00', 'Seminar', 'Dr. Kumar', '203');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 8, 'Tuesday', '12:00-13:00', 'Capstone Project', 'Prof. Singh', '105');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 8, 'Tuesday', '13:00-14:00', 'Industry Internship', 'Dr. Patel', 'Lab-1');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 8, 'Wednesday', '14:00-15:00', 'Capstone Project', 'Dr. Reddy', 'Lab-1');
INSERT IGNORE INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES
('Civil Engineering', 8, 'Wednesday', '08:00-09:00', 'Seminar', 'Prof. Singh', '101');

-- =====================================================
-- FEES DATA
-- =====================================================

-- Assign fees to all students
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs001')), 80000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs002')), 80000.00, 80000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs003')), 80000.00, 53186.72, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs004')), 85000.00, 49306.78, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs005')), 85000.00, 85000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs006')), 85000.00, 85000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs007')), 85000.00, 27099.51, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs008')), 90000.00, 33928.21, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs009')), 90000.00, 90000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs010')), 90000.00, 90000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs011')), 90000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs012')), 95000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs013')), 95000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs014')), 95000.00, 95000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs015')), 95000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs016')), 100000.00, 42677.29, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs017')), 100000.00, 33140.50, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs018')), 100000.00, 100000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs019')), 100000.00, 100000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs020')), 105000.00, 105000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs021')), 105000.00, 67070.72, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs022')), 105000.00, 105000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs023')), 105000.00, 39533.85, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs024')), 110000.00, 110000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs025')), 110000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs026')), 110000.00, 110000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs027')), 110000.00, 110000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs028')), 115000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs029')), 115000.00, 115000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'cs030')), 115000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee031')), 80000.00, 80000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee032')), 80000.00, 80000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee033')), 80000.00, 26495.52, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee034')), 85000.00, 47311.58, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee035')), 85000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee036')), 85000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee037')), 85000.00, 53809.96, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee038')), 90000.00, 90000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee039')), 90000.00, 49708.98, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee040')), 90000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee041')), 90000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee042')), 95000.00, 95000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee043')), 95000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee044')), 95000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee045')), 95000.00, 95000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee046')), 100000.00, 100000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee047')), 100000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee048')), 100000.00, 100000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee049')), 100000.00, 100000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee050')), 105000.00, 105000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee051')), 105000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee052')), 105000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee053')), 105000.00, 105000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee054')), 110000.00, 110000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee055')), 110000.00, 58589.18, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee056')), 110000.00, 75613.46, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee057')), 110000.00, 110000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee058')), 115000.00, 115000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee059')), 115000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ee060')), 115000.00, 115000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me061')), 80000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me062')), 80000.00, 80000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me063')), 80000.00, 24919.77, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me064')), 85000.00, 85000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me065')), 85000.00, 45513.93, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me066')), 85000.00, 45727.17, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me067')), 85000.00, 85000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me068')), 90000.00, 90000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me069')), 90000.00, 90000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me070')), 90000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me071')), 90000.00, 90000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me072')), 95000.00, 95000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me073')), 95000.00, 63779.92, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me074')), 95000.00, 95000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me075')), 95000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me076')), 100000.00, 100000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me077')), 100000.00, 100000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me078')), 100000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me079')), 100000.00, 100000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me080')), 105000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me081')), 105000.00, 105000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me082')), 105000.00, 44386.64, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me083')), 105000.00, 105000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me084')), 110000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me085')), 110000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me086')), 110000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me087')), 110000.00, 76831.82, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me088')), 115000.00, 115000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me089')), 115000.00, 115000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'me090')), 115000.00, 115000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce091')), 80000.00, 41785.02, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce092')), 80000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce093')), 80000.00, 80000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce094')), 85000.00, 85000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce095')), 85000.00, 57038.56, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce096')), 85000.00, 37763.50, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce097')), 85000.00, 50063.35, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce098')), 90000.00, 90000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce099')), 90000.00, 37888.14, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce100')), 90000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce101')), 90000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce102')), 95000.00, 95000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce103')), 95000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce104')), 95000.00, 95000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce105')), 95000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce106')), 100000.00, 100000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce107')), 100000.00, 65643.25, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce108')), 100000.00, 100000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce109')), 100000.00, 100000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce110')), 105000.00, 44967.39, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce111')), 105000.00, 62201.32, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce112')), 105000.00, 49054.42, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce113')), 105000.00, 105000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce114')), 110000.00, 110000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce115')), 110000.00, 110000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce116')), 110000.00, 110000.00, '2026-01-31', '2025-12-15', 'PAID');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce117')), 110000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce118')), 115000.00, 60346.03, '2026-01-31', NULL, 'PARTIAL');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce119')), 115000.00, 0.00, '2026-01-31', NULL, 'PENDING');
INSERT IGNORE INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES
((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = 'ce120')), 115000.00, 115000.00, '2026-01-31', '2025-12-15', 'PAID');

-- =====================================================
-- SAMPLE DATA GENERATION COMPLETE!
-- =====================================================
-- Students: 120
-- Courses: 125
-- Faculty: 30 (already in previous SQL)
-- Timetables: Generated for all semesters
-- Fees: Allocated to all students
-- =====================================================
