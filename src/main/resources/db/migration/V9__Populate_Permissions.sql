-- Populate Permissions
INSERT IGNORE INTO permissions (code, name, category, description) VALUES 
-- System
('MANAGE_SYSTEM', 'Manage System', 'SYSTEM', 'Full system access'),
('VIEW_AUDIT_LOGS', 'View Audit Logs', 'SYSTEM', 'View system audit logs'),

-- Students
('VIEW_STUDENTS', 'View Students', 'STUDENT', 'View student details'),
('MANAGE_STUDENTS', 'Manage Students', 'STUDENT', 'Add/Edit/Delete students'),

-- Faculty
('VIEW_FACULTY', 'View Faculty', 'FACULTY', 'View faculty details'),
('MANAGE_FACULTY', 'Manage Faculty', 'FACULTY', 'Add/Edit/Delete faculty'),

-- Attendance
('VIEW_ATTENDANCE', 'View Attendance', 'ATTENDANCE', 'View attendance records'),
('MANAGE_ATTENDANCE', 'Manage Attendance', 'ATTENDANCE', 'Take/Update attendance'),
('VIEW_OWN_ATTENDANCE', 'View Own Attendance', 'ATTENDANCE', 'View own attendance'),

-- Grades
('VIEW_GRADES', 'View Grades', 'ACADEMIC', 'View student grades'),
('MANAGE_GRADES', 'Manage Grades', 'ACADEMIC', 'Enter/Update grades'),
('VIEW_OWN_GRADES', 'View Own Grades', 'ACADEMIC', 'View own grades'),

-- Library
('VIEW_LIBRARY', 'View Library', 'LIBRARY', 'View library books'),
('MANAGE_LIBRARY', 'Manage Library', 'LIBRARY', 'Manage library books'),

-- Fees
('VIEW_ALL_FEES', 'View All Fees', 'FINANCE', 'View all fee records'),
('MANAGE_FEES', 'Manage Fees', 'FINANCE', 'Manage fee records'),
('VIEW_OWN_FEES', 'View Own Fees', 'FINANCE', 'View personal fee records'),

-- Timetable
('VIEW_TIMETABLE', 'View Timetable', 'ACADEMIC', 'View class timetables'),
('MANAGE_TIMETABLE', 'Manage Timetable', 'ACADEMIC', 'Manage class timetables'),

-- Gate Pass
('REQUEST_GATE_PASS', 'Request Gate Pass', 'HOSTEL', 'Request a gate pass'),
('APPROVE_GATE_PASS', 'Approve Gate Pass', 'HOSTEL', 'Approve gate passes'),

-- Hostel
('MANAGE_HOSTEL', 'Manage Hostel', 'HOSTEL', 'Manage hostel details'),

-- Assignments
('VIEW_ASSIGNMENTS', 'View Assignments', 'ACADEMIC', 'View assignments'),
('SUBMIT_ASSIGNMENTS', 'Submit Assignments', 'ACADEMIC', 'Submit assignments'),
('MANAGE_ASSIGNMENTS', 'Manage Assignments', 'ACADEMIC', 'Create/Grade assignments'),

-- Reports
('VIEW_FEES_REPORT', 'View Fees Report', 'REPORT', 'View fee reports'),
('VIEW_ATTENDANCE_REPORT', 'View Attendance Report', 'REPORT', 'View attendance reports'),
('VIEW_GRADES_REPORT', 'View Grades Report', 'REPORT', 'View grade reports');

-- Assign Permissions to Roles

-- ADMIN (Get ID from subquery)
SET @admin_id = (SELECT id FROM roles WHERE code = 'ADMIN');
INSERT IGNORE INTO role_permissions (role_id, permission_id) 
SELECT @admin_id, id FROM permissions; -- Admin gets ALL permissions

-- FACULTY
SET @faculty_id = (SELECT id FROM roles WHERE code = 'FACULTY');
INSERT IGNORE INTO role_permissions (role_id, permission_id) 
SELECT @faculty_id, id FROM permissions WHERE code IN (
    'VIEW_STUDENTS', 'VIEW_FACULTY', 
    'VIEW_ATTENDANCE', 'MANAGE_ATTENDANCE',
    'VIEW_GRADES', 'MANAGE_GRADES',
    'VIEW_LIBRARY', 'VIEW_TIMETABLE', 
    'VIEW_ASSIGNMENTS', 'MANAGE_ASSIGNMENTS',
    'VIEW_ATTENDANCE_REPORT', 'VIEW_GRADES_REPORT'
);

-- STUDENT
SET @student_id = (SELECT id FROM roles WHERE code = 'STUDENT');
INSERT IGNORE INTO role_permissions (role_id, permission_id) 
SELECT @student_id, id FROM permissions WHERE code IN (
    'VIEW_OWN_ATTENDANCE', 'VIEW_OWN_GRADES', 
    'VIEW_LIBRARY', 'VIEW_TIMETABLE', 
    'VIEW_OWN_FEES', 'REQUEST_GATE_PASS',
    'VIEW_ASSIGNMENTS', 'SUBMIT_ASSIGNMENTS',
    'VIEW_COURSES' -- Assuming this might be needed
);

-- WARDEN (If exists)
SET @warden_id = (SELECT id FROM roles WHERE code = 'WARDEN');
-- Create Warden Role if it doesn't exist (it wasn't in V8)
INSERT IGNORE INTO roles (code, name, description, is_system_role) VALUES ('WARDEN', 'Warden', 'Hostel Warden', TRUE);
SET @warden_id = (SELECT id FROM roles WHERE code = 'WARDEN');

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @warden_id, id FROM permissions WHERE code IN (
    'APPROVE_GATE_PASS', 'MANAGE_HOSTEL', 'VIEW_STUDENTS'
);
