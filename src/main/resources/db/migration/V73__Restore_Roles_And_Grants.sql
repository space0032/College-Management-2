-- ============================================================================
-- V73: Restore non-admin roles and reconcile student/self-service permissions
--
-- Problem: V65 deleted every role except ADMIN. V66 only re-linked existing
-- users but never re-created the STUDENT/FACULTY/FINANCE roles, so on any
-- fresh or sequential migration the V54 grants bound to those roles were lost
-- and non-admin users had role_id = NULL -> 403 on essentially every endpoint.
-- V71 already fixed the same problem for WARDEN; this migration does the same
-- for STUDENT, FACULTY and FINANCE, then re-applies the fine-grained grants.
--
-- It also closes the self-service permission gaps found in the audit:
--   * STUDENT gains VIEW_ASSIGNMENT + SUBMIT_ASSIGNMENT (assignments module)
--   * STUDENT gains VIEW_SYLLABUS (Learning Portal frameworks tab)
--
-- All statements are idempotent and safe to re-run.
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 1) Register any fine-grained codes missing from the permissions table.
-- ---------------------------------------------------------------------------
INSERT INTO permissions (code, name, category) VALUES
('SUBMIT_ASSIGNMENT', 'Submit Assignments', 'Academic')
ON CONFLICT (code) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 2) Recreate the missing system roles (mirrors V71's WARDEN handling).
-- ---------------------------------------------------------------------------
INSERT INTO roles (code, name, description, is_system_role, portal_type)
SELECT 'STUDENT', 'Student', 'Student account with self-service permissions', TRUE, 'STUDENT'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'STUDENT');

INSERT INTO roles (code, name, description, is_system_role, portal_type)
SELECT 'FACULTY', 'Faculty Member', 'Faculty member with teaching and grading permissions', TRUE, 'FACULTY'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'FACULTY');

INSERT INTO roles (code, name, description, is_system_role, portal_type)
SELECT 'FINANCE', 'Finance Officer', 'Finance officer with fees and payroll permissions', TRUE, 'FINANCE'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'FINANCE');

-- ---------------------------------------------------------------------------
-- 3) Re-apply the fine-grained role_permissions grants (idempotent).
-- ---------------------------------------------------------------------------

-- FACULTY (mirrors V54 section 3, plus VIEW_SYLLABUS kept explicit).
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

-- STUDENT: own data + self-service actions + assignments + syllabus.
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
    'VIEW_FACULTY', 'VIEW_COMPLAINT',
    'VIEW_ASSIGNMENT', 'SUBMIT_ASSIGNMENT',
    'VIEW_SYLLABUS'
)
WHERE r.code = 'STUDENT'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- FINANCE (mirrors V54 section 3).
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'VIEW_FEES', 'MANAGE_FEES', 'PAY_FEES', 'VIEW_OWN_FEES', 'VIEW_ALL_FEES', 'VIEW_FEES_REPORT',
    'CREATE_FEES', 'ADJUST_FEES', 'REFUND_FEES', 'BULK_ASSIGN_FEES', 'REVIEW_FEE_REQUESTS', 'MANAGE_FEE_REMINDERS',
    'VIEW_PAYROLL', 'MANAGE_PAYROLL', 'UPDATE_PAYROLL', 'DELETE_PAYROLL', 'APPROVE_PAYROLL', 'PAYROLL_MANAGE',
    'VIEW_STUDENT', 'VIEW_REPORT', 'VIEW_AUDIT', 'UPDATE_PASSWORD',
    'VIEW_EMPLOYEE', 'UPDATE_EMPLOYEE'
)
WHERE r.code = 'FINANCE'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- WARDEN: re-apply base grants (V71 already handles gate pass codes).
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

-- ---------------------------------------------------------------------------
-- 4) Backfill users whose role_id was wiped by V65 (idempotent).
-- ---------------------------------------------------------------------------
UPDATE users SET role_id = (SELECT id FROM roles WHERE code = 'STUDENT'), role = 'STUDENT'
WHERE role_id IS NULL
AND id IN (SELECT user_id FROM students WHERE user_id IS NOT NULL)
AND EXISTS (SELECT 1 FROM roles WHERE code = 'STUDENT');

UPDATE users SET role_id = (SELECT id FROM roles WHERE code = 'FACULTY'), role = 'FACULTY'
WHERE role_id IS NULL
AND id IN (SELECT user_id FROM faculty WHERE user_id IS NOT NULL)
AND EXISTS (SELECT 1 FROM roles WHERE code = 'FACULTY');

UPDATE users SET role_id = (SELECT id FROM roles WHERE code = 'FINANCE'), role = 'FINANCE'
WHERE role_id IS NULL
AND role = 'FINANCE'
AND EXISTS (SELECT 1 FROM roles WHERE code = 'FINANCE');

UPDATE users SET role_id = (SELECT id FROM roles WHERE code = 'STUDENT'), role = 'STUDENT'
WHERE role_id IS NULL
AND role = 'STUDENT'
AND EXISTS (SELECT 1 FROM roles WHERE code = 'STUDENT');

UPDATE users SET role_id = (SELECT id FROM roles WHERE code = 'FACULTY'), role = 'FACULTY'
WHERE role_id IS NULL
AND role = 'FACULTY'
AND EXISTS (SELECT 1 FROM roles WHERE code = 'FACULTY');