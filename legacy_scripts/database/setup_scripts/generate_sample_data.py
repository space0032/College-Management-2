#!/usr/bin/env python3
"""
Generate sample data for College Management System
Creates 100 students, 30 faculty, courses, timetables, and fees
"""

import random
from datetime import datetime, timedelta

# Student names
first_names = ['Aarav', 'Vivaan', 'Aditya', 'Arjun', 'Sai', 'Ayaan', 'Krishna', 'Ishaan', 'Shaurya', 'Atharv',
               'Advik', 'Pranav', 'Ved', 'Kiaan', 'Ansh', 'Dhruv', 'Kabir', 'Vihaan', 'Arnav', 'Reyansh',
               'Aadhya', 'Ananya', 'Pari', 'Anika', 'Navya', 'Angel', 'Diya', 'Myra', 'Sara', 'Jhanvi',
               'Ira', 'Pihu', 'Riya', 'Anvi', 'Tara', 'Saanvi', 'Aarohi', 'Kiara', 'Shanaya', 'Kavya',
               'Advait', 'Aayan', 'Darsh', 'Ekant', 'Harsh', 'Keshav', 'Lakshay', 'Manav', 'Naman', 'Om']

last_names = ['Sharma', 'Patel', 'Kumar', 'Singh', 'Reddy', 'Nair', 'Iyer', 'Das', 'Gupta', 'Joshi',
              'Mehta', 'Desai', 'Shah', 'Agarwal', 'Verma', 'Rao', 'Pillai', 'Menon', 'Bhat', 'Shetty',
              'Hegde', 'Kamath', 'Krishnan', 'Acharya', 'Prabhu', 'Murthy']

# Departments
departments = {
    'Computer Science': 'CS',
    'Electrical Engineering': 'EE',
    'Mechanical Engineering': 'ME',
    'Civil Engineering': 'CE'
}

