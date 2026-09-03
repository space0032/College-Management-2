-- ============================================================================
-- V54: Reconcile Permissions
--
-- Problem: API controllers check fine-grained permission codes (e.g.
-- VIEW_STUDENT, CREATE_*, UPDATE_*, DELETE_*) but the permissions table was
-- seeded with a legacy set (MANAGE_* style) that does not match. Because
-- PermissionService treats ADMIN as a superuser, admins never noticed, but
-- every non-ADMIN role (FACULTY, STUDENT, WARDEN, FINANCE, custom roles)
-- received 403 Forbidden on most endpoints.
--
-- This migration:
--   1. Inserts every permission code checked by the API controllers.
--   2. Assigns all of them to ADMIN.
--   3. Assigns sensible subsets to FACULTY, STUDENT, WARDEN and FINANCE.
--
-- All statements are idempotent (ON CONFLICT ... DO NOTHING).
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 1) Register every permission code the controllers check, including the few
--    legacy codes kept for backward compatibility with seeded data.
-- ---------------------------------------------------------------------------
INSERT INTO permissions (code, name, category) VALUES
-- Students
('VIEW_STUDENT', 'View Students', 'Student'),
('CREATE_STUDENT', 'Create Students', 'Student'),
('UPDATE_STUDENT', 'Update Students', 'Student'),
('DELETE_STUDENT', 'Delete Students', 'Student'),
('MANAGE_STUDENTS', 'Manage Students', 'Student'),

-- Faculty
('VIEW_FACULTY', 'View Faculty', 'Faculty'),
('CREATE_FACULTY', 'Create Faculty', 'Faculty'),
('UPDATE_FACULTY', 'Update Faculty', 'Faculty'),
('DELETE_FACULTY', 'Delete Faculty', 'Faculty'),
('MANAGE_FACULTY', 'Manage Faculty', 'Faculty'),

-- Courses
('VIEW_COURSE', 'View Courses', 'Academic'),
('CREATE_COURSE', 'Create Courses', 'Academic'),
('UPDATE_COURSE', 'Update Courses', 'Academic'),
('DELETE_COURSE', 'Delete Courses', 'Academic'),
('MANAGE_COURSES', 'Manage Courses', 'Academic'),
('MANAGE_ALL_COURSES', 'Manage All Courses', 'Academic'),
('MANAGE_OWN_COURSES', 'Manage Own Courses', 'Academic'),

-- Departments
('VIEW_DEPARTMENT', 'View Departments', 'Department'),
('CREATE_DEPARTMENT', 'Create Departments', 'Department'),
('UPDATE_DEPARTMENT', 'Update Departments', 'Department'),
('DELETE_DEPARTMENT', 'Delete Departments', 'Department'),

-- Employees
('VIEW_EMPLOYEE', 'View Employees', 'HR'),
('CREATE_EMPLOYEE', 'Create Employees', 'HR'),
('UPDATE_EMPLOYEE', 'Update Employees', 'HR'),
('DELETE_EMPLOYEE', 'Delete Employees', 'HR'),
('MANAGE_EMPLOYEES', 'Manage Employees', 'HR'),

-- Users
('VIEW_USER', 'View Users', 'Admin'),
('CREATE_USER', 'Create Users', 'Admin'),
('UPDATE_USER', 'Update Users', 'Admin'),
('DELETE_USER', 'Delete Users', 'Admin'),
('MANAGE_USERS', 'Manage Users', 'Admin'),

-- Attendance
('VIEW_ATTENDANCE', 'View Attendance', 'Academic'),
('CREATE_ATTENDANCE', 'Create Attendance', 'Academic'),
('MANAGE_ATTENDANCE', 'Manage Attendance', 'Academic'),
('VIEW_ATTENDANCE_REPORT', 'View Attendance Report', 'Academic'),

-- Grades
('VIEW_GRADES', 'View Grades', 'Academic'),
('UPDATE_GRADES', 'Update Grades', 'Academic'),
('MANAGE_GRADES', 'Manage Grades', 'Academic'),
('VIEW_GRADES_REPORT', 'View Grades Report', 'Academic'),

