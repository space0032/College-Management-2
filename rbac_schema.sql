-- RBAC Database Schema
-- Run this script to set up Role-Based Access Control

-- 1. Permissions Table
CREATE TABLE IF NOT EXISTS permissions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT
);

-- 2. Roles Table
CREATE TABLE IF NOT EXISTS roles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_system_role BOOLEAN DEFAULT FALSE
);

-- 3. Role-Permissions Mapping
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- 4. Add role_id to users table (keeping existing 'role' column for backward compatibility)
ALTER TABLE users ADD COLUMN IF NOT EXISTS role_id INT;
ALTER TABLE users ADD CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE SET NULL;

-- =====================================================
-- INSERT DEFAULT PERMISSIONS
-- =====================================================

-- Student Management
INSERT INTO permissions (code, name, category) VALUES 
('VIEW_STUDENTS', 'View Students', 'STUDENTS'),
('MANAGE_STUDENTS', 'Add/Edit/Delete Students', 'STUDENTS'),
('VIEW_OWN_PROFILE', 'View Own Profile', 'STUDENTS');

-- Faculty Management
INSERT INTO permissions (code, name, category) VALUES 
('VIEW_FACULTY', 'View Faculty', 'FACULTY'),
('MANAGE_FACULTY', 'Add/Edit/Delete Faculty', 'FACULTY');

-- Course Management
INSERT INTO permissions (code, name, category) VALUES 
('VIEW_COURSES', 'View Courses', 'COURSES'),
('MANAGE_ALL_COURSES', 'Manage All Courses', 'COURSES'),
('MANAGE_OWN_COURSES', 'Manage Assigned Courses', 'COURSES');

-- Department Management
INSERT INTO permissions (code, name, category) VALUES 
('VIEW_DEPARTMENTS', 'View Departments', 'DEPARTMENTS'),
('MANAGE_DEPARTMENTS', 'Manage Departments', 'DEPARTMENTS');

-- Attendance
INSERT INTO permissions (code, name, category) VALUES 
('VIEW_ATTENDANCE', 'View Attendance', 'ATTENDANCE'),
('MANAGE_ATTENDANCE', 'Mark/Edit Attendance', 'ATTENDANCE'),
('VIEW_OWN_ATTENDANCE', 'View Own Attendance', 'ATTENDANCE');

-- Grades
INSERT INTO permissions (code, name, category) VALUES 
('VIEW_GRADES', 'View All Grades', 'GRADES'),
('MANAGE_GRADES', 'Assign Grades', 'GRADES'),
('VIEW_OWN_GRADES', 'View Own Grades', 'GRADES');

-- Assignments
INSERT INTO permissions (code, name, category) VALUES 
('VIEW_ASSIGNMENTS', 'View Assignments', 'ASSIGNMENTS'),
('MANAGE_ASSIGNMENTS', 'Create/Edit Assignments', 'ASSIGNMENTS'),
('SUBMIT_ASSIGNMENTS', 'Submit Assignments', 'ASSIGNMENTS'),
('REVIEW_SUBMISSIONS', 'Review Submissions', 'ASSIGNMENTS');

-- Library
INSERT INTO permissions (code, name, category) VALUES 
('VIEW_LIBRARY', 'View Library', 'LIBRARY'),
('MANAGE_LIBRARY', 'Manage Books/Issues', 'LIBRARY'),
('REQUEST_BOOKS', 'Request Books', 'LIBRARY');

-- Hostel
INSERT INTO permissions (code, name, category) VALUES 
('VIEW_HOSTEL', 'View Hostel Info', 'HOSTEL'),
('MANAGE_HOSTEL', 'Manage Hostel/Rooms', 'HOSTEL'),
('MANAGE_ALLOCATIONS', 'Manage Room Allocations', 'HOSTEL'),
('APPROVE_GATE_PASS', 'Approve Gate Passes', 'HOSTEL'),
('REQUEST_GATE_PASS', 'Request Gate Pass', 'HOSTEL');

-- Fees
INSERT INTO permissions (code, name, category) VALUES 
('VIEW_ALL_FEES', 'View All Fees', 'FEES'),
('MANAGE_FEES', 'Manage Fee Records', 'FEES'),
('VIEW_OWN_FEES', 'View Own Fees', 'FEES');

-- Timetable
INSERT INTO permissions (code, name, category) VALUES 
('VIEW_TIMETABLE', 'View Timetable', 'TIMETABLE'),
('MANAGE_TIMETABLE', 'Manage Timetable', 'TIMETABLE');

-- Reports & Audit
INSERT INTO permissions (code, name, category) VALUES 
('VIEW_REPORTS', 'View Reports', 'REPORTS'),
('VIEW_AUDIT_LOGS', 'View Audit Logs', 'SYSTEM'),
('MANAGE_SYSTEM', 'System Administration', 'SYSTEM');

-- =====================================================
-- INSERT DEFAULT ROLES
-- =====================================================

INSERT INTO roles (code, name, description, is_system_role) VALUES 
('SUPER_ADMIN', 'Super Administrator', 'Full system access', TRUE),
('ADMIN', 'Administrator', 'Administrative access', TRUE),
('HOD', 'Head of Department', 'Department management access', FALSE),
('PROFESSOR', 'Professor', 'Senior faculty access', FALSE),
('ASSOCIATE_PROFESSOR', 'Associate Professor', 'Faculty access', FALSE),
('ASSISTANT_PROFESSOR', 'Assistant Professor', 'Faculty access', FALSE),
('LECTURER', 'Lecturer', 'Basic faculty access', FALSE),
('LAB_ASSISTANT', 'Lab Assistant', 'Lab management access', FALSE),
('WARDEN', 'Hostel Warden', 'Hostel management access', TRUE),
('STUDENT', 'Student', 'Student access', TRUE);