# Courses for each department and semester
courses_data = {
    'CS': {
        1: [('Engineering Mathematics I', 'CS-MAT101', 4), ('Programming in C', 'CS101', 4), ('Engineering Physics', 'CS-PHY101', 3), ('English Communication', 'CS-ENG101', 2)],
        2: [('Engineering Mathematics II', 'CS-MAT102', 4), ('Data Structures', 'CS102', 4), ('Digital Electronics', 'CS103', 4), ('Engineering Graphics', 'CS104', 3)],
        3: [('Discrete Mathematics', 'CS201', 4), ('Computer Organization', 'CS202', 4), ('Object Oriented Programming', 'CS203', 4), ('Database Management', 'CS204', 4)],
        4: [('Design & Analysis of Algorithms', 'CS301', 4), ('Operating Systems', 'CS302', 4), ('Computer Networks', 'CS303', 4), ('Software Engineering', 'CS304', 3)],
        5: [('Machine Learning', 'CS401', 4), ('Compiler Design', 'CS402', 4), ('Web Technologies', 'CS403', 3), ('Elective I', 'CS-E401', 3)],
        6: [('Artificial Intelligence', 'CS501', 4), ('Cloud Computing', 'CS502', 3), ('Network Security', 'CS503', 3), ('Elective II', 'CS-E501', 3)],
        7: [('Big Data Analytics', 'CS601', 3), ('IoT', 'CS602', 3), ('Blockchain', 'CS603', 3), ('Major Project I', 'CS-PRJ701', 4)],
        8: [('Capstone Project', 'CS-PRJ801', 8), ('Industry Internship', 'CS-INT801', 4), ('Seminar', 'CS-SEM801', 2)]
    },
    'EE': {
        1: [('Engineering Mathematics I', 'EE-MAT101', 4), ('Basic Electrical', 'EE101', 4), ('Engineering Physics', 'EE-PHY101', 3), ('English Communication', 'EE-ENG101', 2)],
        2: [('Engineering Mathematics II', 'EE-MAT102', 4), ('Circuit Theory', 'EE102', 4), ('Electronic Devices', 'EE103', 4), ('Engineering Mechanics', 'EE104', 3)],
        3: [('Network Analysis', 'EE201', 4), ('Analog Electronics', 'EE202', 4), ('Electromagnetic Theory', 'EE203', 4), ('Signals & Systems', 'EE204', 4)],
        4: [('Power Systems I', 'EE301', 4), ('Control Systems', 'EE302', 4), ('Digital Electronics', 'EE303', 4), ('Electrical Machines I', 'EE304', 4)],
        5: [('Power Systems II', 'EE401', 4), ('Microprocessors', 'EE402', 4), ('Power Electronics', 'EE403', 4), ('Electrical Machines II', 'EE404', 4)],
        6: [('High Voltage Engineering', 'EE501', 3), ('Digital Signal Processing', 'EE502', 3), ('Instrumentation', 'EE503', 3), ('Elective I', 'EE-E501', 3)],
        7: [('Renewable Energy', 'EE601', 3), ('Smart Grid', 'EE602', 3), ('VLSI Design', 'EE603', 3), ('Major Project I', 'EE-PRJ701', 4)],
        8: [('Capstone Project', 'EE-PRJ801', 8), ('Industry Internship', 'EE-INT801', 4), ('Seminar', 'EE-SEM801', 2)]
    },
    'ME': {
        1: [('Engineering Mathematics I', 'ME-MAT101', 4), ('Engineering Mechanics', 'ME101', 4), ('Engineering Physics', 'ME-PHY101', 3), ('English Communication', 'ME-ENG101', 2)],
        2: [('Engineering Mathematics II', 'ME-MAT102', 4), ('Thermodynamics', 'ME102', 4), ('Material Science', 'ME103', 4), ('Manufacturing Processes', 'ME104', 4)],
        3: [('Fluid Mechanics', 'ME201', 4), ('Strength of Materials', 'ME202', 4), ('Kinematics of Machines', 'ME203', 4), ('Engineering Drawing', 'ME204', 3)],
        4: [('Heat Transfer', 'ME301', 4), ('Dynamics of Machines', 'ME302', 4), ('Machine Design I', 'ME303', 4), ('Manufacturing Technology', 'ME304', 4)],
        5: [('Automobile Engineering', 'ME401', 3), ('Machine Design II', 'ME402', 4), ('Metrology', 'ME403', 3), ('Industrial Engineering', 'ME404', 3)],
        6: [('CAD/CAM', 'ME501', 3), ('Mechatronics', 'ME502', 3), ('Finite Element Analysis', 'ME503', 3), ('Elective I', 'ME-E501', 3)],
        7: [('Robotics', 'ME601', 3), ('Refrigeration & AC', 'ME602', 3), ('Advanced Manufacturing', 'ME603', 3), ('Major Project I', 'ME-PRJ701', 4)],
        8: [('Capstone Project', 'ME-PRJ801', 8), ('Industry Internship', 'ME-INT801', 4), ('Seminar', 'ME-SEM801', 2)]
    },
    'CE': {
        1: [('Engineering Mathematics I', 'CE-MAT101', 4), ('Engineering Mechanics', 'CE101', 4), ('Engineering Physics', 'CE-PHY101', 3), ('English Communication', 'CE-ENG101', 2)],
        2: [('Engineering Mathematics II', 'CE-MAT102', 4), ('Building Materials', 'CE102', 4), ('Surveying', 'CE103', 4), ('Fluid Mechanics', 'CE104', 4)],
        3: [('Strength of Materials', 'CE201', 4), ('Structural Analysis I', 'CE202', 4), ('Geotechnical Engineering I', 'CE203', 4), ('Hydraulics', 'CE204', 4)],
        4: [('Concrete Technology', 'CE301', 4), ('Structural Analysis II', 'CE302', 4), ('Geotechnical Engineering II', 'CE303', 4), ('Transportation Engineering I', 'CE304', 4)],
        5: [('Design of RC Structures', 'CE401', 4), ('Steel Structures', 'CE402', 4), ('Foundation Engineering', 'CE403', 4), ('Water Resources Engineering', 'CE404', 3)],
        6: [('Environmental Engineering', 'CE501', 3), ('Construction Management', 'CE502', 3), ('Earthquake Engineering', 'CE503', 3), ('Elective I', 'CE-E501', 3)],
        7: [('Advanced Structural Design', 'CE601', 3), ('GIS & Remote Sensing', 'CE602', 3), ('Project Management', 'CE603', 3), ('Major Project I', 'CE-PRJ701', 4)],
        8: [('Capstone Project', 'CE-PRJ801', 8), ('Industry Internship', 'CE-INT801', 4), ('Seminar', 'CE-SEM801', 2)]
    }
}

# Time slots for timetable
time_slots = [
    '08:00-09:00', '09:00-10:00', '10:00-11:00', '11:00-12:00',
    '12:00-13:00', '13:00-14:00', '14:00-15:00', '15:00-16:00', '16:00-17:00'
]

days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday']
rooms = ['101', '102', '103', '104', '105', '201', '202', '203', '204', '205', 
         'Lab-1', 'Lab-2', 'Lab-3', 'Lab-4']

def generate_phone():
    return f"98{random.randint(10000000, 99999999)}"

def generate_email(first, last, dept_code, num):
    return f"{first.lower()}.{last.lower()}.{dept_code.lower()}{num:03d}@student.college.edu"

print("-- =====================================================")
print("-- COMPREHENSIVE SAMPLE DATA FOR COLLEGE MANAGEMENT SYSTEM")
print("-- 100 Students, 30 Faculty, Courses, Fees, Timetables")
print("-- =====================================================")
print()
print("USE college_management;")
print()

# Generate student data
print("-- =====================================================")
print("-- STUDENT DATA (100 Students)")
print("-- =====================================================")
print()

student_count = 0
students_by_dept_sem = {}

