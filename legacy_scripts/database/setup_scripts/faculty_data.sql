-- Add 30 Faculty Members with proper roles
-- Password for all faculty: faculty123
-- Hash: 8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918

USE college_management;

-- Create faculty user accounts
INSERT IGNORE INTO users (username, password, role, role_id) VALUES
-- Computer Science Faculty
('dr.amit.sharma', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'HOD' LIMIT 1)),
('prof.priya.kumar', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'PROFESSOR' LIMIT 1)),
('dr.rajesh.singh', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'ASSOCIATE_PROFESSOR' LIMIT 1)),
('prof.neha.patel', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'ASSISTANT_PROFESSOR' LIMIT 1)),
('mr.vikram.mehta', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'LECTURER' LIMIT 1)),
('ms.anita.verma', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'LECTURER' LIMIT 1)),
('mr.suresh.reddy', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'LAB_ASSISTANT' LIMIT 1)),

-- Electrical Engineering Faculty
('dr.lakshmi.nair', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'HOD' LIMIT 1)),
('prof.arun.iyer', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'PROFESSOR' LIMIT 1)),
('dr.meena.das', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'ASSOCIATE_PROFESSOR' LIMIT 1)),
('prof.kiran.joshi', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'ASSISTANT_PROFESSOR' LIMIT 1)),
('mr.deepak.rao', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'LECTURER' LIMIT 1)),
('ms.pooja.desai', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'LECTURER' LIMIT 1)),

-- Mechanical Engineering Faculty
('dr.mohan.pillai', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'HOD' LIMIT 1)),
('prof.sanjay.gupta', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'PROFESSOR' LIMIT 1)),
('dr.kavita.shah', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'ASSOCIATE_PROFESSOR' LIMIT 1)),
('prof.rahul.menon', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'ASSISTANT_PROFESSOR' LIMIT 1)),
('mr.ashok.kumar', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'LECTURER' LIMIT 1)),
('ms.divya.nambiar', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'LECTURER' LIMIT 1)),

-- Civil Engineering Faculty
('dr.ramesh.bhat', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'HOD' LIMIT 1)),
('prof.sunita.rao', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'PROFESSOR' LIMIT 1)),
('dr.vinod.nair', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'ASSOCIATE_PROFESSOR' LIMIT 1)),
('prof.madhavi.prabhu', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'ASSISTANT_PROFESSOR' LIMIT 1)),
('mr.ganesh.hegde', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'LECTURER' LIMIT 1)),
('ms.sneha.kamath', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'LECTURER' LIMIT 1)),

-- General Studies Faculty
('dr.prakash.shetty', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'PROFESSOR' LIMIT 1)),
('prof.uma.krishnan', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'ASSOCIATE_PROFESSOR' LIMIT 1)),
('mr.ravi.acharya', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'LECTURER' LIMIT 1)),
('ms.latha.menon', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'FACULTY', (SELECT id FROM roles WHERE code = 'LECTURER' LIMIT 1)),

-- Hostel Wardens
('mr.krishna.murthy', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'WARDEN', (SELECT id FROM roles WHERE code = 'WARDEN' LIMIT 1));

-- Insert faculty details
INSERT IGNORE INTO faculty (name, email, phone, department, qualification, join_date, user_id) VALUES
-- Computer Science (7 faculty)
('Dr. Amit Sharma', 'amit.sharma@college.edu', '9876543210', 'Computer Science', 'Ph.D. in Computer Science', '2015-07-01', (SELECT id FROM users WHERE username = 'dr.amit.sharma')),
('Prof. Priya Kumar', 'priya.kumar@college.edu', '9876543211', 'Computer Science', 'Ph.D. in AI/ML', '2016-08-15', (SELECT id FROM users WHERE username = 'prof.priya.kumar')),
('Dr. Rajesh Singh', 'rajesh.singh@college.edu', '9876543212', 'Computer Science', 'Ph.D. in Data Science', '2017-06-01', (SELECT id FROM users WHERE username = 'dr.rajesh.singh')),
('Prof. Neha Patel', 'neha.patel@college.edu', '9876543213', 'Computer Science', 'M.Tech in CSE', '2018-07-15', (SELECT id FROM users WHERE username = 'prof.neha.patel')),
('Mr. Vikram Mehta', 'vikram.mehta@college.edu', '9876543214', 'Computer Science', 'M.Tech in CSE', '2019-08-01', (SELECT id FROM users WHERE username = 'mr.vikram.mehta')),
('Ms. Anita Verma', 'anita.verma@college.edu', '9876543215', 'Computer Science', 'M.Tech in Software Engineering', '2020-07-01', (SELECT id FROM users WHERE username = 'ms.anita.verma')),
('Mr. Suresh Reddy', 'suresh.reddy@college.edu', '9876543216', 'Computer Science', 'M.Sc in Computer Science', '2021-06-15', (SELECT id FROM users WHERE username = 'mr.suresh.reddy')),