-- =====================================================
-- ASSIGN PERMISSIONS TO ROLES
-- =====================================================

-- SUPER_ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.code = 'SUPER_ADMIN';

-- ADMIN (similar to SUPER_ADMIN but no system management)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.code = 'ADMIN' AND p.code NOT IN ('MANAGE_SYSTEM');

-- HOD
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.code = 'HOD' AND p.code IN (
    'VIEW_STUDENTS', 'VIEW_FACULTY', 'VIEW_COURSES', 'MANAGE_OWN_COURSES',
    'VIEW_DEPARTMENTS', 'VIEW_ATTENDANCE', 'MANAGE_ATTENDANCE', 
    'VIEW_GRADES', 'MANAGE_GRADES', 'VIEW_ASSIGNMENTS', 'MANAGE_ASSIGNMENTS',
    'REVIEW_SUBMISSIONS', 'VIEW_LIBRARY', 'VIEW_TIMETABLE', 'VIEW_REPORTS'
);

-- PROFESSOR
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.code = 'PROFESSOR' AND p.code IN (
    'VIEW_STUDENTS', 'VIEW_COURSES', 'MANAGE_OWN_COURSES',
    'VIEW_ATTENDANCE', 'MANAGE_ATTENDANCE', 
    'VIEW_GRADES', 'MANAGE_GRADES', 'VIEW_ASSIGNMENTS', 'MANAGE_ASSIGNMENTS',
    'REVIEW_SUBMISSIONS', 'VIEW_LIBRARY', 'VIEW_TIMETABLE'
);

-- ASSOCIATE_PROFESSOR (same as PROFESSOR)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.code = 'ASSOCIATE_PROFESSOR' AND p.code IN (
    'VIEW_STUDENTS', 'VIEW_COURSES', 'MANAGE_OWN_COURSES',
    'VIEW_ATTENDANCE', 'MANAGE_ATTENDANCE', 
    'VIEW_GRADES', 'MANAGE_GRADES', 'VIEW_ASSIGNMENTS', 'MANAGE_ASSIGNMENTS',
    'REVIEW_SUBMISSIONS', 'VIEW_LIBRARY', 'VIEW_TIMETABLE'
);

-- ASSISTANT_PROFESSOR
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.code = 'ASSISTANT_PROFESSOR' AND p.code IN (
    'VIEW_STUDENTS', 'VIEW_COURSES', 'MANAGE_OWN_COURSES',
    'VIEW_ATTENDANCE', 'MANAGE_ATTENDANCE', 
    'VIEW_GRADES', 'MANAGE_GRADES', 'VIEW_ASSIGNMENTS', 'MANAGE_ASSIGNMENTS',
    'REVIEW_SUBMISSIONS', 'VIEW_LIBRARY', 'VIEW_TIMETABLE'
);

-- LECTURER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.code = 'LECTURER' AND p.code IN (
    'VIEW_STUDENTS', 'VIEW_COURSES', 'MANAGE_OWN_COURSES',
    'VIEW_ATTENDANCE', 'MANAGE_ATTENDANCE', 
    'VIEW_ASSIGNMENTS', 'MANAGE_ASSIGNMENTS', 'REVIEW_SUBMISSIONS',
    'VIEW_LIBRARY', 'VIEW_TIMETABLE'
);

-- LAB_ASSISTANT
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.code = 'LAB_ASSISTANT' AND p.code IN (
    'VIEW_STUDENTS', 'VIEW_COURSES', 'VIEW_ATTENDANCE', 
    'MANAGE_ATTENDANCE', 'VIEW_LIBRARY', 'VIEW_TIMETABLE'
);

-- WARDEN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.code = 'WARDEN' AND p.code IN (
    'VIEW_STUDENTS', 'VIEW_HOSTEL', 'MANAGE_HOSTEL', 
    'MANAGE_ALLOCATIONS', 'APPROVE_GATE_PASS', 'VIEW_REPORTS'
);

-- STUDENT
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.code = 'STUDENT' AND p.code IN (
    'VIEW_OWN_PROFILE', 'VIEW_COURSES', 'VIEW_OWN_ATTENDANCE',
    'VIEW_OWN_GRADES', 'VIEW_ASSIGNMENTS', 'SUBMIT_ASSIGNMENTS',
    'VIEW_LIBRARY', 'REQUEST_BOOKS', 'VIEW_HOSTEL', 'REQUEST_GATE_PASS',
    'VIEW_OWN_FEES', 'VIEW_TIMETABLE'
);

-- =====================================================
-- MIGRATE EXISTING USERS TO NEW ROLES
-- =====================================================

-- Set role_id based on existing 'role' column
UPDATE users SET role_id = (SELECT id FROM roles WHERE code = 'SUPER_ADMIN') WHERE role = 'ADMIN';
UPDATE users SET role_id = (SELECT id FROM roles WHERE code = 'LECTURER') WHERE role = 'FACULTY';
UPDATE users SET role_id = (SELECT id FROM roles WHERE code = 'WARDEN') WHERE role = 'WARDEN';
UPDATE users SET role_id = (SELECT id FROM roles WHERE code = 'STUDENT') WHERE role = 'STUDENT';
