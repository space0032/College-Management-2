-- Complete Student Profiles for All 100 Students
-- This adds the missing 92 student profiles to the students table

USE college_management;

-- CSE Students (40 total - 8 already exist)
INSERT INTO students (user_id, name, email, phone, course, batch, enrollment_date, department, semester) VALUES
((SELECT id FROM users WHERE username = 'CS22003'), 'Aadhya Singh', 'cs22003@student.edu', '+91-9100000003', 'B.Tech CSE', '2022', '2022-08-01', 'CSE', 2),
((SELECT id FROM users WHERE username = 'CS22004'), 'Vihaan Gupta', 'cs22004@student.edu', '+91-9100000004', 'B.Tech CSE', '2022', '2022-08-01', 'CSE', 2),
((SELECT id FROM users WHERE username = 'CS22005'), 'Anaya Verma', 'cs22005@student.edu', '+91-9100000005', 'B.Tech CSE', '2022', '2022-08-01', 'CSE', 2),
((SELECT id FROM users WHERE username = 'CS22006'), 'Aryan Joshi', 'cs22006@student.edu', '+91-9100000006', 'B.Tech CSE', '2022', '2022-08-01', 'CSE', 2),
((SELECT id FROM users WHERE username = 'CS22007'), 'Diya Nair', 'cs22007@student.edu', '+91-9100000007', 'B.Tech CSE', '2022', '2022-08-01', 'CSE', 2),
((SELECT id FROM users WHERE username = 'CS22008'), 'Ishaan Pillai', 'cs22008@student.edu', '+91-9100000008', 'B.Tech CSE', '2022', '2022-08-01', 'CSE', 2),
((SELECT id FROM users WHERE username = 'CS22009'), 'Myra Shah', 'cs22009@student.edu', '+91-9100000009', 'B.Tech CSE', '2022', '2022-08-01', 'CSE', 2),
((SELECT id FROM users WHERE username = 'CS22010'), 'Ayaan Kapoor', 'cs22010@student.edu', '+91-9100000010', 'B.Tech CSE', '2022', '2022-08-01', 'CSE', 2),
((SELECT id FROM users WHERE username = 'CS21003'), 'Aarohi Desai', 'cs21003@student.edu', '+91-9100000013', 'B.Tech CSE', '2021', '2021-08-01', 'CSE', 4),
((SELECT id FROM users WHERE username = 'CS21004'), 'Advait Rao', 'cs21004@student.edu', '+91-9100000014', 'B.Tech CSE', '2021', '2021-08-01', 'CSE', 4),
((SELECT id FROM users WHERE username = 'CS21005'), 'Kiara Iyer', 'cs21005@student.edu', '+91-9100000015', 'B.Tech CSE', '2021', '2021-08-01', 'CSE', 4),
((SELECT id FROM users WHERE username = 'CS21006'), 'Kabir Menon', 'cs21006@student.edu', '+91-9100000016', 'B.Tech CSE', '2021', '2021-08-01', 'CSE', 4),
((SELECT id FROM users WHERE username = 'CS21007'), 'Sara Bhat', 'cs21007@student.edu', '+91-9100000017', 'B.Tech CSE', '2021', '2021-08-01', 'CSE', 4),
((SELECT id FROM users WHERE username = 'CS21008'), 'Arnav Pandey', 'cs21008@student.edu', '+91-9100000018', 'B.Tech CSE', '2021', '2021-08-01', 'CSE', 4),
((SELECT id FROM users WHERE username = 'CS21009'), 'Pari Saxena', 'cs21009@student.edu', '+91-9100000019', 'B.Tech CSE', '2021', '2021-08-01', 'CSE', 4),
((SELECT id FROM users WHERE username = 'CS21010'), 'Kian Malhotra', 'cs21010@student.edu', '+91-9100000020', 'B.Tech CSE', '2021', '2021-08-01', 'CSE', 4),
((SELECT id FROM users WHERE username = 'CS20001'), 'Riya Bansal', 'cs20001@student.edu', '+91-9100000021', 'B.Tech CSE', '2020', '2020-08-01', 'CSE', 6),
((SELECT id FROM users WHERE username = 'CS20002'), 'Dhruv Choudhary', 'cs20002@student.edu', '+91-9100000022', 'B.Tech CSE', '2020', '2020-08-01', 'CSE', 6),
((SELECT id FROM users WHERE username = 'CS20003'), 'Navya Agarwal', 'cs20003@student.edu', '+91-9100000023', 'B.Tech CSE', '2020', '2020-08-01', 'CSE', 6),
((SELECT id FROM users WHERE username = 'CS20004'), 'Ved Mishra', 'cs20004@student.edu', '+91-9100000024', 'B.Tech CSE', '2020', '2020-08-01', 'CSE', 6),
((SELECT id FROM users WHERE username = 'CS20005'), 'Anvi Krishnan', 'cs20005@student.edu', '+91-9100000025', 'B.Tech CSE', '2020', '2020-08-01', 'CSE', 6),
((SELECT id FROM users WHERE username = 'CS20006'), 'Atharv Das', 'cs20006@student.edu', '+91-9100000026', 'B.Tech CSE', '2020', '2020-08-01', 'CSE', 6),
((SELECT id FROM users WHERE username = 'CS20007'), 'Anika Yadav', 'cs20007@student.edu', '+91-9100000027', 'B.Tech CSE', '2020', '2020-08-01', 'CSE', 6),
((SELECT id FROM users WHERE username = 'CS20008'), 'Shivansh Bhatt', 'cs20008@student.edu', '+91-9100000028', 'B.Tech CSE', '2020', '2020-08-01', 'CSE', 6),
((SELECT id FROM users WHERE username = 'CS20009'), 'Avni Chawla', 'cs20009@student.edu', '+91-9100000029', 'B.Tech CSE', '2020', '2020-08-01', 'CSE', 6),
((SELECT id FROM users WHERE username = 'CS20010'), 'Rudra Sinha', 'cs20010@student.edu', '+91-9100000030', 'B.Tech CSE', '2020', '2020-08-01', 'CSE', 6),
((SELECT id FROM users WHERE username = 'CS19001'), 'Shanaya Jain', 'cs19001@student.edu', '+91-9100000031', 'B.Tech CSE', '2019', '2019-08-01', 'CSE', 8),
((SELECT id FROM users WHERE username = 'CS19002'), 'Reyansh Khanna', 'cs19002@student.edu', '+91-9100000032', 'B.Tech CSE', '2019', '2019-08-01', 'CSE', 8),
((SELECT id FROM users WHERE username = 'CS19003'), 'Ira Soni', 'cs19003@student.edu', '+91-9100000033', 'B.Tech CSE', '2019', '2019-08-01', 'CSE', 8),
((SELECT id FROM users WHERE username = 'CS19004'), 'Vivaan Bajaj', 'cs19004@student.edu', '+91-9100000034', 'B.Tech CSE', '2019', '2019-08-01', 'CSE', 8),
((SELECT id FROM users WHERE username = 'CS19005'), 'Mira Ahuja', 'cs19005@student.edu', '+91-9100000035', 'B.Tech CSE', '2019', '2019-08-01', 'CSE', 8),
((SELECT id FROM users WHERE username = 'CS19006'), 'Aditya Malik', 'cs19006@student.edu', '+91-9100000036', 'B.Tech CSE', '2019', '2019-08-01', 'CSE', 8),
((SELECT id FROM users WHERE username = 'CS19007'), 'Zara Sethi', 'cs19007@student.edu', '+91-9100000037', 'B.Tech CSE', '2019', '2019-08-01', 'CSE', 8),
((SELECT id FROM users WHERE username = 'CS19008'), 'Aarav Ghosh', 'cs19008@student.edu', '+91-9100000038', 'B.Tech CSE', '2019', '2019-08-01', 'CSE', 8),
((SELECT id FROM users WHERE username = 'CS19009'), 'Samaira Bose', 'cs19009@student.edu', '+91-9100000039', 'B.Tech CSE', '2019', '2019-08-01', 'CSE', 8),
((SELECT id FROM users WHERE username = 'CS19010'), 'Vihan Roy', 'cs19010@student.edu', '+91-9100000040', 'B.Tech CSE', '2019', '2019-08-01', 'CSE', 8);