-- Library
('VIEW_LIBRARY', 'View Library', 'Library'),
('CREATE_LIBRARY', 'Create Library Entries', 'Library'),
('UPDATE_LIBRARY', 'Update Library Entries', 'Library'),
('MANAGE_LIBRARY', 'Manage Library', 'Library'),

-- Hostel
('VIEW_HOSTEL', 'View Hostel', 'Hostel'),
('CREATE_HOSTEL', 'Create Hostel Records', 'Hostel'),
('UPDATE_HOSTEL', 'Update Hostel Records', 'Hostel'),
('DELETE_HOSTEL', 'Delete Hostel Records', 'Hostel'),
('MANAGE_HOSTEL', 'Manage Hostel', 'Hostel'),
('VIEW_HOSTEL_ATTENDANCE', 'View Hostel Attendance', 'Hostel'),

-- Gatepass
('VIEW_GATEPASS', 'View Gate Passes', 'Gate Pass'),
('CREATE_GATEPASS', 'Create Gate Passes', 'Gate Pass'),
('MANAGE_GATEPASS', 'Manage Gate Passes', 'Gate Pass'),
('REQUEST_GATE_PASS', 'Request Gate Pass', 'Gate Pass'),
('APPROVE_GATE_PASS', 'Approve Gate Pass', 'Gate Pass'),

-- Events
('VIEW_EVENT', 'View Events', 'Events'),
('CREATE_EVENT', 'Create Events', 'Events'),
('UPDATE_EVENT', 'Update Events', 'Events'),
('DELETE_EVENT', 'Delete Events', 'Events'),
('MANAGE_EVENTS', 'Manage Events', 'Events'),
('REGISTER_EVENT', 'Register for Events', 'Events'),
('UNREGISTER_EVENT', 'Unregister from Events', 'Events'),
('MARK_ATTENDANCE', 'Mark Event Attendance', 'Events'),
('VIEW_BUDGET', 'View Event Budget', 'Events'),
('CREATE_BUDGET', 'Create Event Budget', 'Events'),
('UPDATE_BUDGET', 'Update Event Budget', 'Events'),
('DELETE_BUDGET', 'Delete Event Budget', 'Events'),
('VIEW_POLL', 'View Event Polls', 'Events'),
('CREATE_POLL', 'Create Event Polls', 'Events'),
('UPDATE_POLL', 'Update Event Polls', 'Events'),
('EVENT_BUDGET_VIEW', 'View Event Budget', 'Events'),
('EVENT_BUDGET_EDIT', 'Edit Event Budget', 'Events'),
('EVENT_POLL_VIEW', 'View Event Polls', 'Events'),
('EVENT_POLL_CREATE', 'Create Event Polls', 'Events'),

-- Clubs
('VIEW_CLUB', 'View Clubs', 'Clubs'),
('CREATE_CLUB', 'Create Clubs', 'Clubs'),
('UPDATE_CLUB', 'Update Clubs', 'Clubs'),
('DELETE_CLUB', 'Delete Clubs', 'Clubs'),
('MANAGE_CLUB', 'Manage Clubs', 'Clubs'),
('MANAGE_CLUBS', 'Manage Clubs', 'Clubs'),
('JOIN_CLUBS', 'Join Clubs', 'Clubs'),

-- Announcements
('VIEW_ANNOUNCEMENT', 'View Announcements', 'Announcements'),
('CREATE_ANNOUNCEMENT', 'Create Announcements', 'Announcements'),
('UPDATE_ANNOUNCEMENT', 'Update Announcements', 'Announcements'),
('DELETE_ANNOUNCEMENT', 'Delete Announcements', 'Announcements'),

-- Notifications
('VIEW_NOTIFICATION', 'View Notifications', 'Notifications'),
('CREATE_NOTIFICATION', 'Create Notifications', 'Notifications'),

