-- V2 Fix Schema, Permissions, and Missing Columns

-- 1. Add missing 'qualification' to faculty table
ALTER TABLE faculty ADD COLUMN IF NOT EXISTS qualification VARCHAR(255);

-- 2. Create missing 'wardens' table (Legacy DAO compatibility)
CREATE TABLE IF NOT EXISTS wardens (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    hostel_id INTEGER REFERENCES hostels(id) ON DELETE SET NULL,
    user_id INTEGER REFERENCES users(id) ON DELETE SET NULL
);

-- 3. Populate ALL missing permissions found in codebase
INSERT INTO permissions (code, name, category) VALUES 
('APPROVE_GATE_PASS', 'Approve Gate Pass', 'Hostel'),
('APPROVE_PAYROLL', 'Approve Payroll', 'Finance'),
('MANAGE_ALL_COURSES', 'Manage All Courses', 'Academic'),
('MANAGE_ASSIGNMENTS', 'Manage Assignments', 'Academic'),
('MANAGE_ATTENDANCE', 'Manage Attendance', 'Academic'),
('MANAGE_EMPLOYEES', 'Manage Employees', 'HR'),
('MANAGE_FACULTY', 'Manage Faculty', 'Admin'),
('MANAGE_GRADES', 'Manage Grades', 'Academic'),
('MANAGE_LIBRARY', 'Manage Library', 'Library'),
('MANAGE_OWN_COURSES', 'Manage Own Courses', 'Academic'),
('MANAGE_PAYROLL', 'Manage Payroll', 'Finance'),
('MANAGE_STUDENTS', 'Manage Students', 'Admin'),
('MANAGE_TIMETABLE', 'Manage Timetable', 'Academic'),
('REQUEST_GATE_PASS', 'Request Gate Pass', 'Hostel'),
('SUBMIT_ASSIGNMENTS', 'Submit Assignments', 'Academic'),
('UPLOAD_SYLLABUS', 'Upload Syllabus', 'Academic'),
('VIEW_ALL_FEES', 'View All Fees', 'Finance'),
('VIEW_ASSIGNMENTS', 'View Assignments', 'Academic'),
('VIEW_ATTENDANCE', 'View Attendance', 'Academic'),
('VIEW_ATTENDANCE_REPORT', 'View Attendance Report', 'Reports'),
('VIEW_AUDIT_LOGS', 'View Audit Logs', 'Admin'),
('VIEW_EMPLOYEES', 'View Employees', 'HR'),
('VIEW_FACULTY', 'View Faculty', 'Admin'),
('VIEW_FEES_REPORT', 'View Fees Report', 'Reports'),
('VIEW_GRADES', 'View Grades', 'Academic'),
('VIEW_GRADES_REPORT', 'View Grades Report', 'Reports'),
('VIEW_HOSTEL_ATTENDANCE', 'View Hostel Attendance', 'Hostel'),
('VIEW_LIBRARY', 'View Library', 'Library'),
('VIEW_OWN_FEES', 'View Own Fees', 'Student'),
('VIEW_PAYROLL', 'View Payroll', 'Finance'),
('VIEW_STUDENTS', 'View Students', 'Admin'),
('VIEW_TIMETABLE', 'View Timetable', 'Academic')
ON CONFLICT (code) DO NOTHING;

-- 4. Assign ALL permissions to ADMIN role (Catch-all)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.code = 'ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- 5. Assign FACULTY permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'FACULTY'
AND p.code IN (
    'MANAGE_ATTENDANCE', 'MANAGE_GRADES', 'MANAGE_ASSIGNMENTS',
    'VIEW_ATTENDANCE', 'VIEW_GRADES', 'VIEW_TIMETABLE', 
    'VIEW_STUDENTS', 'UPLOAD_SYLLABUS', 'UPLOAD_RESOURCES',
    'VIEW_LIBRARY', 'MANAGE_OWN_COURSES'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- 6. Assign STUDENT permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'STUDENT'
AND p.code IN (
    'VIEW_OWN_FEES', 'VIEW_TIMETABLE', 'VIEW_LIBRARY',
    'VIEW_RESOURCES', 'VIEW_EVENTS', 'JOIN_CLUBS',
    'REQUEST_GATE_PASS', 'SUBMIT_ASSIGNMENTS',
    'VIEW_ASSIGNMENTS', 'VIEW_GRADES', 'VIEW_ATTENDANCE'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- 7. Assign WARDEN permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'WARDEN'
AND p.code IN (
    'MANAGE_HOSTEL', 'APPROVE_GATE_PASS', 'VIEW_HOSTEL_ATTENDANCE'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- 8. Assign FINANCE permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'FINANCE'
AND p.code IN (
    'MANAGE_FEES', 'VIEW_ALL_FEES', 'MANAGE_PAYROLL', 'VIEW_PAYROLL',
    'VIEW_FEES_REPORT'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