for dept_name, dept_code in departments.items():
    students_by_dept_sem[dept_code] = {}
    for sem in range(1, 9):
        students_by_dept_sem[dept_code][sem] = []
        # 3-4 students per semester per department
        num_students = 3 if sem in [1, 8] else 4
        for i in range(num_students):
            student_count += 1
            first = random.choice(first_names)
            last = random.choice(last_names)
            username = f"{dept_code.lower()}{student_count:03d}"
            email = generate_email(first, last, dept_code, student_count)
            phone = generate_phone()
            batch = f"{2024 - (sem-1)//2}"
            enrollment_date = f"{2024 - (sem-1)//2}-07-15"
            is_hostelite = 1 if random.random() > 0.4 else 0
            
            # Store for later use
            students_by_dept_sem[dept_code][sem].append({
                'username': username,
                'name': f"{first} {last}",
                'dept': dept_name,
                'sem': sem
            })
            
            # User account with role_id
            print(f"-- {dept_name} Semester {sem} Student {i+1}")
            print(f"INSERT INTO users (username, password, role, role_id) VALUES ('{username}', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STUDENT', (SELECT id FROM roles WHERE code = 'STUDENT' LIMIT 1));")
            print(f"INSERT INTO students (name, email, phone, course, batch, enrollment_date, address, department, semester, is_hostelite, user_id) VALUES")
            print(f"('{first} {last}', '{email}', '{phone}', 'B.Tech {dept_name}', '{batch}', '{enrollment_date}', '{random.randint(1, 999)} Main Street, City', '{dept_name}', {sem}, {is_hostelite}, (SELECT id FROM users WHERE username = '{username}'));")
            print()

print(f"-- Total students created: {student_count}")
print()

# Generate courses
print("-- =====================================================")
print("-- COURSES DATA")
print("-- =====================================================")
print()

course_ids = {}
course_num = 1

for dept_code, semesters in courses_data.items():
    dept_name = [k for k, v in departments.items() if v == dept_code][0]
    dept_id_query = f"(SELECT id FROM departments WHERE code = '{dept_code}' LIMIT 1)"
    
    for sem, course_list in semesters.items():
        for course_name, course_code, credits in course_list:
            print(f"INSERT INTO courses (name, code, credits, department, semester, department_id) VALUES")
            print(f"('{course_name}', '{course_code}', {credits}, '{dept_name}', {sem}, {dept_id_query});")
            course_ids[course_code] = {
                'dept': dept_code,
                'sem': sem,
                'name': course_name
            }
            course_num += 1

print(f"-- Total courses created: {course_num}")
print()

# Generate timetables
print("-- =====================================================")
print("-- TIMETABLE DATA")
print("-- =====================================================")
print()

for dept_code, semesters in courses_data.items():
    dept_name = [k for k, v in departments.items() if v == dept_code][0]
    
    for sem, course_list in semesters.items():
        print(f"-- Timetable for {dept_name} Semester {sem}")
        courses_for_sem = [c[1] for c in course_list]
        
        # Create random timetable
        slot_index = 0
        for day in days:
            daily_courses = random.sample(courses_for_sem, min(4, len(courses_for_sem)))
            for course_code in daily_courses:
                if slot_index < len(time_slots) - 1:  # Avoid last slot for lunch
                    slot = time_slots[slot_index % (len(time_slots) - 2)]
                    room = random.choice(rooms)
                    faculty_name = random.choice(['Dr. Kumar', 'Prof. Sharma', 'Dr. Patel', 'Prof. Singh', 'Dr. Reddy'])
                    course_info = course_ids[course_code]
                    
                    print(f"INSERT INTO timetable (department, semester, day_of_week, time_slot, subject, faculty_name, room_number) VALUES")
                    print(f"('{dept_name}', {sem}, '{day}', '{slot}', '{course_info['name']}', '{faculty_name}', '{room}');")
                    
                    slot_index += 1
        print()

# Generate fees
print("-- =====================================================")
print("-- FEES DATA")
print("-- =====================================================")
print()

print("-- Assign fees to all students")
base_fees = 75000  # Base semester fee

for dept_code in departments.values():
    for sem in range(1, 9):
        students = students_by_dept_sem[dept_code][sem]
        for student in students:
            username = student['username']
            # Fee varies by semester
            total_fees = base_fees + (sem * 5000)
            # Some students have paid, some are pending
            payment_status = random.choice(['PAID', 'PAID', 'PARTIAL', 'PENDING'])
            if payment_status == 'PAID':
                paid_amount = total_fees
            elif payment_status == 'PARTIAL':
                paid_amount = total_fees * random.uniform(0.3, 0.7)
            else:
                paid_amount = 0
            
            due_date = '2026-01-31'
            payment_date = "'2025-12-15'" if payment_status == 'PAID' else 'NULL'
            
            print(f"INSERT INTO fees (student_id, amount, paid_amount, due_date, payment_date, status) VALUES")
            print(f"((SELECT id FROM students WHERE user_id = (SELECT id FROM users WHERE username = '{username}')), {total_fees:.2f}, {paid_amount:.2f}, '{due_date}', {payment_date}, '{payment_status}');")

print()
print("-- =====================================================")
print("-- SAMPLE DATA GENERATION COMPLETE!")
print("-- =====================================================")
print(f"-- Students: {student_count}")
print(f"-- Courses: {course_num}")
print(f"-- Faculty: 30 (already in previous SQL)")
print("-- Timetables: Generated for all semesters")
print("-- Fees: Allocated to all students")
print("-- =====================================================")