-- Electrical Engineering (6 faculty)
('Dr. Lakshmi Nair', 'lakshmi.nair@college.edu', '9876543220', 'Electrical Engineering', 'Ph.D. in Electrical Engineering', '2014-08-01', (SELECT id FROM users WHERE username = 'dr.lakshmi.nair')),
('Prof. Arun Iyer', 'arun.iyer@college.edu', '9876543221', 'Electrical Engineering', 'Ph.D. in Power Systems', '2016-07-01', (SELECT id FROM users WHERE username = 'prof.arun.iyer')),
('Dr. Meena Das', 'meena.das@college.edu', '9876543222', 'Electrical Engineering', 'Ph.D. in Control Systems', '2017-08-15', (SELECT id FROM users WHERE username = 'dr.meena.das')),
('Prof. Kiran Joshi', 'kiran.joshi@college.edu', '9876543223', 'Electrical Engineering', 'M.Tech in EEE', '2018-06-01', (SELECT id FROM users WHERE username = 'prof.kiran.joshi')),
('Mr. Deepak Rao', 'deepak.rao@college.edu', '9876543224', 'Electrical Engineering', 'M.Tech in Electronics', '2019-07-15', (SELECT id FROM users WHERE username = 'mr.deepak.rao')),
('Ms. Pooja Desai', 'pooja.desai@college.edu', '9876543225', 'Electrical Engineering', 'M.Tech in EEE', '2020-08-01', (SELECT id FROM users WHERE username = 'ms.pooja.desai')),

-- Mechanical Engineering (6 faculty)
('Dr. Mohan Pillai', 'mohan.pillai@college.edu', '9876543230', 'Mechanical Engineering', 'Ph.D. in Mechanical Engineering', '2013-07-01', (SELECT id FROM users WHERE username = 'dr.mohan.pillai')),
('Prof. Sanjay Gupta', 'sanjay.gupta@college.edu', '9876543231', 'Mechanical Engineering', 'Ph.D. in Thermal Engineering', '2015-08-15', (SELECT id FROM users WHERE username = 'prof.sanjay.gupta')),
('Dr. Kavita Shah', 'chavita.shah@college.edu', '9876543232', 'Mechanical Engineering', 'Ph.D. in Manufacturing', '2016-06-01', (SELECT id FROM users WHERE username = 'dr.kavita.shah')),
('Prof. Rahul Menon', 'rahul.menon@college.edu', '9876543233', 'Mechanical Engineering', 'M.Tech in Mechanical Engineering', '2018-07-01', (SELECT id FROM users WHERE username = 'prof.rahul.menon')),
('Mr. Ashok Kumar', 'ashok.kumar@college.edu', '9876543234', 'Mechanical Engineering', 'M.Tech in Production', '2019-08-15', (SELECT id FROM users WHERE username = 'mr.ashok.kumar')),
('Ms. Divya Nambiar', 'divya.nambiar@college.edu', '9876543235', 'Mechanical Engineering', 'M.Tech in Mechanical Engineering', '2020-06-01', (SELECT id FROM users WHERE username = 'ms.divya.nambiar')),

-- Civil Engineering (6 faculty)
('Dr. Ramesh Bhat', 'ramesh.bhat@college.edu', '9876543240', 'Civil Engineering', 'Ph.D. in Civil Engineering', '2014-07-15', (SELECT id FROM users WHERE username = 'dr.ramesh.bhat')),
('Prof. Sunita Rao', 'sunita.rao@college.edu', '9876543241', 'Civil Engineering', 'Ph.D. in Structural Engineering', '2015-06-01', (SELECT id FROM users WHERE username = 'prof.sunita.rao')),
('Dr. Vinod Nair', 'vinod.nair@college.edu', '9876543242', 'Civil Engineering', 'Ph.D. in Geotechnical Engineering', '2017-07-15', (SELECT id FROM users WHERE username = 'dr.vinod.nair')),
('Prof. Madhavi Prabhu', 'madhavi.prabhu@college.edu', '9876543243', 'Civil Engineering', 'M.Tech in Civil Engineering', '2018-08-01', (SELECT id FROM users WHERE username = 'prof.madhavi.prabhu')),
('Mr. Ganesh Hegde', 'ganesh.hegde@college.edu', '9876543244', 'Civil Engineering', 'M.Tech in Construction Management', '2019-06-15', (SELECT id FROM users WHERE username = 'mr.ganesh.hegde')),
('Ms. Sneha Kamath', 'sneha.kamath@college.edu', '9876543245', 'Civil Engineering', 'M.Tech in Civil Engineering', '2020-07-01', (SELECT id FROM users WHERE username = 'ms.sneha.kamath')),

-- General Studies (4 faculty)
('Dr. Prakash Shetty', 'prakash.shetty@college.edu', '9876543250', 'General Studies', 'Ph.D. in Mathematics', '2012-08-01', (SELECT id FROM users WHERE username = 'dr.prakash.shetty')),
('Prof. Uma Krishnan', 'uma.krishnan@college.edu', '9876543251', 'General Studies', 'Ph.D. in Physics', '2014-06-15', (SELECT id FROM users WHERE username = 'prof.uma.krishnan')),
('Mr. Ravi Acharya', 'ravi.acharya@college.edu', '9876543252', 'General Studies', 'M.A. in English', '2017-07-01', (SELECT id FROM users WHERE username = 'mr.ravi.acharya')),
('Ms. Latha Menon', 'latha.menon@college.edu', '9876543253', 'General Studies', 'M.Sc in Chemistry', '2018-08-15', (SELECT id FROM users WHERE username = 'ms.latha.menon'));

-- Insert Warden
INSERT IGNORE INTO wardens (name, email, phone, user_id) VALUES
('Mr. Krishna Murthy', 'krishna.murthy@college.edu', '9876543260', (SELECT id FROM users WHERE username = 'mr.krishna.murthy'));

SELECT 'Faculty data loaded successfully!' AS Status;