-- Placements
('VIEW_PLACEMENT', 'View Placements', 'Placements'),
('CREATE_PLACEMENT', 'Create Placements', 'Placements'),
('UPDATE_PLACEMENT', 'Update Placements', 'Placements'),
('DELETE_PLACEMENT', 'Delete Placements', 'Placements'),
('MANAGE_PLACEMENT', 'Manage Placements', 'Placements'),
('MANAGE_PLACEMENTS', 'Manage Placements', 'Placements'),

-- Scholarships
('VIEW_SCHOLARSHIP', 'View Scholarships', 'Community'),
('CREATE_SCHOLARSHIP', 'Create Scholarships', 'Community'),
('UPDATE_SCHOLARSHIP', 'Update Scholarships', 'Community'),
('MANAGE_SCHOLARSHIP', 'Manage Scholarships', 'Community'),
('SCHOLARSHIP_VIEW', 'View Scholarships', 'Community'),
('SCHOLARSHIP_CREATE', 'Create Scholarships', 'Community'),
('SCHOLARSHIP_APPLY', 'Apply for Scholarship', 'Community'),

-- Crowdfunding
('VIEW_CROWDFUNDING', 'View Crowdfunding', 'Community'),
('CREATE_CROWDFUNDING', 'Create Crowdfunding', 'Community'),
('MANAGE_CROWDFUNDING', 'Manage Crowdfunding', 'Community'),
('CROWDFUNDING_VIEW', 'View Crowdfunding', 'Community'),
('CROWDFUNDING_CREATE', 'Create Crowdfunding', 'Community'),
('CROWDFUNDING_DONATE', 'Donate to Crowdfunding', 'Community'),

-- Resources
('VIEW_RESOURCE', 'View Resources', 'Academic'),
('CREATE_RESOURCE', 'Create Resources', 'Academic'),
('UPDATE_RESOURCE', 'Update Resources', 'Academic'),
('DELETE_RESOURCE', 'Delete Resources', 'Academic'),
('MANAGE_RESOURCE', 'Manage Resources', 'Academic'),
('VIEW_RESOURCES', 'View Resources', 'Academic'),
('UPLOAD_RESOURCES', 'Upload Resources', 'Academic'),

-- Syllabus
('VIEW_SYLLABUS', 'View Syllabus', 'Academic'),
('CREATE_SYLLABUS', 'Create Syllabus', 'Academic'),
('DELETE_SYLLABUS', 'Delete Syllabus', 'Academic'),
('UPLOAD_SYLLABUS', 'Upload Syllabus', 'Academic'),

-- Timetable
('VIEW_TIMETABLE', 'View Timetable', 'Academic'),
('CREATE_TIMETABLE', 'Create Timetable', 'Academic'),
('DELETE_TIMETABLE', 'Delete Timetable', 'Academic'),
('MANAGE_TIMETABLE', 'Manage Timetable', 'Academic'),

-- Assignments
('VIEW_ASSIGNMENT', 'View Assignments', 'Academic'),
('CREATE_ASSIGNMENT', 'Create Assignments', 'Academic'),
('UPDATE_ASSIGNMENT', 'Update Assignments', 'Academic'),
('DELETE_ASSIGNMENT', 'Delete Assignments', 'Academic'),
('MANAGE_ASSIGNMENT', 'Manage Assignments', 'Academic'),
('VIEW_ASSIGNMENTS', 'View Assignments', 'Academic'),
('MANAGE_ASSIGNMENTS', 'Manage Assignments', 'Academic'),

-- Calendar
('VIEW_CALENDAR', 'View Calendar', 'Academic'),
('CREATE_CALENDAR', 'Create Calendar Events', 'Academic'),
('DELETE_CALENDAR', 'Delete Calendar Events', 'Academic'),

-- Fees
('VIEW_FEES', 'View Fees', 'Finance'),
('MANAGE_FEES', 'Manage Fees', 'Finance'),
('PAY_FEES', 'Pay Fees', 'Finance'),
('VIEW_OWN_FEES', 'View Own Fees', 'Finance'),
('VIEW_ALL_FEES', 'View All Fees', 'Finance'),
('VIEW_FEES_REPORT', 'View Fees Report', 'Finance'),