-- ECE Students (25 total - 2 already exist)
INSERT INTO students (user_id, name, email, phone, course, batch, enrollment_date, department, semester) VALUES
((SELECT id FROM users WHERE username = 'EC22002'), 'Kavya Sharma', 'ec22002@student.edu', '+91-9100001002', 'B.Tech ECE', '2022', '2022-08-01', 'ECE', 2),
((SELECT id FROM users WHERE username = 'EC22003'), 'Arnav Patel', 'ec22003@student.edu', '+91-9100001003', 'B.Tech ECE', '2022', '2022-08-01', 'ECE', 2),
((SELECT id FROM users WHERE username = 'EC22004'), 'Ishita Kumar', 'ec22004@student.edu', '+91-9100001004', 'B.Tech ECE', '2022', '2022-08-01', 'ECE', 2),
((SELECT id FROM users WHERE username = 'EC22005'), 'Rohan Singh', 'ec22005@student.edu', '+91-9100001005', 'B.Tech ECE', '2022', '2022-08-01', 'ECE', 2),
((SELECT id FROM users WHERE username = 'EC22006'), 'Tara Reddy', 'ec22006@student.edu', '+91-9100001006', 'B.Tech ECE', '2022', '2022-08-01', 'ECE', 2),
((SELECT id FROM users WHERE username = 'EC21002'), 'Aadhya Nair', 'ec21002@student.edu', '+91-9100001012', 'B.Tech ECE', '2021', '2021-08-01', 'ECE', 4),
((SELECT id FROM users WHERE username = 'EC21003'), 'Dhruv Pillai', 'ec21003@student.edu', '+91-9100001013', 'B.Tech ECE', '2021', '2021-08-01', 'ECE', 4),
((SELECT id FROM users WHERE username = 'EC21004'), 'Ananya Iyer', 'ec21004@student.edu', '+91-9100001014', 'B.Tech ECE', '2021', '2021-08-01', 'ECE', 4),
((SELECT id FROM users WHERE username = 'EC21005'), 'Ayaan Menon', 'ec21005@student.edu', '+91-9100001015', 'B.Tech ECE', '2021', '2021-08-01', 'ECE', 4),
((SELECT id FROM users WHERE username = 'EC21006'), 'Sara Desai', 'ec21006@student.edu', '+91-9100001016', 'B.Tech ECE', '2021', '2021-08-01', 'ECE', 4),
((SELECT id FROM users WHERE username = 'EC20001'), 'Vihaan Rao', 'ec20001@student.edu', '+91-9100001021', 'B.Tech ECE', '2020', '2020-08-01', 'ECE', 6),
((SELECT id FROM users WHERE username = 'EC20002'), 'Myra Gupta', 'ec20002@student.edu', '+91-9100001022', 'B.Tech ECE', '2020', '2020-08-01', 'ECE', 6),
((SELECT id FROM users WHERE username = 'EC20003'), 'Arjun Verma', 'ec20003@student.edu', '+91-9100001023', 'B.Tech ECE', '2020', '2020-08-01', 'ECE', 6),
((SELECT id FROM users WHERE username = 'EC20004'), 'Kiara Joshi', 'ec20004@student.edu', '+91-9100001024', 'B.Tech ECE', '2020', '2020-08-01', 'ECE', 6),
((SELECT id FROM users WHERE username = 'EC20005'), 'Kabir Bhat', 'ec20005@student.edu', '+91-9100001025', 'B.Tech ECE', '2020', '2020-08-01', 'ECE', 6),
((SELECT id FROM users WHERE username = 'EC20006'), 'Diya Pandey', 'ec20006@student.edu', '+91-9100001026', 'B.Tech ECE', '2020', '2020-08-01', 'ECE', 6),
((SELECT id FROM users WHERE username = 'EC19001'), 'Advait Saxena', 'ec19001@student.edu', '+91-9100001031', 'B.Tech ECE', '2019', '2019-08-01', 'ECE', 8),
((SELECT id FROM users WHERE username = 'EC19002'), 'Anvi Malhotra', 'ec19002@student.edu', '+91-9100001032', 'B.Tech ECE', '2019', '2019-08-01', 'ECE', 8),
((SELECT id FROM users WHERE username = 'EC19003'), 'Atharv Bansal', 'ec19003@student.edu', '+91-9100001033', 'B.Tech ECE', '2019', '2019-08-01', 'ECE', 8),
((SELECT id FROM users WHERE username = 'EC19004'), 'Navya Choudhary', 'ec19004@student.edu', '+91-9100001034', 'B.Tech ECE', '2019', '2019-08-01', 'ECE', 8),
((SELECT id FROM users WHERE username = 'EC19005'), 'Ved Agarwal', 'ec19005@student.edu', '+91-9100001035', 'B.Tech ECE', '2019', '2019-08-01', 'ECE', 8),
((SELECT id FROM users WHERE username = 'EC19006'), 'Pari Mishra', 'ec19006@student.edu', '+91-9100001036', 'B.Tech ECE', '2019', '2019-08-01', 'ECE', 8),
((SELECT id FROM users WHERE username = 'EC19007'), 'Kian Krishnan', 'ec19007@student.edu', '+91-9100001037', 'B.Tech ECE', '2019', '2019-08-01', 'ECE', 8);