-- Payroll
('VIEW_PAYROLL', 'View Payroll', 'Finance'),
('MANAGE_PAYROLL', 'Manage Payroll', 'Finance'),
('UPDATE_PAYROLL', 'Update Payroll', 'Finance'),
('DELETE_PAYROLL', 'Delete Payroll', 'Finance'),
('APPROVE_PAYROLL', 'Approve Payroll', 'Finance'),
('PAYROLL_MANAGE', 'Manage Payroll', 'Finance'),

-- Leave
('VIEW_LEAVE', 'View Leaves', 'Leave'),
('CREATE_LEAVE', 'Create Leaves', 'Leave'),
('UPDATE_LEAVE', 'Update Leaves', 'Leave'),

-- Reports
('VIEW_REPORT', 'View Reports', 'Reports'),
('MANAGE_REPORT', 'Manage Reports', 'Reports'),

-- Visitors
('VIEW_VISITOR', 'View Visitors', 'Security'),
('MANAGE_VISITOR', 'Manage Visitors', 'Security'),
('MANAGE_VISITORS', 'Manage Visitors', 'Security'),

-- Rooms
('VIEW_ROOM', 'View Rooms', 'Hostel'),
('MANAGE_ROOM', 'Manage Rooms', 'Hostel'),
('ROOM_CHECK', 'Check Room Availability', 'Hostel'),
('MANAGE_ROOMS', 'Manage Rooms', 'Hostel'),

-- Volunteers
('VIEW_VOLUNTEER', 'View Volunteers', 'Community'),
('MANAGE_VOLUNTEER', 'Manage Volunteers', 'Community'),

-- Workload
('VIEW_WORKLOAD', 'View Faculty Workload', 'Academic'),

-- Roles & Settings
('VIEW_ROLE', 'View Roles', 'Admin'),
('CREATE_ROLE', 'Create Roles', 'Admin'),
('UPDATE_ROLE', 'Update Roles', 'Admin'),
('DELETE_ROLE', 'Delete Roles', 'Admin'),
('VIEW_SETTINGS', 'View Settings', 'Admin'),
('UPDATE_SETTINGS', 'Update Settings', 'Admin'),
('UPDATE_PASSWORD', 'Update Password', 'General'),

-- Audit
('VIEW_AUDIT', 'View Audit Logs', 'Admin'),
('VIEW_AUDIT_LOGS', 'View Audit Logs', 'Admin'),

-- Complaints (hostel)
('VIEW_COMPLAINT', 'View Complaints', 'Hostel'),
('CREATE_COMPLAINT', 'Create Complaints', 'Hostel'),
('MANAGE_COMPLAINT', 'Manage Complaints', 'Hostel'),

-- System / misc
('MANAGE_SYSTEM', 'Manage System', 'Admin'),
('MANAGE_COLLEGE_INFO', 'Manage College Info', 'Admin')

ON CONFLICT (code) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 2) Grant every permission to ADMIN (superuser role).
-- ---------------------------------------------------------------------------
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 3) Grant sensible subsets to non-admin roles.
-- ---------------------------------------------------------------------------