-- ME Students (20 total - 2 already exist)
INSERT INTO students (user_id, name, email, phone, course, batch, enrollment_date, department, semester) VALUES
((SELECT id FROM users WHERE username = 'ME22002'), 'Riya Das', 'me22002@student.edu', '+91-9100002002', 'B.Tech ME', '2022', '2022-08-01', 'ME', 2),
((SELECT id FROM users WHERE username = 'ME22003'), 'Shivansh Yadav', 'me22003@student.edu', '+91-9100002003', 'B.Tech ME', '2022', '2022-08-01', 'ME', 2),
((SELECT id FROM users WHERE username = 'ME22004'), 'Avni Bhatt', 'me22004@student.edu', '+91-9100002004', 'B.Tech ME', '2022', '2022-08-01', 'ME', 2),
((SELECT id FROM users WHERE username = 'ME22005'), 'Rudra Chawla', 'me22005@student.edu', '+91-9100002005', 'B.Tech ME', '2022', '2022-08-01', 'ME', 2),
((SELECT id FROM users WHERE username = 'ME21002'), 'Shanaya Sinha', 'me21002@student.edu', '+91-9100002012', 'B.Tech ME', '2021', '2021-08-01', 'ME', 4),
((SELECT id FROM users WHERE username = 'ME21003'), 'Aarohi Jain', 'me21003@student.edu', '+91-9100002013', 'B.Tech ME', '2021', '2021-08-01', 'ME', 4),
((SELECT id FROM users WHERE username = 'ME21004'), 'Reyansh Khanna', 'me21004@student.edu', '+91-9100002014', 'B.Tech ME', '2021', '2021-08-01', 'ME', 4),
((SELECT id FROM users WHERE username = 'ME21005'), 'Ira Soni', 'me21005@student.edu', '+91-9100002015', 'B.Tech ME', '2021', '2021-08-01', 'ME', 4),
((SELECT id FROM users WHERE username = 'ME20002'), 'Mira Bajaj', 'me20002@student.edu', '+91-9100002022', 'B.Tech ME', '2020', '2020-08-01', 'ME', 6),
((SELECT id FROM users WHERE username = 'ME20003'), 'Vivaan Ahuja', 'me20003@student.edu', '+91-9100002023', 'B.Tech ME', '2020', '2020-08-01', 'ME', 6),
((SELECT id FROM users WHERE username = 'ME20004'), 'Zara Malik', 'me20004@student.edu', '+91-9100002024', 'B.Tech ME', '2020', '2020-08-01', 'ME', 6),
((SELECT id FROM users WHERE username = 'ME20005'), 'Aditya Sethi', 'me20005@student.edu', '+91-9100002025', 'B.Tech ME', '2020', '2020-08-01', 'ME', 6),
((SELECT id FROM users WHERE username = 'ME19002'), 'Aarav Ghosh', 'me19002@student.edu', '+91-9100002032', 'B.Tech ME', '2019', '2019-08-01', 'ME', 8),
((SELECT id FROM users WHERE username = 'ME19003'), 'Samaira Bose', 'me19003@student.edu', '+91-9100002033', 'B.Tech ME', '2019', '2019-08-01', 'ME', 8),
((SELECT id FROM users WHERE username = 'ME19004'), 'Vihan Roy', 'me19004@student.edu', '+91-9100002034', 'B.Tech ME', '2019', '2019-08-01', 'ME', 8),
((SELECT id FROM users WHERE username = 'ME19005'), 'Kavya Sen', 'me19005@student.edu', '+91-9100002035', 'B.Tech ME', '2019', '2019-08-01', 'ME', 8);