-- FACULTY: view students/faculty, manage courses/grades/attendance/assignments,
--          syllabus/timetable/announcements, leave requests, workload.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'VIEW_STUDENT', 'VIEW_FACULTY', 'CREATE_FACULTY', 'UPDATE_FACULTY',
    'VIEW_COURSE', 'CREATE_COURSE', 'UPDATE_COURSE', 'DELETE_COURSE',
    'MANAGE_OWN_COURSES',
    'VIEW_ATTENDANCE', 'CREATE_ATTENDANCE', 'MANAGE_ATTENDANCE', 'VIEW_ATTENDANCE_REPORT',
    'VIEW_GRADES', 'UPDATE_GRADES', 'MANAGE_GRADES',
    'VIEW_ASSIGNMENT', 'CREATE_ASSIGNMENT', 'UPDATE_ASSIGNMENT', 'MANAGE_ASSIGNMENT',
    'VIEW_SYLLABUS', 'CREATE_SYLLABUS',
    'VIEW_TIMETABLE',
    'VIEW_ANNOUNCEMENT', 'CREATE_ANNOUNCEMENT', 'UPDATE_ANNOUNCEMENT',
    'CREATE_LEAVE', 'VIEW_LEAVE', 'UPDATE_LEAVE',
    'VIEW_WORKLOAD',
    'VIEW_LIBRARY', 'VIEW_RESOURCES', 'UPLOAD_RESOURCES', 'VIEW_RESOURCE', 'CREATE_RESOURCE',
    'VIEW_EVENT', 'REGISTER_EVENT', 'UNREGISTER_EVENT', 'MARK_ATTENDANCE',
    'VIEW_CLUB', 'VIEW_CALENDAR',
    'VIEW_REPORT', 'UPDATE_PASSWORD',
    'VIEW_NOTIFICATION'
)
WHERE r.code = 'FACULTY'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- STUDENT: view own data, request leave/gatepass, join clubs/events, pay fees,
--          view courses/grades/attendance/library/timetable/scholarships.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'VIEW_COURSE', 'VIEW_TIMETABLE', 'VIEW_LIBRARY',
    'VIEW_ATTENDANCE', 'VIEW_GRADES', 'VIEW_OWN_FEES', 'PAY_FEES',
    'CREATE_LEAVE', 'VIEW_LEAVE',
    'REQUEST_GATE_PASS', 'VIEW_GATEPASS',
    'JOIN_CLUBS', 'VIEW_CLUB', 'VIEW_EVENT', 'REGISTER_EVENT', 'UNREGISTER_EVENT',
    'VIEW_SCHOLARSHIP', 'SCHOLARSHIP_APPLY',
    'VIEW_ANNOUNCEMENT', 'VIEW_NOTIFICATION', 'VIEW_CALENDAR',
    'VIEW_RESOURCES', 'VIEW_RESOURCE',
    'UPDATE_PASSWORD',
    'VIEW_CROWDFUNDING', 'CROWDFUNDING_DONATE',
    'VIEW_PLACEMENT',
    'CREATE_COMPLAINT',
    'VIEW_FACULTY', 'VIEW_COMPLAINT'
)
WHERE r.code = 'STUDENT'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- WARDEN: hostel, rooms, complaints, hostel attendance, manage hostel students,
--         view students.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'VIEW_HOSTEL', 'CREATE_HOSTEL', 'UPDATE_HOSTEL', 'DELETE_HOSTEL', 'MANAGE_HOSTEL',
    'VIEW_HOSTEL_ATTENDANCE',
    'VIEW_ROOM', 'MANAGE_ROOM', 'ROOM_CHECK',
    'VIEW_STUDENT', 'MANAGE_STUDENTS',
    'VIEW_ANNOUNCEMENT', 'VIEW_NOTIFICATION', 'CREATE_ANNOUNCEMENT',
    'VIEW_LEAVE', 'UPDATE_LEAVE',
    'VIEW_COMPLAINT', 'MANAGE_COMPLAINT'
)
WHERE r.code = 'WARDEN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- FINANCE: fees + payroll.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'VIEW_FEES', 'MANAGE_FEES', 'PAY_FEES', 'VIEW_OWN_FEES', 'VIEW_ALL_FEES', 'VIEW_FEES_REPORT',
    'VIEW_PAYROLL', 'MANAGE_PAYROLL', 'UPDATE_PAYROLL', 'DELETE_PAYROLL', 'APPROVE_PAYROLL', 'PAYROLL_MANAGE',
    'VIEW_STUDENT', 'VIEW_REPORT', 'VIEW_AUDIT', 'UPDATE_PASSWORD',
    'VIEW_EMPLOYEE', 'UPDATE_EMPLOYEE'
)
WHERE r.code = 'FINANCE'
ON CONFLICT (role_id, permission_id) DO NOTHING;