-- CE Students (10 total)
INSERT INTO students (user_id, name, email, phone, course, batch, enrollment_date, department, semester) VALUES
((SELECT id FROM users WHERE username = 'CE22001'), 'Arnav Mukherjee', 'ce22001@student.edu', '+91-9100003001', 'B.Tech CE', '2022', '2022-08-01', 'CE', 2),
((SELECT id FROM users WHERE username = 'CE22002'), 'Ishita Chatterjee', 'ce22002@student.edu', '+91-9100003002', 'B.Tech CE', '2022', '2022-08-01', 'CE', 2),
((SELECT id FROM users WHERE username = 'CE21001'), 'Rohan Banerjee', 'ce21001@student.edu', '+91-9100003011', 'B.Tech CE', '2021', '2021-08-01', 'CE', 4),
((SELECT id FROM users WHERE username = 'CE21002'), 'Tara Dutta', 'ce21002@student.edu', '+91-9100003012', 'B.Tech CE', '2021', '2021-08-01', 'CE', 4),
((SELECT id FROM users WHERE username = 'CE21003'), 'Aadhya Sarkar', 'ce21003@student.edu', '+91-9100003013', 'B.Tech CE', '2021', '2021-08-01', 'CE', 4),
((SELECT id FROM users WHERE username = 'CE20001'), 'Dhruv Chakraborty', 'ce20001@student.edu', '+91-9100003021', 'B.Tech CE', '2020', '2020-08-01', 'CE', 6),
((SELECT id FROM users WHERE username = 'CE20002'), 'Ananya Bhattacharya', 'ce20002@student.edu', '+91-9100003022', 'B.Tech CE', '2020', '2020-08-01', 'CE', 6),
((SELECT id FROM users WHERE username = 'CE19001'), 'Ayaan Dasgupta', 'ce19001@student.edu', '+91-9100003031', 'B.Tech CE', '2019', '2019-08-01', 'CE', 8),
((SELECT id FROM users WHERE username = 'CE19002'), 'Sara Ganguly', 'ce19002@student.edu', '+91-9100003032', 'B.Tech CE', '2019', '2019-08-01', 'CE', 8),
((SELECT id FROM users WHERE username = 'CE19003'), 'Vihaan Mazumdar', 'ce19003@student.edu', '+91-9100003033', 'B.Tech CE', '2019', '2019-08-01', 'CE', 8);

-- EEE Students (5 total)
INSERT INTO students (user_id, name, email, phone, course, batch, enrollment_date, department, semester) VALUES
((SELECT id FROM users WHERE username = 'EE22001'), 'Myra Raghavan', 'ee22001@student.edu', '+91-9100004001', 'B.Tech EEE', '2022', '2022-08-01', 'EEE', 2),
((SELECT id FROM users WHERE username = 'EE21001'), 'Arjun Subramaniam', 'ee21001@student.edu', '+91-9100004011', 'B.Tech EEE', '2021', '2021-08-01', 'EEE', 4),
((SELECT id FROM users WHERE username = 'EE21002'), 'Kiara Venkatesh', 'ee21002@student.edu', '+91-9100004012', 'B.Tech EEE', '2021', '2021-08-01', 'EEE', 4),
((SELECT id FROM users WHERE username = 'EE20001'), 'Kabir Ramachandran', 'ee20001@student.edu', '+91-9100004021', 'B.Tech EEE', '2020', '2020-08-01', 'EEE', 6),
((SELECT id FROM users WHERE username = 'EE19001'), 'Diya Srinivasan', 'ee19001@student.edu', '+91-9100004031', 'B.Tech EEE', '2019', '2019-08-01', 'EEE', 8);

SELECT 'All 100 student profiles completed!' AS Status;
